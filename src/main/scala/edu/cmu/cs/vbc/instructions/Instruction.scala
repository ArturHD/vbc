package edu.cmu.cs.vbc.instructions

import edu.cmu.cs.vbc.OpcodePrint
import edu.cmu.cs.vbc.adapter.FieldTransformer
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes._

trait Instruction extends LiftUtils {
  def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block)

  def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block)

  def getVariables: Set[LocalVar] = Set()

  def getJumpInstr: Option[JumpInstruction] = None

  final def isJumpInstr: Boolean = getJumpInstr.isDefined

  def isReturnInstr: Boolean = false
}


case class UNKNOWN(opCode: Int = -1) extends Instruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    throw new RuntimeException("Unknown Instruction: " + OpcodePrint.print(opCode))
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    throw new RuntimeException("Unknown Instruction: " + OpcodePrint.print(opCode))
  }
}


case class InstrNOP() extends Instruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(NOP)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
  }
}


/**
  * Helper instruciton for initializing conditional fields
  */
case class InstrINIT_FIELDS(owner: String) extends Instruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    // do nothing
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    for (i <- FieldTransformer.fields) initCondField(mv, i, owner)
  }
}
