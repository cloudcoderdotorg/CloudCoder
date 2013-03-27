package org.cloudcoder.app.server.persist;

import java.io.IOException;
import java.sql.SQLException;

import org.cloudcoder.app.server.persist.util.ConfigurationUtil;

public class MigrateRepositoryDatabase {
	public static void main(String[] args) throws ClassNotFoundException, IOException {
		ConfigurationUtil.configureLog4j();
		
		try {
			MigrateDatabase.migrateDatabase(CreateRepositoryDatabase.TABLES, "CloudCoder Repository", "cloudcoder.repoapp.db");
		} catch (SQLException e) {
			System.err.println("Database error: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
