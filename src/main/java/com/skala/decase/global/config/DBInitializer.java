package com.skala.decase.global.config;

import com.skala.decase.global.DataInitService;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class DBInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DBInitializer.class);
    private final JdbcTemplate jdbcTemplate;
    private final DataInitService dataInitService;

    @PostConstruct
    public void postConstruct() {
        logger.info("✅ DBInitializer Bean 등록됨 (postConstruct)");
    }

    @Override
    public void run(ApplicationArguments args) {
        alterSourceTable();
        insertInitialData();
    }

    private void alterSourceTable() {
        try {
            logger.info("Attempting to alter TD_SOURCE table...");
            String sql = "ALTER TABLE TD_SOURCE MODIFY COLUMN source_id BIGINT NOT NULL AUTO_INCREMENT;";
            jdbcTemplate.execute(sql);
            logger.info("Successfully altered TD_SOURCE table: source_id is now AUTO_INCREMENT.");

            // AUTO_INCREMENT 설정 확인
            checkAutoIncrementSetting();

        } catch (Exception e) {
            logger.warn("Could not alter TD_SOURCE table. This might be because it's already been altered or another issue occurred: {}", e.getMessage());
        }
    }

    private void checkAutoIncrementSetting() {
        try {
            // MariaDB/MySQL에서 컬럼 정보 조회
            String checkSql = "SHOW COLUMNS FROM TD_SOURCE WHERE Field = 'source_id';";
            List<Map<String, Object>> results = jdbcTemplate.queryForList(checkSql);

            if (!results.isEmpty()) {
                Map<String, Object> columnInfo = results.get(0);
                String extra = (String) columnInfo.get("Extra");
                String type = (String) columnInfo.get("Type");
                String nullable = (String) columnInfo.get("Null");

                logger.info("TD_SOURCE.source_id column info:");
                logger.info("  - Type: {}", type);
                logger.info("  - Nullable: {}", nullable);
                logger.info("  - Extra: {}", extra);

                if (extra != null && extra.contains("auto_increment")) {
                    logger.info("✅ AUTO_INCREMENT is properly set on source_id column");
                } else {
                    logger.warn("⚠️ AUTO_INCREMENT is NOT set on source_id column");
                }
            } else {
                logger.warn("Could not find source_id column in TD_SOURCE table");
            }

        } catch (Exception e) {
            logger.error("Error checking AUTO_INCREMENT setting: {}", e.getMessage());
        }
    }

    public void insertInitialData() {
        logger.info("📝 초기 데이터 삽입 시작");
        dataInitService.insertCompany();
        dataInitService.insertDepartment();
        logger.info("✅ 초기 데이터 삽입 완료");
    }
} 