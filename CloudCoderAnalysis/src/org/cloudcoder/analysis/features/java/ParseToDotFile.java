package org.cloudcoder.analysis.features.java;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;


/**
 * Convert a program to an AST and print it to a file format
 * for a DOT file viewable with GraphViz.  This is primarily
 * useful for testing and debugging.
 * 
 * @author jaimespacco
 *
 */
public class ParseToDotFile extends ASTVisitor
{
    private int nodes=0;
    private Map<ASTNode,String> map=new HashMap<ASTNode,String>();
    private Map<ASTNode,String> names=new HashMap<ASTNode,String>();
    private StringBuffer dotfile=new StringBuffer();
    
    

    private static String readFile(String filename) throws IOException 
    {
        String res="";
        Scanner scan=new Scanner(new FileInputStream(filename));
        while (scan.hasNextLine()) {
            String line=scan.nextLine();
            res+=line+"\n";
        }
        return res;
    }

    public static void parseProgramTextToDotFile(String programText, String outfile)
    throws IOException
    {
        ASTParser parser = ASTParser.newParser(AST.JLS4);
        parser.setSource(programText.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        Map<String, String> options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_7, options);
        parser.setCompilerOptions(options);
        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        ParseToDotFile visitor=new ParseToDotFile();
        cu.accept(visitor);
        PrintStream out=new PrintStream(new FileOutputStream(outfile));
        out.print(visitor.getDotFormatString());
        out.flush();
        out.close();
    }
    
    public static void parseToDotFile(String infile, String outfile)
    throws IOException
    {
        String src = readFile(infile);
        
        parseProgramTextToDotFile(src, outfile);
    }
    
    public String getDotFormatString() {
        String lookup="";
        for (Entry<ASTNode,String> entry : names.entrySet()) {
            lookup+=map.get(entry.getKey())+" [label=\""+entry.getValue()+"\"];\n";
        }
        return "digraph ast {\n" +
            lookup+"\n"+
            dotfile +"\n"+
            "}";
    }
    
    private static String getTypeName(Class cl) {
        return cl.getSimpleName();
    }
        
    
    @Override
    public boolean preVisit2(ASTNode node) {
        
        if (!map.containsKey(node)) {
            map.put(node, "n"+nodes);
            names.put(node, getTypeName(node.getClass()));
            nodes++;
        }
        ASTNode parent=node.getParent();
        if (parent==null) {
            return true;
        }
        if (!map.containsKey(parent)) {
            map.put(parent, "n"+nodes);
            names.put(parent, getTypeName(parent.getClass()));
            nodes++;
        }
        dotfile.append(map.get(parent)+" -> "+map.get(node)+"\n");
        
        return true;
    }
    
    
}
