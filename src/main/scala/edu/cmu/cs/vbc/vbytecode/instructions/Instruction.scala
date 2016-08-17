package edu.cmu.cs.vbc.vbytecode.instructions

import edu.cmu.cs.vbc.OpcodePrint
import edu.cmu.cs.vbc.analysis.VBCFrame
import edu.cmu.cs.vbc.analysis.VBCFrame.UpdatedFrame
import edu.cmu.cs.vbc.utils.LiftUtils
import edu.cmu.cs.vbc.vbytecode._
import org.objectweb.asm.Opcodes._
import org.objectweb.asm.{Label, MethodVisitor, Type}

trait Instruction {

  def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block)

  def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block)

  /**
    * Update the stack symbolically after executing this instruction
    *
    * @return UpdatedFrame is a tuple consisting of new VBCFrame and a backtrack instructions.
    *         If backtrack instruction set is not empty, we need to backtrack because we finally realise we need to lift
    *         that instruction. By default every backtracked instruction should be lifted, except for GETFIELD,
    *         PUTFIELD, INVOKEVIRTUAL, and INVOKESPECIAL, because lifting them or not depends on the type of object
    *         currently on stack. If the object is a V, we need to lift these instructions with INVOKEDYNAMIC.
    *
    *         If backtrack instruction set is not empty, the returned VBCFrame is useless, current frame will be pushed
    *         to queue again and reanalyze later. (see [[edu.cmu.cs.vbc.analysis.VBCAnalyzer.computeBeforeFrames]]
    */
  def updateStack(s: VBCFrame, env: VMethodEnv): UpdatedFrame

  def doBacktrack(env: VMethodEnv) = env.setLift(this)

  def getVariables: Set[LocalVar] = Set()

  def getJumpInstr: Option[JumpInstruction] = None

  final def isJumpInstr: Boolean = getJumpInstr.isDefined

  def isReturnInstr: Boolean = false


  /**
    * Used to identify the start of init method
    *
    * @see [[Rewrite.rewrite()]]
    */
  def isALOAD0: Boolean = false

  /**
    * Used to identify the start of init method
    *
    * @see [[Rewrite.rewrite()]]
    */
  def isINVOKESPECIAL_OBJECT_INIT: Boolean = false

  /**
    * instructions should not be compared for structural equality but for object identity.
    * overwriting case class defaults to original Java defaults
    */
  override def equals(that: Any) = that match {
    case t: AnyRef => t eq this
    case _ => false
  }
}


case class UNKNOWN(opCode: Int = -1) extends Instruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    throw new RuntimeException("Unknown Instruction: " + OpcodePrint.print(opCode))
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    throw new RuntimeException("Unknown Instruction: " + OpcodePrint.print(opCode))
  }


  override def updateStack(s: VBCFrame, env: VMethodEnv): UpdatedFrame =
    throw new RuntimeException("Unknown Instruction: " + OpcodePrint.print(opCode))
}

trait EmptyInstruction extends Instruction

case class InstrNOP() extends EmptyInstruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(NOP)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = toByteCode(mv, env, block)

  override def updateStack(s: VBCFrame, env: VMethodEnv): UpdatedFrame = (s, Set.empty[Instruction])
}

case class InstrLINENUMBER(line: Int) extends EmptyInstruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    val l = new Label()
    mv.visitLabel(l)
    mv.visitLineNumber(line, l)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = toByteCode(mv, env, block)

  override def updateStack(s: VBCFrame, env: VMethodEnv): UpdatedFrame = (s, Set())
}


/**
  * Helper instruction for initializing conditional fields
  */
case class InstrINIT_CONDITIONAL_FIELDS() extends Instruction {

  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    // do nothing
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {

    import LiftUtils._
    def createChoice(fName: String, mv: MethodVisitor): Unit = {
      mv.visitLdcInsn(fName)
      mv.visitMethodInsn(INVOKESTATIC, fexprfactoryClassName, "createDefinedExternal", "(Ljava/lang/String;)Lde/fosd/typechef/featureexpr/SingleFeatureExpr;", false)
      mv.visitInsn(ICONST_1)
      mv.visitMethodInsn(INVOKESTATIC, vInt, "valueOf", s"(I)$vIntType", false)
      callVCreateOne(mv, (m) => loadCurrentCtx(m, env, block))
      mv.visitInsn(ICONST_0)
      mv.visitMethodInsn(INVOKESTATIC, vInt, "valueOf", s"(I)$vIntType", false)
      callVCreateOne(mv, (m) => loadCurrentCtx(m, env, block))
      callVCreateChoice(mv)
    }

    def createOne(fDesc: String, mv: MethodVisitor): Unit = {
      Type.getType(fDesc).getSort match {
        case Type.INT => mv.visitInsn(ICONST_0); mv.visitMethodInsn(INVOKESTATIC, vInt, "valueOf", s"(I)$vIntType", false)
        case Type.OBJECT => mv.visitInsn(ACONST_NULL)
        //          case Type.BOOLEAN => mv.visitIntInsn(BIPUSH, 0); mv.visitMethodInsn(INVOKESTATIC, vBoolean, "valueOf", s"(Z)$vBooleanType", false)
        case Type.BOOLEAN => mv.visitInsn(ICONST_0); mv.visitMethodInsn(INVOKESTATIC, vInt, "valueOf", s"(I)$vIntType", false)
        case _ => ???
      }
      callVCreateOne(mv, (m) => loadCurrentCtx(m, env, block))
    }

    def inClinit = env.method.name == "___clinit___"
    if (inClinit) {
      env.clazz.fields.filter(f => f.isStatic && f.hasConditionalAnnotation()).foreach(f => {
        createChoice(f.name, mv);
        mv.visitFieldInsn(PUTSTATIC, env.clazz.name, f.name, "Ledu/cmu/cs/varex/V;")
      })
      env.clazz.fields.filter(f => f.isStatic && !f.hasConditionalAnnotation()).foreach(f => {
        createOne(f.desc, mv);
        mv.visitFieldInsn(PUTSTATIC, env.clazz.name, f.name, "Ledu/cmu/cs/varex/V;")
      })
    }
    else {
      env.clazz.fields.filter(f => !f.isStatic && f.hasConditionalAnnotation()).foreach(f => {
        mv.visitVarInsn(ALOAD, 0)
        createChoice(f.name, mv)
        mv.visitFieldInsn(PUTFIELD, env.clazz.name, f.name, "Ledu/cmu/cs/varex/V;")
      })
      env.clazz.fields.filter(f => !f.isStatic && !f.hasConditionalAnnotation()).foreach(f => {
        mv.visitVarInsn(ALOAD, 0)
        createOne(f.desc, mv)
        mv.visitFieldInsn(PUTFIELD, env.clazz.name, f.name, "Ledu/cmu/cs/varex/V;")
      })
    }
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): UpdatedFrame = (s, Set())
}
