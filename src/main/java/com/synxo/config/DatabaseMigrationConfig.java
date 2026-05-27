package com.synxo.config;

import java.util.List;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DatabaseMigrationConfig {

	@Bean
	public ApplicationRunner lightweightSqliteMigrations(JdbcTemplate jdbcTemplate) {
		return arguments -> {
			ensureProfileLocationColumns(jdbcTemplate);
			ensureNotificationTable(jdbcTemplate);
		};
	}

	private void ensureProfileLocationColumns(JdbcTemplate jdbcTemplate) {
		List<String> columns = jdbcTemplate.query(
			"PRAGMA table_info(profiles)",
			(resultSet, rowNumber) -> resultSet.getString("name")
		);

		addColumnIfMissing(jdbcTemplate, columns, "precise_latitude", "ALTER TABLE profiles ADD COLUMN precise_latitude REAL");
		addColumnIfMissing(jdbcTemplate, columns, "precise_longitude", "ALTER TABLE profiles ADD COLUMN precise_longitude REAL");
		addColumnIfMissing(
			jdbcTemplate,
			columns,
			"precise_location_enabled",
			"ALTER TABLE profiles ADD COLUMN precise_location_enabled INTEGER NOT NULL DEFAULT 0"
		);
	}

	private void addColumnIfMissing(JdbcTemplate jdbcTemplate, List<String> columns, String column, String sql) {
		if (!columns.contains(column)) {
			jdbcTemplate.execute(sql);
		}
	}

	private void ensureNotificationTable(JdbcTemplate jdbcTemplate) {
		jdbcTemplate.execute("""
			CREATE TABLE IF NOT EXISTS user_notifications (
				id INTEGER PRIMARY KEY AUTOINCREMENT,
				recipient_user_id INTEGER NOT NULL,
				type TEXT NOT NULL CHECK (type IN ('MATCH', 'MESSAGE', 'SYSTEM')),
				message TEXT NOT NULL,
				created_at TIMESTAMP NOT NULL,
				dismissed_at TIMESTAMP,
				FOREIGN KEY (recipient_user_id) REFERENCES users(id)
			)
			""");
	}
}
