// 简洁、修复后的前端脚本：仅保留入场/出场/识别/统计功能
// 调试提示（可删除）

let currentPlate = null;
let currentFee = 0;

// ================== 入场 ==================
function carEnter() {
    const plateEl = document.getElementById('plateEnter');
    const plate = (plateEl && plateEl.value || '').trim();
    if (!plate) return alert('请输入车牌号');
    (async () => {
        try {
            const res = await fetch('/enter', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8' },
                body: 'plate=' + encodeURIComponent(plate)
            });
            const ct = res.headers.get('content-type') || '';
            let json = null;
            if (ct.includes('application/json')) json = await res.json(); else {
                const t = await res.text();
                try { json = JSON.parse(t); } catch (e) { json = { success: res.ok, message: t }; }
            }
            if (!res.ok) return alert('入场失败: ' + (json?.message || ('HTTP ' + res.status)));
            alert(json?.message || '入场成功');
        } catch (err) {
            console.error('POST /enter error:', err);
            alert('入场失败: ' + (err && err.message ? err.message : err));
        }
    })();
}

// ================== 出场（计算费用） ==================
function carOut() {
    const plateEl = document.getElementById('plateOut');
    const plate = (plateEl && plateEl.value || '').trim();
    if (!plate) return alert('请输入车牌号');
    fetch('/out?plate=' + encodeURIComponent(plate), { method: 'POST' })
        .then(async res => {
            const text = await res.text();
            if (!res.ok) {
                // 尝试解析后端返回的 JSON 错误信息，否则显示文本
                try { const j = JSON.parse(text); throw new Error(j.message || text || ('HTTP ' + res.status)); } catch (e) { throw new Error(text || ('HTTP ' + res.status)); }
            }
            try { return JSON.parse(text); } catch (e) { return { fee: 0 }; }
        })
        .then(data => {
            const feeEl = document.getElementById('fee');
            if (feeEl) feeEl.innerHTML = `应付费用：<b>${data.fee}</b> 元`;
            currentPlate = plate;
            currentFee = data.fee || 0;

            // 生成移动支付链接并展示二维码
            // 优先使用用户填写的 baseUrl（方便使用 ngrok 或局域网地址），否则使用 window.location.origin
            let origin = (document.getElementById('baseUrl') && document.getElementById('baseUrl').value.trim()) || window.location.origin;
            if (!/^https?:\/\//i.test(origin)) origin = window.location.origin;
            const payUrl = `${origin.replace(/\/$/, '')}/mobile/pay?plate=${encodeURIComponent(plate)}`;
            const qr = document.getElementById('qrImg');
            if (qr) {
                qr.src = 'https://api.qrserver.com/v1/create-qr-code/?size=180x180&data=' + encodeURIComponent(payUrl);
                qr.style.display = 'block';
            }

            const payLinkArea = document.getElementById('payLinkArea');
            const payLink = document.getElementById('payLink');
            const copyBtn = document.getElementById('copyPayLink');
            if (payLinkArea && payLink) {
                payLink.href = payUrl; payLink.innerText = payUrl; payLinkArea.style.display = 'block';
            }
            if (copyBtn) copyBtn.onclick = async () => { try { await navigator.clipboard.writeText(payUrl); alert('支付链接已复制'); } catch (e) { prompt('请手动复制链接：', payUrl); } };

            // 显示支付区域并启动轮询状态
            const payArea = document.getElementById('payArea'); if (payArea) payArea.style.display = 'block';
            document.getElementById('payMsg').innerText = '';
            if (window.__paymentPoller) clearInterval(window.__paymentPoller);
            let checks = 0;
            window.__paymentPoller = setInterval(async () => {
                checks++;
                try {
                    const sres = await fetch('/payment/status?plate=' + encodeURIComponent(plate));
                    if (!sres.ok) return;
                    const sj = await sres.json();
                    if (sj && sj.paid) {
                        document.getElementById('payMsg').innerText = '支付成功，车辆已放行';
                        if (payArea) payArea.style.display = 'none';
                        clearInterval(window.__paymentPoller); window.__paymentPoller = null; currentPlate = null; currentFee = 0;
                    }
                    if (checks > 60) { clearInterval(window.__paymentPoller); window.__paymentPoller = null; }
                } catch (e) { console.error('payment status poll error', e); }
            }, 2000);
        })
        .catch(err => {
            console.error(err);
            const feeEl = document.getElementById('fee'); if (feeEl) feeEl.innerHTML = '';
            alert('出场失败: ' + (err && err.message ? err.message : '未知错误'));
        });
}

// 模拟支付（在桌面上点击）
function pay() {
    if (!currentPlate) return alert('无支付车辆');
    (async () => {
        try {
            const res = await fetch('/pay', {
                method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8' },
                body: 'plate=' + encodeURIComponent(currentPlate)
            });
            const ct = res.headers.get('content-type') || '';
            let json = null;
            if (ct.includes('application/json')) json = await res.json(); else { const t = await res.text(); try { json = JSON.parse(t); } catch (e) { json = { success: res.ok, message: t }; } }
            if (json && json.success) {
                document.getElementById('payMsg').innerText = json.message || '支付成功，车辆已放行';
                const payArea = document.getElementById('payArea'); if (payArea) payArea.style.display = 'none';
                currentPlate = null; currentFee = 0; if (window.__paymentPoller) { clearInterval(window.__paymentPoller); window.__paymentPoller = null; }
            } else {
                alert('支付失败: ' + (json?.message || '未知错误'));
            }
        } catch (e) { console.error('POST /pay error', e); alert('支付失败: ' + (e && e.message ? e.message : e)); }
    })();
}

// 已移除自动检测局域网 IP 的实现（改为手动输入 baseUrl）

// ================== OCR 识别 ==================
function uploadPlateImage() {
    const fileEl = document.getElementById('plateImage');
    if (!fileEl || !fileEl.files || fileEl.files.length === 0) return alert('请选择图片');
    const file = fileEl.files[0];
    const fd = new FormData();
    fd.append('file', file);
    fetch('/ocr/plate', { method: 'POST', body: fd })
        .then(res => res.json())
        .then(data => {
            const enterEl = document.getElementById('plateEnter');
            const outEl = document.getElementById('plateOut');
            if (enterEl) enterEl.value = data.plate;
            if (outEl) outEl.value = data.plate;
            alert('识别成功：' + data.plate);
            
            // 识别完成后隐藏图片预览
            const preview = document.getElementById('platePreview');
            if (preview) {
                preview.style.display = 'none';
            }
        })
        .catch(err => { console.error(err); alert('识别失败'); });
}

// ================== 统计 ==================
function loadStats() {
    fetch('/stats')
        .then(res => res.json())
        .then(data => {
            const map = {
                inParkingCount: '当前在场车辆数',
                todayOutCount: '今日出场车辆数',
                todayFee: '今日收费（元）',
                totalFee: '累计收费（元）'
            };
            const body = document.getElementById('statsBody');
            if (!body) return;
            body.innerHTML = '';
            for (let k in map) {
                const tr = document.createElement('tr');
                tr.innerHTML = `<td>${map[k]}</td><td>${data[k]}</td>`;
                body.appendChild(tr);
            }
        })
        .catch(err => { console.error(err); alert('统计加载失败'); });
}

// 暴露到全局，供内联按钮调用
try {
    window.carEnter = carEnter;
    window.carOut = carOut;
    window.uploadPlateImage = uploadPlateImage;
    window.loadStats = loadStats;
} catch (e) {
    console.warn('无法将函数绑定到 window：', e);
}

// 自动为文件输入添加预览与 change 事件：预览图片并自动触发识别
try {
    document.addEventListener('DOMContentLoaded', () => {
        const plateImageEl = document.getElementById('plateImage');
        const preview = document.getElementById('platePreview');
        if (!plateImageEl) return;
        plateImageEl.addEventListener('change', () => {
            const f = plateImageEl.files && plateImageEl.files[0];
            if (f && preview) {
                const reader = new FileReader();
                reader.onload = (e) => { preview.src = e.target.result; preview.style.display = 'block'; };
                reader.readAsDataURL(f);
                try { uploadPlateImage(); } catch (e) { console.warn('自动触发识别失败', e); }
            } else if (preview) {
                preview.style.display = 'none'; preview.src = '#';
            }
        });
    });
} catch (e) { console.warn('添加文件输入监听失败', e); }
