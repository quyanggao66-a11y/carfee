package com.example.carfee.dao;

import com.example.carfee.entity.CarRecord;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.sql.Timestamp;
import org.springframework.dao.DataAccessException;
import java.util.List;

@Repository
public class CarRecordDao {

    private final JdbcTemplate jdbcTemplate;

    public CarRecordDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        // 在启动时尝试确保需要的列存在（向后兼容数据库未更新的情况）
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

    // 查询最新的已出场但可能未支付的记录
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

    // ================== 支付（简易实现） ==================
    public void markPaid(Long recordId, LocalDateTime payTime) {
        // 使用参数占位符来设置 pay_time 和 paid，避免数据库方言或拼写错误造成的问题
        String sql = """
            UPDATE car_record
            SET pay_time = ?, paid = ?
            WHERE id = ?
        """;
        Timestamp ts = payTime == null ? null : Timestamp.valueOf(payTime);
        try {
            jdbcTemplate.update(sql, ts, 1, recordId);
        } catch (DataAccessException ex) {
            // 如果是因为列不存在导致的语法错误，尝试检查信息模式并添加列，然后重试一次
            String msg = ex.getMessage() == null ? "" : ex.getMessage();
            String causeMsg = ex.getCause() == null || ex.getCause().getMessage() == null ? "" : ex.getCause().getMessage();
            if (msg.contains("Unknown column") || causeMsg.contains("Unknown column") || msg.contains("bad SQL grammar")) {
                try {
                    ensurePayColumnsExist();
                } catch (Exception ignored) {}
                try {
                    jdbcTemplate.update(sql, ts, 1, recordId);
                    return;
                } catch (DataAccessException ex2) {
                    ex2.printStackTrace();
                    throw ex2;
                }
            }
            ex.printStackTrace();
            throw ex;
        }
    }

    // 检查并在需要时添加 pay_time / paid 列（兼容不同 MySQL 版本）
    private void ensurePayColumnsExist() {
        try {
            Integer cnt = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'car_record' AND COLUMN_NAME = 'pay_time'",
                    Integer.class);
            if (cnt == null || cnt == 0) {
                jdbcTemplate.execute("ALTER TABLE car_record ADD COLUMN pay_time DATETIME NULL");
            }
        } catch (Exception ignored) {}

        try {
            Integer cnt2 = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'car_record' AND COLUMN_NAME = 'paid'",
                    Integer.class);
            if (cnt2 == null || cnt2 == 0) {
                jdbcTemplate.execute("ALTER TABLE car_record ADD COLUMN paid TINYINT(1) DEFAULT 0");
            }
        } catch (Exception ignored) {}
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









