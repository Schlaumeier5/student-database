let studentData = null;

function createTaskList(tasks, labelText) {
  const label = document.createElement('h4');
  label.textContent = labelText;
  const list = document.createElement('ul');
  tasks.forEach(task => {
    const li = document.createElement('li');
    li.textContent = `${task.number} ${task.name} (Niveau ${task.niveau}, Gesamtanteil: ${task.ratio * 100}%)`;
    list.appendChild(li);
  });
  return { label, list };
}

function createRequestButton(subject, type, label) {
  const btn = document.createElement('button');
  btn.textContent = label;
  btn.addEventListener('click', () => {
    fetch('/subject_request', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ subjectId: subject.id, type })
    }).then(() => {
      btn.textContent = 'Anfrage gesendet';
      btn.disabled = true;
    });
  });
  return btn;
}

async function fetchJson(url, options) {
  const res = await fetch(url, options);
  return await res.json();
}

function setStudentInfo(studentData) {
  document.getElementById('student-name').textContent = `${studentData.firstName} ${studentData.lastName}`;
  document.getElementById('student-class').textContent = studentData.schoolClass.label;
  document.getElementById('student-email').textContent = studentData.email;
  const graduationLevels = ["Neustarter", "Starter", "Durchstarter", "Lernprofi"];
  document.getElementById('student-graduation').textContent = graduationLevels[studentData.graduationLevel];
}

function populateRoomSelect(roomSelect, rooms, graduationLevel) {
  rooms.forEach(room => {
    if (graduationLevel >= room.minimumLevel) {
      const option = document.createElement('option');
      option.value = room.label;
      option.textContent = room.label;
      roomSelect.appendChild(option);
    }
  });
}

function createPanel(subject, studentData) {
  const panel = document.createElement('div');
  panel.className = 'subject-panel';

  const header = document.createElement('h3');
  header.textContent = subject.name;

  const body = document.createElement('div');
  body.className = 'panel-body';

  header.addEventListener('click', async () => {
    panel.classList.toggle('active');
    if (panel.classList.contains('loaded')) return;
    panel.classList.add('loaded');

    // Load current topic for this subject
    const topic = await fetchJson('/currenttopic', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ subjectId: subject.id })
    });

    const topicTitle = document.createElement('p');
    topicTitle.innerHTML = `<strong>Aktuelles Thema:</strong> ${topic.name} (${topic.number})`;
    body.appendChild(topicTitle);

    // Filter tasks for the current topic
    const selectedTasks = studentData.selectedTasks.filter(
      task => task.topic.id === topic.id
    );
    const completedTasks = studentData.completedTasks.filter(
      task => task.topic.id === topic.id
    );
    let allTasks = [];
    if (Array.isArray(topic.tasks) && topic.tasks.length > 0) {
      allTasks = await fetchJson('/tasks', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ ids: topic.tasks })
      });
    }

    const otherTasks = allTasks.filter(
      task =>
        !selectedTasks.some(t => t.id === task.id) &&
        !completedTasks.some(t => t.id === task.id)
    );

    // Current stage
    const { label: selectedLabel, list: selectedList } = createTaskList(selectedTasks, 'Aktuelle Etappe:');
    body.appendChild(selectedLabel);
    body.appendChild(selectedList);

    // Completed stages
    const { label: completedLabel, list: completedList } = createTaskList(completedTasks, 'Abgeschlossene Etappen:');
    body.appendChild(completedLabel);
    body.appendChild(completedList);

    // Other stages
    const { label: otherLabel, list: otherList } = createTaskList(otherTasks, 'Weitere Etappen:');
    body.appendChild(otherLabel);
    body.appendChild(otherList);
  });

  // Request buttons
  ['hilfe', 'partner', 'betreuung', 'gelingensnachweis'].forEach(type => {
    const label = {
      hilfe: 'Ich brauche Hilfe',
      partner: 'Ich suche einen Partner',
      betreuung: 'Ich brauche Betreuung für ein Experiment',
      gelingensnachweis: 'Ich bin bereit für den Gelingensnachweis'
    }[type];

    const btn = createRequestButton(subject, type, label);
    body.appendChild(btn);
  });

  panel.appendChild(header);
  panel.appendChild(body);
  return panel;
}

document.addEventListener('DOMContentLoaded', async () => {
  // Load base data
  studentData = await fetchJson('/mydata');
  const rooms = await fetchJson('/rooms');
  const subjects = await fetchJson('/mysubjects');

  // Show student info
  setStudentInfo(studentData);

  // Show rooms
  const roomSelect = document.getElementById('room');
  populateRoomSelect(roomSelect, rooms, studentData.graduationLevel);

  roomSelect.addEventListener('change', async () => {
    await fetch('/updateroom', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ room: roomSelect.value })
    });
  });

  // Show subjects
  const subjectList = document.getElementById('subject-list');
  subjects.forEach(subject => {
    const panel = createPanel(subject, studentData);
    subjectList.appendChild(panel);
  });
});
