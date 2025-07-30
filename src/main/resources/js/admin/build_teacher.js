async function fetchJson(url, options) {
  const res = await fetch(url, options);
  return await res.json();
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

function populateClassSelect(classSelect, classes) {
  classSelect.innerHTML = ""; // clear previous options if any
  classes.forEach(cls => {
    const option = document.createElement('option');
    option.value = cls.classId;
    option.textContent = cls.name;
    classSelect.appendChild(option);
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
function viewStudent(studentId) {
    // Add studentId to session storage
    sessionStorage.setItem('selectedStudentId', studentId);
    // Redirect to student dashboard
    window.location.href = `/student`;
}

document.addEventListener('DOMContentLoaded', async () => {
  const classSelect = document.getElementById('classSelect');
  const classes = await fetchClasses();
  populateClassSelect(classSelect, classes);
  classSelect.addEventListener('change', onClassChange);
  onClassChange({ target: classSelect }); // Trigger initial load
});
