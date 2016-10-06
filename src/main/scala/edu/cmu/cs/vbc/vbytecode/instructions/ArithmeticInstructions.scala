package edu.cmu.cs.vbc.vbytecode.instructions

import edu.cmu.cs.vbc.analysis.VBCFrame.UpdatedFrame
import edu.cmu.cs.vbc.analysis.{INT_TYPE, LONG_TYPE, VBCFrame, V_TYPE}
import edu.cmu.cs.vbc.utils.LiftUtils._
import edu.cmu.cs.vbc.utils.{InvokeDynamicUtils, VCall}
import edu.cmu.cs.vbc.vbytecode._
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes._

trait BinOpInstruction extends Instruction {

  override def updateStack(s: VBCFrame, env: VMethodEnv): UpdatedFrame = {
    if (s.stack.take(2).exists(_._1 == V_TYPE()))
      env.setLift(this)
    val (v1, prev1, frame1) = s.pop()
    val (v2, prev2, frame2) = frame1.pop()
    val newFrame =
      if (env.shouldLiftInstr(this))
        frame2.push(V_TYPE(), Set(this))
      else {
        //todo: float, double
        frame2.push(INT_TYPE(), Set(this))
      }
    val backtrack: Set[Instruction] =
      if (env.shouldLiftInstr(this)) {
        if (v1 != V_TYPE()) prev1
        else if (v2 != V_TYPE()) prev2
        else Set()
      }
      else
        Set()
    (newFrame, backtrack)
  }
}

/**
  * IADD instruction
  */
case class InstrIADD() extends BinOpInstruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(IADD)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this))
      mv.visitMethodInsn(INVOKESTATIC, vopsclassname, "IADD", "(Ledu/cmu/cs/varex/V;Ledu/cmu/cs/varex/V;)Ledu/cmu/cs/varex/V;", false)
    else
      mv.visitInsn(IADD)
  }
}


case class InstrISUB() extends BinOpInstruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(ISUB)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this))
      mv.visitMethodInsn(INVOKESTATIC, vopsclassname, "ISUB", "(Ledu/cmu/cs/varex/V;Ledu/cmu/cs/varex/V;)Ledu/cmu/cs/varex/V;", false)
    else
      mv.visitInsn(ISUB)
  }
}


case class InstrIMUL() extends BinOpInstruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(IMUL)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this))
      mv.visitMethodInsn(INVOKESTATIC, vopsclassname, "IMUL", "(Ledu/cmu/cs/varex/V;Ledu/cmu/cs/varex/V;)Ledu/cmu/cs/varex/V;", false)
    else
      mv.visitInsn(IMUL)
  }
}


case class InstrIDIV() extends BinOpInstruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(IDIV)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      mv.visitMethodInsn(INVOKESTATIC, vopsclassname, "IDIV", "(Ledu/cmu/cs/varex/V;Ledu/cmu/cs/varex/V;)Ledu/cmu/cs/varex/V;", false)
    } else
      mv.visitInsn(IDIV)
  }
}

/**
  * Negate int.
  *
  * Operand stack: ..., value -> ..., result
  */
case class InstrINEG() extends Instruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(INEG)
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = {
    val (v, prev, frame) = s.pop()
    if (v == V_TYPE())
      env.setLift(this)
    val newFrame =
      if (env.shouldLiftInstr(this))
        frame.push(V_TYPE(), Set(this))
      else {
        frame.push(INT_TYPE(), Set(this))
      }
    if (env.shouldLiftInstr(this) && v != V_TYPE())
        return (s, prev)
    (newFrame, Set())
  }

  /**
    * Lifting means performing operations on a V object
    */
  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      InvokeDynamicUtils.invoke(VCall.smap, mv, env, loadCurrentCtx(_, env, block), "INEG", s"$IntType()$IntType") {
        (visitor: MethodVisitor) => {
          visitor.visitVarInsn(ALOAD, 1)
          visitor.visitMethodInsn(INVOKEVIRTUAL, IntClass, "intValue", "()I", false)
          visitor.visitInsn(INEG)
          visitor.visitMethodInsn(INVOKESTATIC, IntClass, "valueOf", s"(I)$IntType", false)
          visitor.visitInsn(ARETURN)
        }
      }
    }
  }
}

/** Shift left int
  *
  * ..., value1(int), value2(int) -> ..., result
  */
case class InstrISHL() extends Instruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(ISHL)
  }

  /** Lifting means invoking ISHL on V.
    *
    * If lifting, assume that value2 is V
    */
  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      InvokeDynamicUtils.invoke(
        VCall.sflatMap,
        mv,
        env,
        loadCtx = loadCurrentCtx(_, env, block),
        lambdaName = "ISHL",
        desc = TypeDesc.getInt + "(" + TypeDesc.getInt + ")" + vclasstype,
        nExplodeArgs = 1
      ) {
        (mv: MethodVisitor) => {
          mv.visitVarInsn(ALOAD, 0) // Integer
          Integer2int(mv)  // int
          mv.visitVarInsn(ALOAD, 2) // Integer
          Integer2int(mv) // int
          mv.visitInsn(ISHL)
          int2Integer(mv)
          callVCreateOne(mv, m => m.visitVarInsn(ALOAD, 1))
          mv.visitInsn(ARETURN)
        }
      }
    }
    else
      mv.visitInsn(ISHL)
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = {
    val (argType, argBacktrack, frame1) = s.pop()
    val (receiverType, receiverBacktrack, frame2) = frame1.pop()
    val hasV = argType == V_TYPE() || receiverType == V_TYPE()
    if (hasV) env.setLift(this)
    val newFrame = frame2.push(if (hasV) V_TYPE() else INT_TYPE(), Set(this))
    val backtrack: Set[Instruction] = (argType, receiverType) match {
      case (INT_TYPE(), _) => argBacktrack
      case (_, INT_TYPE()) => receiverBacktrack
      case _ => Set()
    }
    (newFrame, backtrack)
  }
}

/** Compare long
  *
  * ..., value1(long), value2(long) -> ..., result(int)
  */
case class InstrLCMP() extends Instruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(LCMP)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      InvokeDynamicUtils.invoke(
        VCall.sflatMap,
        mv,
        env,
        loadCtx = loadCurrentCtx(_, env, block),
        lambdaName = "lcmp",
        desc = TypeDesc.getLong + s"(${TypeDesc.getLong})" + vclasstype,
        nExplodeArgs = 1
      ) {
        (mv: MethodVisitor) => {
          mv.visitVarInsn(ALOAD, 0) //value1
          Long2long(mv)
          mv.visitVarInsn(ALOAD, 2) //value2
          Long2long(mv)
          mv.visitInsn(LCMP)
          int2Integer(mv)
          callVCreateOne(mv, (m) => m.visitVarInsn(ALOAD, 1))
          mv.visitInsn(ARETURN)
        }
      }
    }
    else
      mv.visitInsn(LCMP)
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = {
    val (value2Type, value2Backtrack, frame2) = s.pop()
    val (value1Type, value1Backtrack, frame1) = frame2.pop()
    (value1Type, value2Type) match {
      case (LONG_TYPE(), LONG_TYPE()) => (frame1.push(INT_TYPE(), Set(this)), Set())
      case (LONG_TYPE(), V_TYPE()) => (frame1, value1Backtrack) // backtrack, frame1 will be discarded
      case (V_TYPE(), LONG_TYPE()) => (frame1, value2Backtrack) // backtrack, frame2 will be discarded
      case (V_TYPE(), V_TYPE()) =>
        env.setLift(this)
        (frame1.push(V_TYPE(), Set(this)), Set())
    }
  }
}

case class InstrLNEG() extends Instruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(LNEG)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = ???

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
  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = ???
}

case class InstrISHR() extends Instruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(ISHR)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = ???

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
  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = ???
}

case class InstrIAND() extends Instruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(IAND)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = ???

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
  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = ???
}

case class InstrIOR() extends Instruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(IOR)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = ???

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = ???
}

case class InstrLSUB() extends Instruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(LSUB)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = ???

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = ???
}