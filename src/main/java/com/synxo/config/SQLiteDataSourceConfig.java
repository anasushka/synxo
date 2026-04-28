package com.synxo.config;

import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.sql.DataSource;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SQLiteDataSourceConfig {

	private static final String SQLITE_PREFIX = "jdbc:sqlite:";

	@Bean
	public DataSource dataSource(DataSourceProperties properties) {
		Path databasePath = resolveDatabasePath(properties.getUrl());
		if (databasePath == null || databasePath.getParent() == null) {
			return properties.initializeDataSourceBuilder()
				.type(HikariDataSource.class)
				.build();
		}

		try {
			Files.createDirectories(databasePath.getParent());
		} catch (IOException exception) {
			throw new IllegalStateException("Unable to initialize SQLite database directory", exception);
		}

		return properties.initializeDataSourceBuilder()
			.type(HikariDataSource.class)
			.build();
	}

	static Path resolveDatabasePath(String datasourceUrl) {
		if (datasourceUrl == null || !datasourceUrl.startsWith(SQLITE_PREFIX)) {
			return null;
		}

		String location = datasourceUrl.substring(SQLITE_PREFIX.length());
		if (location.isBlank() || ":memory:".equals(location)) {
			return null;
		}

		int queryStart = location.indexOf('?');
		if (queryStart >= 0) {
			location = location.substring(0, queryStart);
		}

		if (location.startsWith("file:")) {
			location = location.substring("file:".length());
		}

		if (location.isBlank()) {
			return null;
		}

		return Paths.get(location).toAbsolutePath().normalize();
	}
}
