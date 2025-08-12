package de.igslandstuhl.database.server;

import java.io.*;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.UnknownHostException;
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
import de.igslandstuhl.database.server.webserver.HttpHeader;
import de.igslandstuhl.database.server.webserver.PostRequest;
import de.igslandstuhl.database.server.webserver.PostRequestHandler;
import de.igslandstuhl.database.server.webserver.PostResponse;
import de.igslandstuhl.database.server.webserver.SessionManager;

/**
 * A simple HTTPS web server that handles various requests related to student data.
 * It supports GET and POST requests for login, subject requests, current topics, tasks, and room updates.
 */
public class WebServer implements Runnable {
    public static final int SESSION_DURATION = 21600; // six hours
    public static final int MAXIMUM_INACTIVITY_DURATION = 3600; // An hour

    private volatile boolean running;
    private final SSLServerSocket serverSocket;
    private final SessionManager userManager = new SessionManager(SESSION_DURATION, MAXIMUM_INACTIVITY_DURATION);
    private final ExecutorService clientPool = Executors.newCachedThreadPool();
    private final boolean secure = true;

    public SessionManager getSessionManager() {
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
     * Constructs a test webserver without server socket
     */
    protected WebServer() {
        this.serverSocket = null;
    }

    class ClientHandler implements Runnable {
        private final SSLSocket clientSocket;
        private final String clientIp;

        ClientHandler(SSLSocket socket) {
            this.clientSocket = socket;
            InetAddress inetAddress;
            try {
                inetAddress = socket != null ? socket.getInetAddress() : InetAddress.getLocalHost();
            } catch (UnknownHostException e) {
                inetAddress = null;
            }
            clientIp = inetAddress != null ? inetAddress.getHostAddress() : null;
        }

        @Override
        public void run() {
            try (BufferedOutputStream rawOut = new BufferedOutputStream(clientSocket.getOutputStream())) {
                PrintStream out = new PrintStream(rawOut, true, StandardCharsets.UTF_8);
                try (BufferedInputStream bis = new BufferedInputStream(clientSocket.getInputStream())) {
                    String headerString = readHeadersAsString(bis);
                    if (headerString == null) {
                        GetResponse.internalServerError().respond(out);
                        return;
                    }
                    if (headerString.startsWith("GET")) {
                        handleGet(headerString, out);
                    } else if (headerString.startsWith("POST")) {
                        handlePost(headerString, bis, out);
                    } else {
                        GetResponse.internalServerError().respond(out);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    PrintStream out = new PrintStream(clientSocket.getOutputStream(), true);
                    GetResponse.internalServerError().respond(out);
                } catch (IOException ignored) {}
            } finally {
                try { clientSocket.close(); } catch (IOException ignored) {}
            }
        }

        String readHeadersAsString(InputStream in) throws IOException {
            byte[] headerBytes = readUntilDoubleCRLF(in);
            if (headerBytes == null || headerBytes.length == 0) return null;
            return new String(headerBytes, StandardCharsets.ISO_8859_1);
        }

        void handleGet(String headerString, PrintStream out) {
            SessionManager sessionManager = Server.getInstance().getWebServer().getSessionManager();
            GetRequest get = new GetRequest(headerString, clientIp, secure);
            GetResponse response;
            if (!sessionManager.validateSession(get)) {
                response = GetResponse.internalServerError();
            } else {
                String user = sessionManager.getSessionUser(get).getUsername();
                response = GetResponse.getResource(get.toResourceLocation(user), user);
            }
            response.respond(out);
        }

        void handlePost(String headerString, InputStream in, PrintStream out) throws IOException {
            Map<String, String> headerMap = parseHeaders(headerString);
            HttpHeader postHeader = new HttpHeader(headerString);
            int contentLength = headerMap.containsKey("content-length") ? Integer.parseInt(headerMap.get("content-length")) : 0;
            Charset bodyCharset = determineCharset(headerMap.get("content-type"));
            String body = null;
            if (contentLength > 0) {
                byte[] bodyBytes = readNBytes(in, contentLength);
                String raw = new String(bodyBytes, bodyCharset);
                body = URLDecoder.decode(raw, bodyCharset.name());
            }
            PostRequest parsedRequest = new PostRequest(postHeader, body, clientIp, secure);
            PostResponse response = Server.getInstance().getWebServer().getSessionManager().validateSession(parsedRequest) ? PostRequestHandler.getInstance().handlePostRequest(parsedRequest) : PostResponse.badRequest("Bad request: session manipulation", parsedRequest);
            response.respond(out);
        }

        byte[] readUntilDoubleCRLF(InputStream in) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            LinkedList<Integer> window = new LinkedList<>();
            int current;
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
                if (baos.size() > 64 * 1024) break;
            }
            return baos.toByteArray();
        }

        byte[] readNBytes(InputStream in, int n) throws IOException {
            byte[] buffer = new byte[n];
            int read = 0;
            while (read < n) {
                int r = in.read(buffer, read, n - read);
                if (r == -1) return null;
                read += r;
            }
            return buffer;
        }

        Map<String, String> parseHeaders(String headerString) {
            Map<String, String> map = new HashMap<>();
            String[] lines = headerString.split("\\r?\\n");
            for (int i = 1; i < lines.length; i++) {
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

        Charset determineCharset(String contentType) {
            if (contentType != null) {
                for (String p : contentType.split(";")) {
                    p = p.trim();
                    if (p.toLowerCase(Locale.ROOT).startsWith("charset=")) {
                        try {
                            return Charset.forName(p.substring(8).replace("\"", ""));
                        } catch (Exception ignored) {
                            return StandardCharsets.UTF_8;
                        }
                    }
                }
            }
            return StandardCharsets.UTF_8;
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
        try { serverSocket.close(); } catch (IOException e) { e.printStackTrace(); }
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
