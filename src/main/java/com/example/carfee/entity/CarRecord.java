package com.example.carfee.entity;

import java.time.LocalDateTime;

public class CarRecord {

    private Long id;
    private String plateNumber;
    private LocalDateTime inTime;
    private LocalDateTime outTime;
    private Double fee;
    private String status;      // IN / OUT

    // 支付相关字段（用于二维码支付闭环）
    private Boolean paid;       // 是否已支付
    private LocalDateTime payTime;


    public CarRecord() {}

    // ===== getter / setter =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPlateNumber() { return plateNumber; }
    public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }

    public LocalDateTime getInTime() { return inTime; }
    public void setInTime(LocalDateTime inTime) { this.inTime = inTime; }

    public LocalDateTime getOutTime() { return outTime; }
    public void setOutTime(LocalDateTime outTime) { this.outTime = outTime; }

    public Double getFee() { return fee; }
    public void setFee(Double fee) { this.fee = fee; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Boolean getPaid() { return paid; }
    public void setPaid(Boolean paid) { this.paid = paid; }

    public LocalDateTime getPayTime() { return payTime; }
    public void setPayTime(LocalDateTime payTime) { this.payTime = payTime; }
}



