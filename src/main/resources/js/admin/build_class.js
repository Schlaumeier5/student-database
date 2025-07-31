cls = JSON.parse(sessionStorage.getItem('currentClass'));

async function fetchJson(url, options) {
    const res = await fetch(url, options);
    return await res.json();
}

function populateStudentTable(studentTable, students) {
    studentTable.innerHTML = ''; // Clear existing rows
  const graduationLevels = ["Neustarter", "Starter", "Durchstarter", "Lernprofi"];
    students.forEach(function(student) {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${student.name}</td>
            <td>${student.room}</td>
            <td>${graduationLevels[student.graduationLevel]}</td>
            <td><button class="edit-student" onclick="viewStudent(${JSON.stringify(student).replaceAll('"', "'")})">Bearbeiten</button></td>
        `;
        studentTable.appendChild(row);
    });
}
function populateSubjectList(subjectList, subjects) {
    subjectList.innerHTML = ''; // Clear existing items
    subjects.forEach(function(subject) {
        const listItem = document.createElement('li');
        listItem.textContent = subject.name;
        subjectList.appendChild(listItem);
    });
}
function populateSubjectSelect(subjectSelect, subjects) {
    subjectSelect.innerHTML = ''; // Clear existing options
    subjects.forEach(function(subject) {
        const option = document.createElement('option');
        option.value = subject.id;
        option.textContent = subject.name;
        subjectSelect.appendChild(option);
    });
}

function viewStudent(student) {
    sessionStorage.setItem('currentStudent', JSON.stringify(student));
    window.location.href = '/student';
}

document.addEventListener('DOMContentLoaded', async function() {
    if (cls) {
        document.getElementById('className').value = cls.label;
        document.getElementById('classGrade').value = cls.grade;

        Array.from(document.getElementsByClassName('classId')).forEach(function(element) {
            element.value = cls.id;
        });
    } else {
        console.error('No class data found in sessionStorage.');
    }
    document.getElementById('deleteClassButton').addEventListener('click', function() {
        if (confirm('Are you sure you want to delete this class?')) {
            fetch('/delete-class', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ id: cls.id })
            })
            .then(response => {
                if (response.ok) {
                    alert('Class deleted successfully.');
                    window.location.href = '/manage_classes';
                } else {
                    alert('Failed to delete class. Please try again.');
                }
            })
            .catch(error => {
                console.error('Error deleting class:', error);
                alert('An error occurred while trying to delete the class.');
            });
        }
    });

    const studentTableBody = document.querySelector('#studentTable tbody');
    students = await fetchJson("/student-list", {
        method: 'POST',
        body: JSON.stringify({ classId: Number(cls.id) }),
        headers: {
            'Content-Type': 'application/json'
        }
    });
    populateStudentTable(studentTableBody, students);

    const subjectList = document.getElementById('subjectList');
    const subjects = await fetchJson("/class-subjects", {
        method: 'POST',
        body: JSON.stringify({ classId: Number(cls.id) }),
        headers: {
            'Content-Type': 'application/json'
        }
    });
    populateSubjectList(subjectList, subjects);

    const subjectSelect = document.getElementById('subjectSelect');
    const allSubjects = await fetchJson('/subjects')
    populateSubjectSelect(subjectSelect, allSubjects);
});