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

    // ================== 手动 / OCR 共用入场接口（核心修复点）==================
    @PostMapping("/enter")
    public String carEnter(@RequestParam String plate) {

        // ⭐⭐⭐ 核心防御：防止前端把 JSON 整段塞进来
        if (plate.startsWith("{")) {
            throw new IllegalArgumentException("非法车牌参数，请只传车牌号");
        }

        carService.carEnter(plate.trim());
        return "车辆入场成功：" + plate;
    }

    // ================== OCR 识别（只负责识别，不入库）==================
    @PostMapping("/ocr/plate")
    public Map<String, Object> ocrPlate(@RequestParam("file") MultipartFile file) throws Exception {

        String plate = baiduOcrService.recognizePlate(file);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("plate", plate);

        return result;
    }

    // ================== 车辆出场 ==================
    @PostMapping("/out")
    public double carOut(@RequestParam String plate) {

        if (plate.startsWith("{")) {
            throw new IllegalArgumentException("非法车牌参数");
        }

        return carService.carOut(plate.trim());
    }

    // ================== 统计 ==================
    @GetMapping("/stats")
    public Map<String, Object> stats() {
        return carService.statistics();
    }
}








