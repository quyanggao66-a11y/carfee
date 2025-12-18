package com.example.carfee.controller;

import com.example.carfee.ocr.BaiduOcrService;
import com.example.carfee.service.CarService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin
public class CarController {

    private final CarService carService;
    private final BaiduOcrService baiduOcrService;

    public CarController(CarService carService, BaiduOcrService baiduOcrService) {
        this.carService = carService;
        this.baiduOcrService = baiduOcrService;
    }

    // ================== 车辆入场 ==================
    @PostMapping("/enter")
    public Object carEnter(@RequestParam String plate) {
        try {
            carService.carEnter(plate.trim());

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "车辆入场成功：" + plate);
            return result;
        } catch (Exception ex) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", ex.getMessage() == null ? "入场失败" : ex.getMessage());
            return err;
        }
    }

    // ================== OCR ==================
    @PostMapping("/ocr/plate")
    public Map<String, Object> ocrPlate(@RequestParam("file") MultipartFile file) throws Exception {
        String plate = baiduOcrService.recognizePlate(file);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("plate", plate);
        return result;
    }

    // ================== 车辆出场（只算钱，不放行） ==================
    @PostMapping("/out")
    public Map<String, Object> carOut(@RequestParam String plate) {

        double fee = carService.carOut(plate.trim());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("fee", fee);
        result.put("message", "出场记录已生成");

        return result;
    }

    // ================== 模拟支付接口（桌面/移动通用） ==================
    @PostMapping("/pay")
    public Map<String, Object> pay(@RequestParam String plate) {
        Map<String, Object> result = new HashMap<>();
        try {
            carService.payByPlate(plate.trim());
            result.put("success", true);
            result.put("message", "支付成功，车辆已放行");
        } catch (Exception ex) {
            ex.printStackTrace();
            result.put("success", false);
            result.put("message", ex.getMessage() == null ? "支付失败" : ex.getMessage());
        }
        return result;
    }

    // 移动端支付页面（扫码打开）
    @GetMapping(value = "/mobile/pay", produces = "text/html;charset=UTF-8")
    public String mobilePayPage(@RequestParam String plate) {
        try {
            var record = carService.findLatestOutByPlate(plate.trim());
            double fee = record == null ? 0.0 : (record.getFee() == null ? 0.0 : record.getFee());
            String html = "<!doctype html>" +
                    "<html><head><meta charset=\"utf-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">" +
                    "<title>移动支付</title>" +
                    "<style>body{font-family: -apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,'Helvetica Neue',Arial;margin:0;background:#f3f7fb;color:#222} .container{max-width:520px;margin:28px auto;padding:18px} .card{background:#fff;padding:18px;border-radius:12px;box-shadow:0 6px 18px rgba(20,40,60,0.08)} h1{font-size:20px;margin:0 0 8px} p{margin:8px 0;color:#333} .price{font-size:20px;color:#e64a19;font-weight:700} button{display:inline-block;padding:10px 14px;border-radius:8px;border:0;background:#1976d2;color:#fff;font-size:16px} #msg{margin-top:12px;color:green;font-weight:600} .meta{color:#666;font-size:13px;margin-top:6px}</style>" +
                    "</head><body><div class=\"container\">" +
                    "<div class=\"card\">" +
                    "<h1>停车缴费</h1>" +
                    "<p>车牌：<strong>" + plate + "</strong></p>" +
                    "<p>应付：<span class=\"price\">" + fee + " 元</span></p>" +
                    "<div style=\"margin-top:12px\"><button id=\"payBtn\">立即支付</button></div>" +
                    "<p id=\"msg\" class=\"meta\"></p>" +
                    "<p class=\"meta\">若长时间无响应，请尝试刷新页面或联系管理员。</p>" +
                    "</div></div>" +
                    "<script>document.getElementById('payBtn').addEventListener('click', async function(){" +
                    "  var btn=this; btn.disabled=true; btn.style.opacity=0.7; try{ const res = await fetch('/mobile/pay', {method:'POST', headers:{'Content-Type':'application/x-www-form-urlencoded'}, body:'plate='+encodeURIComponent('"+plate+"')}); const j = await res.json(); if(j.success){ document.getElementById('msg').innerText = j.message; document.getElementById('msg').style.color='green'; } else { document.getElementById('msg').innerText = '支付失败: '+(j.message||''); document.getElementById('msg').style.color='red'; } }catch(e){ document.getElementById('msg').innerText = '支付失败'; document.getElementById('msg').style.color='red'; } finally{ btn.disabled=false; btn.style.opacity=1; } });</script>" +
                    "</body></html>";
            return html;
        } catch (Exception ex) {
            ex.printStackTrace();
            return "<html><body>错误: " + (ex.getMessage() == null ? "" : ex.getMessage()) + "</body></html>";
        }
    }

    // 移动端提交支付请求（扫码后点击）
    @PostMapping("/mobile/pay")
    public Map<String, Object> mobilePay(@RequestParam String plate) {
        Map<String, Object> result = new HashMap<>();
        try {
            carService.payByPlate(plate.trim());
            result.put("success", true);
            result.put("message", "支付成功，车辆已放行");
        } catch (Exception ex) {
            ex.printStackTrace();
            result.put("success", false);
            result.put("message", ex.getMessage() == null ? "支付失败" : ex.getMessage());
        }
        return result;
    }

    // 支付状态查询（前端轮询用）
    @GetMapping("/payment/status")
    public Map<String, Object> paymentStatus(@RequestParam String plate) {
        Map<String, Object> r = new HashMap<>();
        try {
            var rec = carService.findLatestOutByPlate(plate.trim());
            boolean paid = rec != null && Boolean.TRUE.equals(rec.getPaid());
            r.put("paid", paid);
            r.put("success", true);
        } catch (Exception ex) {
            ex.printStackTrace();
            r.put("success", false);
            r.put("paid", false);
            r.put("message", ex.getMessage());
        }
        return r;
    }

    // 内部接口：尝试从本地 ngrok 控制台读取 public_url，避免浏览器 CORS 限制
    @GetMapping("/internal/ngrok")
    public Map<String, Object> getNgrokUrl() {
        Map<String, Object> r = new HashMap<>();
        try {
            java.net.URL url = new java.net.URL("http://127.0.0.1:4040/api/tunnels");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(1000);
            conn.setReadTimeout(1000);

            int code = conn.getResponseCode();
            if (code != 200) {
                r.put("url", null);
                r.put("error", "ngrok api not available");
                return r;
            }

            java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) sb.append(line);
            in.close();

            com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = om.readTree(sb.toString());
            if (root.has("tunnels") && root.get("tunnels").isArray()) {
                for (com.fasterxml.jackson.databind.JsonNode t : root.get("tunnels")) {
                    if (t.has("public_url")) {
                        String pu = t.get("public_url").asText();
                        if (pu.startsWith("https://")) {
                            r.put("url", pu.replaceAll("/$", ""));
                            return r;
                        }
                    }
                }
                // fallback to first tunnel
                if (root.get("tunnels").size() > 0 && root.get("tunnels").get(0).has("public_url")) {
                    r.put("url", root.get("tunnels").get(0).get("public_url").asText().replaceAll("/$", ""));
                    return r;
                }
            }

            r.put("url", null);
            return r;
        } catch (Exception ex) {
            r.put("url", null);
            r.put("error", ex.getMessage());
            return r;
        }
    }



    // ================== 统计 ==================
    @GetMapping("/stats")
    public Map<String, Object> stats() {
        return carService.statistics();
    }

    // ngrok helper removed
}










