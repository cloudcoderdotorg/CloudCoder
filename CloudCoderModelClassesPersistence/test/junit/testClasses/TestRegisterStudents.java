package testClasses;


import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;

import org.cloudcoder.app.server.persist.util.ConfigurationUtil;
import org.cloudcoder.app.server.persist.util.DBUtil;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestRegisterStudents
{
    

    @Test
    public void testRegisterStudents1()
    throws Exception
    {
        ConfigurationUtil.configureLog4j();
        Connection conn=null;
        try {
            conn=DBUtil.getConnection();
            
            ConfigurationUtil.registerStudentsForCourseId(
                    new FileInputStream(new File("test/files/students1.txt")),
                    1,
                    conn);
            
        } finally {
            DBUtil.closeQuietly(conn);
        }
    }
    
    @Test
    public void testRegisterStudents2()
    throws Exception
    {
        ConfigurationUtil.configureLog4j();
        Connection conn=null;
        try {
            conn=DBUtil.getConnection();
            
            ConfigurationUtil.registerStudentsForCourseId(
                    new FileInputStream(new File("test/files/full-students.txt")),
                    1,
                    conn);
            
        } finally {
            DBUtil.closeQuietly(conn);
        }
    }
}
