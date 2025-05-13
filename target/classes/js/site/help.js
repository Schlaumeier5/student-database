function buildList(template, list, insertions) {
    let build = "";
    list.forEach(element => {
        let s = template
        for (let i = 0; i < insertions.length; i++) {
            const insertion = insertions[i];
            s = s.replace("{" + i + "}", insertion(element));
        }
        build += s
    });
    return build;
}