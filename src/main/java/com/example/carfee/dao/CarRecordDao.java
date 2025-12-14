package com.example.carfee.dao;

import com.example.carfee.entity.CarRecord;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CarRecordDao {

    private final JdbcTemplate jdbcTemplate;

    public CarRecordDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // 插入一条车辆记录
    public void insert(CarRecord record) {
        String sql = """
            INSERT INTO car_record (plate_number, in_time, status)
            VALUES (?, ?, ?)
        """;

        jdbcTemplate.update(
                sql,
                record.getPlateNumber(),
                record.getInTime(),
                record.getStatus()
        );
    }
    public CarRecord findInParkingByPlate(String plate) {
        String sql = "SELECT * FROM car_record WHERE plate_number = ? AND status = 'IN' LIMIT 1";
        return jdbcTemplate.queryForObject(
            sql,
            new BeanPropertyRowMapper<>(CarRecord.class),
            plate
        );
    }
    public void updateOut(CarRecord record) {
        String sql = """
            UPDATE car_record
            SET out_time = ?, fee = ?, status = 'OUT'
            WHERE id = ?
            """;

        jdbcTemplate.update(
                sql,
                record.getOutTime(),
                record.getFee(),
                record.getId()
        );
    }


}






