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
			ensureProfileMatchingColumns(jdbcTemplate);
			ensureNotificationTable(jdbcTemplate);
			ensureAchievementTable(jdbcTemplate);
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

	private void ensureProfileMatchingColumns(JdbcTemplate jdbcTemplate) {
		List<String> columns = jdbcTemplate.query(
			"PRAGMA table_info(profiles)",
			(resultSet, rowNumber) -> resultSet.getString("name")
		);

		addColumnIfMissing(
			jdbcTemplate,
			columns,
			"last_activity_date",
			"ALTER TABLE profiles ADD COLUMN last_activity_date DATE NOT NULL DEFAULT '1970-01-01'"
		);
		addColumnIfMissing(
			jdbcTemplate,
			columns,
			"activity_streak_days",
			"ALTER TABLE profiles ADD COLUMN activity_streak_days INTEGER NOT NULL DEFAULT 1"
		);
		addColumnIfMissing(
			jdbcTemplate,
			columns,
			"matching_preferences_enabled",
			"ALTER TABLE profiles ADD COLUMN matching_preferences_enabled INTEGER NOT NULL DEFAULT 0"
		);
		addColumnIfMissing(
			jdbcTemplate,
			columns,
			"interest_priority",
			"ALTER TABLE profiles ADD COLUMN interest_priority INTEGER NOT NULL DEFAULT 100"
		);
		addColumnIfMissing(
			jdbcTemplate,
			columns,
			"distance_priority",
			"ALTER TABLE profiles ADD COLUMN distance_priority INTEGER NOT NULL DEFAULT 100"
		);
		addColumnIfMissing(
			jdbcTemplate,
			columns,
			"intention_priority",
			"ALTER TABLE profiles ADD COLUMN intention_priority INTEGER NOT NULL DEFAULT 100"
		);
		addColumnIfMissing(
			jdbcTemplate,
			columns,
			"activity_priority",
			"ALTER TABLE profiles ADD COLUMN activity_priority INTEGER NOT NULL DEFAULT 100"
		);
		addColumnIfMissing(
			jdbcTemplate,
			columns,
			"social_priority",
			"ALTER TABLE profiles ADD COLUMN social_priority INTEGER NOT NULL DEFAULT 100"
		);

		jdbcTemplate.update(
			"UPDATE profiles SET last_activity_date = COALESCE(last_activity_date, date(COALESCE(last_active_at, CURRENT_TIMESTAMP)))"
		);
		jdbcTemplate.update("""
			UPDATE profiles
			SET last_activity_date = date(
				CASE
					WHEN typeof(last_activity_date) IN ('integer', 'real') THEN last_activity_date / 1000
					WHEN instr(last_activity_date, '-') = 0 THEN CAST(last_activity_date AS INTEGER) / 1000
					ELSE last_active_at / 1000
				END,
				'unixepoch'
			)
			WHERE last_activity_date IS NOT NULL
			  AND (typeof(last_activity_date) IN ('integer', 'real') OR instr(last_activity_date, '-') = 0)
			""");
		jdbcTemplate.update(
			"UPDATE profiles SET activity_streak_days = COALESCE(activity_streak_days, 1)"
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

	private void ensureAchievementTable(JdbcTemplate jdbcTemplate) {
		jdbcTemplate.execute("""
			CREATE TABLE IF NOT EXISTS user_achievements (
				id INTEGER PRIMARY KEY AUTOINCREMENT,
				user_id INTEGER NOT NULL,
				type TEXT NOT NULL CHECK (type IN ('FIRST_MATCH', 'FIRST_DIALOG', 'TEN_MUTUAL_LIKES', 'PROFILE_COMPLETE', 'ACTIVE_WEEK')),
				unlocked_at TIMESTAMP NOT NULL,
				UNIQUE (user_id, type),
				FOREIGN KEY (user_id) REFERENCES users(id)
			)
			""");
	}
}
