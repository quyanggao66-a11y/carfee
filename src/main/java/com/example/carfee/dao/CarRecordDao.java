package com.example.carfee.dao;

import com.example.carfee.entity.CarRecord;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.dao.DataAccessException;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class CarRecordDao {

    private final JdbcTemplate jdbcTemplate;

    public CarRecordDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        // 启动时确保列存在（兼容旧数据库）
        try {
            jdbcTemplate.execute("ALTER TABLE car_record ADD COLUMN IF NOT EXISTS paid TINYINT(1) DEFAULT 0");
        } catch (Exception ignored) { }
        try {
            jdbcTemplate.execute("ALTER TABLE car_record ADD COLUMN IF NOT EXISTS pay_time DATETIME NULL");
        } catch (Exception ignored) { }
    }

    // ================== 入场 ==================
    public void insert(CarRecord record) {
        String sql = """
            INSERT INTO car_record (plate_number, in_time, status)
            VALUES (?, ?, ?)
        """;
        Timestamp inTs = record.getInTime() == null ? null : Timestamp.valueOf(record.getInTime());
        jdbcTemplate.update(
                sql,
                record.getPlateNumber(),
                inTs,
                record.getStatus()
        );
    }

    // 防止重复入场
    public int countInParkingByPlate(String plate) {
        String sql = """
            SELECT COUNT(*)
            FROM car_record
            WHERE plate_number = ? AND status = 'IN'
        """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, plate);
        return count == null ? 0 : count;
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

    // 查询最新出场记录
    public CarRecord findLatestOutByPlate(String plate) {
        String sql = """
            SELECT * FROM car_record
            WHERE plate_number = ? AND status = 'OUT'
            ORDER BY id DESC
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
        Timestamp outTs = record.getOutTime() == null ? null : Timestamp.valueOf(record.getOutTime());
        jdbcTemplate.update(
                sql,
                outTs,
                record.getFee(),
                record.getId()
        );
    }

    // ================== 支付 ==================
    public void markPaid(Long recordId, LocalDateTime payTime) {
        String sql = """
            UPDATE car_record
            SET pay_time = ?, paid = ?
            WHERE id = ?
        """;
        Timestamp ts = payTime == null ? null : Timestamp.valueOf(payTime);
        try {
            jdbcTemplate.update(sql, ts, 1, recordId);
        } catch (DataAccessException ex) {
            try {
                ensurePayColumnsExist();
                jdbcTemplate.update(sql, ts, 1, recordId);
            } catch (Exception e) {
                e.printStackTrace();
                throw ex;
            }
        }
    }

    // 检查并补齐列（warning 已处理）
    private void ensurePayColumnsExist() {
        Integer cnt = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                        "WHERE TABLE_SCHEMA = DATABASE() " +
                        "AND TABLE_NAME = 'car_record' " +
                        "AND COLUMN_NAME = 'pay_time'",
                Integer.class
        );
        if (cnt == null || cnt == 0) {
            jdbcTemplate.execute("ALTER TABLE car_record ADD COLUMN pay_time DATETIME NULL");
        }

        Integer cnt2 = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                        "WHERE TABLE_SCHEMA = DATABASE() " +
                        "AND TABLE_NAME = 'car_record' " +
                        "AND COLUMN_NAME = 'paid'",
                Integer.class
        );
        if (cnt2 == null || cnt2 == 0) {
            jdbcTemplate.execute("ALTER TABLE car_record ADD COLUMN paid TINYINT(1) DEFAULT 0");
        }
    }

    // ================== 统计 ==================
    public int countInParking() {
        String sql = """
            SELECT COUNT(*)
            FROM car_record
            WHERE status = 'IN'
        """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count == null ? 0 : count;
    }

    public int countTodayOut() {
        String sql = """
            SELECT COUNT(*)
            FROM car_record
            WHERE DATE(out_time) = CURDATE()
        """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count == null ? 0 : count;
    }

    public double sumTodayFee() {
        String sql = """
            SELECT SUM(fee)
            FROM car_record
            WHERE DATE(out_time) = CURDATE()
        """;
        Double sum = jdbcTemplate.queryForObject(sql, Double.class);
        return sum == null ? 0.0 : sum;
    }

    public double sumAllFee() {
        String sql = "SELECT SUM(fee) FROM car_record";
        Double sum = jdbcTemplate.queryForObject(sql, Double.class);
        return sum == null ? 0.0 : sum;
    }
}










