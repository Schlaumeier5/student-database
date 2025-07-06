package de.igslandstuhl.database.server.webserver;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import de.igslandstuhl.database.api.Room;
import de.igslandstuhl.database.api.Student;
import de.igslandstuhl.database.api.Subject;
import de.igslandstuhl.database.api.Task;
import de.igslandstuhl.database.api.Topic;
import de.igslandstuhl.database.api.User;
import de.igslandstuhl.database.server.Server;

public class PostRequestHandler {
    /**
     * The singleton instance of the PostRequestHandler class.
     * This instance is used to handle POST requests in the web server.
     * It ensures that only one instance of the handler is created, providing a consistent interface for processing requests.
     */
    private static final PostRequestHandler instance = new PostRequestHandler();
    /**
     * Returns the singleton instance of the PostRequestHandler class.
     * This method provides access to the handler instance, ensuring that only one instance is used throughout the application.
     *
     * @return The singleton instance of the PostRequestHandler class.
     */
    public static PostRequestHandler getInstance() {
        return instance;
    }
    /**
     * Private constructor to prevent instantiation.
     * This constructor is private to ensure that the PostRequestHandler class cannot be instantiated directly,
     * enforcing the singleton pattern.
     */
    private PostRequestHandler() {
        // Private constructor to prevent instantiation
    }

    /**
     * Handles the POST request based on the path specified in the request.
     * It routes the request to the appropriate handler method based on the path.
     * @param request
     * @param in
     * @param out
     * @throws IOException
     */
    public PostResponse handlePostRequest(PostRequest request) throws IOException {
        String path = request.getPath();

        switch (path) {
            case "/login":
                return handleLogin(request);
            case "/subject-request":
                return handleSubjectRequest(request);
            case "/current-topic":
                return handleCurrentTopic(request);
            case "/tasks":
                return handleTasks(request);
            case "/update-room":
                return handleUpdateRoom(request);
            default:
                return PostResponse.notFound("Unknown POST request path: " + path);
        }
    }

    /**
     * Handles a POST request for subject requests, parsing the request body and updating the student's subject requests.
     * Subject requests can be requests for help, a partner etc.
     * Subject requests are expected to be in JSON format with fields for subjectId and type.
     * If the request is successful, it responds with a 200 OK status and a message.
     * If the request is invalid or the user is not logged in, it responds with an appropriate error status.
     *
     * @param request The parsed POST request containing the subject ID.
     * @throws IOException If an I/O error occurs while reading or writing.
     */
    private PostResponse handleSubjectRequest(PostRequest request) throws IOException {
        int contentLength = request.getContentLength();
        if (contentLength <= 0) {
            return PostResponse.badRequest("Missing or invalid Content-Length!");
        }
        Map<String, Object> json = request.getJson();
        // Parse JSON body for subjectId and type using Gson
        int subjectId = ((Number) json.get("subjectId")).intValue();
        String type = (String) json.get("type");

        String user = Server.getInstance().getWebServer().getUserManager().getSessionUser(request);
        Student student = User.getUser(user) instanceof Student ? (Student) User.getUser(user) : null;
        PostResponse response;
        if (student != null) {
            student.addSubjectRequest(subjectId, type);
            response = PostResponse.ok("Saved request", ContentType.TEXT_PLAIN);
        } else {
            response = PostResponse.unauthorized("Not logged in or invalid session");
        }
        return response;
    }
    /**
     * Handles a POST request for user login, parsing the request body and validating the credentials.
     * If the login is successful, it generates a session ID and stores it in the session store.
     * If the login fails, it responds with a 401 Unauthorized status.
     *
     * @param request The parsed POST request containing the login credentials.
     * @throws IOException If an I/O error occurs while reading or writing.
     */
    private PostResponse handleLogin(PostRequest request) throws IOException {
        int contentLength = request.getContentLength();
    
        // If no Content-Length header is present or it is invalid, respond with bad request
        if (contentLength <= 0) {
            return PostResponse.badRequest("Missing or invalid Content-Length!");
        }

        // Expected format: "username=user&password=pass"
        Map<String, String> params = request.getFormData();
        String username = params.get("username");
        String password = params.get("password");

        // Check login credentials in the database
        if (Server.getInstance().isValidUser(username, password)) {
            String sessionId = UUID.randomUUID().toString();
            Server.getInstance().getWebServer().getUserManager().addSession(sessionId, username);

            return PostResponse.ok("Login successful", ContentType.TEXT_PLAIN, new Cookie("session", sessionId));
        } else {
            return PostResponse.unauthorized("Wrong credentials!");
        }
    }
    /**
     * Handles a POST request for the current topic of a student in a specific subject.
     * It reads the request body, extracts the subject ID, and retrieves the current topic for that subject.
     * If successful, it responds with the topic details in JSON format.
     * If the user is not logged in or there is no current topic, it responds with an appropriate error status.
     *
     * @param request The parsed POST request containing the subject ID.
     * @throws IOException If an I/O error occurs while reading or writing.
     */
    private PostResponse handleCurrentTopic(PostRequest request) throws IOException {
        int contentLength = request.getContentLength();
        if (contentLength <= 0) {
            return PostResponse.badRequest("Missing or invalid Content-Length!");
        }
        // Parse JSON body for subjectId using Gson
        Map<String, Object> json = request.getJson();
        int subjectId = ((Number) json.get("subjectId")).intValue();

        String user = Server.getInstance().getWebServer().getUserManager().getSessionUser(request);
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
        return response;
    }
    /**
     * Handles a POST request for tasks, parsing the request body to retrieve a list of task IDs.
     * It retrieves the tasks associated with those IDs and responds with their details in JSON format.
     * If the user is not logged in or the request is invalid, it responds with an appropriate error status.
     *
     * @param request The parsed POST request containing the task IDs.
     * @throws IOException If an I/O error occurs while reading or writing.
     */
    private PostResponse handleTasks(PostRequest request) throws IOException {
        int contentLength = request.getContentLength();
        if (contentLength <= 0) {
            return PostResponse.badRequest("Fehlende oder ungültige Content-Length!");
        }

        Map<String, Object> json = request.getJson();

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

        String user = Server.getInstance().getWebServer().getUserManager().getSessionUser(request);
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
        return response;
    }
    /**
     * Handles a POST request to update the current room of a student.
     * It reads the request body, extracts the room label, and updates the student's current room.
     * If successful, it responds with a 200 OK status; otherwise, it responds with an error status.
     *
     * @param request The parsed POST request containing the room label.
     * @throws IOException If an I/O error occurs while reading or writing.
     */
    private PostResponse handleUpdateRoom(PostRequest request) throws IOException {
        int contentLength = request.getContentLength();
        if (contentLength <= 0) {
            return PostResponse.badRequest("Missing or invalid Content-Length!");
        }
        Map<String, Object> json = request.getJson();
        String roomLabel = (String) json.get("room");

        String user = Server.getInstance().getWebServer().getUserManager().getSessionUser(request);
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
        return response;
    }
}