package testClasses;

import static org.junit.Assert.*;

import java.io.File;

import org.cloudcoder.app.server.persist.util.DBUtil;
import org.junit.Test;

public class TestDBUtil
{
    @Test
    public void testFindRecursively()
    throws Exception
    {
        File f=DBUtil.findRecursively("cloudcoder.properties");
        assertEquals("cloudcoder.properties", f.getName());
    }
    
    @Test
    public void testFindRecursively2()
    throws Exception
    {
        File f=DBUtil.findRecursively("FODOFODOFODOFODODOFJDFDFdfdDF");
        assertNull(f);
    }
}
