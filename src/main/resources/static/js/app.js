// ================== 车辆入场（手动 / OCR） ==================
function carEnter() {
    const plate = document.getElementById("plateEnter").value.trim();
    if (!plate) {
        alert("请输入车牌号");
        return;
    }

    fetch(`/enter?plate=${encodeURIComponent(plate)}`, {
        method: "POST"
    })
        .then(res => {
            if (!res.ok) throw new Error("入场失败");
            return res.text();
        })
        .then(msg => alert(msg))
        .catch(err => alert(err.message));
}

// ================== 车辆出场 ==================
function carOut() {
    const plate = document.getElementById("plateOut").value.trim();
    if (!plate) {
        alert("请输入车牌号");
        return;
    }

    fetch(`/out?plate=${encodeURIComponent(plate)}`, {
        method: "POST"
    })
        .then(res => {
            if (!res.ok) throw new Error("出场失败");
            return res.text();
        })
        .then(fee => {
            document.getElementById("fee").innerText =
                "应付费用：" + fee + " 元";
        })
        .catch(err => alert(err.message));
}

// ================== 上传图片识别车牌 ⭐ 正确版 ==================
function uploadPlateImage() {
    const fileInput = document.getElementById("plateImage");
    const file = fileInput.files[0];

    if (!file) {
        alert("请先选择一张图片");
        return;
    }

    const formData = new FormData();
    formData.append("file", file);

    fetch("/ocr/plate", {
        method: "POST",
        body: formData
    })
        .then(res => res.json())
        .then(data => {
            if (!data.success || !data.plate) {
                throw new Error("识别失败");
            }

            // ⭐⭐ 只写车牌号
            document.getElementById("plateEnter").value = data.plate;
            document.getElementById("plateOut").value = data.plate;

            // ❌ 不要再显示 JSON
            // document.getElementById("ocrResult").innerText = JSON.stringify(data);

            alert("识别成功：" + data.plate);
        })
        .catch(err => alert(err.message));
}





