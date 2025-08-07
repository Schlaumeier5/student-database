package de.igslandstuhl.database.server.webserver;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import de.igslandstuhl.database.Application;
import de.igslandstuhl.database.api.Room;
import de.igslandstuhl.database.api.SchoolClass;
import de.igslandstuhl.database.api.SerializationException;
import de.igslandstuhl.database.api.Student;
import de.igslandstuhl.database.api.Subject;
import de.igslandstuhl.database.api.SubjectRequest;
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
            case "/change-current-topic":
                return handleChangeCurrentTopic(request);
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
            case "/teacher-subjects":
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
            case "/teacher":
            case "/add-subject-to-teacher":
                return handleAddSubjectToTeacher(request);
            case "/add-subject":
                return handleAddSubject(request);
            case "/edit-subject":
                return handleEditSubject(request);
            case "/delete-subject":
                return handleDeleteSubject(request);
            case "/class-subjects":
                return handleClassSubjects(request);
            case "/delete-class":
                return handleDeleteClass(request);
            case "/add-class":
                return handleAddClass(request);
            case "/edit-class":
                return handleEditClass(request);
            case "/add-subject-to-class":
                return handleAddSubjectToClass(request);
            case "/grade-list":
                return handleGradeList(request);
            case "/topic-list":
                return handleTopicList(request);
            case "/add-grade-to-subject":
                return handleAddGradeToSubject(request);
            case "/delete-grade-from-subject":
                return handleDeleteGradeFromSubject(request);
            case "/add-class-to-teacher":
                return addClassToTeacher(request);
            case "/lpt-file":
                return handleLPTFile(request);
            case "/delete-topics":
                return handleDeleteTopics(request);
            case "/change-graduation-level":
                return handleChangeGraduationLevel(request);
            default:
                return PostResponse.notFound("Unknown POST request path: " + path);
        }
    }

    private Student getCurrentStudent(PostRequest request) {
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
        return prepare(webInput, true);
    }
    private String prepare(String webInput, boolean sanitize) {
        webInput = webInput.replaceAll("%20", " ")
                .replaceAll("\\+", " ")
                .replaceAll("%0A", "\n")
                .replaceAll("%0D", "\r")
                .replaceAll("%21", "$")
                .replaceAll("%23", "#")
                .replaceAll("%26", "&")
                .replaceAll("%28", "(")
                .replaceAll("%29", ")")
                .replaceAll("%2A", "*")
                .replaceAll("%2B", "+")
                .replaceAll("%2C", ",")
                .replaceAll("%2F", "/")
                .replaceAll("%3A", ":")
                .replaceAll("%3B", ";")
                .replaceAll("%3C", "<")
                .replaceAll("%3D", "=")
                .replaceAll("%3E", ">")
                .replaceAll("%3F", "?")
                .replaceAll("%40", "@")
                .replaceAll("%5B", "[")
                .replaceAll("%5D", "]")
                .replaceAll("%7B", "{")
                .replaceAll("%7D", "}")
                .replaceAll("ÃŸ", "ß")
                .replaceAll("Ã¤", "ä")
                .replaceAll("Ã¶", "ö")
                .replaceAll("Ã¼", "ü")
                .replaceAll("Ã„", "Ä")
                .replaceAll("Ã–", "Ö")
                .replaceAll("Ãœ", "Ü")
                .replaceAll("%C3%A4", "ä")
                .replaceAll("%C3%BC", "ü")
                .replaceAll("%C3%B6", "ö")
                .replaceAll("%C3%9F", "ß")
                .replaceAll("%C2%A0", " ")
                ;
        if (sanitize) {
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
        }
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

        Student student = getCurrentStudent(request);
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
        String username = prepare(params.get("username"));
        String password = prepare(params.get("password"), false);

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

        Student student = getCurrentStudent(request);
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
     * Handles a POST request to change the current topic for a student in a specific subject.
     * It reads the request body, extracts the subject ID and topic ID, and updates the student's current topic.
     * If successful, it responds with a 200 OK status; otherwise, it responds with an error status.
     *
     * @param request The parsed POST request containing the subject and topic IDs.
     * @throws IOException If an I/O error occurs while reading or writing.
     */
    private PostResponse handleChangeCurrentTopic(PostRequest request) throws IOException {
        // Test if current user is admin or teacher
        User user = User.getUser(Server.getInstance().getWebServer().getUserManager().getSessionUser(request));
        if (user == null || (!user.isAdmin() && !user.isTeacher())) {
            return PostResponse.unauthorized("Not logged in or invalid session");
        }
        int contentLength = request.getContentLength();
        if (contentLength <= 0) {
            return PostResponse.badRequest("Missing or invalid Content-Length!");
        }
        Map<String, Object> json = request.getJson();
        if (!json.containsKey("subjectId") || !json.containsKey("topicId")) {
            return PostResponse.badRequest("Missing subjectId or topicId in request.");
        }
        int subjectId = ((Number) json.get("subjectId")).intValue();
        int topicId = ((Number) json.get("topicId")).intValue();

        Student student = getCurrentStudent(request);
        PostResponse response;
        if (student != null) {
            Subject subject = Subject.get(subjectId);
            Topic topic = Topic.get(topicId);
            if (subject != null && topic != null) {
                try {
                    student.setCurrentTopic(subject, topic);
                    response = PostResponse.ok("Current topic changed successfully", ContentType.TEXT_PLAIN);
                } catch (Exception e) {
                    response = PostResponse.internalServerError("Error changing topic: " + e.getMessage());
                }
            } else {
                response = PostResponse.badRequest("Subject or topic not found.");
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

        Student student = getCurrentStudent(request);
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

        Student student = getCurrentStudent(request);
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
        } else if (path.equals("/teacher-subjects")) {
            path = "/mysubjects";
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
                .append(", \"actionRequired\":").append(student.isActionRequired())
                .append(", \"graduationLevel\":").append(student.getGraduationLevel())
                .append(", \"room\":\"").append(student.getCurrentRoom() != null ? student.getCurrentRoom().getLabel() : "None").append("\"");
            if (request.getJson().containsKey("subjectId") && request.getJson().get("subjectId") instanceof Number subjectId) {
                Set<SubjectRequest> subjectRequests = student.getCurrentRequests().keySet().contains(subjectId.intValue()) ? student.getCurrentRequests().get(subjectId.intValue()) : Set.of();
                responseBuilder.append(", \"experiment\":").append(subjectRequests.stream().anyMatch(r -> r == SubjectRequest.EXPERIMENT))
                .append(", \"help\":").append(subjectRequests.stream().anyMatch(r -> r == SubjectRequest.HELP))
                .append(", \"test\":").append(subjectRequests.stream().anyMatch(r -> r == SubjectRequest.EXAM));
            }
            responseBuilder.append("}");
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

        Student student = getCurrentStudent(request);
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
        Map<String,String> data = request.getFormData();
        String firstName = data.get("firstName");
        String lastName = data.get("lastName");
        String email = data.get("email");
        String password = Teacher.generateRandomPassword(12, (contentLength << 4 + firstName.length() + lastName.length()) << 7 + System.currentTimeMillis() * new Random().nextInt());

        try {
            Teacher teacher = Teacher.registerTeacher(firstName, lastName, email, password);
            return PostResponse.ok(teacher.toString().replace("}", "") + ", \"password\": " + password + "}", ContentType.JSON);
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
    private PostResponse handleAddSubjectToTeacher(PostRequest request) throws IOException {
        // Test if current user is admin
        User user = User.getUser(Server.getInstance().getWebServer().getUserManager().getSessionUser(request));
        if (user == null || !user.isAdmin()) {
            return PostResponse.unauthorized("Not logged in or invalid session");
        }
        int contentLength = request.getContentLength();
        if (contentLength <= 0) {
            return PostResponse.badRequest("Missing or invalid Content-Length!");
        }
        Map<String, String> data = request.getFormData();
        int teacherId = Integer.parseInt(data.get("teacherId"));
        int subjectId = Integer.parseInt(data.get("subject"));

        Teacher teacher = Teacher.get(teacherId);
        Subject subject = Subject.get(subjectId);
        if (teacher == null || subject == null) {
            return PostResponse.notFound("Teacher or subject not found");
        }

        try {
            teacher.addSubject(subject);
            return PostResponse.redirect("/teacher");
        } catch (java.sql.SQLException e) {
            return PostResponse.internalServerError("Database error: " + e.getMessage());
        }
    }
    private PostResponse handleAddSubject(PostRequest request) throws IOException {
        // Test if current user is admin
        User user = User.getUser(Server.getInstance().getWebServer().getUserManager().getSessionUser(request));
        if (user == null || !user.isAdmin()) {
            return PostResponse.unauthorized("Not logged in or invalid session");
        }
        int contentLength = request.getContentLength();
        if (contentLength <= 0) {
            return PostResponse.badRequest("Missing or invalid Content-Length!");
        }
        Map<String, String> data = request.getFormData();
        String name = prepare(data.get("name"));

        try {
            Subject.addSubject(name);
            return PostResponse.redirect("/manage_subjects");
        } catch (java.sql.SQLException e) {
            return PostResponse.internalServerError("Database error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return PostResponse.badRequest("Invalid input: " + e.getMessage());
        }
    }
    private PostResponse handleClassSubjects(PostRequest request) throws IOException {
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
        int classId = ((Number) json.get("classId")).intValue();

        SchoolClass schoolClass = SchoolClass.get(classId);
        if (schoolClass == null) {
            return PostResponse.notFound("Class not found");
        }

        java.util.List<Subject> subjects = schoolClass.getSubjects();
        StringBuilder responseBuilder = new StringBuilder("[");
        for (int i = 0; i < subjects.size(); i++) {
            Subject subject = subjects.get(i);
            responseBuilder.append("{\"id\":").append(subject.getId())
                .append(",\"name\":\"").append(subject.getName()).append("\"}");
            if (i < subjects.size() - 1) {
                responseBuilder.append(",");
            }
        }
        responseBuilder.append("]");
        return PostResponse.ok(responseBuilder.toString(), ContentType.JSON);
    }
    private PostResponse handleDeleteClass(PostRequest request) throws IOException {
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
        int classId = ((Number)json.get("id")).intValue();

        SchoolClass schoolClass = SchoolClass.get(classId);
        if (schoolClass == null) {
            return PostResponse.notFound("Class not found");
        }

        try {
            schoolClass.delete();
            return PostResponse.ok("Class deleted successfully", ContentType.TEXT_PLAIN);
        } catch (java.sql.SQLException e) {
            return PostResponse.internalServerError("Database error: " + e.getMessage());
        }
    }
    private PostResponse handleAddClass(PostRequest request) throws IOException {
        // Test if current user is admin
        User user = User.getUser(Server.getInstance().getWebServer().getUserManager().getSessionUser(request));
        if (user == null || !user.isAdmin()) {
            return PostResponse.unauthorized("Not logged in or invalid session");
        }
        int contentLength = request.getContentLength();
        if (contentLength <= 0) {
            return PostResponse.badRequest("Missing or invalid Content-Length!");
        }
        Map<String, String> data = request.getFormData();
        String name = data.get("className");
        int grade = Integer.parseInt(data.get("grade"));

        try {
            SchoolClass.addClass(name, grade);
            return PostResponse.redirect("/manage_classes");
        } catch (java.sql.SQLException e) {
            return PostResponse.internalServerError("Database error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return PostResponse.badRequest("Invalid input: " + e.getMessage());
        }
    }
    /**
     * Handles a POST request to edit an existing class.
     * Only admins are allowed to perform this action.
     *
     * @param request The parsed POST request containing the class edit data.
     * @throws IOException If an I/O error occurs while reading or writing.
     */
    private PostResponse handleEditClass(PostRequest request) throws IOException {
        // Test if current user is admin
        User user = User.getUser(Server.getInstance().getWebServer().getUserManager().getSessionUser(request));
        if (user == null || !user.isAdmin()) {
            return PostResponse.unauthorized("Not logged in or invalid session");
        }
        int contentLength = request.getContentLength();
        if (contentLength <= 0) {
            return PostResponse.badRequest("Missing or invalid Content-Length!");
        }
        Map<String, String> data = request.getFormData();
        int classId;
        try {
            classId = Integer.parseInt(data.get("id"));
        } catch (NumberFormatException | NullPointerException e) {
            return PostResponse.badRequest("Invalid or missing classId.");
        }
        String name = data.get("name");
        int grade;
        try {
            grade = Integer.parseInt(data.get("grade"));
        } catch (NumberFormatException | NullPointerException e) {
            return PostResponse.badRequest("Invalid or missing grade.");
        }

        SchoolClass schoolClass = SchoolClass.get(classId);
        if (schoolClass == null) {
            return PostResponse.notFound("Class not found");
        }

        try {
            schoolClass.edit(name, grade);
            return PostResponse.redirect("/manage_classes");
        } catch (java.sql.SQLException e) {
            return PostResponse.internalServerError("Database error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return PostResponse.badRequest("Invalid input: " + e.getMessage());
        }
    }
    /**
     * Handles a POST request to add a subject to a class.
     * Only admins are allowed to perform this action.
     *
     * @param request The parsed POST request containing the class and subject data.
     * @throws IOException If an I/O error occurs while reading or writing.
     */
    private PostResponse handleAddSubjectToClass(PostRequest request) throws IOException {
        // Test if current user is admin
        User user = User.getUser(Server.getInstance().getWebServer().getUserManager().getSessionUser(request));
        if (user == null || !user.isAdmin()) {
            return PostResponse.unauthorized("Not logged in or invalid session");
        }
        int contentLength = request.getContentLength();
        if (contentLength <= 0) {
            return PostResponse.badRequest("Missing or invalid Content-Length!");
        }
        Map<String, String> data = request.getFormData();
        int classId;
        int subjectId;
        try {
            classId = Integer.parseInt(data.get("classId"));
            subjectId = Integer.parseInt(data.get("subject"));
        } catch (NumberFormatException | NullPointerException e) {
            return PostResponse.badRequest("Invalid or missing classId or subjectId.");
        }

        SchoolClass schoolClass = SchoolClass.get(classId);
        Subject subject = Subject.get(subjectId);
        if (schoolClass == null || subject == null) {
            return PostResponse.notFound("Class or subject not found");
        }

        try {
            schoolClass.addSubject(subject);
            return PostResponse.redirect("/class");
        } catch (java.sql.SQLException e) {
            return PostResponse.internalServerError("Database error: " + e.getMessage());
        }
    }

    /**
     * Handles a POST request to edit an existing subject.
     * Only admins are allowed to perform this action.
     *
     * @param request The parsed POST request containing the subject edit data.
     * @throws IOException If an I/O error occurs while reading or writing.
     */
    private PostResponse handleEditSubject(PostRequest request) throws IOException {
        // Test if current user is admin
        User user = User.getUser(Server.getInstance().getWebServer().getUserManager().getSessionUser(request));
        if (user == null || !user.isAdmin()) {
            return PostResponse.unauthorized("Not logged in or invalid session");
        }
        int contentLength = request.getContentLength();
        if (contentLength <= 0) {
            return PostResponse.badRequest("Missing or invalid Content-Length!");
        }
        Map<String, String> data = request.getFormData();
        int subjectId;
        try {
            subjectId = Integer.parseInt(data.get("id"));
        } catch (NumberFormatException | NullPointerException e) {
            return PostResponse.badRequest("Invalid or missing subjectId.");
        }
        String name = prepare(data.get("name"));

        Subject subject = Subject.get(subjectId);
        if (subject == null) {
            return PostResponse.notFound("Subject not found");
        }

        try {
            subject.edit(name);
            return PostResponse.redirect("/manage_subjects");
        } catch (java.sql.SQLException e) {
            return PostResponse.internalServerError("Database error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return PostResponse.badRequest("Invalid input: " + e.getMessage());
        }
    }

    /**
     * Handles a POST request to delete an existing subject.
     * Only admins are allowed to perform this action.
     *
     * @param request The parsed POST request containing the subject delete data.
     * @throws IOException If an I/O error occurs while reading or writing.
     */
    private PostResponse handleDeleteSubject(PostRequest request) throws IOException {
        // Test if current user is admin
        User user = User.getUser(Server.getInstance().getWebServer().getUserManager().getSessionUser(request));
        if (user == null || !user.isAdmin()) {
            return PostResponse.unauthorized("Not logged in or invalid session");
        }
        int contentLength = request.getContentLength();
        if (contentLength <= 0) {
            return PostResponse.badRequest("Missing or invalid Content-Length!");
        }
        Map<String, Object> data = request.getJson();
        int subjectId;
        try {
            subjectId = ((Number)data.get("id")).intValue();
        } catch (NumberFormatException | NullPointerException e) {
            return PostResponse.badRequest("Invalid or missing subjectId.");
        }

        Subject subject = Subject.get(subjectId);
        if (subject == null) {
            return PostResponse.notFound("Subject not found");
        }

        try {
            subject.delete();
            return PostResponse.redirect("/manage_subjects");
        } catch (java.sql.SQLException e) {
            return PostResponse.internalServerError("Database error: " + e.getMessage());
        }
    }
    /**
     * Handles a POST request to retrieve the list of topics for a given subject.
     * Only admins are allowed to perform this action.
     *
     * @param request The parsed POST request.
     * @return PostResponse containing the topic list or an error.
     * @throws IOException If an I/O error occurs while reading or writing.
     */
    private PostResponse handleTopicList(PostRequest request) throws IOException {
        // Test if current user is admin
        User user = User.getUser(Server.getInstance().getWebServer().getUserManager().getSessionUser(request));
        if (user == null || !user.isAdmin()) {
            return PostResponse.unauthorized("Not logged in or invalid session");
        }

        Map<String, Object> json = request.getJson();
        if (!json.containsKey("subjectId")) {
            return PostResponse.badRequest("Missing subjectId in request.");
        }

        int subjectId = ((Number) json.get("subjectId")).intValue();
        Subject subject = Subject.get(subjectId);
        if (subject == null) {
            return PostResponse.notFound("Subject not found.");
        }

        int grade = ((Number) json.get("grade")).intValue();
        
        java.util.List<Topic> topics = subject.getTopics(grade);
        StringBuilder responseBuilder = new StringBuilder("[");
        for (int i = 0; i < topics.size(); i++) {
            Topic topic = topics.get(i);
            responseBuilder.append(topic.toString());
            if (i < topics.size() - 1) {
                responseBuilder.append(",");
            }
        }
        responseBuilder.append("]");
        return PostResponse.ok(responseBuilder.toString(), ContentType.JSON);
    }
    /**
     * Handles a POST request to retrieve the list of grades.
     * Only admins are allowed to perform this action.
     *
     * @param request The parsed POST request.
     * @return PostResponse containing the grade list or an error.
     * @throws IOException If an I/O error occurs while reading or writing.
     */
    private PostResponse handleGradeList(PostRequest request) throws IOException {
        // Test if current user is admin
        User user = User.getUser(Server.getInstance().getWebServer().getUserManager().getSessionUser(request));
        if (user == null || !user.isAdmin()) {
            return PostResponse.unauthorized("Not logged in or invalid session");
        }
        
        Subject subject = Subject.get(((Number)request.getJson().get("subjectId")).intValue());

        int[] grades = subject.getGrades();

        StringBuilder responseBuilder = new StringBuilder("[");
        for (int i = 0; i < grades.length; i++) {
            responseBuilder.append("\"").append(grades[i]).append("\"");
            if (i < grades.length - 1) {
                responseBuilder.append(",");
            }
        }
        responseBuilder.append("]");
        return PostResponse.ok(responseBuilder.toString(), ContentType.JSON);
    }
    /**
     * Handles a POST request to add a grade to a subject.
     * Only admins are allowed to perform this action.
     *
     * @param request The parsed POST request containing the subject and grade data.
     * @throws IOException If an I/O error occurs while reading or writing.
     */
    private PostResponse handleAddGradeToSubject(PostRequest request) throws IOException {
        // Test if current user is admin
        User user = User.getUser(Server.getInstance().getWebServer().getUserManager().getSessionUser(request));
        if (user == null || !user.isAdmin()) {
            return PostResponse.unauthorized("Not logged in or invalid session");
        }
        int contentLength = request.getContentLength();
        if (contentLength <= 0) {
            return PostResponse.badRequest("Missing or invalid Content-Length!");
        }
        Map<String, String> data = request.getFormData();
        int subjectId;
        int grade;
        try {
            subjectId = Integer.parseInt(data.get("subjectId"));
            grade = Integer.parseInt(data.get("grade"));
        } catch (NumberFormatException | NullPointerException e) {
            return PostResponse.badRequest("Invalid or missing subjectId or grade.");
        }

        Subject subject = Subject.get(subjectId);
        if (subject == null) {
            return PostResponse.notFound("Subject not found");
        }

        try {
            subject.addToGrade(grade);
            return PostResponse.redirect("/subject");
        } catch (java.sql.SQLException e) {
            return PostResponse.internalServerError("Database error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return PostResponse.badRequest("Invalid input: " + e.getMessage());
        }
    }

    /**
     * Handles a POST request to delete a grade from a subject.
     * Only admins are allowed to perform this action.
     *
     * @param request The parsed POST request containing the subject and grade data.
     * @throws IOException If an I/O error occurs while reading or writing.
     */
    private PostResponse handleDeleteGradeFromSubject(PostRequest request) throws IOException {
        // Test if current user is admin
        User user = User.getUser(Server.getInstance().getWebServer().getUserManager().getSessionUser(request));
        if (user == null || !user.isAdmin()) {
            return PostResponse.unauthorized("Not logged in or invalid session");
        }
        int contentLength = request.getContentLength();
        if (contentLength <= 0) {
            return PostResponse.badRequest("Missing or invalid Content-Length!");
        }
        Map<String, String> data = request.getFormData();
        int subjectId;
        int grade;
        try {
            subjectId = Integer.parseInt(data.get("subject"));
            grade = Integer.parseInt(data.get("grade"));
        } catch (NumberFormatException | NullPointerException e) {
            return PostResponse.badRequest("Invalid or missing subjectId or grade.");
        }

        Subject subject = Subject.get(subjectId);
        if (subject == null) {
            return PostResponse.notFound("Subject not found");
        }

        try {
            subject.removeFromGrade(grade);
            return PostResponse.redirect("/subject");
        } catch (java.sql.SQLException e) {
            return PostResponse.internalServerError("Database error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return PostResponse.badRequest("Invalid input: " + e.getMessage());
        }
    }
    /**
     * Handles a POST request to add a class to a teacher.
     * Only admins are allowed to perform this action.
     *
     * @param request The parsed POST request containing the teacher and class data.
     * @throws IOException If an I/O error occurs while reading or writing.
     */
    private PostResponse addClassToTeacher(PostRequest request) throws IOException {
        // Test if current user is admin
        User user = User.getUser(Server.getInstance().getWebServer().getUserManager().getSessionUser(request));
        if (user == null || !user.isAdmin()) {
            return PostResponse.unauthorized("Not logged in or invalid session");
        }
        int contentLength = request.getContentLength();
        if (contentLength <= 0) {
            return PostResponse.badRequest("Missing or invalid Content-Length!");
        }
        Map<String, String> data = request.getFormData();
        int teacherId;
        int classId;
        try {
            teacherId = Integer.parseInt(data.get("teacherId"));
            classId = Integer.parseInt(data.get("class"));
        } catch (NumberFormatException | NullPointerException e) {
            return PostResponse.badRequest("Invalid or missing teacherId or classId.");
        }

        Teacher teacher = Teacher.get(teacherId);
        SchoolClass schoolClass = SchoolClass.get(classId);
        if (teacher == null || schoolClass == null) {
            return PostResponse.notFound("Teacher or class not found");
        }

        try {
            teacher.addClass(schoolClass);
            return PostResponse.redirect("/teacher");
        } catch (java.sql.SQLException e) {
            return PostResponse.internalServerError("Database error: " + e.getMessage());
        }
    }
    private PostResponse handleLPTFile(PostRequest request) {
        // Test if current user is admin
        User user = User.getUser(Server.getInstance().getWebServer().getUserManager().getSessionUser(request));
        if (user == null || !user.isAdmin()) {
            return PostResponse.unauthorized("Not logged in or invalid session");
        }
        int contentLength = request.getContentLength();
        if (contentLength <= 0) {
            return PostResponse.badRequest("Missing or invalid Content-Length!");
        }

        String file = prepare(request.getBodyAsString().replaceFirst("file=", "").replace("Â", ""));
        try {
            Application.getInstance().readFile(file);
        } catch (SerializationException e) {
            if (e.getCause() == null)
                return PostResponse.badRequest("File is malformed (" + e + ")");
            else
                return PostResponse.badRequest("File is malformed(" + e + ", caused by " + e.getCause() + ")");
        } catch (SQLException e) {
            return PostResponse.internalServerError("Server sql database access failed.");
        }

        return PostResponse.ok("File data stored", ContentType.TEXT_PLAIN);
    }
    /**
     * Handles a POST request to delete topics from a subject.
     * Only admins are allowed to perform this action.
     *
     * @param request The parsed POST request containing the subject and topic IDs.
     * @throws IOException If an I/O error occurs while reading or writing.
     */
    private PostResponse handleDeleteTopics(PostRequest request) throws IOException {
        // Test if current user is admin
        User user = User.getUser(Server.getInstance().getWebServer().getUserManager().getSessionUser(request));
        if (user == null || !user.isAdmin()) {
            return PostResponse.unauthorized("Not logged in or invalid session");
        }
        int contentLength = request.getContentLength();
        if (contentLength <= 0) {
            return PostResponse.badRequest("Missing or invalid Content-Length!");
        }
        Map<String, String> json = request.getFormData();
        if (!json.containsKey("subjectId")) {
            return PostResponse.badRequest("Missing subjectId or topicIds in request.");
        }
        int subjectId = Integer.parseInt(json.get("subjectId"));
        Subject subject = Subject.get(subjectId);
        if (subject == null) {
            return PostResponse.notFound("Subject not found");
        }
        int grade = Integer.parseInt(json.get("grade"));
        try {
            subject.getTopics(grade).forEach((topic) -> {
                try {
                    topic.delete();
                } catch (SQLException e) {
                    throw new IllegalStateException(e);
                }
            });
            return PostResponse.redirect("/subject");
        } catch (IllegalStateException e) {
            return PostResponse.internalServerError("Database error: " + e.getCause().getMessage());
        }
    }
    private PostResponse handleChangeGraduationLevel(PostRequest request) throws IOException {
        // Test if current user is admin
        User user = User.getUser(Server.getInstance().getWebServer().getUserManager().getSessionUser(request));
        if (user == null || !user.isAdmin()) {
            return PostResponse.unauthorized("Not logged in or invalid session");
        }
        int contentLength = request.getContentLength();
        if (contentLength <= 0) {
            return PostResponse.badRequest("Missing or invalid Content-Length!");
        }
        Map<String, Object> data = request.getJson();
        Student student = getCurrentStudent(request);
        int graduationLevel;
        try {
            graduationLevel = ((Number)data.get("graduationLevel")).intValue();
        } catch (NumberFormatException | NullPointerException e) {
            return PostResponse.badRequest("Invalid or missing subjectId or grade.");
        }

        try {
            student.changeGraduationLevel(graduationLevel);
            return PostResponse.ok("Changed graduation level", ContentType.TEXT_PLAIN);
        } catch (java.sql.SQLException e) {
            return PostResponse.internalServerError("Database error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return PostResponse.badRequest("Invalid input: " + e.getMessage());
        }
    }
}