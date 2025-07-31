subject = JSON.parse(sessionStorage.getItem("currentSubject"))

async function fetchJson(url, options) {
    const res = await fetch(url, options);
    return await res.json();
}

function openUrlWithPostParams(url, params) {
    const form = document.createElement("form");
    form.setAttribute("method", "post");
    form.setAttribute("action", url);

    Object.keys(params).forEach((key) => {
        const input = document.createElement("input");
        input.setAttribute("type", "hidden")
        input.setAttribute("name", key)
        input.setAttribute("value", params[key])
        form.appendChild(input)
    })

    const submitButton = document.createElement("button")
    submitButton.setAttribute("type", "submit")
    
    form.appendChild(submitButton)
    document.getElementsByTagName("body")[0].appendChild(form)

    submitButton.click()
}

function populateGradeList(list, grades) {
    list.innerHTML = '';
    grades.forEach(grade => {
        const li = document.createElement("li");
        li.textContent = grade;
        li.style.cursor = 'pointer';
        li.title = 'Klicken, um die Klasenstufe zu entfernen';
        li.addEventListener('click', (e) => {
            if (confirm('Soll die Klassenstufe wirklich von diesem Fach entfernt werden?')) {
                openUrlWithPostParams('/delete-grade-from-subject', {
                    "subject": subject.id,
                    "grade": grade
                })
            }
        })
        list.appendChild(li)
    });
}
function populateGradeSelect(gradeSelect, grades) {
    gradeSelect.innerHTML = '';
    grades.forEach(grade => {
        const option = document.createElement('option');
        option.text = grade;
        option.value = grade;
        gradeSelect.appendChild(option)
    })
}

function populateTopicTable(table, topics) {
    table.innerHTML = '';
    topics.forEach(topic => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td class="topic-name">${topic.name}</td>
            <td class="topic-ratio">${topic.ratio}</td>
            <td class="topic-number">${topic.number}</td>
            <td class="topic-tasks">${topic.tasks}</td>
        `
        table.appendChild(row)
    })
}

document.addEventListener('DOMContentLoaded', async (e) => {
    if (subject) {
        document.getElementById('subjectNameField').value = subject.name;
        Array.from(document.getElementsByClassName('subjectId')).forEach(function(element) {
            element.value = subject.id;
        });
    } else {
        console.error('No class data found in sessionStorage.');
    }

    document.getElementById('deleteSubjectButton').addEventListener('click', function() {
        if (confirm('Are you sure you want to delete this subject?')) {
            fetch('/delete-subject', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ id: subject.id })
            })
            .then(response => {
                if (response.ok) {
                    alert('Subject deleted successfully.');
                    window.location.href = '/manage_subjects';
                } else {
                    alert('Failed to delete subject. Please try again.');
                }
            })
            .catch(error => {
                console.error('Error deleting subject:', error);
                alert('An error occurred while trying to delete the subject.');
            });
        }
    });

    const gradeList = document.getElementById('gradeList')
    const gradeSelect = document.getElementById('gradeSelect')
    const grades = await fetchJson('/grade-list', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            subjectId: subject.id
        })
    })
    populateGradeList(gradeList, grades);
    populateGradeSelect(gradeSelect, grades);

    async function updateTopicTable(e) {
        const grade = Number(e.target.value);
        console.log(document.getElementById("topicTable"))
        const topicTable = document.querySelector("#topicTable tbody");
        const topics = await fetchJson('/topic-list', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                subjectId: subject.id, grade
            })
        })
        populateTopicTable(topicTable, topics);
    }

    gradeSelect.addEventListener('change', updateTopicTable);
    updateTopicTable({target: {value: gradeSelect.value}});

    document.getElementById("deleteAllTopics").addEventListener('click', e => 
        openUrlWithPostParams("/delete-topics", {
            subjectId: subject.id,
            grade: Number(gradeSelect.value)
        })
    );
})