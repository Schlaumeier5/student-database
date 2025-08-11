package de.igslandstuhl.database.server;

import java.io.*;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.igslandstuhl.database.server.webserver.GetRequest;
import de.igslandstuhl.database.server.webserver.GetResponse;
import de.igslandstuhl.database.server.webserver.PostHeader;
import de.igslandstuhl.database.server.webserver.PostRequest;
import de.igslandstuhl.database.server.webserver.PostRequestHandler;
import de.igslandstuhl.database.server.webserver.PostResponse;
import de.igslandstuhl.database.server.webserver.UserManager;

/**
 * A simple HTTPS web server that handles various requests related to student data.
 * It supports GET and POST requests for login, subject requests, current topics, tasks, and room updates.
 */
public class WebServer implements Runnable {
    private volatile boolean running;
    private final SSLServerSocket serverSocket;
    private final UserManager userManager = new UserManager();
    private final ExecutorService clientPool = Executors.newCachedThreadPool();

    public UserManager getUserManager() {
        return userManager;
    }

    public WebServer(int port, String keystorePath, String keystorePassword)
            throws KeyStoreException, FileNotFoundException, IOException,
            NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, KeyManagementException {
        KeyStore ks = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(keystorePath)) {
            ks.load(fis, keystorePassword.toCharArray());
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, keystorePassword.toCharArray());

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, null);

        SSLServerSocketFactory factory = sslContext.getServerSocketFactory();
        serverSocket = (SSLServerSocket) factory.createServerSocket(port);
    }

    /**
     * Handles incoming client connections in a separate thread.
     * This class implements Runnable to allow concurrent handling of multiple clients.
     */
    class ClientHandler implements Runnable {
        /**
         * The SSLSocket representing the client connection.
         */
        private SSLSocket clientSocket;

        ClientHandler(SSLSocket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (BufferedOutputStream rawOut = new BufferedOutputStream(clientSocket.getOutputStream())) {
                // Writer for responses (use UTF-8 for response text)
                PrintWriter out = new PrintWriter(new OutputStreamWriter(rawOut, StandardCharsets.UTF_8), true);

                // Use a buffered input stream so we can read headers as bytes and then the body as bytes
                try (BufferedInputStream bis = new BufferedInputStream(clientSocket.getInputStream())) {
                    bis.mark(64 * 1024); // allow reset if needed

                    // 1) Read header bytes until CRLF CRLF
                    byte[] headerBytes = readUntilDoubleCRLF(bis);
                    if (headerBytes == null || headerBytes.length == 0) {
                        GetResponse.internalServerError().respond(out);
                        return;
                    }

                    // Per HTTP spec, headers are ISO-8859-1 (a superset of bytes 0-255)
                    String headerString = new String(headerBytes, StandardCharsets.ISO_8859_1);

                    // Use existing helper to read the first-line-based resource location if you want, otherwise parse
                    if (headerString.startsWith("GET")) {
                        String user = Server.getInstance().getWebServer().getUserManager().getSessionUser(headerString);
                        GetRequest get = new GetRequest(headerString);
                        GetResponse response = GetResponse.getResource(get.toResourceLocation(user), user);
                        response.respond(out);
                        return;
                    }

                    if (headerString.startsWith("POST")) {
                        // Parse headers into a map
                        Map<String, String> headerMap = parseHeaders(headerString);
                        PostHeader postHeader = new PostHeader(headerString);

                        int contentLength = 0;
                        if (headerMap.containsKey("content-length")) {
                            try {
                                contentLength = Integer.parseInt(headerMap.get("content-length"));
                            } catch (NumberFormatException ignore) {
                                contentLength = 0;
                            }
                        }

                        // Determine charset from Content-Type header if provided
                        Charset bodyCharset = StandardCharsets.UTF_8; // default
                        if (headerMap.containsKey("content-type")) {
                            String ct = headerMap.get("content-type");
                            String cs = extractCharset(ct);
                            if (cs != null) {
                                try {
                                    bodyCharset = Charset.forName(cs);
                                } catch (Exception ignored) {
                                    bodyCharset = StandardCharsets.UTF_8;
                                }
                            }
                        }

                        String body = null;
                        if (contentLength > 0) {
                            // Read exactly contentLength bytes from the stream
                            byte[] bodyBytes = readNBytes(bis, contentLength);
                            if (bodyBytes == null) {
                                GetResponse.internalServerError().respond(out);
                                return;
                            }
                            // If body is percent-encoded (typical for form submissions), decode using the chosen charset
                            // URLDecoder.decode expects an application/x-www-form-urlencoded string
                            String raw = new String(bodyBytes, bodyCharset);
                            // decode percent-encoding into Unicode using bodyCharset -> URLDecoder requires the charset name
                            body = URLDecoder.decode(raw, bodyCharset.name());
                        }

                        PostRequest parsedRequest = new PostRequest(postHeader, body);
                        PostResponse response = PostRequestHandler.getInstance().handlePostRequest(parsedRequest);
                        response.respond(out);
                        return;
                    }

                    // not a GET or POST we expect
                    GetResponse.internalServerError().respond(out);
                } catch (SocketTimeoutException ste) {
                    ste.printStackTrace();
                    GetResponse.internalServerError().respond(new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), true));
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    PrintWriter out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), true);
                    GetResponse.internalServerError().respond(out);
                } catch (IOException ignored) {
                }
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException ignored) {
                }
            }
        }

        /**
         * Read bytes from InputStream until we find CRLF CRLF. Returns the header bytes (without consuming
         * beyond the CRLFCRLF). If stream ends before that, returns what was read.
         */
        private byte[] readUntilDoubleCRLF(InputStream in) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int current;
            // We need to detect the sequence \r\n\r\n (13,10,13,10)
            // We'll buffer last 4 bytes in a rolling window
            LinkedList<Integer> window = new LinkedList<>();
            while ((current = in.read()) != -1) {
                baos.write(current);
                window.addLast(current);
                if (window.size() > 4) window.removeFirst();
                if (window.size() == 4) {
                    Integer[] w = window.toArray(new Integer[0]);
                    if (w[0] == 13 && w[1] == 10 && w[2] == 13 && w[3] == 10) {
                        break;
                    }
                }
                // simple safety: stop if headers exceed some reasonable size
                if (baos.size() > 64 * 1024) break;
            }
            return baos.toByteArray();
        }

        private byte[] readNBytes(InputStream in, int n) throws IOException {
            byte[] buffer = new byte[n];
            int read = 0;
            while (read < n) {
                int r = in.read(buffer, read, n - read);
                if (r == -1) {
                    return null; // stream ended unexpectedly
                }
                read += r;
            }
            return buffer;
        }

        private Map<String, String> parseHeaders(String headerString) {
            Map<String, String> map = new HashMap<>();
            String[] lines = headerString.split("\r?\n");
            for (int i = 1; i < lines.length; i++) { // skip request line
                String line = lines[i];
                int idx = line.indexOf(':');
                if (idx > 0) {
                    String name = line.substring(0, idx).trim().toLowerCase(Locale.ROOT);
                    String value = line.substring(idx + 1).trim();
                    map.put(name, value);
                }
            }
            return map;
        }

        private String extractCharset(String contentType) {
            if (contentType == null) return null;
            String[] parts = contentType.split(";");
            for (String p : parts) {
                p = p.trim();
                if (p.toLowerCase(Locale.ROOT).startsWith("charset=")) {
                    return p.substring(8).replaceAll("\"", "").trim();
                }
            }
            return null;
        }
    }

    public void start() {
        if (!running) {
            running = true;
            new Thread(this).start();
        } else {
            throw new IllegalStateException("Server already started");
        }
    }

    public void stop() {
        running = false;
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        clientPool.shutdownNow();
    }

    @Override
    public void run() {
        while (running) {
            try {
                final SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                clientPool.submit(new ClientHandler(clientSocket));
            } catch (IOException e) {
                if (running) {
                    System.err.println("Error while accepting client");
                    e.printStackTrace();
                }
            }
        }
    }
}
