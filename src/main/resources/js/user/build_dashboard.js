let studentData = null;

function decodeEntities(str) {
  const txt = document.createElement("textarea");
  txt.innerHTML = str;
  return txt.value;
}

function createTaskList(tasks, labelText, onClick) {
  const label = document.createElement('h4');
  label.textContent = labelText;
  const list = document.createElement('ul');
  tasks.forEach(task => {
    const li = document.createElement('li');
    li.textContent = `${task.number} ${decodeEntities(task.name)} (Niveau ${task.niveau}, Gesamtanteil: ${Math.round(task.ratio * 10000) / 100}%)`;
    if (typeof onClick === 'function') {
      li.style.cursor = 'pointer';
      li.addEventListener('click', () => onClick(task, li));
    }
    list.appendChild(li);
  });
  return { label, list };
}

function createRequestButton(subject, type, label) {
  const btn = document.createElement('button');
  btn.textContent = label;

  // Helper to check if this request is active
  function isActive() {
    return (
      studentData.currentRequests &&
      studentData.currentRequests[subject.id] &&
      studentData.currentRequests[subject.id].includes(type)
    );
  }

  // Set initial state
  function updateButton() {
    if (isActive()) {
      btn.classList.add('active-request');
    } else {
      btn.classList.remove('active-request');
    }
  }
  updateButton();

  btn.addEventListener('click', async () => {
    if (isActive()) {
      // Remove request
      await fetch('/subject-request', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ subjectId: subject.id, type, remove: true })
      });
      // Update local state
      if (studentData.currentRequests[subject.id]) {
        studentData.currentRequests[subject.id] = studentData.currentRequests[subject.id].filter(t => t !== type);
        if (studentData.currentRequests[subject.id].length === 0) {
          delete studentData.currentRequests[subject.id];
        }
      }
    } else {
      // Add request
      await fetch('/subject-request', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ subjectId: subject.id, type })
      });
      // Update local state
      if (!studentData.currentRequests[subject.id]) {
        studentData.currentRequests[subject.id] = [];
      }
      studentData.currentRequests[subject.id].push(type);
    }
    updateButton();
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
  roomSelect.innerHTML = ""; // clear previous options if any
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

  function createRequestButtons() {
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
  }
  function refreshPanel() {
    body.innerHTML = '';
    header.click(); // Re-trigger the header click to close the panel
    panel.classList.remove('loaded'); // Reset loaded state
    createRequestButtons();
    header.click(); // Re-trigger the header click to load tasks
  }

  header.addEventListener('click', async () => {
    panel.classList.toggle('active');
    if (panel.classList.contains('loaded')) return;
    panel.classList.add('loaded');

    // Load current topic for this subject
    const topic = await fetchJson('/current-topic', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ subjectId: subject.id })
    });

    const topicTitle = document.createElement('p');
    topicTitle.innerHTML = `<strong>Aktuelles Thema:</strong> ${topic.name} (${topic.number})`;
    body.appendChild(topicTitle);

    // Filter tasks for the current topic
    const selectedTasks = studentData.selectedTasks.filter(
      task => task.topic && task.topic.id === topic.id
    );
    const completedTasks = studentData.completedTasks.filter(
      task => task.topic && task.topic.id === topic.id
    );
    const lockedTasks = studentData.completedTasks.filter(
      task => task.topic && task.topic.id === topic.id
    )
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
        !completedTasks.some(t => t.id === task.id) &&
        !lockedTasks.some(t => t.id === task.id)
    );

    // Current stage (selectedTasks)
    const { label: selectedLabel, list: selectedList } = createTaskList(selectedTasks, 'Aktuelle Etappe:', async (task) => {
      if (window.confirm(`Möchtest du die Etappe "${task.name}" wirklich abbrechen?`)) {
        console.log(`Abbrechen der Etappe: ${task.name} (ID: ${task.id})`);
        // Cancel task
        await fetch('/cancel-task', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ taskId: task.id })
        });
        studentData.selectedTasks = studentData.selectedTasks.filter(t => t.id !== task.id);
        // No need to push to completedTasks or otherTasks, UI will refresh
        refreshPanel(); // Refresh the panel to show updated tasks
      }
    });
    body.appendChild(selectedLabel);
    body.appendChild(selectedList);

    // Completed stages
    const { label: completedLabel, list: completedList } = createTaskList(completedTasks, 'Abgeschlossene Etappen:');
    body.appendChild(completedLabel);
    body.appendChild(completedList);

    // Stages locked by the teacher
    const { label: lockedLabel, list: lockedList} = createTaskList(lockedTasks, 'Gesperrte Etappen:');
    body.appendChild(lockedLabel);
    body.appendChild(lockedList);

    // Other stages
    const { label: otherLabel, list: otherList } = createTaskList(otherTasks, 'Weitere Etappen:', async (task) => {
      await fetch('/begin-task', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ taskId: task.id })
      });
      studentData.selectedTasks.push(task);
      refreshPanel(); // Refresh the panel to show updated tasks
    });
    body.appendChild(otherLabel);
    body.appendChild(otherList);
  });

  // Request buttons
  createRequestButtons();

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

  // Wait for the browser to render (next tick)
  setTimeout(() => {
    if (studentData.currentRoom && studentData.currentRoom.label) {
      roomSelect.value = studentData.currentRoom.label;
    }
  }, 0);

  roomSelect.addEventListener('change', async () => {
    await fetch('/update-room', {
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
