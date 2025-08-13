async function fetchJson(url, options) {
  const res = await fetch(url, options);
  if (res.ok) {
    return await res.json();
  } else {
    return []
  }
}

async function fetchClasses() {
  const classes = await fetchJson('/teacher-classes', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ teacherId })
  });
  return classes;
}
async function fetchSubjects() {
  const subjects = await fetchJson('/teacher-subjects', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ teacherId })
  });
  return subjects;
}
function populateRoomSelect(roomSelect, rooms) {
  roomSelect.innerHTML = ""; // clear previous options if any
  rooms.forEach(room => {
    const option = document.createElement('option');
    option.value = room.label;
    option.textContent = room.label;
    roomSelect.appendChild(option);
  });
}

function populateClassSelect(classSelect, classes) {
  classSelect.innerHTML = ""; // clear previous options if any
  classes.forEach(cls => {
    const option = document.createElement('option');
    option.value = cls.classId || cls.id;
    option.textContent = cls.name || cls.label;
    classSelect.appendChild(option);
  });
}
function populateSubjectSelect(subjectSelect, subjects) {
  console.log("Populating subject select with subjects:", subjects);
  subjectSelect.innerHTML = ""; // clear previous options if any
  subjects.forEach(subject => {
    const option = document.createElement('option');
    option.value = subject.subjectId || subject.id; // Use subjectId or id based on your API
    option.textContent = subject.name;
    subjectSelect.appendChild(option);
  });
}

async function onClassChange(event) {
    const selectedClassId = event.target.value;
    const students = await fetchJson("/student-list", {
        method: 'POST',
        body: JSON.stringify({ classId: Number(selectedClassId) }),
        headers: {
            'Content-Type': 'application/json'
        }
    });
    const studentTable = document.getElementById("studentTableBody");
    studentTable.innerHTML = ""; // clear previous rows
    students.forEach(student => {
        const row = document.createElement('tr');
        const graduationLevels = ["Neustarter", "Starter", "Durchstarter", "Lernprofi"];
        row.innerHTML = `
            <td class="student-name">${student.name}</td>
            <td class="student-room">${student.room}</td>
            <td class="student-graduation-level">${graduationLevels[student.graduationLevel]}</td>
            <td class="student-action"><button onclick="viewStudent(${student.id})">Bearbeiten</button></td>
        `;
        studentTable.appendChild(row);
    });
}
async function populateSubjectStudentList(event) {
  const subjectSelect = document.getElementById('subjectSelect');
  const classSelect = document.getElementById('classSelect');
  const selectedClassId = classSelect.value;

  if (!selectedClassId) {
    subjectSelect.innerHTML = ""; // clear previous options if no class is selected
    return;
  }

  const students = await fetchJson("/student-list", {
    method: 'POST',
    body: JSON.stringify({ classId: Number(selectedClassId), subjectId: Number(subjectSelect.value), teacherId }),
    headers: {
      'Content-Type': 'application/json'
    }
  });

  const studentTable = document.getElementById("subjectStudentTableBody");
  studentTable.innerHTML = ""; // clear previous rows
  students.forEach(student => {
      const row = document.createElement('tr');
      row.innerHTML = `
          <td class="student-name">${student.name}</td>
          <td class="student-help">${student.help ? "Ja" : "Nein"}</td>
          <td class="student-experiment">${student.experiment ? "Ja" : "Nein"}</td>
          <td class="student-partner">${student.partner ? "Ja" : "Nein"}</td>
          <td class="student-test">${student.test ? "Ja" : "Nein"}</td>
          <td class="student-action"><button onclick="viewStudent(${student.id})">Bearbeiten</button></td>
      `;
      studentTable.appendChild(row);
  });
}
async function populateRoomStudentList(event) {
  const room = event.target.value;

  const students = await fetchJson("/get-students-by-room", {
    method: 'POST',
    body: JSON.stringify({ room }),
    headers: {
      'Content-Type': 'application/json'
    }
  });

  const studentTable = document.getElementById("roomStudentTableBody");
  studentTable.innerHTML = ""; // clear previous rows
  students.forEach(student => {
      const row = document.createElement('tr');
      row.innerHTML = `
          <td class="student-name">${student.name}</td>
          <td class="student-action-required">${student.actionRequired ? "Ja" : "Nein"}</td>
          <td class="student-action"><button onclick="viewStudent(${student.id})">Bearbeiten</button></td>
      `;
      studentTable.appendChild(row);
  });
}
function viewStudent(studentId) {
    // Add studentId to session storage
    sessionStorage.setItem('selectedStudentId', studentId);
    // Redirect to student dashboard
    window.location.href = `/student`;
}

document.addEventListener('DOMContentLoaded', async () => {
  const classSelect = document.getElementById('classSelect');
  const subjectClassSelect = document.getElementById('classSelectSubject');
  const classes = await fetchClasses();
  populateClassSelect(classSelect, classes);
  populateClassSelect(subjectClassSelect, classes);
  classSelect.addEventListener('change', onClassChange);
  subjectClassSelect.addEventListener('change', populateSubjectStudentList);
  onClassChange({ target: classSelect }); // Trigger initial load
  const subjectSelect = document.getElementById('subjectSelect');
  const subjects = await fetchSubjects();
  populateSubjectSelect(subjectSelect, subjects);
  subjectSelect.addEventListener('change', populateSubjectStudentList);
  populateSubjectStudentList({ target: subjectSelect }); // Trigger initial load

  const editSubjectSelect = document.getElementById('editSubjectSelect');
  const allSubjects = await fetchJson('/subjects');
  populateSubjectSelect(editSubjectSelect, allSubjects);

  const editClassSelect = document.getElementById('editClassSelect');
  const allClasses = await fetchJson('/classes');
  populateClassSelect(editClassSelect, allClasses);
  const roomSelect = document.getElementById("roomSelect");
  const rooms = await fetchJson("/rooms");
  populateRoomSelect(roomSelect, rooms);
  roomSelect.addEventListener('change', populateRoomStudentList);
  populateRoomStudentList({target: roomSelect});
});
