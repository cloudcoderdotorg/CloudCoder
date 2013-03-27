package org.cloudcoder.app.server.persist;

import java.io.IOException;
import java.sql.SQLException;

import org.cloudcoder.app.server.persist.util.ConfigurationUtil;

public class MigrateWebappDatabase {
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		ConfigurationUtil.configureLog4j();
		
		try {
			MigrateDatabase.migrateDatabase(CreateWebappDatabase.TABLES, "CloudCoder", "cloudcoder.db");
		} catch (SQLException e) {
			System.err.println("Database error: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
