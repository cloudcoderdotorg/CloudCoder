package org.cloudcoder.app.server.persist;

import java.io.FileInputStream;
import java.sql.Connection;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import org.cloudcoder.app.server.persist.util.ConfigurationUtil;
import org.cloudcoder.app.server.persist.util.DBUtil;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.IFactory;

public class RegisterStudents
{
    public static void main(String[] args) {
        try {
            registerStudents();
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.err.println("Database error: " + e.getMessage());
        }
    }

    /**
     * @param args
     */
    private static void registerStudents() throws Exception
    {
        Scanner keyboard=new Scanner(System.in);
        Class.forName("com.mysql.jdbc.Driver");
        Properties config = DBUtil.getConfigProperties();
        Connection conn = DBUtil.connectToDatabase(config, "cloudcoder.db");

        List<Course> courses = DBUtil.getAllModelObjects(conn, Course.SCHEMA, new IFactory<Course>() {
            @Override
            public Course create() {
                return new Course();
            }
        });
        Course c=ConfigurationUtil.choose(keyboard, "For which course would you like to register students?", courses);
        // TODO: look up the term for each course
        String filename=ConfigurationUtil.ask(keyboard, "Enter the name of the file containing a tab-separated list student registration entries in this format: \n" +
                "username\tfirstname\tlastname\temail\tpassword\tsection\n" +
                "Usernames in the datbase will be re-used, but the names/email/password will not be updated," +
                "and users will not be registered for a course if they are already registered");
        System.out.println("Note that this may be a slow operation");
        System.out.println("Some logging results will be appended to logs/cloudcoder.log rather than echoed to stdout");
        int num=ConfigurationUtil.registerStudentsForCourseId2(new FileInputStream(filename), c.getId(), conn);
        System.out.println("Registered "+num+" students for "+c.getName());
    }

}
