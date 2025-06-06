package de.igslandstuhl.database.server;

import java.io.*;
import javax.net.ssl.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.igslandstuhl.database.api.Student;
import de.igslandstuhl.database.api.Subject;
import de.igslandstuhl.database.api.Task;
import de.igslandstuhl.database.api.Topic;
import de.igslandstuhl.database.api.User;
import de.igslandstuhl.database.server.resources.ResourceHelper;
import de.igslandstuhl.database.server.webserver.GetRequest;
import de.igslandstuhl.database.server.webserver.GetResponse;
import de.igslandstuhl.database.server.webserver.PostResponse;

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
                } else if (request.startsWith("POST /subject_request")) {
                    handleSubjectRequest(in, out, request);
                } else if (request.startsWith("POST /currenttopic")) {
                    handleCurrentTopic(in, out, request);
                } else if (request.startsWith("POST /tasks")) {
                    handleTasks(in, out, request);
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

        private void handleSubjectRequest(BufferedReader in, PrintWriter out, String request) throws IOException {
            int contentLength = 0;
            for (String line : request.split("\n")) {
                if (line.startsWith("Content-Length:")) {
                    contentLength = Integer.parseInt(line.split(":")[1].trim());
                }
            }
            if (contentLength <= 0) {
                PostResponse.badRequest("Fehlende oder ungültige Content-Length!").respond(out);
                return;
            }
            char[] bodyChars = new char[contentLength];
            in.read(bodyChars, 0, contentLength);
            String body = new String(bodyChars);

            // Parse JSON body for subjectId and type using Gson
            Gson gson = new Gson();
            java.lang.reflect.Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> json = gson.fromJson(body, mapType);
            int subjectId = ((Number) json.get("subjectId")).intValue();
            String type = (String) json.get("type");

            String user = getSessionUser(request);
            Student student = User.getUser(user) instanceof Student ? (Student) User.getUser(user) : null;
            PostResponse response;
            if (student != null) {
                student.addSubjectRequest(subjectId, type);
                response = PostResponse.ok("Saved request", "text/plain");
            } else {
                response = PostResponse.unauthorized("Not logged in or invalid session");
            }
            response.respond(out);
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

        private void handleCurrentTopic(BufferedReader in, PrintWriter out, String request) throws IOException {
            int contentLength = 0;
            for (String line : request.split("\n")) {
                if (line.startsWith("Content-Length:")) {
                    contentLength = Integer.parseInt(line.split(":")[1].trim());
                }
            }
            if (contentLength <= 0) {
                PostResponse.badRequest("Fehlende oder ungültige Content-Length!").respond(out);
                return;
            }
            char[] bodyChars = new char[contentLength];
            in.read(bodyChars, 0, contentLength);
            String body = new String(bodyChars);

            // Parse JSON body for subjectId using Gson
            Gson gson = new Gson();
            java.lang.reflect.Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> json = gson.fromJson(body, mapType);
            int subjectId = ((Number) json.get("subjectId")).intValue();

            String user = getSessionUser(request);
            Student student = User.getUser(user) instanceof Student ? (Student) User.getUser(user) : null;
            PostResponse response;
            if (student != null) {
                Subject subject = Subject.get(subjectId);
                Topic topic = student.getCurrentTopic(subject);
                if (topic != null) {
                    response = PostResponse.ok(topic.toString(), "application/json");
                } else {
                    response = PostResponse.badRequest("No current topic for this subject.");
                }
            } else {
                response = PostResponse.unauthorized("Not logged in or invalid session");
            }
            response.respond(out);
        }

        private void handleTasks(BufferedReader in, PrintWriter out, String request) throws IOException {
            int contentLength = 0;
            for (String line : request.split("\n")) {
            if (line.startsWith("Content-Length:")) {
                contentLength = Integer.parseInt(line.split(":")[1].trim());
            }
            }
            if (contentLength <= 0) {
            PostResponse.badRequest("Fehlende oder ungültige Content-Length!").respond(out);
            return;
            }
            char[] bodyChars = new char[contentLength];
            in.read(bodyChars, 0, contentLength);
            String body = new String(bodyChars);

            // Parse JSON body for ids (list of task ids) using Gson
            Gson gson = new Gson();
            java.lang.reflect.Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> json = gson.fromJson(body, mapType);

            // Expecting: { "ids": [1,2,3,...] }
            java.util.List<Integer> ids = null;
            if (json.get("ids") instanceof java.util.List<?> l) {
                ids = new java.util.ArrayList<>();
                for (Object o : l) {
                    if (o instanceof Number n) {
                        ids.add(n.intValue());
                    }
                }
            }

            String user = getSessionUser(request);
            Student student = User.getUser(user) instanceof Student ? (Student) User.getUser(user) : null;
            PostResponse response;
            if (student != null && ids != null) {
                // Assuming Student has a method getTasksByIds(List<Integer> ids)
                // and returns a List<Task> or similar
                java.util.List<Task> tasks = Task.getTasksByIds(ids);
                String jsonResponse = tasks.toString();
            response = PostResponse.ok(jsonResponse, "application/json");
            } else if (ids == null) {
            response = PostResponse.badRequest("Missing or invalid 'ids' in request body.");
            } else {
            response = PostResponse.unauthorized("Not logged in or invalid session");
            }
            response.respond(out);
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
