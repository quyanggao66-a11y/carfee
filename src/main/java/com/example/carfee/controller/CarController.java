package com.example.carfee.controller;

import com.example.carfee.service.CarService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class CarController {

    private final CarService carService;
    public CarController(CarService carService) {
        this.carService = carService;
    }

    // 入场
    @GetMapping("/enter")
    public String carEnter(@RequestParam String plate) {
        carService.carEnter(plate);
        return "车辆入场成功，车牌号：" + plate;
    }

    // 出场
    @GetMapping("/out")
    public String carOut(@RequestParam String plate) {
        double fee = carService.carOut(plate);
        return "车辆出场成功，车牌号：" + plate + "，停车费：" + fee + " 元";
    }

    // 统计接口 ⭐
    @GetMapping("/stats")
    public Map<String, Object> stats() {
        return carService.statistics();
    }
}





