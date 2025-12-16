package com.example.carfee.dao;

import com.example.carfee.entity.CarRecord;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class CarRecordDao {

    private final JdbcTemplate jdbcTemplate;

    public CarRecordDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // ================== 入场 ==================
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

    // 防止重复入场（⭐ 修复点 1）
    public int countInParkingByPlate(String plate) {
        String sql = """
            SELECT COUNT(*)
            FROM car_record
            WHERE plate_number = ? AND status = 'IN'
        """;

        return jdbcTemplate.queryForObject(sql, Integer.class, plate);
    }

    // 查询在场车辆
    public CarRecord findInParkingByPlateSafe(String plate) {
        String sql = """
            SELECT * FROM car_record
            WHERE plate_number = ? AND status = 'IN'
            LIMIT 1
        """;

        List<CarRecord> list = jdbcTemplate.query(
                sql,
                new BeanPropertyRowMapper<>(CarRecord.class),
                plate
        );

        return list.isEmpty() ? null : list.get(0);
    }

    // ================== 出场 ==================
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

    // ================== 支付（⭐ 冲 A 核心） ==================
    public void markPaid(Long recordId, LocalDateTime payTime) {
        String sql = """
            UPDATE car_record
            SET pay_time = ?, paid = 1
            WHERE id = ?
        """;

        jdbcTemplate.update(sql, payTime, recordId);
    }

    // ================== 统计 ==================
    public int countInParking() {
        String sql = """
            SELECT COUNT(*)
            FROM car_record
            WHERE status = 'IN'
        """;
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    public int countTodayOut() {
        String sql = """
            SELECT COUNT(*)
            FROM car_record
            WHERE DATE(out_time) = CURDATE()
        """;
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    public double sumTodayFee() {
        String sql = """
            SELECT IFNULL(SUM(fee), 0)
            FROM car_record
            WHERE DATE(out_time) = CURDATE()
        """;
        return jdbcTemplate.queryForObject(sql, Double.class);
    }

    public double sumAllFee() {
        String sql = "SELECT IFNULL(SUM(fee), 0) FROM car_record";
        return jdbcTemplate.queryForObject(sql, Double.class);
    }
}









