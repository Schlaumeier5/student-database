package de.igslandstuhl.database.server.webserver.requests;

import de.igslandstuhl.database.api.Student;
import de.igslandstuhl.database.api.Subject;
import de.igslandstuhl.database.api.SubjectRequest;
import de.igslandstuhl.database.api.Topic;
import de.igslandstuhl.database.api.User;
import de.igslandstuhl.database.server.Server;
import de.igslandstuhl.database.server.webserver.HttpHeader;

public class APIPostRequest extends PostRequest {
    public APIPostRequest(HttpHeader header, String body, String ipAddress, boolean secureConnection) {
        super(header, body, ipAddress, secureConnection);
    }
    public Student getCurrentStudent() {
        User user = Server.getInstance().getWebServer().getSessionManager().getSessionUser(this);
        if (user == null || user == User.ANONYMOUS) {
            return null; // User is not logged in
        }
        if (user instanceof Student student) {
            return student;
        } else if ((user.isTeacher() || user.isAdmin()) && getJson().containsKey("studentId")) {
            return Student.get(getInt("studentId"));
        }
        return null;
    }
    public Subject getSubject() {
        return Subject.get(getInt("subjectId"));
    }
    public Topic getTopic() {
        return Topic.get(getInt("topicId"));
    }
    public SubjectRequest getSubjectRequest() {
        return SubjectRequest.fromGermanTranslation(getString("subjectRequest"));
    }
}
