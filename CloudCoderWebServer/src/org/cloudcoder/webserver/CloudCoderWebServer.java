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

package org.cloudcoder.webserver;

import org.cloudcoder.jetty.WebServer;

/**
 * Main entry point for the self-contained single-jar-file
 * deployment for the CloudCoder web application.
 * 
 * @author David Hovemeyer
 */
public class CloudCoderWebServer extends WebServer {
	
	public CloudCoderWebServer() {
		super(new CloudCoderDaemonController());

		addAdminCommand("createdb", "org.cloudcoder.app.server.persist.CreateWebappDatabase",
				"Create a fresh cloudcoder database using the current\n" +
                "configuration settings in the jarfile that you are\n" +
				"currently executing.  This should only be done once!");
		addAdminCommand("migratedb", "org.cloudcoder.app.server.persist.MigrateWebappDatabase", 
				"Update the currently existing database with any new\n" +
				"tables or columns");
		addAdminCommand("createcourse", "org.cloudcoder.app.server.persist.CreateCourse",
				"Create a new course in the database of the CloudCoder\n" +
				"installation");
		addAdminCommand("registerstudents", "org.cloudcoder.app.server.persist.RegisterStudents",
				"Register students defined in a a tab-delimited text file");
		addAdminCommand("createuser", "org.cloudcoder.app.server.persist.CreateUser",
				"Create a new user account in database of the CloudCoder\n" +
				"installation");
		addAdminCommand("configure", "org.cloudcoder.app.server.persist.ConfigureCloudCoder",
				"Set new configuration parameters in the CloudCoder\n" +
				"jarfile. Parameters can be read interactively or can\n" +
				"be set in a properties file named cloudcoder.properties");
		addAdminCommand("listconfig", "org.cloudcoder.app.server.persist.ListCloudCoderProperties",
				"Lists configuration parameters set in the current\n" +
				"CloudCoder jarfile");
		addAdminCommand("upgrade", "org.cloudcoder.webserver.UpgradeApp",
				"Download the latest release and automatically configure\n" +
				"to match the current jar file");
	}
	
	public static void main(String[] args) throws Exception {
		new CloudCoderWebServer().handleCommand(args);
	}
}
