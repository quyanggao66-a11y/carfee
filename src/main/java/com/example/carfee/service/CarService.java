package com.example.carfee.service;

import com.example.carfee.dao.CarRecordDao;
import com.example.carfee.entity.CarRecord;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class CarService {

    private final CarRecordDao carRecordDao;

    public CarService(CarRecordDao carRecordDao) {
        this.carRecordDao = carRecordDao;
    }

    // 车辆入场
    public void carEnter(String plate) {

        // 如果已在场，直接返回
        if (carRecordDao.countInParkingByPlate(plate) > 0) {
            throw new RuntimeException("该车辆已在场");
        }

        CarRecord record = new CarRecord();
        record.setPlateNumber(plate);
        record.setInTime(LocalDateTime.now());
        record.setStatus("IN");

        carRecordDao.insert(record);
    }


    // 车辆出场（⚠️ 这里只计算费用，不放行）
    public double carOut(String plate) {

        CarRecord record = carRecordDao.findInParkingByPlateSafe(plate);
        if (record == null) {
            throw new RuntimeException("该车辆不在场");
        }

        LocalDateTime outTime = LocalDateTime.now();
        record.setOutTime(outTime);

        double fee = calculateFee(record.getInTime(), outTime);
        record.setFee(fee);

        carRecordDao.updateOut(record);
        return fee;
    }


    // 模拟支付（冲 A 核心）
    public void pay(Long recordId) {
        carRecordDao.markPaid(recordId, LocalDateTime.now());
    }

    private double calculateFee(LocalDateTime inTime, LocalDateTime outTime) {
        long minutes = Duration.between(inTime, outTime).toMinutes();
        long hours = (minutes + 59) / 60;
        return hours <= 1 ? 5 : 5 + (hours - 1) * 3;
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






