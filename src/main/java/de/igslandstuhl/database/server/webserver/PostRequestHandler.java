package de.igslandstuhl.database.server.webserver;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import de.igslandstuhl.database.api.Room;
import de.igslandstuhl.database.api.SchoolClass;
import de.igslandstuhl.database.api.Student;
import de.igslandstuhl.database.api.Subject;
import de.igslandstuhl.database.api.Task;
import de.igslandstuhl.database.api.Teacher;
import de.igslandstuhl.database.api.Topic;
import de.igslandstuhl.database.api.User;
import de.igslandstuhl.database.api.results.StudentGenerationResult;
import de.igslandstuhl.database.api.results.TeacherGenerationResult;
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
            case "/begin-task":
                return handleTaskChange(request, Task.STATUS_IN_PROGRESS);
            case "/complete-task":
                return handleTaskChange(request, Task.STATUS_COMPLETED);
            case "/cancel-task":
                return handleTaskChange(request, Task.STATUS_NOT_STARTED);
            case "/reopen-task":
                return handleTaskChange(request, Task.STATUS_NOT_STARTED);
            case "/student-data":
            case "/rooms":
            case "/student-subjects":
                return handleStudentGetData(request);
            case "/teacher-classes":
                return handleTeacherGetData(request);
            case "/student-list":
                return handleStudentList(request);
            case "/add-students":
                return handleAddStudents(request);
            case "/add-rooms":
                return handleAddRooms(request);
            case "/add-teacher":
                return handleAddTeacher(request);
            case "/add-teachers":
                return handleAddTeachers(request);
            default:
                return PostResponse.notFound("Unknown POST request path: " + path);
        }
    }

    private Student getActualStudent(PostRequest request) {
        String user = Server.getInstance().getWebServer().getUserManager().getSessionUser(request);
        if (user == null) {
            return null; // User is not logged in
        }
        User u = User.getUser(user);
        if (u instanceof Student student) {
            return student;
        } else if ((u.isTeacher() || u.isAdmin()) && request.getJson().containsKey("studentId")) {
            return Student.get(((Number) request.getJson().get("studentId")).intValue());
        }
        return null;
    }
    private String prepare(String webInput) {
        webInput = webInput.replaceAll("%20", " ")
                .replaceAll("%0A", "\n")
                .replaceAll("%0D", "\r")
                .replaceAll("%2C", ",")
                .replaceAll("%40", "@")
                .replaceAll("%3A", ":")
                .replaceAll("%3B", ";")
                .replaceAll("%3D", "=")
                .replaceAll("%3F", "?")
                .replaceAll("%23", "#")
                .replaceAll("%26", "&")
                .replaceAll("%2F", "/")
                .replaceAll("%5B", "[")
                .replaceAll("%5D", "]")
                .replaceAll("%7B", "{")
                .replaceAll("%7D", "}")
                .replaceAll("%3C", "<")
                .replaceAll("%3E", ">")
                .replaceAll("%C3%A4", "ä")
                .replaceAll("ÃŸ", "ß")
                .replaceAll("Ã¤", "ä")
                .replaceAll("Ã¶", "ö")
                .replaceAll("Ã¼", "ü")
                .replaceAll("Ã„", "Ä")
                .replaceAll("Ã–", "Ö")
                .replaceAll("Ãœ", "Ü")
                .replaceAll("%C3%BC", "ü")
                .replaceAll("%C3%B6", "ö")
                .replaceAll("%C3%9F", "ß")
                .replaceAll("%C2%A0", " ")
                ;
        // Sanitize the input to prevent XSS attacks
        webInput = webInput.replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\"", "&quot;")
                .replaceAll("'", "&#39;")
                .replaceAll("&", "&amp;");
        // Remove any script tags
        webInput = webInput.replaceAll("(?i)<script.*?>.*?</script>", "")
                .replaceAll("(?i)<script.*?/>", "")
                .replaceAll("(?i)<script.*?>", "")
                .replaceAll("(?i)</script>", "");
        // Remove any SQL injection attempts
        webInput = webInput.replaceAll("(?i)select", "")
                .replaceAll("(?i)insert", "")
                .replaceAll("(?i)update", "")
                .replaceAll("(?i)delete", "")
                .replaceAll("(?i)drop", "")
                .replaceAll("(?i)union", "")
                .replaceAll("(?i)exec", "")
                .replaceAll("(?i)execute", "");
        return webInput;
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

        Student student = getActualStudent(request);
        boolean remove = json.containsKey("remove") && (boolean) json.get("remove");
        PostResponse response;
        if (student != null) {
            if (remove) {
                student.removeSubjectRequest(subjectId, type);
                response = PostResponse.ok("Removed request", ContentType.TEXT_PLAIN);
            } else {
                student.addSubjectRequest(subjectId, type);
                response = PostResponse.ok("Saved request", ContentType.TEXT_PLAIN);
            }
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

        Student student = getActualStudent(request);
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

        Student student = getActualStudent(request);
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

        Student student = getActualStudent(request);
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
    private PostResponse handleStudentGetData(PostRequest request) {
        String path = request.getPath();
        if (path.equals("/student-data")) {
            path = "/mydata";
        } else if (path.equals("/student-subjects")) {
            path = "/mysubjects";
        }

        int studentID = ((Number) request.getJson().get("studentId")).intValue();
        Student student = Student.get(studentID);
        User user = User.getUser(Server.getInstance().getWebServer().getUserManager().getSessionUser(request));
        if (student == null) {
            return PostResponse.notFound("Student with ID " + studentID + " not found.");
        } else if (!student.hasTeacher(user.asTeacher()) && !user.isAdmin()) {
            return PostResponse.forbidden("You are not allowed to access this student's data.");
        }
        String email = student.getEmail(); // Email is the username for the student

        return PostResponse.getResource(WebResourceHandler.locationFromPath(path, email), email);
    }
    private PostResponse handleTeacherGetData(PostRequest request) {
        String path = request.getPath();
        if (path.equals("/teacher-classes")) {
            path = "/myclasses";
        }
        if (!User.getUser(Server.getInstance().getWebServer().getUserManager().getSessionUser(request)).isAdmin()) {
            return PostResponse.unauthorized("Not logged in or invalid session");
        }

        int id = ((Number) request.getJson().get("teacherId")).intValue();
        Teacher teacher = Teacher.get(id);

        String email = teacher.getEmail(); // Email is the username for the teacher
        return PostResponse.getResource(WebResourceHandler.locationFromPath(path, email), email);
    }
    private PostResponse handleStudentList(PostRequest request) {
        String path = request.getPath();
        if (!path.equals("/student-list")) {
            return PostResponse.notFound("Unknown POST request path: " + path);
        }

        User user = User.getUser(Server.getInstance().getWebServer().getUserManager().getSessionUser(request));
        if (!user.isTeacher() && !user.isAdmin()) {
            return PostResponse.unauthorized("Not logged in or invalid session");
        }

        SchoolClass schoolClass = SchoolClass.get(((Number) request.getJson().get("classId")).intValue());
        if (schoolClass == null || (user.isTeacher() && !user.asTeacher().getClassIds().contains(schoolClass.getId()))) {
            return PostResponse.forbidden("You are not allowed to access this class's student list.");
        }
        // Get the list of students for given SchoolClass
        java.util.List<Student> students = schoolClass.getStudents();
        StringBuilder responseBuilder = new StringBuilder("[");
        for (int i = 0; i < students.size(); i++) {
            Student student = students.get(i);
            responseBuilder.append("{\"id\":").append(student.getId())
                .append(",\"name\":\"").append(student.getFirstName()).append(" ").append(student.getLastName()).append('"')
                .append(", \"actionRequired\":").append(student.isActionRequired()).append("}");
            if (i < students.size() - 1) {
                responseBuilder.append(", ");
            }
        }
        responseBuilder.append("]");

        return PostResponse.ok(responseBuilder.toString(), ContentType.JSON);
    }
    private PostResponse handleTaskChange(PostRequest request, int newStatus) throws IOException {
        int contentLength = request.getContentLength();
        if (contentLength <= 0) {
            return PostResponse.badRequest("Missing or invalid Content-Length!");
        }
        Map<String, Object> json = request.getJson();
        int taskId = ((Number) json.get("taskId")).intValue();

        Student student = getActualStudent(request);
        PostResponse response;
        if (student != null) {
            Task task = Task.get(taskId);
            if (task != null) {
                try {
                    student.changeTaskStatus(task, newStatus);
                    response = PostResponse.ok("Task status changed successfully", ContentType.TEXT_PLAIN);
                } catch (java.sql.SQLException e) {
                    response = PostResponse.internalServerError("Database error: " + e.getMessage());
                }
            } else {
                response = PostResponse.notFound("Task not found");
            }
        } else {
            response = PostResponse.unauthorized("Not logged in or invalid session");
        }
        return response;
    }
    private PostResponse handleAddStudents(PostRequest request) throws IOException {
        // Test if current user is admin
        User user = User.getUser(Server.getInstance().getWebServer().getUserManager().getSessionUser(request));
        if (user == null || !user.isAdmin()) {
            return PostResponse.unauthorized("Not logged in or invalid session");
        }
        int contentLength = request.getContentLength();
        if (contentLength <= 0) {
            return PostResponse.badRequest("Missing or invalid Content-Length!");
        }
        String csv = prepare(request.getBodyAsString().replaceFirst("csv=", ""));
        try {
            StudentGenerationResult[] results = Student.generateStudentsFromCSV(csv);
            StringBuilder responseBuilder = new StringBuilder("[\n");
            for (int i = 0; i < results.length; i++) {
                StudentGenerationResult result = results[i];
                responseBuilder.append("    {\"id\":").append(result.getStudent().getId())
                    .append(",\"firstName\":\"").append(result.getStudent().getFirstName()).append('"')
                    .append(",\"lastName\":\"").append(result.getStudent().getLastName()).append('"')
                    .append(",\"email\":\"").append(result.getStudent().getEmail()).append('"')
                    .append(",\"password\":\"").append(result.getPassword()).append("\"}");
                if (i < results.length - 1) {
                    responseBuilder.append(",\n");
                }
            }
            responseBuilder.append("\n]");
            return PostResponse.ok(responseBuilder.toString(), ContentType.JSON);
        } catch (java.sql.SQLException e) {
            return PostResponse.internalServerError("Database error: " + e.getMessage());
        }
    }
    private PostResponse handleAddRooms(PostRequest request) throws IOException {
        // Test if current user is admin
        User user = User.getUser(Server.getInstance().getWebServer().getUserManager().getSessionUser(request));
        if (user == null || !user.isAdmin()) {
            return PostResponse.unauthorized("Not logged in or invalid session");
        }
        int contentLength = request.getContentLength();
        if (contentLength <= 0) {
            return PostResponse.badRequest("Missing or invalid Content-Length!");
        }
        String csv = prepare(request.getBodyAsString().replaceFirst("csv=", ""));
        try {
            Room[] rooms = Room.generateRoomsFromCSV(csv);
            StringBuilder responseBuilder = new StringBuilder("[\n");
            for (int i = 0; i < rooms.length; i++) {
                Room room = rooms[i];
                responseBuilder.append("    {\"label\":\"").append(room.getLabel()).append('"')
                        .append(",\"minimumLevel\":").append(room.getMinimumLevel()).append('}');
                if (i < rooms.length - 1) {
                    responseBuilder.append(",\n");
                }
            }
            responseBuilder.append("\n]");
            return PostResponse.ok(responseBuilder.toString(), ContentType.JSON);
        } catch (java.sql.SQLException e) {
            return PostResponse.internalServerError("Database error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return PostResponse.badRequest("Invalid CSV format: " + e.getMessage());
        }
    }
    private PostResponse handleAddTeacher(PostRequest request) throws IOException {
        // Test if current user is admin
        User user = User.getUser(Server.getInstance().getWebServer().getUserManager().getSessionUser(request));
        if (user == null || !user.isAdmin()) {
            return PostResponse.unauthorized("Not logged in or invalid session");
        }
        int contentLength = request.getContentLength();
        if (contentLength <= 0) {
            return PostResponse.badRequest("Missing or invalid Content-Length!");
        }
        Map<String, Object> json = request.getJson();
        String firstName = (String) json.get("firstName");
        String lastName = (String) json.get("lastName");
        String email = (String) json.get("email");
        String password = (String) json.get("password");

        try {
            Teacher teacher = Teacher.registerTeacher(firstName, lastName, email, password);
            return PostResponse.ok(teacher.toString(), ContentType.JSON);
        } catch (java.sql.SQLException e) {
            return PostResponse.internalServerError("Database error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return PostResponse.badRequest("Invalid input: " + e.getMessage());
        }
    }
    private PostResponse handleAddTeachers(PostRequest request) throws IOException {
        // Test if current user is admin
        User user = User.getUser(Server.getInstance().getWebServer().getUserManager().getSessionUser(request));
        if (user == null || !user.isAdmin()) {
            return PostResponse.unauthorized("Not logged in or invalid session");
        }
        int contentLength = request.getContentLength();
        if (contentLength <= 0) {
            return PostResponse.badRequest("Missing or invalid Content-Length!");
        }
        String csv = prepare(request.getBodyAsString().replaceFirst("csv=", ""));
        try {
            TeacherGenerationResult[] teachers = Teacher.generateTeachersFromCSV(csv);
            StringBuilder responseBuilder = new StringBuilder("[\n");
            for (int i = 0; i < teachers.length; i++) {
                TeacherGenerationResult result = teachers[i];
                responseBuilder.append("    {\"id\":").append(result.getId())
                        .append(",\"firstName\":\"").append(result.getFirstName()).append('"')
                        .append(",\"lastName\":\"").append(result.getLastName()).append('"')
                        .append(",\"email\":\"").append(result.getEmail()).append('"')
                        .append(",\"password\":\"").append(result.getPassword()).append("\"}");
                if (i < teachers.length - 1) {
                    responseBuilder.append(",\n");
                }
            }
            responseBuilder.append("\n]");
            return PostResponse.ok(responseBuilder.toString(), ContentType.JSON);
        } catch (java.sql.SQLException e) {
            return PostResponse.internalServerError("Database error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return PostResponse.badRequest("Invalid CSV format: " + e.getMessage());
        }
    }
}