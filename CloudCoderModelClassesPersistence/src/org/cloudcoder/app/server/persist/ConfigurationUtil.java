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

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.cloudcoder.app.shared.model.CourseRegistration;
import org.cloudcoder.app.shared.model.CourseRegistrationType;
import org.cloudcoder.app.shared.model.User;
import org.slf4j.LoggerFactory;

/**
 * TODO: Move the database code here into JDBCDatabase?
 * 
 * @author jaimespacco
 *
 */
public class ConfigurationUtil
{
    private static final long serialVersionUID = 1L;
    private static final org.slf4j.Logger logger=LoggerFactory.getLogger(ConfigurationUtil.class);


    static String ask(Scanner keyboard, String prompt) {
        return ConfigurationUtil.ask(keyboard, prompt, null);
    }

    static<E> E choose(Scanner keyboard, String prompt, List<E> values) {
        System.out.println(prompt);
        int count = 0;
        for (E val : values) {
            System.out.println((count++) + " - " + val);
        }
        System.out.print("[Enter value in range 0.." + (values.size()-1) + "] ");
        int choice = Integer.parseInt(keyboard.nextLine().trim());
        return values.get(choice);
    }

    static int askInt(Scanner keyboard, String prompt) {
        System.out.print(prompt);
        return Integer.parseInt(keyboard.nextLine().trim());
    }

    static String askString(Scanner keyboard, String prompt) {
        System.out.print(prompt);
        return keyboard.nextLine();
    }

    static CourseRegistration findRegistration(Connection conn, int userId, int courseId) throws SQLException
    {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;

        try {
            stmt = conn.prepareStatement("select * from " + CourseRegistration.SCHEMA.getDbTableName() + " where user_id = ? and course_id = ?");
            stmt.setInt(1, userId);
            stmt.setInt(2, courseId);

            resultSet= stmt.executeQuery();
            if (!resultSet.next()) {
                return null;
            }

            CourseRegistration reg=new CourseRegistration();
            DBUtil.loadModelObjectFields(reg, CourseRegistration.SCHEMA, resultSet);
            return reg;

        } finally {
            DBUtil.closeQuietly(resultSet);
            DBUtil.closeQuietly(stmt);
        }
    }
    
    static User findUser(Connection conn, String username) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;

        try {
            stmt = conn.prepareStatement("select * from " + User.SCHEMA.getDbTableName() + " where username = ?");
            stmt.setString(1, username);

            resultSet= stmt.executeQuery();
            if (!resultSet.next()) {
                return null;
            }

            User user = new User();
            DBUtil.loadModelObjectFields(user, User.SCHEMA, resultSet);
            return user;

        } finally {
            DBUtil.closeQuietly(resultSet);
            DBUtil.closeQuietly(stmt);
        }
    }
    
    static User findUser(Connection conn, int userid) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;

        try {
            stmt = conn.prepareStatement("select * from " + User.SCHEMA.getDbTableName() + " where id = ?");
            stmt.setInt(1, userid);

            resultSet= stmt.executeQuery();
            if (!resultSet.next()) {
                return null;
            }

            User user = new User();
            DBUtil.loadModelObjectFields(user, User.SCHEMA, resultSet);
            return user;

        } finally {
            DBUtil.closeQuietly(resultSet);
            DBUtil.closeQuietly(stmt);
        }
    }
    
    /**
     * Update the text fields of the given record.  Any
     * parameters left blank will remain unchanged.  For example,
     * if the password parameter is an empty string, this method
     * will not change the password currently stored in the database.
     * 
     * @param conn
     * @param userid
     * @param username
     * @param firstname
     * @param lastname
     * @param email
     * @param password
     * @return The userid of the record that was changed.
     * @throws SQLException
     */
    public static boolean updateUser(Connection conn,
        int userid,
        String username,
        String firstname,
        String lastname,
        String email,
        String password)
    throws SQLException
    {
        User user=findUser(conn, userid);
        if (user!=null) {
            if (username.length()>0) {
                user.setUsername(username);
            }
            if (firstname.length()>0) {
                user.setFirstname(firstname);
            }
            if (lastname.length()>0) {
                user.setLastname(lastname);
            }
            if (email.length()>0) {
                user.setEmail(email);
            }
            if (password.length()>0) {
                user.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt(12)));
            }
            // update all the fields other than id and username
            updateUserById(conn, user);
            return true;
        } 
        // couldn't find
        throw new SQLException("Unable to find user record with id "+userid);
    }
    
    /**
     * Create a user.
     * 
     * @param conn        database connection
     * @param ccUserName  user name
     * @param ccPassword  password (plaintext)
     * @param ccWebsite   user's website URL
     * @return the user id of the newly-created user
     * @throws SQLException
     */
    public static int createOrUpdateUser(Connection conn, 
            String ccUserName, 
            String firstname, 
            String lastname, 
            String email, 
            String ccPassword,
            String ccWebsite) 
    throws SQLException
    {
        User user=findUser(conn, ccUserName);
        if (user!=null) {
            user.setFirstname(firstname);
            user.setLastname(lastname);
            user.setEmail(email);
            if (ccPassword.length()!=60 && !ccPassword.startsWith("$2a$12$")) {
                //XXX hack alert!  Not sure how to know if we've been
                // given a hashed password or a fresh password to be
                // hashed.  So I'm relying on two things:
                // 1) hashed passwords are of length 60
                // 2) hashed passwords start with $2a$12$
                user.setPasswordHash(BCrypt.hashpw(ccPassword, BCrypt.gensalt(12)));
            }
            // update all the fields other than id and username
            updateUserByUsername(conn, user);
            return user.getId();
        } else {
            user = new User();
            user.setUsername(ccUserName);
            user.setFirstname(firstname);
            user.setLastname(lastname);
            user.setEmail(email);
            user.setPasswordHash(BCrypt.hashpw(ccPassword, BCrypt.gensalt(12)));
            user.setWebsite(ccWebsite);
            DBUtil.storeModelObject(conn, user);
            return user.getId();
        }
    }
    
    static void updateUserById(Connection conn, User user) throws SQLException
    {
        if (user.getPasswordHash().length()!=60 && !user.getPasswordHash().startsWith("$2")) {
            //XXX hack alert!  Not sure how to know if we've been
            // given a hashed password or a fresh password to be
            // hashed.  So I'm relying on two things:
            // 1) hashed passwords are of length 60
            // 2) hashed passwords start with $2a$12$
            user.setPasswordHash(BCrypt.hashpw(user.getPasswordHash(), BCrypt.gensalt(12)));
        }
        
        String update="update " + User.SCHEMA.getDbTableName() +
        " set " + DBUtil.getUpdatePlaceholdersNoId(User.SCHEMA) +
        " where id = ? ";
                
        PreparedStatement stmt=null;
        try {
            stmt=conn.prepareStatement(update);
            int index=DBUtil.bindModelObjectValuesForUpdate(user, user.getSchema(), stmt);
            stmt.setInt(index, user.getId());
            
            stmt.executeUpdate();
            
        } finally {
            DBUtil.closeQuietly(stmt);
        }
    }
    
    static void updateUserByUsername(Connection conn, User user) throws SQLException
    {
        String update="update " + User.SCHEMA.getDbTableName() +
        " set " + DBUtil.getUpdatePlaceholdersNoId(User.SCHEMA) +
        " where username = ? ";
                
        PreparedStatement stmt=null;
        try {
            stmt=conn.prepareStatement(update);
            int index=DBUtil.bindModelObjectValuesForUpdate(user, user.getSchema(), stmt);
            stmt.setString(index, user.getUsername());
            
            stmt.executeUpdate();
            
        } finally {
            DBUtil.closeQuietly(stmt);
        }
    }

    static String ask(Scanner keyboard, String prompt, String defval) {
    	System.out.println(prompt);
    	System.out.print("[default: " + (defval != null ? defval : "") + "] ==> ");
    	String value = keyboard.nextLine();
    	if (value.trim().equals("") && defval != null) {
    		value = defval;
    	}
    	return value;
    }

    public static int registerStudentsForCourseId(InputStream in, int courseId, Connection conn) throws SQLException
    {
		boolean isAutoCommit = conn.getAutoCommit();
    	try {
    		conn.setAutoCommit(false);
    		
	        Scanner scan=new Scanner(in);
	        int numAdded=0;
	        int numProcessed=0;
	        long totalStart=System.currentTimeMillis();
	        while (scan.hasNextLine()) {
	            String line=scan.nextLine().replaceAll("#.*","").trim();
	            if (line.equals("")) {
	                continue;
	            }
	            numProcessed++;
	            String[] tokens=line.split("\t");
	            String username=tokens[0];
	            String firstname=tokens[1];
	            String lastname=tokens[2];
	            String email=tokens[3];
	            String password=tokens[4];
	            String website = ""; // We don't attempt to set a website URL for students
	            int section = 101; // The default section number
	            if (tokens.length > 5 && tokens[5] != null) {
	                section=Integer.parseInt(tokens[5]);
	            }
	            logger.info("Registering "+username+" for courseId "+courseId);
	            // Look up the user to see if they already exist
	            long start=System.currentTimeMillis();
	            int userId;
	            User u=findUser(conn, username);
	            if (u!=null) {
	                userId=u.getId();
	            } else {
	                // user doesn't already exist, so create a new one
	                userId=createOrUpdateUser(conn, 
	                        username,
	                        firstname,
	                        lastname,
	                        email,
	                        password,
	                        website);
	            }
	            if (registerUser(conn, userId, courseId, CourseRegistrationType.STUDENT, section)) {
	                numAdded++;
	            }
	            
	            conn.commit();
	            
	            long register=System.currentTimeMillis()-start;
	            logger.info(register+" millis to regsiter "+username);
	        }
	        long totalTime=System.currentTimeMillis()-totalStart;
	        logger.warn("Total time to process "+numProcessed+" students was "+totalTime);
	        return numAdded;
    	} finally {
    		conn.setAutoCommit(isAutoCommit);
    	}
    }

    public static int registerStudentsForCourseId2(InputStream in, int courseId, Connection conn) throws SQLException
    {
        Scanner scan=new Scanner(in);
        Map<String,User> userMap=getAllUsers(conn);
        Set<Integer> usersInCourse=getUsersInCourse(conn, courseId);
        
        int numAdded=0;
        int numProcessed=0;
        
        long totalStart=System.currentTimeMillis();
        while (scan.hasNextLine()) {
            String line=scan.nextLine().replaceAll("#.*","").trim();
            if (line.equals("")) {
                continue;
            }
            numProcessed++;
            String[] tokens=line.split("\t");
            String username=tokens[0];
            String firstname=tokens[1];
            String lastname=tokens[2];
            String email=tokens[3];
            String password=tokens[4];
            String website = ""; // We don't attempt to set a website URL for students
            int section = 101; // The default section number
            if (tokens.length > 5 && tokens[5] != null) {
                section=Integer.parseInt(tokens[5]);
            }
            logger.info("Registering "+username+" for courseId "+courseId);
            // Look up the user to see if they already exist
            long start=System.currentTimeMillis();
            int userId;
            if (userMap.containsKey(username)) {
                userId=userMap.get(username).getId();
            } else {
                // user doesn't already exist, so create a new one
                userId=createOrUpdateUser(conn, 
                        username,
                        firstname,
                        lastname,
                        email,
                        password,
                        website);
            }
            if (!usersInCourse.contains(userId)) {
                registerUser(conn, userId, courseId, CourseRegistrationType.STUDENT, section);
            }
            long register=System.currentTimeMillis()-start;
            logger.info(register+" millis to register "+username);
        }
        long totalTime=System.currentTimeMillis()-totalStart;
        logger.warn("Total time to process "+numProcessed+" students was "+totalTime);
        return numAdded;
    }
    
    private static Set<Integer> getUsersInCourse(Connection conn, int courseId)
    throws SQLException
    {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            stmt = conn.prepareStatement("select * from " + CourseRegistration.SCHEMA.getDbTableName()+
                    " where courseId = ?");
            stmt.setInt(1, courseId);
            Set<Integer> users=new HashSet<Integer>();

            resultSet= stmt.executeQuery();
            while (resultSet.next()) {
                User user=new User();
                DBUtil.loadModelObjectFields(user, User.SCHEMA, resultSet);
                users.add(user.getId());
            }
            return users;
        } finally {
            DBUtil.closeQuietly(resultSet);
            DBUtil.closeQuietly(stmt);
        }    }

    private static Map<String, User> getAllUsers(Connection conn)
    throws SQLException
    {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            stmt = conn.prepareStatement("select * from " + User.SCHEMA.getDbTableName());
            Map<String, User> userMap=new HashMap<String,User>();

            resultSet= stmt.executeQuery();
            while (resultSet.next()) {
                User user=new User();
                DBUtil.loadModelObjectFields(user, User.SCHEMA, resultSet);
                userMap.put(user.getUsername(), user);
            }
            return userMap;
        } finally {
            DBUtil.closeQuietly(resultSet);
            DBUtil.closeQuietly(stmt);
        }
    }

    static final String YES = "yes";

    /**
     * Configure log4j to log to stdout.
     */
	public static void configureLog4j() {
		// See: http://robertmaldon.blogspot.com/2007/09/programmatically-configuring-log4j-and.html
		Logger rootLogger = Logger.getRootLogger();
		if (!rootLogger.getAllAppenders().hasMoreElements()) {
			// Set this to Level.DEBUG if there are problems running the migration
			rootLogger.setLevel(Level.WARN);
			rootLogger.addAppender(new ConsoleAppender(new PatternLayout("%-5p [%t]: %m%n")));
		}
	}

    /**
     * Register a user for a course.
     * 
     * @param conn              the database connection
     * @param userId            the user id
     * @param courseId          the course id
     * @param registrationType  the registration type
     * @param section           the section number
     * @throws SQLException
     */
    public static boolean registerUser(Connection conn, int userId, int courseId, CourseRegistrationType registrationType, int section) throws SQLException {
        if (findRegistration(conn, userId, courseId)!=null) {
            // already registered!
            return false;
        }
        CourseRegistration courseReg = new CourseRegistration();
        courseReg.setCourseId(courseId);
        courseReg.setUserId(userId);
        courseReg.setRegistrationType(registrationType);
        courseReg.setSection(section);
        DBUtil.storeModelObject(conn, courseReg);
        return true;
    }

}
