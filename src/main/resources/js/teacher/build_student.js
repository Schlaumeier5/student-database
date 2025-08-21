let studentData = null;

let studentId = Number(sessionStorage.getItem('selectedStudentId'));

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
        body: JSON.stringify({ subjectId: subject.id, subjectRequest: type, remove: true, studentId })
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
        body: JSON.stringify({ subjectId: subject.id, subjectRequest: type, studentId })
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
  if (res.ok){
    return await res.json();
  }
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
        hilfe: 'Schüler braucht Hilfe',
        partner: 'Schüler sucht einen Partner',
        betreuung: 'Schüler braucht Betreuung für ein Experiment',
        gelingensnachweis: 'Schüler ist bereit für den Gelingensnachweis'
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
      body: JSON.stringify({ subjectId: subject.id, studentId: studentId })
    });

    // Load all topics for this subject
    const topics = await fetchJson('/topic-list', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                subjectId: subject.id, grade: studentData.schoolClass.grade
            })
      })

    const topicTitle = document.createElement('p');
    topicTitle.innerHTML = `<label for="topicSelect">Aktuelles Thema:</label>`;
    const topicSelect = document.createElement('select');
    topics.forEach(t => {
      const option = document.createElement('option');
      option.value = t.id;
      option.textContent = `${t.name} (${t.number})`;
      option.selected = (topic && topic.id == t.id);
      topicSelect.appendChild(option);
    });
    topicSelect.addEventListener('change', async e => {
      result = await fetch('/change-current-topic', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ subjectId: subject.id, studentId, topicId: Number(e.target.value) })
      });
      if (result.ok) {
        refreshPanel();
      } else {
        alert('Fehler beim Ändern des Themas')
      }
    });
    topicSelect.id = 'topicSelect';
    topicTitle.appendChild(topicSelect);
    body.appendChild(topicTitle);

    // Filter tasks for the current topic
    const selectedTasks = studentData.selectedTasks.filter(
      task => task.topic && task.topic.id === topic.id
    );
    const completedTasks = studentData.completedTasks.filter(
      task => task.topic && task.topic.id === topic.id
    );
    const lockedTasks = studentData.lockedTasks.filter(
      task => task.topic && task.topic.id === topic.id
    );
    let allTasks = [];
    if (Array.isArray(topic.tasks) && topic.tasks.length > 0) {
      allTasks = await fetchJson('/tasks', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ ids: topic.tasks, studentId: studentId })
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
      const action = window.prompt(
        'Was möchten Sie tun?\n1: Als abgeschlossen markieren\n2: Aufgabe abbrechen\n3: Aufgabe sperren',
        '1'
      );
      if (action === '1') {
        // Move to completed
        await fetch('/complete-task', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ taskId: task.id, studentId })
        });
        // Update local state
        studentData.selectedTasks = studentData.selectedTasks.filter(t => t.id !== task.id);
        studentData.completedTasks.push(task);
      } else if (action === '2') {
        // Cancel task
        await fetch('/cancel-task', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ taskId: task.id, studentId })
        });
        studentData.selectedTasks = studentData.selectedTasks.filter(t => t.id !== task.id);
        // No need to push to completedTasks or otherTasks, UI will refresh
      } else if (action === '3') {
        await fetch('/lock-task', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ taskId: task.id, studentId })
        });
        studentData.selectedTasks = studentData.selectedTasks.filter(t => t.id !== task.id);
        studentData.lockedTasks.push(task)
      }
      refreshPanel(); // Refresh the panel to show updated tasks
    });
    body.appendChild(selectedLabel);
    body.appendChild(selectedList);

    // Completed stages
    const { label: completedLabel, list: completedList } = createTaskList(completedTasks, 'Abgeschlossene Etappen:', async (task) => {
      if (window.confirm('Soll diese Aufgabe wirklich wieder in die offenen Aufgaben verschoben werden?')) {
        await fetch('/reopen-task', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ taskId: task.id, studentId })
        });
        studentData.completedTasks = studentData.completedTasks.filter(t => t.id !== task.id);
        // No need to push to otherTasks, UI will refresh
        refreshPanel(); // Refresh the panel to show updated tasks
      }
    });
    body.appendChild(completedLabel);
    body.appendChild(completedList);

    // locked stages
    const { label: lockedLabel, list: lockedList } = createTaskList(lockedTasks, 'Gesperrte Etappen:', async (task) => {
      if (window.confirm('Soll diese Aufgabe wirklich wieder in die offenen Aufgaben verschoben werden?')) {
        await fetch('/reopen-task', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ taskId: task.id, studentId })
        });
        studentData.lockedTasks = studentData.lockedTasks.filter(t => t.id !== task.id);
        // No need to push to otherTasks, UI will refresh
        refreshPanel(); // Refresh the panel to show updated tasks
      }
    });
    body.appendChild(lockedLabel);
    body.appendChild(lockedList);

    // Other stages
    const { label: otherLabel, list: otherList } = createTaskList(otherTasks, 'Weitere Etappen:', async (task) => {
      await fetch('/begin-task', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ taskId: task.id, studentId })
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
  studentData = await fetchJson('/student-data', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ studentId: studentId })
  });
  const rooms = await fetchJson('/rooms', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ studentId: studentId })
  });
  const subjects = await fetchJson('/student-subjects', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ studentId: studentId })
  });

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
      body: JSON.stringify({ room: roomSelect.value, studentId })
    });
  });

  // Show subjects
  const subjectList = document.getElementById('subject-list');
  subjects.forEach(subject => {
    const panel = createPanel(subject, studentData);
    subjectList.appendChild(panel);
  });
});
