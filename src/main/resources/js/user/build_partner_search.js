async function fetchJson(url, options) {
    const res = await fetch(url, options);
    return await res.json();
}

const studentData = fetchJson("/mydata");

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
async function populateSubjectStudentList(event) {
    const subjectId = Number(event.target.value);
    const topicId = (await fetchJson('/current-topic', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ subjectId })
    })).id;
    const classId = (await studentData).schoolClass.id;

    const students = await fetchJson("/search-partner", {
        method: 'POST',
        body: JSON.stringify({ subjectId, topicId, classId }),
        headers: {
            'Content-Type': 'application/json'
        }
    });

    const studentTable = document.getElementById("studentTableBody");
    studentTable.innerHTML = ""; // clear previous rows
    students.forEach(student => {
        const row = document.createElement('tr');
        row.innerHTML = `
          <td class="student-name">${student.name}</td>
          <td class="student-room">${student.room}</td>
      `;
        studentTable.appendChild(row);
    });
}
document.addEventListener('DOMContentLoaded', async (_) => {
    const subjectSelect = document.getElementById('subjectSelect');
    const subjects = await fetchJson('/mysubjects');
    populateSubjectSelect(subjectSelect, subjects);
    subjectSelect.addEventListener('change', populateSubjectStudentList);
    populateSubjectStudentList({ target: subjectSelect });
})