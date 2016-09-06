package edu.cmu.cs.vbc.vbytecode

import edu.cmu.cs.vbc.utils.{LiftUtils, LiftingPolicy}
import edu.cmu.cs.vbc.vbytecode.instructions.{InstrINIT_CONDITIONAL_FIELDS, InstrINVOKESTATIC, InstrRETURN, Instruction}
import org.objectweb.asm.Opcodes._
import org.objectweb.asm._
import org.objectweb.asm.tree._


case class VBCMethodNode(access: Int,
                         name: String,
                         desc: String,
                         signature: Option[String],
                         exceptions: List[String],
                         body: CFG,
                         localVar: List[Variable] = Nil // initial local variables
                        ) {

  import LiftUtils._

  def toByteCode(cw: ClassVisitor, clazz: VBCClassNode) = {
    val mv = cw.visitMethod(access, name, desc, signature.getOrElse(null), exceptions.toArray)
    mv.visitCode()
    body.toByteCode(mv, new MethodEnv(clazz, this))
    mv.visitMaxs(0, 0)
    mv.visitEnd()
  }

  def toVByteCode(cw: ClassVisitor, clazz: VBCClassNode) = {
    createBackupMethod(cw, clazz)
    val liftedMethodDesc = liftMethodDescription(desc)
    val mv = cw.visitMethod(access, liftMethodName(name), liftedMethodDesc, liftMethodSignature(desc, signature).getOrElse(null), exceptions.toArray)
    mv.visitCode()
    val labelStart = new Label()
    mv.visitLabel(labelStart)

    if (body.blocks.nonEmpty) {
      val env = new VMethodEnv(clazz, this)
      body.toVByteCode(mv, env)

      val labelEnd = new Label()
      mv.visitLabel(labelEnd)

      //storing local variable information for debugging
      for (v <- localVar ++ env.getFreshVars()) v match {
        case p: Parameter =>
          val pidx = if (isStatic) p.idx else p.idx - 1
          if (p.name != "$unknown")
            mv.visitLocalVariable(p.name, if (pidx == -1) "L" + clazz.name + ";" else Type.getArgumentTypes(liftedMethodDesc)(p.idx).getDescriptor, null, labelStart, labelEnd, p.idx)
        case l: LocalVar =>
          if (l.name != "$unknown")
            mv.visitLocalVariable(l.name, l.desc, null, labelStart, labelEnd, env.getVarIdx(l))
      }
      //ctx parameter
      mv.visitLocalVariable("$ctx", fexprclasstype, null, labelStart, labelEnd, env.getVarIdx(env.ctxParameter))
    }


    mv.visitMaxs(0, 0)
    mv.visitEnd()
  }

  lazy val returnsVoid = Type.getMethodType(desc).getReturnType == Type.VOID_TYPE

  lazy val isMain =
    desc == "([Ljava/lang/String;)V" && isStatic && isPublic

  lazy val isStatic: Boolean = (access & Opcodes.ACC_STATIC) > 0

  lazy val isPublic: Boolean = (access & Opcodes.ACC_PUBLIC) > 0

  /**
    * We need special handling for <init> method lifting.
    * JVM will complain if ALOAD 0 and INVOKESPECIAL java.lang.object.<init> is invoked
    * inside a branch, which is usually the case in our lifting (we check the ctx in the
    * beginning of each method call)
    *
    * name should be sufficient because <init> method name is special enough
    *
    * @see [[CFG.toVByteCode()]] and [[Rewrite]]
    * @return
    */
  lazy val isInit =
    name == "<init>"

  lazy val isClinit = name == "<clinit>"

  /**
    * Create a wrapper method that call the V method
    *
    * Useful for methods like compare(), which is called in JDK
    */
  def createBackupMethod(cw: ClassVisitor, clazz: VBCClassNode): Unit = {
    // todo: checking interface name might not be accurate enough
    if (clazz.interfaces.exists(_.startsWith("java"))) {
      val mv = cw.visitMethod(access, name, desc, signature.getOrElse(null), exceptions.toArray)
      mv.visitCode()
      // wrap all arguments into V and call V method
      val callType = if ((access & Opcodes.ACC_STATIC) != 0) INVOKESTATIC else if (name == "<init>") INVOKESPECIAL else INVOKEVIRTUAL
      val nArgs = Type.getArgumentTypes(desc).length
      if (callType != INVOKESTATIC) mv.visitVarInsn(ALOAD, 0)
      (1 to nArgs) foreach {i =>
        // todo: could be primitive type
        mv.visitVarInsn(ALOAD, i)
        callVCreateOne(mv, pushConstantTRUE)
      }
      pushConstantTRUE(mv)  //ctx
      mv.visitMethodInsn(callType, clazz.name, name, liftMethodDescription(desc), false)
      // unwrap return type
      Type.getReturnType(desc).getSort match {
        case Type.INT =>
          mv.visitMethodInsn(INVOKEINTERFACE, vclassname, "getOne", "()Ljava/lang/Object;", true)
          mv.visitTypeInsn(CHECKCAST, vInt)
          mv.visitMethodInsn(INVOKEVIRTUAL, vInt, "intValue", "()I", false)
          mv.visitInsn(IRETURN)
        case Type.OBJECT =>
          mv.visitMethodInsn(INVOKEINTERFACE, vclassname, "getOne", "()Ljava/lang/Object;", true)
          mv.visitInsn(ARETURN)
        case Type.VOID =>
          mv.visitInsn(RETURN)
        case _ =>
          throw new RuntimeException("Unsupported type")
      }

      mv.visitMaxs(0, 0)
      mv.visitEnd()
    }
  }
}


/**
  * Variable, Parameter, and LocalVar are to be used in the construction
  * of the byte code
  *
  * In contrast EnvVariable, EnvParameter, and EnvLocalVar are used
  * internally to refer to specific
  */
sealed trait Variable {
  def getIdx(): Option[Int] = None
}

/**
  * the name is used solely for debugging purposes
  *
  * equality by idx
  */
class Parameter(val idx: Int, val name: String) extends Variable {
  override def getIdx(): Option[Int] = Some(idx)

  override def hashCode = idx

  override def equals(that: Any) = that match {
    case that: Parameter => this.idx == that.idx
    case _ => false
  }
}

/**
  * the name and description are used solely for debugging purposes
  */
class LocalVar(val name: String, val desc: String) extends Variable


case class VBCClassNode(
                         version: Int,
                         access: Int,
                         name: String,
                         signature: Option[String],
                         superName: String,
                         interfaces: List[String],
                         fields: List[VBCFieldNode],
                         methods: List[VBCMethodNode],
                         source: Option[(String, String)] = None,
                         outerClass: Option[(String, String, String)] = None,
                         visibleAnnotations: List[AnnotationNode] = Nil,
                         invisibleAnnotations: List[AnnotationNode] = Nil,
                         visibleTypeAnnotations: List[TypeAnnotationNode] = Nil,
                         invisibleTypeAnnotations: List[TypeAnnotationNode] = Nil,
                         attrs: List[Attribute] = Nil,
                         innerClasses: List[VBCInnerClassNode] = Nil
                       ) {

  import LiftUtils._

  def toByteCode(cv: ClassVisitor, rewriter: VBCMethodNode => VBCMethodNode = a => a) = {
    cv.visit(version, access, name, signature.getOrElse(null), superName, interfaces.toArray)
    commonToByteCode(cv)
    //        innerClasses.foreach(_.toByteCode(cv))
    fields.foreach(_.toByteCode(cv))
    methods.foreach(m => rewriter(Rewrite.rewrite(m)).toByteCode(cv, this))
    cv.visitEnd()
  }


  def liftSuperName(superName: Owner): Owner = {
    // Super class of interface must be java/lang/Object, could not be VObject
    if ((access & ACC_INTERFACE) == 0)
      LiftingPolicy.liftClassName(superName)
    else
      superName
  }

  def toVByteCode(cv: ClassVisitor, rewriter: VBCMethodNode => VBCMethodNode = a => a) = {
    val liftedSuperName = liftSuperName(Owner(superName))
    cv.visit(version, access, name, signature.getOrElse(null), liftedSuperName, interfaces.toArray)
    commonToByteCode(cv)
    //        innerClasses.foreach(_.toVByteCode(cv))
    fields.foreach(_.toVByteCode(cv))
    methods.foreach(m => rewriter(Rewrite.rewriteV(m)).toVByteCode(cv, this))
    //if the class has a main method, create also an unlifted main method
    if (methods.exists(_.isMain))
      createUnliftedMain(cv)
    // create <clinit> method
    if (hasStaticConditionalFields) createCLINIT(cv, rewriter)
    // Write lambda methods
    // Lambda methods might be generated while generating other lambda methods (e.g. nested invokedynamic see InvokeDynamicUtils),
    // so we need this while to ensure that all lambda methods are generated
    var toWrite: Set[String] = lambdaMethods.keySet -- writtenLambdaMethods
    while (toWrite.nonEmpty) {
      toWrite.foreach((name: String) => {
        writtenLambdaMethods += name
        lambdaMethods(name)(cv)
      })
      toWrite = lambdaMethods.keySet -- writtenLambdaMethods
    }
    cv.visitEnd()
  }

  lazy val hasStaticConditionalFields = fields.exists(_.isStatic)

  lazy val hasCLINIT = methods.exists(_.name == "<clinit>")

  /**
    * Creates our own clinit methods.
    *
    * Original clinit method is renamed to ______clinit______. Our own clinit simply calls
    * ___clinit___, which does initialization of conditional fields and then call ______clinit______.
    *
    * @todo remove ___clinit___
    * @param cv
    * @param rewriter
    */
  def createCLINIT(cv: ClassVisitor, rewriter: VBCMethodNode => VBCMethodNode = a => a) = {
    val instrs: Array[Instruction] =
      if (hasCLINIT)
        Array(
          InstrINIT_CONDITIONAL_FIELDS(),
          InstrINVOKESTATIC(Owner(name), MethodName("______clinit______"), MethodDesc("()V"), false),
          InstrRETURN()
        )
      else
        Array(InstrINIT_CONDITIONAL_FIELDS(), InstrRETURN())
    val vbcMtd = VBCMethodNode(
      ACC_STATIC,
      "___clinit___",
      "()V",
      None,
      List.empty,
      CFG(List(Block(instrs: _*)))
    )
    rewriter(Rewrite.rewriteV(vbcMtd)).toVByteCode(cv, this)

    val mv = cv.visitMethod(ACC_STATIC, "<clinit>", "()V", null, Array.empty)
    mv.visitCode()
    pushConstantTRUE(mv)
    mv.visitMethodInsn(INVOKESTATIC, name, "___clinit___", s"($fexprclasstype)V", false)
    mv.visitInsn(RETURN)
    mv.visitMaxs(10, 10)
    mv.visitEnd()
  }

  /**
    * we assume the main method gets lifted just as everything else;
    * here we create a new entry into the program with a normal
    * main method (without a context) that just starts the lifted
    * main method in context True
    */
  def createUnliftedMain(cv: ClassVisitor) = {
    val mainMethodSig = "([Ljava/lang/String;)V"
    val mv = cv.visitMethod(ACC_PUBLIC | ACC_STATIC, "main", mainMethodSig, mainMethodSig, Array.empty)
    mv.visitCode()
    //load array param
    mv.visitVarInsn(ALOAD, 0)
    //create a V<String[]>
    callVCreateOne(mv, (m) => pushConstantTRUE(m))
    //set context to True
    pushConstantTRUE(mv)
    mv.visitMethodInsn(INVOKESTATIC, name, "main", liftMethodDescription(mainMethodSig), false)
    mv.visitInsn(RETURN)
    mv.visitMaxs(2, 0)
    mv.visitEnd()
  }


  /**
    * parts that are not lifted
    */
  private def commonToByteCode(cv: ClassVisitor): Unit = {
    source.map(s => cv.visitSource(s._1, s._2))
    outerClass.map(s => cv.visitOuterClass(s._1, s._2, s._3))
    visibleAnnotations.foreach(a => a.accept(cv.visitAnnotation(a.desc, true)))
    invisibleAnnotations.foreach(a => a.accept(cv.visitAnnotation(a.desc, false)))
    visibleTypeAnnotations.foreach(a => a.accept(cv.visitTypeAnnotation(a.typeRef, a.typePath, a.desc, true)))
    invisibleTypeAnnotations.foreach(a => a.accept(cv.visitTypeAnnotation(a.typeRef, a.typePath, a.desc, false)))
    attrs.foreach(cv.visitAttribute)
  }

  /**
    * Generated lambdaMethods, indexed by name
    */
  var lambdaMethods: Map[String, (ClassVisitor) => Unit] = Map()
  /**
    * Bookkeeping lambda methods that are already written to ClassWriter
    */
  var writtenLambdaMethods: Set[String] = Set()

}

case class VBCFieldNode(
                         access: Int,
                         name: String,
                         desc: String,
                         signature: String,
                         value: Object,

                         visibleAnnotations: List[AnnotationNode] = Nil,
                         invisibleAnnotations: List[AnnotationNode] = Nil,
                         visibleTypeAnnotations: List[TypeAnnotationNode] = Nil,
                         invisibleTypeAnnotations: List[TypeAnnotationNode] = Nil,
                         attrs: List[Attribute] = Nil
                       ) {

  import LiftUtils._

  def toByteCode(cv: ClassVisitor) = {
    val fv = cv.visitField(access, name, desc, signature, value)

    commonToByteCode(fv)

    fv.visitEnd()
  }

  def toVByteCode(cv: ClassVisitor) = {
    val fv = cv.visitField(access, name, vclasstype, liftPrimitiveType(desc), value)
    //TODO lift initial value for

    commonToByteCode(fv)

    fv.visitEnd()
  }

  def hasConditionalAnnotation() =
    (visibleAnnotations ++ invisibleAnnotations).exists(_.desc == "Ledu/cmu/cs/varex/annotation/VConditional;")

  def isStatic: Boolean = (access & ACC_STATIC) != 0


  private def commonToByteCode(fv: FieldVisitor): Unit = {
    visibleAnnotations.foreach(a => a.accept(fv.visitAnnotation(a.desc, true)))
    invisibleAnnotations.foreach(a => a.accept(fv.visitAnnotation(a.desc, false)))
    visibleTypeAnnotations.foreach(a => a.accept(fv.visitTypeAnnotation(a.typeRef, a.typePath, a.desc, true)))
    invisibleTypeAnnotations.foreach(a => a.accept(fv.visitTypeAnnotation(a.typeRef, a.typePath, a.desc, false)))
    attrs.foreach(fv.visitAttribute)
  }
}

case class VBCInnerClassNode(
                            name: String,
                            outerName: String,
                            innerName: String,
                            access: Int
                            )
