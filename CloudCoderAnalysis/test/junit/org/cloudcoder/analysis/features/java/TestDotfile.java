package junit.org.cloudcoder.analysis.features.java;

import static org.junit.Assert.*;

import org.cloudcoder.analysis.features.java.ParseToDotFile;
import org.junit.Test;

public class TestDotfile
{
    static final String INPUT="testing/features/java";
    static final String OUTPUT="output";

    @Test
    public void testBadCode() throws Exception {
        String filename=INPUT+"/MissingSemicolon.java";
        String outfile=OUTPUT+"/missingsemicolon.dot";
        ParseToDotFile.parseToDotFile(filename, outfile);
    }
    
    @Test
    public void testDotFile() throws Exception {
        String filename=INPUT+"/A1.java";
        String outfile=OUTPUT+"/a1.dot";
        ParseToDotFile.parseToDotFile(filename, outfile);
    }
}
