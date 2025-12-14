package com.example.carfee.entity;

import java.time.LocalDateTime;

public class CarRecord {

    private Long id;              // 主键
    private String plateNumber;   // 车牌号
    private LocalDateTime inTime; // 入场时间
    private LocalDateTime outTime;// 离场时间
    private Double fee;           // 停车费用
    private String status;        // in / out

    // ===== 构造方法 =====
    public CarRecord() {}

    public CarRecord(String plateNumber, LocalDateTime inTime, String status) {
        this.plateNumber = plateNumber;
        this.inTime = inTime;
        this.status = status;
    }

    // ===== getter / setter =====

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public LocalDateTime getInTime() {
        return inTime;
    }

    public void setInTime(LocalDateTime inTime) {
        this.inTime = inTime;
    }

    public LocalDateTime getOutTime() {
        return outTime;
    }

    public void setOutTime(LocalDateTime outTime) {
        this.outTime = outTime;
    }

    public Double getFee() {
        return fee;
    }

    public void setFee(Double fee) {
        this.fee = fee;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}



