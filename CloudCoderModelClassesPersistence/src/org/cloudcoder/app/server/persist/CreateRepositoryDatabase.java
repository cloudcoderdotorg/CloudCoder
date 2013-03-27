// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <david.hovemeyer@gmail.com>
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package org.cloudcoder.app.server.persist;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Scanner;

import org.cloudcoder.app.server.persist.util.ConfigurationUtil;
import org.cloudcoder.app.server.persist.util.DBUtil;
import org.cloudcoder.app.server.persist.util.SchemaUtil;
import org.cloudcoder.app.shared.model.ModelObjectSchema;
import org.cloudcoder.app.shared.model.RepoProblem;
import org.cloudcoder.app.shared.model.RepoProblemAndTestCaseList;
import org.cloudcoder.app.shared.model.RepoProblemTag;
import org.cloudcoder.app.shared.model.RepoTestCase;
import org.cloudcoder.app.shared.model.User;
import org.cloudcoder.app.shared.model.UserRegistrationRequest;

/**
 * Create the exercise repository database.
 * 
 * @author David Hovemeyer
 */
public class CreateRepositoryDatabase {
	public static final ModelObjectSchema<?>[] TABLES = {
		User.SCHEMA,
		RepoProblem.SCHEMA,
		RepoTestCase.SCHEMA,
		RepoProblemTag.SCHEMA,
		UserRegistrationRequest.SCHEMA,
	};
	
	public static void main(String[] args) throws Exception {
		try {
			doCreateRepositoryDatabase();
		} catch (SQLException e) {
		    e.printStackTrace(System.err);
			System.err.println("Database error: " + e.getMessage());
		}
	}
	
	private static void doCreateRepositoryDatabase() throws Exception {
		Scanner keyboard = new Scanner(System.in);
		
		String ccUserName = ConfigurationUtil.ask(keyboard, "Enter a username for your repository server account: ");
        String ccPassword = ConfigurationUtil.ask(keyboard, "Enter a password for your repository server account: ");
        String ccFirstname = ConfigurationUtil.ask(keyboard, "What is your first name?");
        String ccLastname= ConfigurationUtil.ask(keyboard, "What is your last name?");
        String ccEmail= ConfigurationUtil.ask(keyboard, "What is your email address?");
        String ccWebsite= ConfigurationUtil.ask(keyboard, "What is your website URL?");
		
		Class.forName("com.mysql.jdbc.Driver");

		Properties config = DBUtil.getConfigProperties();
		
		Connection conn = DBUtil.connectToDatabaseServer(config, "cloudcoder.repoapp.db");
		
		System.out.println("Creating the database");
		DBUtil.createDatabase(conn, config.getProperty("cloudcoder.repoapp.db.databaseName"));
		
		// Connect to the newly-created database
		conn.close();
		conn = DBUtil.connectToDatabase(config, "cloudcoder.repoapp.db");

		// Create schema version table
		System.out.println("Creating schema version table...");
		SchemaUtil.createSchemaVersionTableIfNeeded(conn, TABLES);

		// Create tables
		for (ModelObjectSchema<?> table : TABLES) {
			createTable(conn, table);
		}

		// Create an initial user
		System.out.println("Creating initial user...");
		int userId = ConfigurationUtil.createOrUpdateUser(conn, ccUserName, ccFirstname, ccLastname, ccEmail, ccPassword, ccWebsite);

		RepoProblem repoProblem = new RepoProblem();
		repoProblem.setUserId(userId);
		CreateSampleData.populateSampleProblemData(repoProblem);
		
		// Change the testname to ensure it gets a different hash code than
		// the sample problem in the webapp database.
		repoProblem.setTestname("helloRepository");

		RepoTestCase repoTestCase = new RepoTestCase();
		CreateSampleData.populateSampleTestCaseData(repoTestCase);

		// At this point, all of the data needed for the hash computation is available.
		RepoProblemAndTestCaseList exercise = new RepoProblemAndTestCaseList();
		exercise.setProblem(repoProblem);
		exercise.addTestCase(repoTestCase);
		exercise.computeHash();
		
		// Now we can store the problem and its test case in the database
		DBUtil.storeModelObject(conn, repoProblem);
		repoTestCase.setRepoProblemId(repoProblem.getId());
		DBUtil.storeModelObject(conn, repoTestCase);
		
		System.out.println("Done!");
	}

	private static<E> void createTable(Connection conn, ModelObjectSchema<E> schema) throws SQLException {
		System.out.println("Creating table " + schema.getDbTableName());
		DBUtil.createTable(conn, schema);
	}
}
