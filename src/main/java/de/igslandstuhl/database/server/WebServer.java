package de.igslandstuhl.database.server;

import java.io.*;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import de.igslandstuhl.database.server.resources.ResourceHelper;
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
    /**
     * Indicates whether the server is currently running.
     */
    private boolean running;
    /**
     * The SSL server socket that listens for incoming HTTPS connections.
     */
    private SSLServerSocket serverSocket;
    /**
     * The UserManager instance that handles user-related operations such as login and session management.
     */
    private final UserManager userManager = new UserManager();
    /**
     * Returns the session store used by the server.
     * This method provides access to the session store, allowing for session management operations.
     *
     * @return The session store map containing session IDs and associated usernames.
     */
    public UserManager getUserManager() {
        return userManager;
    }
    
    /**
     * Constructs a new WebServer instance with the specified port and keystore.
     *
     * @param port The port on which the server will listen for HTTPS requests.
     * @param keystorePath The path to the keystore file containing the server's SSL certificate.
     * @param keystorePassword The password for the keystore.
     * @throws KeyStoreException If the keystore cannot be initialized.
     * @throws FileNotFoundException If the keystore file is not found.
     * @throws IOException If an I/O error occurs while reading the keystore.
     * @throws NoSuchAlgorithmException If the specified algorithm is not available.
     * @throws CertificateException If there is an error with the certificate in the keystore.
     * @throws UnrecoverableKeyException If the key cannot be recovered from the keystore.
     * @throws KeyManagementException If there is an error initializing the SSL context.
     */
    public WebServer(int port, String keystorePath, String keystorePassword) throws KeyStoreException, FileNotFoundException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, KeyManagementException {
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

        /**
         * Constructs a new ClientHandler for the given SSLSocket.
         *
         * @param socket The SSLSocket representing the client connection.
         */
        public ClientHandler(SSLSocket socket) {
            this.clientSocket = socket;
        }

        /**
         * Responds to a GET request by retrieving the requested resource.
         *
         * @param request The GetRequest object containing the request details.
         * @param user The username associated with the session, or null if not logged in.
         * @return A GetResponse containing the requested resource or an error response.
         */
        private GetResponse respond(GetRequest request, String user) {
            return GetResponse.getResource(request.toResourceLocation(user), user);
        }

        /**
         * Handles the client request by reading the input, processing it, and sending a response.
         * This method supports GET and POST requests for various functionalities such as login,
         * subject requests, current topics, tasks, and room updates.
         */
        @Override
        public void run() {
            PrintWriter out;
            try {
                out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), true);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

                String request = ResourceHelper.readResourceTillEmptyLine(in);

                if (request.startsWith("GET")) {
                    String user = Server.getInstance().getWebServer().getUserManager().getSessionUser(request);
                    GetRequest get = new GetRequest(request);
                    GetResponse response = respond(get, user);
                    response.respond(out);
                } else if (request.startsWith("POST")) {
                    PostHeader header = new PostHeader(request);
                    int contentLength = header.getContentLength();
                    String body = null;
                    if (contentLength > 0) {
                        char[] bodyChars = new char[contentLength];
                        in.read(bodyChars, 0, contentLength);
                        body = new String(bodyChars);
                    }
                    PostRequest parsedRequest = new PostRequest(header, body);
                    PostResponse response = PostRequestHandler.getInstance().handlePostRequest(parsedRequest);
                    response.respond(out);
                }
            } catch (IOException e) {
                e.printStackTrace();
                GetResponse.internalServerError().respond(out);
                e.printStackTrace(out);
            } catch (Exception e) {
                e.printStackTrace();
                GetResponse.internalServerError().respond(out);
                e.printStackTrace(out);
            } finally {
                try {
                    out.flush();
                    out.close();
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Starts the web server, allowing it to accept incoming HTTPS requests.
     * If the server is already running, it throws an IllegalStateException.
     */
    public void start() {
        if (!running) {
            running = true;
            new Thread(this).start();
        } else {
            throw new IllegalStateException("Server already started");
        }
    }
    /**
     * Stops the web server, preventing it from accepting new requests.
     * It closes the server socket and releases any resources held by the server.
     */
    public void stop() {
        running = false;
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (running) {
            try {
                SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            } catch (IOException e) {
                System.err.println("Error while accepting client");
                e.printStackTrace();
            }
        }
    }
}
