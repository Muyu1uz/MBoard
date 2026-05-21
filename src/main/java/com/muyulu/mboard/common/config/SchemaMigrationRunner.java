package com.muyulu.mboard.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(0)
@RequiredArgsConstructor
public class SchemaMigrationRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        ensureColumn("album", "genre", "ALTER TABLE album ADD COLUMN genre VARCHAR(64) NULL AFTER name");
        ensureColumnLength("album", "genre", 255, "ALTER TABLE album MODIFY COLUMN genre VARCHAR(255) NULL");
        ensureColumn("album", "cover_url", "ALTER TABLE album ADD COLUMN cover_url VARCHAR(512) NULL AFTER genre");
        ensureColumn("album", "summary", "ALTER TABLE album ADD COLUMN summary TEXT NULL AFTER cover_url");
        ensureColumn("album", "release_date", "ALTER TABLE album ADD COLUMN release_date DATE NULL AFTER summary");
        ensureColumn("album", "trending", "ALTER TABLE album ADD COLUMN trending BIT NOT NULL DEFAULT b'0' AFTER release_date");
        ensureColumn("album", "published", "ALTER TABLE album ADD COLUMN published BIT NOT NULL DEFAULT b'1' AFTER trending");
    }

    private void ensureColumn(String tableName, String columnName, String ddl) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(1)
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                  AND COLUMN_NAME = ?
                """,
                Integer.class,
                tableName,
                columnName
        );
        if (count != null && count == 0) {
            jdbcTemplate.execute(ddl);
        }
    }

    private void ensureColumnLength(String tableName, String columnName, int expectedLength, String ddl) {
        Integer length = jdbcTemplate.queryForObject(
                """
                SELECT CHARACTER_MAXIMUM_LENGTH
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                  AND COLUMN_NAME = ?
                """,
                Integer.class,
                tableName,
                columnName
        );
        if (length != null && length < expectedLength) {
            jdbcTemplate.execute(ddl);
        }
    }
}
