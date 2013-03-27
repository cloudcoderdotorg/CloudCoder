package org.cloudcoder.app.server.persist;

import java.sql.Connection;
import java.util.Properties;
import java.util.Scanner;

import org.cloudcoder.app.server.persist.util.ConfigurationUtil;
import org.cloudcoder.app.server.persist.util.DBUtil;

public class CreateUser {
	public static void main(String[] args) throws Exception {
		Scanner keyboard = new Scanner(System.in);
		
		Class.forName("com.mysql.jdbc.Driver");
		Properties config = DBUtil.getConfigProperties();
		Connection conn = DBUtil.connectToDatabase(config, "cloudcoder.db");
		
		System.out.println("Create a new CloudCoder user account");
		
		String ccUserName = ConfigurationUtil.ask(keyboard, "Username? ");
		String ccPassword = ConfigurationUtil.ask(keyboard, "Password? ");
		String ccFirstname = ConfigurationUtil.ask(keyboard, "First name? " );
		String ccLastname= ConfigurationUtil.ask(keyboard, "Last name? ");
		String ccEmail= ConfigurationUtil.ask(keyboard, "Email address? ");
		String ccWebsite= ConfigurationUtil.ask(keyboard, "Website URL? ");
		
		ConfigurationUtil.createOrUpdateUser(conn, ccUserName, ccFirstname, ccLastname, ccEmail, ccPassword, ccWebsite);

		System.out.println("Success!");
	}
}
