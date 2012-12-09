package org.cloudcoder.analysis.features.java;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberRef;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.MethodRefParameter;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.UnionType;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.WildcardType;


public class FeatureVisitor extends ASTVisitor
{
    /*
     * TODO
     * Extract features from the AST using the visitor pattern.
     * Probably need to create a list of features, and probably
     * a feature class, and then have these visit methods create
     * the features.
     * 
     * Not all of the visit methods will be necessary; for example,
     * do we care about import statements or annotations?  Probably not.
     */
    
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

    /**
     * 
     * @param filePath
     * @throws IOException
     */
    public void extractFeatures(String filePath) throws IOException
    {
        //TODO:  Maybe change the return type to return
        // a list of features?
        ASTParser parser = ASTParser.newParser(AST.JLS4);
        String src = readFile(filePath);
        parser.setSource(src.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        Map<String, String> options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_7, options);
        parser.setCompilerOptions(options);
        CompilationUnit cu = (CompilationUnit) parser.createAST(null);

        cu.accept(new FeatureVisitor());
    }

    @Override
    public boolean visit(AnnotationTypeDeclaration node) {
        
        return true;
    }

    @Override
    public boolean visit(AnnotationTypeMemberDeclaration node) {
        
        return true;
    }

    @Override
    public boolean visit(AnonymousClassDeclaration node) {
        
        return true;
    }

    @Override
    public boolean visit(ArrayAccess node) {
        
        return true;
    }

    @Override
    public boolean visit(ArrayCreation node) {
        
        return true;
    }

    @Override
    public boolean visit(ArrayInitializer node) {
        
        return true;
    }

    @Override
    public boolean visit(ArrayType node) {
        
        return true;
    }

    @Override
    public boolean visit(AssertStatement node) {
        
        return true;
    }

    @Override
    public boolean visit(Assignment node) {
        
        return true;
    }

    @Override
    public boolean visit(Block node) {
        System.out.println(node);
        return true;
    }

    @Override
    public boolean visit(BlockComment node) {
        
        return true;
    }

    @Override
    public boolean visit(BooleanLiteral node) {
        
        return true;
    }

    @Override
    public boolean visit(BreakStatement node) {
        
        return true;
    }

    @Override
    public boolean visit(CastExpression node) {
        
        return true;
    }

    @Override
    public boolean visit(CatchClause node) {
        
        return true;
    }

    @Override
    public boolean visit(CharacterLiteral node) {
        
        return true;
    }

    @Override
    public boolean visit(ClassInstanceCreation node) {
        
        return true;
    }

    @Override
    public boolean visit(CompilationUnit node) {
        
        return true;
    }

    @Override
    public boolean visit(ConditionalExpression node) {
        
        return true;
    }

    @Override
    public boolean visit(ConstructorInvocation node) {
        
        return true;
    }

    @Override
    public boolean visit(ContinueStatement node) {
        
        return true;
    }

    @Override
    public boolean visit(DoStatement node) {
        
        return true;
    }

    @Override
    public boolean visit(EmptyStatement node) {
        
        return true;
    }

    @Override
    public boolean visit(EnhancedForStatement node) {
        
        return true;
    }

    @Override
    public boolean visit(EnumConstantDeclaration node) {
        
        return true;
    }

    @Override
    public boolean visit(EnumDeclaration node) {
        
        return true;
    }

    @Override
    public boolean visit(ExpressionStatement node) {
        
        return true;
    }

    @Override
    public boolean visit(FieldAccess node) {
        
        return true;
    }

    @Override
    public boolean visit(FieldDeclaration node) {
        
        return true;
    }

    @Override
    public boolean visit(ForStatement node) {
        
        return true;
    }

    @Override
    public boolean visit(IfStatement node) {
        
        return true;
    }

    @Override
    public boolean visit(ImportDeclaration node) {
        
        return true;
    }

    @Override
    public boolean visit(InfixExpression node) {
        
        return true;
    }

    @Override
    public boolean visit(Initializer node) {
        
        return true;
    }

    @Override
    public boolean visit(InstanceofExpression node) {
        
        return true;
    }

    @Override
    public boolean visit(Javadoc node) {
        
        return true;
    }

    @Override
    public boolean visit(LabeledStatement node) {
        
        return true;
    }

    @Override
    public boolean visit(LineComment node) {
        
        return true;
    }

    @Override
    public boolean visit(MarkerAnnotation node) {
        
        return true;
    }

    @Override
    public boolean visit(MemberRef node) {
        
        return true;
    }

    @Override
    public boolean visit(MemberValuePair node) {
        
        return true;
    }

    @Override
    public boolean visit(MethodDeclaration node) {
        //System.out.println(node.getName());
        System.out.println(node.getClass());
        return true;
    }

    @Override
    public boolean visit(MethodInvocation node) {
        
        return true;
    }

    @Override
    public boolean visit(MethodRef node) {
        
        return true;
    }

    @Override
    public boolean visit(MethodRefParameter node) {
        
        return true;
    }

    @Override
    public boolean visit(Modifier node) {
        
        return true;
    }

    @Override
    public boolean visit(NormalAnnotation node) {
        
        return true;
    }

    @Override
    public boolean visit(NullLiteral node) {
        
        return true;
    }

    @Override
    public boolean visit(NumberLiteral node) {
        
        return true;
    }

    @Override
    public boolean visit(PackageDeclaration node) {
        
        return true;
    }

    @Override
    public boolean visit(ParameterizedType node) {
        
        return true;
    }

    @Override
    public boolean visit(ParenthesizedExpression node) {
        
        return true;
    }

    @Override
    public boolean visit(PostfixExpression node) {
        
        return true;
    }

    @Override
    public boolean visit(PrefixExpression node) {
        
        return true;
    }

    @Override
    public boolean visit(PrimitiveType node) {
        
        return true;
    }

    @Override
    public boolean visit(QualifiedName node) {
        
        return true;
    }

    @Override
    public boolean visit(QualifiedType node) {
        
        return true;
    }

    @Override
    public boolean visit(ReturnStatement node) {
        
        return true;
    }

    @Override
    public boolean visit(SimpleName node) {
        
        return true;
    }

    @Override
    public boolean visit(SimpleType node) {
        
        return true;
    }

    @Override
    public boolean visit(SingleMemberAnnotation node) {
        
        return true;
    }

    @Override
    public boolean visit(SingleVariableDeclaration node) {
        
        return true;
    }

    @Override
    public boolean visit(StringLiteral node) {
        
        return true;
    }

    @Override
    public boolean visit(SuperConstructorInvocation node) {
        
        return true;
    }

    @Override
    public boolean visit(SuperFieldAccess node) {
        
        return true;
    }

    @Override
    public boolean visit(SuperMethodInvocation node) {
        
        return true;
    }

    @Override
    public boolean visit(SwitchCase node) {
        
        return true;
    }

    @Override
    public boolean visit(SwitchStatement node) {
        
        return true;
    }

    @Override
    public boolean visit(SynchronizedStatement node) {
        
        return true;
    }

    @Override
    public boolean visit(TagElement node) {
        
        return true;
    }

    @Override
    public boolean visit(TextElement node) {
        
        return true;
    }

    @Override
    public boolean visit(ThisExpression node) {
        
        return true;
    }

    @Override
    public boolean visit(ThrowStatement node) {
        
        return true;
    }

    @Override
    public boolean visit(TryStatement node) {
        
        return true;
    }

    @Override
    public boolean visit(TypeDeclaration node) {
        
        return true;
    }

    @Override
    public boolean visit(TypeDeclarationStatement node) {
        
        return true;
    }

    @Override
    public boolean visit(TypeLiteral node) {
        
        return true;
    }

    @Override
    public boolean visit(TypeParameter node) {
        
        return true;
    }

    @Override
    public boolean visit(UnionType node) {
        
        return true;
    }

    @Override
    public boolean visit(VariableDeclarationExpression node) {
        
        return true;
    }

    @Override
    public boolean visit(VariableDeclarationFragment node) {
        
        return true;
    }

    @Override
    public boolean visit(VariableDeclarationStatement node) {
        
        return true;
    }

    @Override
    public boolean visit(WhileStatement node) {
        
        return true;
    }

    @Override
    public boolean visit(WildcardType node) {
        
        return true;
    }

}
