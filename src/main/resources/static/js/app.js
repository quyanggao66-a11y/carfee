function carEnter() {
    const plate = document.getElementById("plateEnter").value;

    fetch(`/enter?plate=${plate}`, { method: "POST" })
        .then(() => alert("入场成功"));
}

function carOut() {
    const plate = document.getElementById("plateOut").value;

    fetch(`/out?plate=${plate}`, { method: "POST" })
        .then(res => res.text())
        .then(fee => {
            document.getElementById("fee").innerText = "应付费用：" + fee + " 元";
        });
}

function loadStats() {
    fetch("/stats")
        .then(res => res.json())
        .then(data => {
            document.getElementById("stats").innerText =
                JSON.stringify(data, null, 2);
        });
}
