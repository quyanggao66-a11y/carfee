package com.example.carfee.ocr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class BaiduOcrService {

    @Value("${baidu.ocr.api-key}")
    private String apiKey;

    @Value("${baidu.ocr.secret-key}")
    private String secretKey;

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * 获取 access_token
     */
    private String getAccessToken() throws Exception {
        String urlStr = "https://aip.baidubce.com/oauth/2.0/token"
                + "?grant_type=client_credentials"
                + "&client_id=" + apiKey
                + "&client_secret=" + secretKey;

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            JsonNode json = mapper.readTree(sb.toString());
            return json.get("access_token").asText();
        }
    }

    /**
     * 车牌识别
     */
    public String recognizePlate(MultipartFile file) throws Exception {

        // 1. 图片转 Base64
        byte[] imageBytes = file.getBytes();
        String imageBase64 = Base64.getEncoder().encodeToString(imageBytes);
        String imageParam = URLEncoder.encode(imageBase64, StandardCharsets.UTF_8);

        // 2. 请求地址
        String accessToken = getAccessToken();
        String urlStr = "https://aip.baidubce.com/rest/2.0/ocr/v1/license_plate"
                + "?access_token=" + accessToken;

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        String body = "image=" + imageParam;

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        // 3. 读取返回
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            JsonNode json = mapper.readTree(sb.toString());

            // 4. 解析车牌号
            return json
                    .get("words_result")
                    .get("number")
                    .asText();
        }
    }
}


