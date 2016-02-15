package edu.cmu.cs.vbc.instructions

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes._

/**
  * @author chupanw
  */

/**
  * ICONST_n instruction
  * @param v
  */
case class InstrICONST(v: Int) extends Instruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    pushConstant(mv, v)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    pushConstant(mv, v)
    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false)
    callVCreateOne(mv)
  }
}



case class InstrLDC(o: Object) extends Instruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitLdcInsn(o)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, blockA: Block): Unit = {
    mv.visitLdcInsn(o)
    //TODO: wrap into a V
//    callVCreateOne(mv)
  }
}