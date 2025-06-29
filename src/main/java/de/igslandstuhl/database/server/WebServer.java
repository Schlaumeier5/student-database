package de.igslandstuhl.database.server;

import java.io.*;
import java.nio.charset.StandardCharsets;

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

import de.igslandstuhl.database.api.Room;
import de.igslandstuhl.database.api.Student;
import de.igslandstuhl.database.api.Subject;
import de.igslandstuhl.database.api.Task;
import de.igslandstuhl.database.api.Topic;
import de.igslandstuhl.database.api.User;
import de.igslandstuhl.database.server.resources.ResourceHelper;
import de.igslandstuhl.database.server.webserver.ContentType;
import de.igslandstuhl.database.server.webserver.GetRequest;
import de.igslandstuhl.database.server.webserver.GetResponse;
import de.igslandstuhl.database.server.webserver.PostResponse;

/**
 * A simple HTTPS web server that handles various requests related to student data.
 * It supports GET and POST requests for login, subject requests, current topics, tasks, and room updates.
 */
public class WebServer implements Runnable {
    /**
     * A map to store session IDs and their associated usernames.
     * This is a simple in-memory session store.
     */
    private static Map<String, String> sessionStore = new HashMap<>();
    /**
     * Indicates whether the server is currently running.
     */
    private boolean running;
    /**
     * The SSL server socket that listens for incoming HTTPS connections.
     */
    private SSLServerSocket serverSocket;
    
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
            return GetResponse.getResource(request.toResourceLocation(), user);
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
                } else if (request.startsWith("POST /updateroom")) {
                    handleUpdateRoom(in, out, request);
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

        /**
         * Handles a POST request for subject requests, parsing the request body and updating the student's subject requests.
         * Subject requests can be requests for help, a partner etc.
         * Subject requests are expected to be in JSON format with fields for subjectId and type.
         * If the request is successful, it responds with a 200 OK status and a message.
         * If the request is invalid or the user is not logged in, it responds with an appropriate error status.
         *
         * @param in The BufferedReader to read the request body.
         * @param out The PrintWriter to send the response.
         * @param request The raw HTTP request string.
         * @throws IOException If an I/O error occurs while reading or writing.
         */
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
                response = PostResponse.ok("Saved request", ContentType.TEXT_PLAIN);
            } else {
                response = PostResponse.unauthorized("Not logged in or invalid session");
            }
            response.respond(out);
        }
        /**
         * Handles a POST request for user login, parsing the request body and validating the credentials.
         * If the login is successful, it generates a session ID and stores it in the session store.
         * If the login fails, it responds with a 401 Unauthorized status.
         *
         * @param in The BufferedReader to read the request body.
         * @param out The PrintWriter to send the response.
         * @param request The raw HTTP request string.
         * @throws IOException If an I/O error occurs while reading or writing.
         */
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
        /**
         * Handles a POST request for the current topic of a student in a specific subject.
         * It reads the request body, extracts the subject ID, and retrieves the current topic for that subject.
         * If successful, it responds with the topic details in JSON format.
         * If the user is not logged in or there is no current topic, it responds with an appropriate error status.
         *
         * @param in The BufferedReader to read the request body.
         * @param out The PrintWriter to send the response.
         * @param request The raw HTTP request string.
         * @throws IOException If an I/O error occurs while reading or writing.
         */
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
                    response = PostResponse.ok(topic.toString(), ContentType.JSON);
                } else {
                    response = PostResponse.badRequest("No current topic for this subject.");
                }
            } else {
                response = PostResponse.unauthorized("Not logged in or invalid session");
            }
            response.respond(out);
        }
        /**
         * Handles a POST request for tasks, parsing the request body to retrieve a list of task IDs.
         * It retrieves the tasks associated with those IDs and responds with their details in JSON format.
         * If the user is not logged in or the request is invalid, it responds with an appropriate error status.
         *
         * @param in The BufferedReader to read the request body.
         * @param out The PrintWriter to send the response.
         * @param request The raw HTTP request string.
         * @throws IOException If an I/O error occurs while reading or writing.
         */
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
            response = PostResponse.ok(jsonResponse, ContentType.JSON);
            } else if (ids == null) {
            response = PostResponse.badRequest("Missing or invalid 'ids' in request body.");
            } else {
            response = PostResponse.unauthorized("Not logged in or invalid session");
            }
            response.respond(out);
        }
        /**
         * Handles a POST request to update the current room of a student.
         * It reads the request body, extracts the room label, and updates the student's current room.
         * If successful, it responds with a 200 OK status; otherwise, it responds with an error status.
         *
         * @param in The BufferedReader to read the request body.
         * @param out The PrintWriter to send the response.
         * @param request The raw HTTP request string.
         * @throws IOException If an I/O error occurs while reading or writing.
         */
        private void handleUpdateRoom(BufferedReader in, PrintWriter out, String request) throws IOException {
            int contentLength = 0;
            for (String line : request.split("\n")) {
                if (line.startsWith("Content-Length:")) {
                    contentLength = Integer.parseInt(line.split(":")[1].trim());
                }
            }
            if (contentLength <= 0) {
                PostResponse.badRequest("Missing or invalid Content-Length!").respond(out);
                return;
            }
            char[] bodyChars = new char[contentLength];
            in.read(bodyChars, 0, contentLength);
            String body = new String(bodyChars);

            Gson gson = new Gson();
            java.lang.reflect.Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> json = gson.fromJson(body, mapType);
            String roomLabel = (String) json.get("room");

            String user = getSessionUser(request);
            Student student = User.getUser(user) instanceof Student ? (Student) User.getUser(user) : null;
            PostResponse response;
            if (student != null && roomLabel != null) {
                try {
                    Room room = Room.getRoom(roomLabel);
                    if (room != null) {
                        student.setCurrentRoom(room);
                        response = PostResponse.ok("{\"status\":\"ok\"}", ContentType.JSON);
                    } else {
                        response = PostResponse.badRequest("Room not found.");
                    }
                } catch (Exception e) {
                    response = PostResponse.internalServerError(e.getMessage());
                }
            } else {
                response = PostResponse.unauthorized("Not logged in or invalid session");
            }
            response.respond(out);
        }
        /**
         * Retrieves the username associated with the session from the request headers.
         * It searches for a cookie named "session" and checks if it exists in the session store.
         *
         * @param request The raw HTTP request string.
         * @return The username associated with the session, or null if no valid session is found.
         */
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

        /**
         * Parses the form data from the request body into a map of parameters.
         * The expected format is "key1=value1&key2=value2".
         *
         * @param body The request body containing the form data.
         * @return A map containing the parsed key-value pairs.
         */
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
                System.out.println("Neue HTTPS-Anfrage erhalten");
                new Thread(new ClientHandler(clientSocket)).start();
            } catch (IOException e) {
                System.err.println("Error while accepting client");
                e.printStackTrace();
            }
        }
    }
}
