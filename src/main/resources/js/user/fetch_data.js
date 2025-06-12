let studentData = null;

document.addEventListener('DOMContentLoaded', async () => {
  // Lade Basisdaten
  const studentRes = await fetch('/mydata');
  studentData = await studentRes.json();

  const roomsRes = await fetch('/rooms');
  const rooms = await roomsRes.json();

  const subjectsRes = await fetch('/mysubjects');
  const subjects = await subjectsRes.json();

  // Zeige Schülerdaten
  document.getElementById('student-name').textContent = `${studentData.firstName} ${studentData.lastName}`;
  document.getElementById('student-class').textContent = studentData.schoolClass.label;
  document.getElementById('student-email').textContent = studentData.email;
  const graduationLevels = ["Neustarter", "Starter", "Durchstarter", "Lernprofi"];
  document.getElementById('student-graduation').textContent = graduationLevels[studentData.graduationLevel];

  // Räume anzeigen
  const roomSelect = document.getElementById('room');
  rooms.forEach(room => {
    if (studentData.graduationLevel >= room.minimumLevel) {
      const option = document.createElement('option');
      option.value = room.label;
      option.textContent = room.label;
      roomSelect.appendChild(option);
    }
  });

  roomSelect.addEventListener('change', async () => {
    await fetch('/updateroom', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ room: roomSelect.value })
    });
  });

  // Fächer anzeigen
  const subjectList = document.getElementById('subject-list');
  subjects.forEach(subject => {
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

      // Lade aktuelles Thema für dieses Fach
      const res = await fetch('/currenttopic', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ subjectId: subject.id })
      });

      const topic = await res.json();

      const topicTitle = document.createElement('p');
      topicTitle.innerHTML = `<strong>Aktuelles Thema:</strong> ${topic.name} (${topic.number})`;
      body.appendChild(topicTitle);

      // Lernjobs zum aktuellen Thema herausfiltern
      const selectedTasks = studentData.selectedTasks.filter(
        task => task.topic.id === topic.id
      );
      const completedTasks = studentData.completedTasks.filter(
        task => task.topic.id === topic.id
      );
      let allTasks = [];
      if (Array.isArray(topic.tasks) && topic.tasks.length > 0) {
        const tasksRes = await fetch('/tasks', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ ids: topic.tasks })
        });
        allTasks = await tasksRes.json();
      }

      const otherTasks = allTasks.filter(
        task =>
          !selectedTasks.some(t => t.id === task.id) &&
          !completedTasks.some(t => t.id === task.id)
      );

      // Aktuelle Etappe
      const selectedLabel = document.createElement('h4');
      selectedLabel.textContent = 'Aktuelle Etappe:';
      body.appendChild(selectedLabel);

      const selectedList = document.createElement('ul');
      selectedTasks.forEach(task => {
        const li = document.createElement('li');
        li.textContent = `${task.number} ${task.name} (Niveau ${task.niveau}, Gesamtanteil: ${task.ratio * 100}%)`;
        selectedList.appendChild(li);
      });
      body.appendChild(selectedList);

      // Abgeschlossene Etappen
      const completedLabel = document.createElement('h4');
      completedLabel.textContent = 'Abgeschlossene Etappen:';
      body.appendChild(completedLabel);

      const completedList = document.createElement('ul');
      completedTasks.forEach(task => {
        const li = document.createElement('li');
        li.textContent = `${task.number} ${task.name} (Niveau ${task.niveau}, Gesamtanteil: ${task.ratio * 100}%)`;
        completedList.appendChild(li);
      });
      body.appendChild(completedList);

      // Weitere Etappen
      const otherLabel = document.createElement('h4');
      otherLabel.textContent = 'Weitere Etappen:';
      body.appendChild(otherLabel);

      const otherList = document.createElement('ul');
      otherTasks.forEach(task => {
        const li = document.createElement('li');
        li.textContent = `${task.number} ${task.name} (Niveau ${task.niveau}, Gesamtanteil: ${task.ratio * 100}%)`;
        otherList.appendChild(li);
      });
      body.appendChild(otherList);
    });

    // Anfrage-Buttons
    ['hilfe', 'partner', 'betreuung', 'gelingensnachweis'].forEach(type => {
      const label = {
        hilfe: 'Ich brauche Hilfe',
        partner: 'Ich suche einen Partner',
        betreuung: 'Ich brauche Betreuung für ein Experiment',
        gelingensnachweis: 'Ich bin bereit für den Gelingensnachweis'
      }[type];

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

      body.appendChild(btn);
    });

    panel.appendChild(header);
    panel.appendChild(body);
    subjectList.appendChild(panel);
  });
});
