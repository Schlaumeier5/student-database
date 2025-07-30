function getGrade(progress) {
  if (progress >= 0.85) return 1;
  if (progress >= 0.70) return 2;
  if (progress >= 0.55) return 3;
  if (progress >= 0.40) return 4;
  if (progress >= 0.20) return 5;
  return 6;
}

function getGradeLabel(grade) {
  return [
    "1 (Sehr gut)",
    "2 (Gut)",
    "3 (Befriedigend)",
    "4 (Ausreichend)",
    "5 (Mangelhaft)",
    "6 (Ungenügend)"
  ][grade - 1];
}

function getTaskColorClass(level) {
  if (level === 1) return "level1";
  if (level === 2) return "level2";
  if (level === 3) return "level3";
  return "special";
}

function createGradeScale() {
  const scale = document.createElement('div');
  scale.className = 'grade-scale';
  [6,5,4,3,2,1].forEach(grade => {
    const label = document.createElement('span');
    label.className = 'grade-label';
    label.textContent = getGradeLabel(grade);
    scale.appendChild(label);
  });
  return scale;
}

function createBarChart(subject, subjectName, studentData) {
  const chart = document.createElement('div');
  chart.className = 'bar-chart';

  const title = document.createElement('h3');
  title.textContent = subjectName;
  chart.appendChild(title);

  // Grade scale
  chart.appendChild(createGradeScale());

  // Get all tasks for this subject (from completed, selected, and topic.tasks)
  const completed = studentData.completedTasks.filter(t => (t.topic && t.topic.subject && t.topic.subject.name === subject.name) || (!t.topic && t.subject && t.subject == subject.name));
  const selected = studentData.selectedTasks.filter(t => t.topic && t.topic.id && t.topic.subject && t.topic.subject.name === subject.name);
  // For demo: If you have all tasks for the subject, fetch them here. Otherwise, use completed+selected as all tasks.
  let allTasks = [...completed, ...selected];
  console.log(`Creating bar chart for subject: ${subjectName}`, subject, {
    completed: completed.length,
    selected: selected.length,
    allTasks: allTasks.length
  });
  // Remove duplicates by id
  allTasks = allTasks.filter((task, idx, arr) => arr.findIndex(t => t.id === task.id && t.name === task.name) === idx);

  // Sort by completion: completed first (by completion order if available), then selected, then others
  allTasks.sort((a, b) => {
    const aCompleted = completed.some(t => t.id === a.id);
    const bCompleted = completed.some(t => t.id === b.id);
    if (aCompleted && !bCompleted) return -1;
    if (!aCompleted && bCompleted) return 1;
    // Optionally: sort by task.number or id
    return (a.number || 0) - (b.number || 0);
  });

  // Bar container
  const bar = document.createElement('div');
  bar.className = 'bar-container';

  allTasks.forEach(task => {
    const div = document.createElement('div');
    div.className = 'bar-task ' + getTaskColorClass(task.niveau);
    if (completed.some(t => t.id === task.id && t.name === task.name)) {
      div.classList.add('completed');
    } else if (selected.some(t => t.id === task.id && t.name === task.name)) {
      div.classList.add('selected', 'hatched');
    }
    div.style.width = (100 * task.ratio) + '%';
    // Optional: show task number or name
    const label = document.createElement('span');
    label.className = 'bar-task-label';
    label.textContent = task.number || '';
    div.appendChild(label);
    bar.appendChild(div);
  });

  chart.appendChild(bar);

  // Progress and grade info
  const progress = studentData.currentProgress && studentData.currentProgress[subjectName]
    ? studentData.currentProgress[subjectName].progress : 0;
  const predicted = studentData.predictedProgress && studentData.predictedProgress[subjectName]
    ? studentData.predictedProgress[subjectName].predictedProgress : 0;

  const grade = getGrade(progress);
  const predictedGrade = getGrade(predicted);

  const gradeInfo = document.createElement('div');
  gradeInfo.className = 'grade-current';
  gradeInfo.innerHTML = `<strong>Aktuelle Note:</strong> ${getGradeLabel(grade)} (${Math.round(progress * 100)}%)`;
  chart.appendChild(gradeInfo);

  const predInfo = document.createElement('div');
  predInfo.className = 'grade-prediction';
  predInfo.innerHTML = `<strong>Prognose für Jahresende:</strong> ${getGradeLabel(predictedGrade)} (${Math.round(predicted * 100)}%)`;
  chart.appendChild(predInfo);

  return chart;
}

document.addEventListener('DOMContentLoaded', async () => {
  // Load student data (reuse endpoint from dashboard)
  const res = await fetch('/mydata');
  const studentData = await res.json();

  document.getElementById('student-name').textContent = `${studentData.firstName} ${studentData.lastName}`;

  // Get all subjects from progress keys
  const subjectNames = Object.keys(studentData.currentProgress || {});
  // If you have subject objects, map them here; else, use names as fallback
  // For demo: create fake subject objects
  const subjects = subjectNames.map(name => ({ id: name, name }));

  const charts = document.getElementById('charts');
  subjects.forEach(subject => {
    charts.appendChild(createBarChart(subject, subject.name, studentData));
  });
});