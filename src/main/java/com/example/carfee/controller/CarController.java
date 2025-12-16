package com.example.carfee.controller;

import com.example.carfee.ocr.BaiduOcrService;
import com.example.carfee.service.CarService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.carfee.util.QrCodeUtil;
import java.nio.file.Paths;

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
    public String carEnter(@RequestParam String plate) {
        carService.carEnter(plate.trim());
        return "车辆入场成功：" + plate;
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
        result.put("message", "请扫码支付后离场");

        return result;
    }

    // ================== 模拟支付接口（展示用） ==================
    @PostMapping("/pay")
    public String pay() {
        return "支付成功，车辆已放行";
    }

    // ================== 生成支付二维码 ==================
    @PostMapping("/qrcode")
    public String generateQr(@RequestParam String plate) throws Exception {

        String payInfo = "PAY_FOR_PLATE:" + plate;

        QrCodeUtil.generate(
                payInfo,
                Paths.get("src/main/resources/static/qrcode.png")
        );

        return "二维码生成成功";
}


    // ================== 统计 ==================
    @GetMapping("/stats")
    public Map<String, Object> stats() {
        return carService.statistics();
    }
}










