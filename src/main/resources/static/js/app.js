alert("app.js 已加载");

let currentPlate = null;
let currentFee = 0;

// ================== 入场 ==================
function carEnter() {
    const plate = plateEnter.value.trim();
    if (!plate) return alert("请输入车牌号");

    fetch(`/enter?plate=${encodeURIComponent(plate)}`, { method: "POST" })
        .then(res => res.text())
        .then(msg => alert(msg))
        .catch(() => alert("入场失败"));
}

// ================== 出场（计算费用，不放行） ==================
function carOut() {
    const plate = plateOut.value.trim();
    if (!plate) return alert("请输入车牌号");

    fetch(`/out?plate=${encodeURIComponent(plate)}`, { method: "POST" })
        .then(res => res.json())
        .then(data => {
            currentPlate = plate;
            currentFee = data.fee;

            // 显示费用
            document.getElementById("fee").innerHTML =
                `应付费用：<b>${data.fee}</b> 元`;

            // ⭐ 前端生成二维码（核心）
            const payInfo = `停车缴费\n车牌:${plate}\n金额:${data.fee}元`;
            document.getElementById("qrImg").src =
                "https://api.qrserver.com/v1/create-qr-code/?size=180x180&data="
                + encodeURIComponent(payInfo);

            // 显示支付区域
            document.getElementById("payArea").style.display = "block";
            document.getElementById("payMsg").innerText = "";
        })
        .catch(() => alert("出场失败"));
}

// ================== 模拟支付（冲 A 核心） ==================
function pay() {
    if (!currentPlate) return alert("无支付车辆");

    document.getElementById("payMsg").innerText =
        "支付成功，车辆已放行";

    // 支付完成后可以清空状态（更真实）
    currentPlate = null;
    currentFee = 0;
}

// ================== OCR 识别 ==================
function uploadPlateImage() {
    const file = plateImage.files[0];
    if (!file) return alert("请选择图片");

    const fd = new FormData();
    fd.append("file", file);

    fetch("/ocr/plate", { method: "POST", body: fd })
        .then(res => res.json())
        .then(data => {
            plateEnter.value = data.plate;
            plateOut.value = data.plate;
            alert("识别成功：" + data.plate);
        })
        .catch(() => alert("识别失败"));
}

// ================== 统计 ==================
function loadStats() {
    fetch("/stats")
        .then(res => res.json())
        .then(data => {
            const map = {
                inParkingCount: "当前在场车辆数",
                todayOutCount: "今日出场车辆数",
                todayFee: "今日收费（元）",
                totalFee: "累计收费（元）"
            };

            statsBody.innerHTML = "";
            for (let k in map) {
                const tr = document.createElement("tr");
                tr.innerHTML = `<td>${map[k]}</td><td>${data[k]}</td>`;
                statsBody.appendChild(tr);
            }
        })
        .catch(() => alert("统计加载失败"));
}












