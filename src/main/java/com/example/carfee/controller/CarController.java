package com.example.carfee.controller;

import com.example.carfee.service.CarService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CarController {

    private final CarService carService;

    // 构造器注入（Spring 推荐方式）
    public CarController(CarService carService) {
        this.carService = carService;
    }

    @GetMapping("/enter")
    public String carEnter(@RequestParam String plate) {
    carService.carEnter(plate);
    return "车辆入场成功，车牌号：" + plate;
}
    @GetMapping("/out")
    public String carOut(@RequestParam String plate) {
        double fee = carService.carOut(plate);
        return "车辆出场成功，车牌号：" + plate + "，停车费用：" + fee + " 元";
    }

}




