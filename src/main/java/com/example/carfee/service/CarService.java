package com.example.carfee.service;

import com.example.carfee.dao.CarRecordDao;
import com.example.carfee.entity.CarRecord;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.time.Duration;


@Service
public class CarService {

    private final CarRecordDao carRecordDao;

    public CarService(CarRecordDao carRecordDao) {
        this.carRecordDao = carRecordDao;
    }

    // 车辆入场
    public void carEnter(String plate) {

        CarRecord record = new CarRecord();
        record.setPlateNumber(plate);
        record.setInTime(LocalDateTime.now());
        record.setStatus("IN");

        carRecordDao.insert(record);
    }
    // 车辆出场
    public double carOut(String plate) {

        // 1. 查在场记录
        CarRecord record = carRecordDao.findInParkingByPlate(plate);

        // 2. 设置出场时间
        LocalDateTime outTime = LocalDateTime.now();
        record.setOutTime(outTime);

        // 3. 计算费用
        double fee = calculateFee(record.getInTime(), outTime);
        record.setFee(fee);

        // 4. 更新数据库
        carRecordDao.updateOut(record);

        return fee;
    }
    private double calculateFee(LocalDateTime inTime, LocalDateTime outTime) {

        long minutes = Duration.between(inTime, outTime).toMinutes();
        long hours = (minutes + 59) / 60; // 向上取整

        if (hours <= 1) {
            return 5;
       }
       return 5 + (int) (hours - 1) * 3;
    }
    public Map<String, Object> statistics() {

        Map<String, Object> result = new HashMap<>();

        result.put("todayFee", carRecordDao.sumTodayFee());
        result.put("todayOutCount", carRecordDao.countTodayOut());
        result.put("inParkingCount", carRecordDao.countInParking());
        result.put("totalFee", carRecordDao.sumAllFee());

        return result;
    }



}





