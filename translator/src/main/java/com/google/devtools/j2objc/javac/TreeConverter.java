/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.devtools.j2objc.javac;

import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.Annotation;
import com.google.devtools.j2objc.ast.AnnotationTypeDeclaration;
import com.google.devtools.j2objc.ast.AnonymousClassDeclaration;
import com.google.devtools.j2objc.ast.ArrayAccess;
import com.google.devtools.j2objc.ast.ArrayCreation;
import com.google.devtools.j2objc.ast.ArrayInitializer;
import com.google.devtools.j2objc.ast.ArrayType;
import com.google.devtools.j2objc.ast.AssertStatement;
import com.google.devtools.j2objc.ast.Assignment;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.BlockComment;
import com.google.devtools.j2objc.ast.BodyDeclaration;
import com.google.devtools.j2objc.ast.BooleanLiteral;
import com.google.devtools.j2objc.ast.BreakStatement;
import com.google.devtools.j2objc.ast.CastExpression;
import com.google.devtools.j2objc.ast.CatchClause;
import com.google.devtools.j2objc.ast.CharacterLiteral;
import com.google.devtools.j2objc.ast.ClassInstanceCreation;
import com.google.devtools.j2objc.ast.Comment;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.ConditionalExpression;
import com.google.devtools.j2objc.ast.ConstructorInvocation;
import com.google.devtools.j2objc.ast.ContinueStatement;
import com.google.devtools.j2objc.ast.DoStatement;
import com.google.devtools.j2objc.ast.EmptyStatement;
import com.google.devtools.j2objc.ast.EnhancedForStatement;
import com.google.devtools.j2objc.ast.EnumConstantDeclaration;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.ExpressionStatement;
import com.google.devtools.j2objc.ast.FieldAccess;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.ForStatement;
import com.google.devtools.j2objc.ast.IfStatement;
import com.google.devtools.j2objc.ast.InfixExpression;
import com.google.devtools.j2objc.ast.InstanceofExpression;
import com.google.devtools.j2objc.ast.Javadoc;
import com.google.devtools.j2objc.ast.LineComment;
import com.google.devtools.j2objc.ast.MarkerAnnotation;
import com.google.devtools.j2objc.ast.MemberValuePair;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.Name;
import com.google.devtools.j2objc.ast.NormalAnnotation;
import com.google.devtools.j2objc.ast.NullLiteral;
import com.google.devtools.j2objc.ast.NumberLiteral;
import com.google.devtools.j2objc.ast.PackageDeclaration;
import com.google.devtools.j2objc.ast.ParameterizedType;
import com.google.devtools.j2objc.ast.ParenthesizedExpression;
import com.google.devtools.j2objc.ast.PostfixExpression;
import com.google.devtools.j2objc.ast.PrefixExpression;
import com.google.devtools.j2objc.ast.PrimitiveType;
import com.google.devtools.j2objc.ast.PropertyAnnotation;
import com.google.devtools.j2objc.ast.QualifiedName;
import com.google.devtools.j2objc.ast.ReturnStatement;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SimpleType;
import com.google.devtools.j2objc.ast.SingleMemberAnnotation;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.SourcePosition;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.StringLiteral;
import com.google.devtools.j2objc.ast.SuperConstructorInvocation;
import com.google.devtools.j2objc.ast.SuperFieldAccess;
import com.google.devtools.j2objc.ast.SwitchCase;
import com.google.devtools.j2objc.ast.SwitchStatement;
import com.google.devtools.j2objc.ast.ThisExpression;
import com.google.devtools.j2objc.ast.ThrowStatement;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TryStatement;
import com.google.devtools.j2objc.ast.Type;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.TypeLiteral;
import com.google.devtools.j2objc.ast.VariableDeclarationExpression;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.ast.VariableDeclarationStatement;
import com.google.devtools.j2objc.ast.WhileStatement;
import com.google.devtools.j2objc.file.InputFile;
import com.google.devtools.j2objc.file.JarredInputFile;
import com.google.devtools.j2objc.file.RegularInputFile;
import com.google.devtools.j2objc.types.ExecutablePair;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.FileUtil;
import com.google.devtools.j2objc.util.TranslationEnvironment;
import com.google.j2objc.annotations.Property;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.PackageSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.tree.DocCommentTable;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.util.Position;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;

/**
 * Converts a Java AST from the JDT data structure to our J2ObjC data structure.
 */
public class TreeConverter {
  private JCTree.JCCompilationUnit unit;

  public static CompilationUnit convertCompilationUnit(
      TranslationEnvironment env, JCTree.JCCompilationUnit javacUnit) {
    TreeConverter converter = new TreeConverter(javacUnit);
    String sourceFilePath = javacUnit.getSourceFile().toUri().getPath();
    InputFile sourceFile = convertFileObject(javacUnit.getSourceFile());
    String source = getSource(sourceFile);
    String mainTypeName = FileUtil.getMainTypeName(sourceFile);
    CompilationUnit unit = new CompilationUnit(env, sourceFilePath, mainTypeName, source);
    unit.setPackage(converter.convertPackage(javacUnit.packge));
    for (JCTree type : javacUnit.getTypeDecls()) {
      unit.addType((AbstractTypeDeclaration) converter.convert(type));
    }

    // TODO(tball): add unparented comments, once comment scanner is implemented.
    return unit;
  }

  private static InputFile convertFileObject(JavaFileObject fileObject) {
    assert fileObject.getKind() == JavaFileObject.Kind.SOURCE;
    URI uri = fileObject.toUri();
    String scheme = uri.getScheme();
    if (scheme.equals("file")) {
      return new RegularInputFile(uri.getPath());
    } else if (scheme.equals("jar")) {
      String path = uri.getPath();
      if (path.startsWith("file:")) {
        path = path.substring(5);
      }
      int bang = path.indexOf('!');
      String jarPath = path.substring(0, bang);
      String internalPath = path.substring(bang + 1);
      return new JarredInputFile(jarPath, internalPath);
    }
    return null;
  }

  private static String getSource(InputFile file) {
    try (Reader reader = file.openReader(); StringWriter writer = new StringWriter()) {
      char[] buf = new char[4096];
      int n;
      while ((n = reader.read(buf)) > 0) {
        writer.write(buf, 0, n);
      }
      return writer.toString();
    } catch (IOException e) {
      throw new AssertionError("failed re-reading source file: " + file.getPath(), e);
    }
  }

  private TreeConverter(JCTree.JCCompilationUnit javacUnit) {
    unit = javacUnit;
  }

  private TreeNode convert(Object obj) {
    if (obj == null) {
      return null;
    }
    JCTree node = (JCTree) obj;
    TreeNode newNode = convertInner(node)
        .setPosition(getPosition(node));
    newNode.validate();
    return newNode;
  }

  private SourcePosition getPosition(JCTree node) {
    int startPosition = TreeInfo.getStartPos(node);
    int endPosition = TreeInfo.getEndPos(node, unit.endPositions);
    int length = startPosition == Position.NOPOS || endPosition == Position.NOPOS
        ? 0 : endPosition - startPosition;
    if (unit.getLineMap() != null) {
      int line = unit.getLineMap().getLineNumber(startPosition);
      return new SourcePosition(startPosition, length, line);
    } else {
      return new SourcePosition(startPosition, length);
    }
  }

  @SuppressWarnings("fallthrough")
  private TreeNode convertInner(JCTree javacNode) {
    switch (javacNode.getKind()) {
      default:
        throw new AssertionError("Unknown node type: " + javacNode.getKind());

      case ANNOTATION:
        return convertAnnotation((JCTree.JCAnnotation) javacNode);
      case ANNOTATION_TYPE:
        return convertAnnotationTypeDeclaration((JCTree.JCClassDecl) javacNode);
      case ARRAY_ACCESS:
        return convertArrayAccess((JCTree.JCArrayAccess) javacNode);
      case ARRAY_TYPE:
        return convertArrayType((JCTree.JCArrayTypeTree) javacNode);
      case ASSERT:
        return convertAssert((JCTree.JCAssert) javacNode);
      case ASSIGNMENT:
        return convertAssignment((JCTree.JCAssign) javacNode);
      case BLOCK:
        return convertBlock((JCTree.JCBlock) javacNode);
      case BREAK:
        return convertBreakStatement((JCTree.JCBreak) javacNode);
      case CASE:
        return convertCase((JCTree.JCCase) javacNode);
      case CATCH:
        return convertCatch((JCTree.JCCatch) javacNode);
      case CLASS:
        return convertClassDeclaration((JCTree.JCClassDecl) javacNode);
      case COMPILATION_UNIT:
        throw new AssertionError(
            "CompilationUnit must be converted using convertCompilationUnit()");
      case CONDITIONAL_EXPRESSION:
        return convertConditionalExpression((JCTree.JCConditional) javacNode);
      case CONTINUE:
        return convertContinueStatement((JCTree.JCContinue) javacNode);
      case DO_WHILE_LOOP:
        return convertDoStatement((JCTree.JCDoWhileLoop) javacNode);
      case EMPTY_STATEMENT:
        return new EmptyStatement();
      case ENHANCED_FOR_LOOP:
        return convertEnhancedForStatement((JCTree.JCEnhancedForLoop) javacNode);
      case ENUM:
        return convertEnum((JCTree.JCClassDecl) javacNode);
      case EXPRESSION_STATEMENT:
        return convertExpressionStatement((JCTree.JCExpressionStatement) javacNode);
      case FOR_LOOP:
        return convertForLoop((JCTree.JCForLoop) javacNode);
      case IDENTIFIER:
        return convertIdent((JCTree.JCIdent) javacNode);
      case INSTANCE_OF:
        return convertInstanceOf((JCTree.JCInstanceOf) javacNode);
      case INTERFACE:
        return convertClassDeclaration((JCTree.JCClassDecl) javacNode);
      case IF:
        return convertIf((JCTree.JCIf) javacNode);
      case MEMBER_SELECT:
        return convertFieldAccess((JCTree.JCFieldAccess) javacNode);
      case METHOD:
        return convertMethodDeclaration((JCTree.JCMethodDecl) javacNode);
      case METHOD_INVOCATION:
        return convertMethodInvocation((JCTree.JCMethodInvocation) javacNode);
      case NEW_ARRAY:
        return convertNewArray((JCTree.JCNewArray) javacNode);
      case NEW_CLASS:
        return convertNewClass((JCTree.JCNewClass) javacNode);
      case PARAMETERIZED_TYPE:
        return convertTypeApply((JCTree.JCTypeApply) javacNode);
      case PARENTHESIZED:
        return convertParens((JCTree.JCParens) javacNode);
      case PRIMITIVE_TYPE:
        return convertPrimitiveType((JCTree.JCPrimitiveTypeTree) javacNode);
      case RETURN:
        return convertReturn((JCTree.JCReturn) javacNode);
      case SWITCH:
        return convertSwitch((JCTree.JCSwitch) javacNode);
      case THROW:
        return convertThrow((JCTree.JCThrow) javacNode);
      case TRY:
        return convertTry((JCTree.JCTry) javacNode);
      case TYPE_CAST:
        return convertTypeCast((JCTree.JCTypeCast) javacNode);
      case VARIABLE:
        return convertVariableDeclaration((JCTree.JCVariableDecl) javacNode);
      case WHILE_LOOP:
        return convertWhileLoop((JCTree.JCWhileLoop) javacNode);

      case BOOLEAN_LITERAL:
        return convertBooleanLiteral((JCTree.JCLiteral) javacNode);
      case CHAR_LITERAL:
        return convertCharLiteral((JCTree.JCLiteral) javacNode);
      case DOUBLE_LITERAL:
      case FLOAT_LITERAL:
      case INT_LITERAL:
      case LONG_LITERAL:
        return convertNumberLiteral((JCTree.JCLiteral) javacNode);
      case STRING_LITERAL:
        return convertStringLiteral((JCTree.JCLiteral) javacNode);
      case NULL_LITERAL:
        return new NullLiteral();

      case AND:
      case CONDITIONAL_AND:
      case CONDITIONAL_OR:
      case DIVIDE:
      case EQUAL_TO:
      case GREATER_THAN:
      case GREATER_THAN_EQUAL:
      case LEFT_SHIFT:
      case LESS_THAN:
      case LESS_THAN_EQUAL:
      case MINUS:
      case MULTIPLY:
      case NOT_EQUAL_TO:
      case OR:
      case PLUS:
      case REMAINDER:
      case RIGHT_SHIFT:
      case UNSIGNED_RIGHT_SHIFT:
      case XOR:
        return convertBinary((JCTree.JCBinary) javacNode);

      case BITWISE_COMPLEMENT:
      case LOGICAL_COMPLEMENT:
      case PREFIX_DECREMENT:
      case PREFIX_INCREMENT:
      case UNARY_MINUS:
      case UNARY_PLUS:
        return convertPrefixExpr((JCTree.JCUnary) javacNode);

      case POSTFIX_DECREMENT:
      case POSTFIX_INCREMENT:
        return convertPostExpr((JCTree.JCUnary) javacNode);

      case AND_ASSIGNMENT:
      case DIVIDE_ASSIGNMENT:
      case LEFT_SHIFT_ASSIGNMENT:
      case MINUS_ASSIGNMENT:
      case MULTIPLY_ASSIGNMENT:
      case OR_ASSIGNMENT:
      case PLUS_ASSIGNMENT:
      case REMAINDER_ASSIGNMENT:
      case RIGHT_SHIFT_ASSIGNMENT:
      case UNSIGNED_RIGHT_SHIFT_ASSIGNMENT:
      case XOR_ASSIGNMENT:
        return convertAssignOp((JCTree.JCAssignOp) javacNode);
    }
  }

  private TreeNode convertAbstractTypeDeclaration(
      JCTree.JCClassDecl node, AbstractTypeDeclaration newNode) {
    convertBodyDeclaration(node, newNode);
    List<BodyDeclaration> bodyDeclarations = new ArrayList<>();
    for (JCTree bodyDecl : node.getMembers()) {
      // Skip synthetic methods. Synthetic default constructors are not marked
      // synthetic for backwards-compatibility reasons, so they are detected
      // by a source position that's the same as their declaring class.
      // TODO(tball): keep synthetic default constructors after front-end switch,
      // and get rid of DefaultConstructorAdder translator.
      if (bodyDecl.getKind() == Kind.METHOD
          && (ElementUtil.isSynthetic(((JCTree.JCMethodDecl) bodyDecl).sym)
              || bodyDecl.pos == node.pos)) {
        continue;
      }
      Object member = convert(bodyDecl);
      if (member instanceof BodyDeclaration) {  // Not true for enum constants.
        bodyDeclarations.add((BodyDeclaration) member);
      }
    }
    return newNode
        .setName(convertSimpleName(node.sym))
        .setTypeElement(node.sym)
        .setBodyDeclarations(bodyDeclarations);
  }

  private TreeNode convertAnnotation(JCTree.JCAnnotation node) {
    List<JCTree.JCExpression> args = node.getArguments();
    Annotation newNode;
    if (args.isEmpty()) {
      newNode = new MarkerAnnotation()
          .setAnnotationMirror(node.attribute);
    } else if (args.size() == 1) {
      String annotationName = node.getAnnotationType().toString();
      if (annotationName.equals(Property.class.getSimpleName())
          || annotationName.equals(Property.class.getName())) {
        newNode = new PropertyAnnotation();
        //TODO(tball): parse property attribute string.
        throw new AssertionError("not implemented");
      } else {
        newNode = new SingleMemberAnnotation()
            .setValue((Expression) convert(args.get(0)));
      }
    } else {
      NormalAnnotation normalAnn = new NormalAnnotation();
      for (Object obj : args) {
        normalAnn.addValue((MemberValuePair) convert(obj));
      }
      newNode = normalAnn;
    }
    return newNode
        .setAnnotationMirror(node.attribute)
        .setTypeName((Name) convert(node.getAnnotationType()));
  }

  private TreeNode convertAnnotationTypeDeclaration(JCTree.JCClassDecl node) {
    return convertAbstractTypeDeclaration(node, new AnnotationTypeDeclaration());
  }

  private TreeNode convertArrayAccess(JCTree.JCArrayAccess node) {
    return new ArrayAccess()
        .setArray((Expression) convert(node.getExpression()))
        .setIndex((Expression) convert(node.getIndex()));
  }

  private TreeNode convertArrayType(JCTree.JCArrayTypeTree node) {
    ArrayType newNode = new ArrayType();
    convertType(node, newNode);
    Type componentType = (Type) Type.newType(node.getType().type);
    return newNode.setComponentType(componentType);
  }

  private TreeNode convertAssert(JCTree.JCAssert node) {
    return new AssertStatement()
        .setExpression((Expression) convert(node.getCondition()))
        .setMessage((Expression) convert(node.getDetail()));
  }

  private TreeNode convertAssignment(JCTree.JCAssign node) {
    Assignment newNode = new Assignment();
    convertExpression(node, newNode);
    return newNode
        .setOperator(Assignment.Operator.ASSIGN)
        .setLeftHandSide((Expression) convert(node.getVariable()))
        .setRightHandSide((Expression) convert(node.getExpression()));
  }

  private TreeNode convertAssignOp(JCTree.JCAssignOp node) {
    Assignment newNode = new Assignment();
    convertExpression(node, newNode);
    String operatorName = node.getOperator().getSimpleName().toString() + "=";
    return newNode
        .setOperator(Assignment.Operator.fromJdtOperatorName(operatorName))
        .setLeftHandSide((Expression) convert(node.getVariable()))
        .setRightHandSide((Expression) convert(node.getExpression()));
  }

  private TreeNode convertBinary(JCTree.JCBinary node) {
    return new InfixExpression()
        .setTypeMirror(node.type)
        .setOperator(InfixExpression.Operator.parse(node.operator.name.toString()))
        .addOperand((Expression) convert(node.getLeftOperand()))
        .addOperand((Expression) convert(node.getRightOperand()));
  }

  private TreeNode convertBlock(JCTree.JCBlock node) {
    Block newNode = new Block();
    for (StatementTree stmt : node.getStatements()) {
      newNode.addStatement((Statement) convert(stmt));
    }
    return newNode;
  }

  private TreeNode convertBodyDeclaration(JCTree.JCClassDecl node, BodyDeclaration newNode) {
    List<Annotation> annotations = new ArrayList<>();
    for (AnnotationTree annotation : node.getModifiers().getAnnotations()) {
      annotations.add((Annotation) convert(annotation));
    }
    return newNode
        .setModifiers((int) node.getModifiers().flags)
        .setAnnotations(annotations)
        .setJavadoc((Javadoc) getAssociatedJavaDoc(node));
  }

  private TreeNode convertBooleanLiteral(JCTree.JCLiteral node) {
    return new BooleanLiteral((Boolean) node.getValue(), node.type);
  }

  private TreeNode convertBreakStatement(JCTree.JCBreak node) {
    BreakStatement newNode = new BreakStatement();
    newNode.setLabel((SimpleName) convert(node.getLabel()));
    return newNode;
  }

  private TreeNode convertCase(JCTree.JCCase node) {
    // Case statements are converted in convertSwitch().
    return new SwitchCase()
        .setExpression((Expression) convert(node.getExpression()));
  }

  private TreeNode convertCatch(JCTree.JCCatch node) {
    return new CatchClause()
        .setException((SingleVariableDeclaration) convert(node.getParameter()))
        .setBody((Block) convert(node.getBlock()));
  }

  private TreeNode convertCharLiteral(JCTree.JCLiteral node) {
    return new CharacterLiteral((Character) node.getValue(), node.type);
  }

  private TreeNode convertClassDeclaration(JCTree.JCClassDecl node) {
    // javac defines all type declarations with JCClassDecl, so differentiate here
    // to support our different declaration nodes.
    if (node.sym.isAnonymous()) {
      throw new AssertionError("Anonymous class declaration tree conversion not implemented");
    }
    if (node.sym.getKind() == ElementKind.ANNOTATION_TYPE) {
      throw new AssertionError("Annotation type declaration tree conversion not implemented");
    }

    TypeDeclaration newNode =
        (TypeDeclaration) convertAbstractTypeDeclaration(node, new TypeDeclaration());

    newNode.setSuperclassType((Type) convert(node.getExtendsClause()));
    newNode.setInterface(
        node.getKind() == Kind.INTERFACE || node.getKind() == Kind.ANNOTATION_TYPE);
    for (JCTree superInterface : node.getImplementsClause()) {
      newNode.addSuperInterfaceType(Type.newType(nameType(superInterface)));
    }
    return newNode;
  }

  private TreeNode convertConditionalExpression(JCTree.JCConditional node) {
    return new ConditionalExpression()
        .setTypeMirror(node.type)
        .setExpression((Expression) convert(node.getCondition()))
        .setThenExpression((Expression) convert(node.getTrueExpression()))
        .setElseExpression((Expression) convert(node.getFalseExpression()));
  }

  private TreeNode convertContinueStatement(JCTree.JCContinue node) {
    return new ContinueStatement()
        .setLabel((SimpleName) convert(node.getLabel()));
  }

  private TreeNode convertDoStatement(JCTree.JCDoWhileLoop node) {
    return new DoStatement()
        .setExpression((Expression) convert(node.getCondition()))
        .setBody((Statement) convert(node.getStatement()));
  }

  private TreeNode convertEnhancedForStatement(JCTree.JCEnhancedForLoop node) {
    return new EnhancedForStatement()
        .setParameter((SingleVariableDeclaration) convertSingleVariable(node.getVariable()))
        .setExpression((Expression) convert(node.getExpression()))
        .setBody((Statement) convert(node.getStatement()));
  }

  private TreeNode convertEnum(JCTree.JCClassDecl node) {
    EnumDeclaration newNode =
        (EnumDeclaration) convertAbstractTypeDeclaration(node, new EnumDeclaration());
    for (Object superInterface : node.getImplementsClause()) {
      newNode.addSuperInterfaceType((Type) convert(superInterface));
    }
    for (Object bodyDecl : node.getMembers()) {
      Object member = convert(bodyDecl);
      if (member instanceof EnumConstantDeclaration) {
        // Other members were converted by convertAbstractTypeDeclaration().
        newNode.addEnumConstant((EnumConstantDeclaration) member);
      }
    }
    return newNode;
  }

  private TreeNode convertExpression(
      JCTree.JCExpression node, Expression newNode) {
    return newNode
        .setConstantValue(node.type.constValue());
  }

  private TreeNode convertExpressionStatement(JCTree.JCExpressionStatement node) {
    TreeNode expr = convert(node.getExpression());
    if (expr instanceof Statement) {
      return expr;
    }
    return new ExpressionStatement()
        .setExpression((Expression) convert(node.getExpression()));
  }

  private TreeNode convertFieldAccess(JCTree.JCFieldAccess node) {
    String fieldName = node.name.toString();
    JCTree.JCExpression selected = node.getExpression();
    if (fieldName.equals("this")) {
      return new ThisExpression()
          .setQualifier((Name) convert(selected));
    }
    if (selected.toString().equals("super")) {
      return new SuperFieldAccess()
          .setVariableElement((VariableElement) node.sym)
          .setName(new SimpleName(node.sym, node.type));
    }
    if (node.getIdentifier().toString().equals("class")) {
      com.sun.tools.javac.code.Type type = node.sym.asType();
      return new TypeLiteral(type)
          .setType(Type.newType(type));
    }
    if (selected.getKind() == Kind.IDENTIFIER || selected.getKind() == Kind.MEMBER_SELECT) {
      return new QualifiedName()
          .setName(new SimpleName(node.sym, node.type))
          .setQualifier((Name) convert(selected));
    }
    return new FieldAccess()
        .setVariableElement((VariableElement) node.sym)
        .setExpression((Expression) convert(selected))
        .setName(new SimpleName(node.sym, node.type));
  }

  private TreeNode convertForLoop(JCTree.JCForLoop node) {
    ForStatement newNode = new ForStatement()
        .setExpression((Expression) convert(node.getCondition()))
        .setBody((Statement) convert(node.getStatement()));
    for (JCTree.JCStatement initializer : node.getInitializer()) {
      if (initializer.getKind() == Kind.VARIABLE) {
        JCTree.JCVariableDecl var = (JCTree.JCVariableDecl) initializer;
        newNode.addInitializer((Expression) convertVariableExpression(var));
      } else {
        assert initializer.getKind() == Kind.EXPRESSION_STATEMENT;
        newNode.addInitializer((Expression)
          convert(((JCTree.JCExpressionStatement) initializer).getExpression()));
      }
    }
    for (JCTree.JCExpressionStatement updater : node.getUpdate()) {
      newNode.addUpdater((Expression) convert(updater.getExpression()));
    }
    return newNode;
  }

  private TreeNode convertIdent(JCTree.JCIdent node) {
    return new SimpleName(node.sym, node.type);
  }

  private TreeNode convertIf(JCTree.JCIf node) {
    Expression condition = (Expression) convert(node.getCondition());
    if (condition.getKind() == TreeNode.Kind.PARENTHESIZED_EXPRESSION) {
      condition = TreeUtil.remove(((ParenthesizedExpression) condition).getExpression());
    }
    return new IfStatement()
        .setExpression(condition)
        .setThenStatement((Statement) convert(node.getThenStatement()))
        .setElseStatement((Statement) convert(node.getElseStatement()));
  }

  private TreeNode convertInstanceOf(JCTree.JCInstanceOf node) {
    TypeMirror clazz = nameType(node.getType());
    return new InstanceofExpression()
        .setLeftOperand((Expression) convert(node.getExpression()))
        .setRightOperand(Type.newType(clazz));
  }

  private TreeNode convertMethodDeclaration(JCTree.JCMethodDecl node) {
    MethodDeclaration newNode = new MethodDeclaration();
    List<Annotation> annotations = new ArrayList<>();
    for (AnnotationTree annotation : node.getModifiers().getAnnotations()) {
      annotations.add((Annotation) convert(annotation));
    }
    for (JCTree.JCVariableDecl param : node.getParameters()) {
      newNode.addParameter((SingleVariableDeclaration) convert(param));
    }
    if (ElementUtil.isConstructor(node.sym)) {
      newNode
          .setName(convertSimpleName(ElementUtil.getDeclaringClass(node.sym)))
          .setIsConstructor(true);
    } else {
      newNode
          .setName(convertSimpleName(node.sym))
          .setReturnType(convertType(node.type.asMethodType().getReturnType(), false));
    }
    return newNode
        .setExecutableElement(node.sym)
        .setBody((Block) convert(node.getBody()))
        .setModifiers((int) node.getModifiers().flags)
        .setAnnotations(annotations)
        .setJavadoc((Javadoc) getAssociatedJavaDoc(node));
  }

  private TreeNode convertMethodInvocation(JCTree.JCMethodInvocation node) {
    JCTree.JCExpression method = node.getMethodSelect();
    if (method.getKind() == Kind.IDENTIFIER) {
      ExecutableElement element = (ExecutableElement) ((JCTree.JCIdent) method).sym;
      if (method.toString().equals("this")) {
        ConstructorInvocation newNode = new ConstructorInvocation()
            // TODO(tball): Add the appropriate ExecutableType.
            .setExecutablePair(new ExecutablePair(element, null));
        for (JCTree.JCExpression arg : node.getArguments()) {
          newNode.addArgument((Expression) convert(arg));
        }
        return newNode;
      }
      if (method.toString().equals("super")) {
        SuperConstructorInvocation newNode = new SuperConstructorInvocation()
            // TODO(tball): Add the appropriate ExecutableType.
            .setExecutablePair(new ExecutablePair(element, null));
        for (JCTree.JCExpression arg : node.getArguments()) {
          newNode.addArgument((Expression) convert(arg));
        }
        // If there's no expression node, javac sets it to be "<init>" which we don't want.
        Expression expr = ((Expression) convert(method));
        if (!expr.toString().equals("<init>")) {
          newNode.setExpression(expr);
        }
        return newNode;
      }

    }
    if (method.getKind() == Kind.MEMBER_SELECT
        && ((JCTree.JCFieldAccess) method).name.toString().equals("super")) {
      SuperConstructorInvocation newNode = new SuperConstructorInvocation()
          .setExecutablePair(new ExecutablePair(
              // TODO(tball): Add the appropriate ExecutableType.
              (ExecutableElement) ((JCTree.JCFieldAccess) method).sym, null))
          .setExpression((Expression) convert(method));
      for (JCTree.JCExpression arg : node.getArguments()) {
        newNode.addArgument((Expression) convert(arg));
      }
      return newNode;
    }

    MethodInvocation newNode = new MethodInvocation();
    if (method.getKind() == Kind.IDENTIFIER) {
      newNode
          .setName((SimpleName) convert(method))
          // TODO(tball): Add the appropriate ExecutableType.
          .setExecutablePair(new ExecutablePair(
              (ExecutableElement) ((JCTree.JCIdent) method).sym, null));
    } else {
      JCTree.JCFieldAccess select = (JCTree.JCFieldAccess) method;
      newNode
          .setName(convertSimpleName(select.sym))
          .setExpression((Expression) convert(select.selected))
          // TODO(tball): Add the appropriate ExecutableType.
          .setExecutablePair(new ExecutablePair((ExecutableElement) select.sym, null));
    }
    for (JCTree.JCExpression arg : node.getArguments()) {
      newNode.addArgument((Expression) convert(arg));
    }
    return newNode;
  }

  private SimpleName convertSimpleName(Element element) {
    return new SimpleName(element);
  }

  private Name convertName(Symbol symbol) {
    if (symbol.owner == null || symbol.owner.name.isEmpty()) {
      return new SimpleName(symbol);
    }
    return new QualifiedName(symbol, symbol.asType(), convertName(symbol.owner));
  }

  private TreeNode convertNewArray(JCTree.JCNewArray node) {
    ArrayCreation newNode = new ArrayCreation();
    convertExpression(node, newNode);
    List<Expression> dimensions = new ArrayList<>();
    for (JCTree.JCExpression dimension : node.getDimensions()) {
      dimensions.add((Expression) convert(dimension));
    }
    javax.lang.model.type.ArrayType type = (javax.lang.model.type.ArrayType) node.type;
    ArrayInitializer initializers = new ArrayInitializer(type);
    if (node.getInitializers() != null) {
      for (JCTree.JCExpression initializer : node.getInitializers()) {
        initializers.addExpression((Expression) convert(initializer));
      }
    }
    return newNode
        .setType(new ArrayType(type))
        .setDimensions(dimensions)
        .setInitializer(initializers);
  }

  private TreeNode convertNewClass(JCTree.JCNewClass node) {
    ClassInstanceCreation newNode = new ClassInstanceCreation()
        // TODO(tball): Add the appropriate ExecutableType.
        .setExecutablePair(new ExecutablePair((ExecutableElement) node.constructor, null))
        .setExpression((Expression) convert(node.getEnclosingExpression()))
        .setType(Type.newType(node.type));
    for (JCTree.JCExpression arg : node.getArguments()) {
      newNode.addArgument((Expression) convert(arg));
    }
    Object classBody = convert(node.getClassBody());
    if (classBody != null) {
      EnumDeclaration anonymousEnum = (EnumDeclaration) classBody;
      AnonymousClassDeclaration anonymousEnumType = new AnonymousClassDeclaration()
          .setTypeElement(anonymousEnum.getTypeElement());
      newNode.setAnonymousClassDeclaration(anonymousEnumType);
    }
    return newNode;
  }

  private TreeNode convertNumberLiteral(JCTree.JCLiteral node) {
    return new NumberLiteral((Number) node.getValue(), node.type)
        .setToken(node.toString());
  }

  private PackageDeclaration convertPackage(PackageSymbol pkg) {
    PackageDeclaration newNode = new PackageDeclaration()
        .setPackageElement(pkg);
    return newNode.setName(convertName(pkg));
  }

  private TreeNode convertPrefixExpr(JCTree.JCUnary node) {
    return new PrefixExpression()
        .setTypeMirror(node.type)
        .setOperator(PrefixExpression.Operator.parse(node.getOperator().name.toString()))
        .setOperand((Expression) convert(node.getExpression()));
  }

  private TreeNode convertParens(JCTree.JCParens node) {
    return new ParenthesizedExpression()
        .setExpression((Expression) convert(node.getExpression()));
  }

  private TreeNode convertPostExpr(JCTree.JCUnary node) {
    return new PostfixExpression()
        .setOperator(PostfixExpression.Operator.parse(node.getOperator().name.toString()))
        .setOperand((Expression) convert(node.getExpression()));
  }

  private TreeNode convertPrimitiveType(JCTree.JCPrimitiveTypeTree node) {
    return new PrimitiveType(node.type);
  }

  private TreeNode convertReturn(JCTree.JCReturn node) {
    return new ReturnStatement((Expression) convert(node.getExpression()));
  }

  private TreeNode convertStringLiteral(JCTree.JCLiteral node) {
    return new StringLiteral((String) node.getValue(), node.type);
  }

  private TreeNode convertSwitch(JCTree.JCSwitch node) {
    SwitchStatement newNode = new SwitchStatement()
        .setExpression((Expression) convert(node.getExpression()));
    for (JCTree.JCCase switchCase : node.getCases()) {
      newNode.addStatement((SwitchCase) convert(switchCase));
      for (JCTree.JCStatement s : switchCase.getStatements()) {
        newNode.addStatement((Statement) convert(s));
      }
    }
    return newNode;
  }

  private TreeNode convertThrow(JCTree.JCThrow node) {
    return new ThrowStatement()
        .setExpression((Expression) convert(node.getExpression()));
  }

  private TreeNode convertTry(JCTree.JCTry node) {
    TryStatement newNode = new TryStatement();
    for (Object obj : node.getResources()) {
      newNode.addResource((VariableDeclarationExpression) convert(obj));
    }
    for (Object obj : node.getCatches()) {
      newNode.addCatchClause((CatchClause) convert(obj));
    }
    return newNode
        .setBody((Block) convert(node.getBlock()))
        .setFinally((Block) convert(node.getFinallyBlock()));
  }

  private TreeNode convertType(JCTree.JCExpression node, Type newType) {
    return newType
        .setTypeMirror(node.type);
  }

  private TypeMirror nameType(JCTree node) {
    if (node.getKind() == Kind.PARAMETERIZED_TYPE) {
      return ((JCTree.JCTypeApply) node).clazz.type;
    }
    return node.getKind() == Kind.MEMBER_SELECT
        ? ((JCTree.JCFieldAccess) node).sym.asType()
        : ((JCTree.JCIdent) node).sym.asType();
  }

  private TreeNode convertTypeApply(JCTree.JCTypeApply node) {
    return new ParameterizedType()
        .setType(Type.newType(node.type))
        .setTypeMirror(node.type);
  }

  private TreeNode convertTypeCast(JCTree.JCTypeCast node) {
    return new CastExpression(node.type, (Expression) convert(node.getExpression()));
  }

  private TreeNode convertVariableDeclaration(JCTree.JCVariableDecl node) {
    VarSymbol var = node.sym;
    if (var.getKind() == ElementKind.FIELD) {
      return new FieldDeclaration(var, (Expression) convert(node.getInitializer()));
    }
    if (var.getKind() == ElementKind.LOCAL_VARIABLE) {
      return new VariableDeclarationStatement(var, (Expression) convert(node.getInitializer()))
          .setType(convertType(var.asType(), false));
    }
    if (var.getKind() == ElementKind.ENUM_CONSTANT) {
      EnumConstantDeclaration newNode = new EnumConstantDeclaration()
          .setName(convertSimpleName(var))
          .setVariableElement(var);
      ClassInstanceCreation init = (ClassInstanceCreation) convert(node.getInitializer());
      for (Expression arg : init.getArguments()) {
        newNode.addArgument(arg);
      }
      if (init.getAnonymousClassDeclaration() != null) {
        newNode.setAnonymousClassDeclaration(TreeUtil.remove(init.getAnonymousClassDeclaration()));
      }
      return newNode
          .setExecutablePair(init.getExecutablePair());
    }
    return convertSingleVariable(node);
  }

  private TreeNode convertSingleVariable(JCTree.JCVariableDecl node) {
    VarSymbol var = node.sym;
    boolean isVarargs = (node.sym.flags() & Flags.VARARGS) > 0;
    Type newType = convertType(var.asType(), isVarargs);
    return new SingleVariableDeclaration()
        .setType(newType)
        .setIsVarargs(isVarargs)
        .setName(convertSimpleName(var))
        .setVariableElement(var)
        .setInitializer((Expression) convert(node.getInitializer()));
  }

  private Type convertType(TypeMirror varType, boolean isVarargs) {
    Type newType;
    if (isVarargs) {
      newType = Type.newType(((javax.lang.model.type.ArrayType) varType).getComponentType());
    } else {
      if (varType.getKind() == TypeKind.DECLARED
          && !((DeclaredType) varType).getTypeArguments().isEmpty()) {
        newType = new ParameterizedType()
            .setType(new SimpleType(varType))
            .setTypeMirror(varType);
      } else {
        newType = Type.newType(varType);
      }
    }
    return newType;
  }

  private TreeNode convertVariableExpression(JCTree.JCVariableDecl node) {
    VarSymbol var = node.sym;
    boolean isVarargs = (node.sym.flags() & Flags.VARARGS) > 0;
    Type newType = convertType(var.asType(), isVarargs);
    VariableDeclarationFragment fragment = new VariableDeclarationFragment();
    fragment
        .setName(convertSimpleName(var))
        .setVariableElement(var)
        .setInitializer((Expression) convert(node.getInitializer()));
    return new VariableDeclarationExpression()
        .setType(newType)
        .addFragment(fragment);
  }

  private TreeNode convertWhileLoop(JCTree.JCWhileLoop node) {
    return new WhileStatement()
        .setExpression((Expression) convert(node.getCondition()))
        .setBody((Statement) convert(node.getStatement()));
  }

  private TreeNode getAssociatedJavaDoc(JCTree node) {
    Comment comment = convertAssociatedComment(node);
    return comment != null && comment.isDocComment() ? comment : null;
  }

  private Comment convertAssociatedComment(JCTree node) {
    DocCommentTable docComments = unit.docComments;
    if (docComments == null || !docComments.hasComment(node)) {
      return null;
    }
    com.sun.tools.javac.parser.Tokens.Comment javacComment = docComments.getComment(node);
    Comment comment;
    switch (javacComment.getStyle()) {
      case BLOCK:
        comment = new BlockComment();
        break;
      case JAVADOC:
        comment = new Javadoc();
        break;
      case LINE:
        comment = new LineComment();
        break;
      default:
        throw new AssertionError("unknown comment type");
    }
    int startPos = javacComment.getSourcePos(0);
    int endPos = startPos + javacComment.getText().length();
    comment.setSourceRange(startPos, endPos);
    return comment;
  }
}
