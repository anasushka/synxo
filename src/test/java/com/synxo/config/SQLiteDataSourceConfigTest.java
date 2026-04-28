package com.synxo.config;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SQLiteDataSourceConfigTest {

	@Test
	void resolvesRegularSqliteDatabasePath() {
		Path databasePath = SQLiteDataSourceConfig.resolveDatabasePath("jdbc:sqlite:./data/synxo.db");

		assertThat(databasePath).isNotNull();
		assertThat(databasePath.toString()).endsWith("/data/synxo.db");
	}

	@Test
	void stripsFilePrefixAndQueryParameters() {
		Path databasePath = SQLiteDataSourceConfig.resolveDatabasePath(
			"jdbc:sqlite:file:./build/test-data/synxo-test.db?mode=rwc&cache=shared"
		);

		assertThat(databasePath).isNotNull();
		assertThat(databasePath.toString()).endsWith("/build/test-data/synxo-test.db");
	}

	@Test
	void ignoresInMemoryAndNonSqliteUrls() {
		assertThat(SQLiteDataSourceConfig.resolveDatabasePath("jdbc:sqlite::memory:")).isNull();
		assertThat(SQLiteDataSourceConfig.resolveDatabasePath("jdbc:test://localhost:5432/synxo")).isNull();
	}
}
