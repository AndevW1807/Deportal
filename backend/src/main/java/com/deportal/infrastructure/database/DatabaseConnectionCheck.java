package com.deportal.infrastructure.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Checks that the pool can reach MySQL. Failures stay in the exception and
 * credentials are never written to the console.
 */
@Component
public class DatabaseConnectionCheck implements ApplicationRunner {

	private final DataSource dataSource;

	public DatabaseConnectionCheck(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public void run(ApplicationArguments args) {
		try (Connection connection = dataSource.getConnection();
				Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery("SELECT 1")) {
			if (!resultSet.next() || resultSet.getInt(1) != 1) {
				throw new IllegalStateException("Database connection failed");
			}
		}
		catch (SQLException exception) {
			throw new IllegalStateException("Database connection failed", exception);
		}
	}

}
