package de.igslandstuhl.database.server;

import java.io.*;
import javax.net.ssl.*;

import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.igslandstuhl.database.server.resources.ResourceHelper;
import de.igslandstuhl.database.server.webserver.GetRequest;
import de.igslandstuhl.database.server.webserver.GetResponse;

public class WebServer implements Runnable {
    private static Map<String, String> sessionStore = new HashMap<>(); 
    private boolean running;
    private SSLServerSocket serverSocket;
    
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

    class ClientHandler implements Runnable {
        private SSLSocket clientSocket;

        public ClientHandler(SSLSocket socket) {
            this.clientSocket = socket;
        }

        private GetResponse respond(GetRequest request, String user) {
            return GetResponse.getResource(request.toResourceLocation(), user);
        }

        @Override
        public void run() {
            PrintWriter out;
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
                
                String request = ResourceHelper.readResourceTillEmptyLine(in);
                System.out.println("Anfrage erhalten: " + request);
                
                if (request.startsWith("GET")) {
                    String user = getSessionUser(request);
                    GetRequest get = new GetRequest(request);
                    GetResponse response = respond(get, user);
                    response.respond(out);
                } else if (request.startsWith("POST /login")) {
                    handleLogin(in, out, request);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
                GetResponse.internalServerError().respond(out);
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
        private void handleLogin(BufferedReader in, PrintWriter out, String request) throws IOException {
            int contentLength = 0;
        
            // Lese die Header und finde Content-Length
            for (String line : request.split("\n")) {
                if (line.startsWith("Content-Length:")) {
                    contentLength = Integer.parseInt(line.split(":")[1].trim());
                }
            }
        
            // Falls kein Content-Length vorhanden ist, Fehler ausgeben
            if (contentLength <= 0) {
                out.println("HTTP/1.1 400 Bad Request");
                out.println("Content-Type: text/plain");
                out.println();
                out.println("Fehlende oder ungültige Content-Length!");
                return;
            }
        
            // Lese den Body basierend auf Content-Length
            char[] bodyChars = new char[contentLength];
            in.read(bodyChars, 0, contentLength);
            String body = new String(bodyChars);
            System.out.println("POST Body: " + body);
        
            // Erwartetes Format: "username=user&password=pass"
            Map<String, String> params = parseFormData(body);
            String username = params.get("username");
            String password = params.get("password");
        
            // Prüfe die Login-Daten in der Datenbank
            if (Server.getInstance().isValidUser(username, password)) {
                String sessionId = UUID.randomUUID().toString();
                sessionStore.put(sessionId, username);
        
                out.println("HTTP/1.1 200 OK");
                out.println("Set-Cookie: session=" + sessionId + "; HttpOnly; Secure");
                out.println("Content-Type: text/plain");
                out.println();
                out.println("Login erfolgreich!");
            } else {
                out.println("HTTP/1.1 401 Unauthorized");
                out.println("Content-Type: text/plain");
                out.println();
                out.println("Falsche Anmeldedaten!");
            }
        }

        private String getSessionUser(String request) {
            // Search for cookie header
            String[] lines = request.split("\n");
            for (String line : lines) {
                if (line.startsWith("Cookie:")) {
                    String[] cookies = line.substring(7).split("; ");
                    for (String cookie : cookies) {
                        String[] keyValue = cookie.split("=");
                        if (keyValue.length == 2 && keyValue[0].trim().equals("session")) {
                            return sessionStore.get(keyValue[1].trim()); // Überprüfe, ob die Session existiert
                        }
                    }
                }
            }
            return null; // No valid session
        }
        

        private Map<String, String> parseFormData(String body) {
            Map<String, String> params = new HashMap<>();
            String[] pairs = body.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    params.put(keyValue[0], keyValue[1]);
                }
            }
            return params;
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
    }

    @Override
    public void run() {
        while (running) {
            try {
                SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                System.out.println("Neue HTTPS-Anfrage erhalten");
                new Thread(new ClientHandler(clientSocket)).start();
            } catch (IOException e) {
                System.err.println("Error while accepting client");
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        String keystorePath = "keys/web/keystore.jks";
        String keystorePassword = "changeit";
        int port = 443;
        
        WebServer webServer = new WebServer(port, keystorePath, keystorePassword);
        webServer.start();
    }
}
