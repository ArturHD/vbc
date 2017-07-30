package edu.cmu.cs.vbc.vbytecode.instructions

import edu.cmu.cs.vbc.analysis.VBCFrame.UpdatedFrame
import edu.cmu.cs.vbc.analysis._
import edu.cmu.cs.vbc.utils.LiftUtils._
import edu.cmu.cs.vbc.vbytecode._
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes._

trait StackInstructions extends Instruction {
  def isVOf64Bit(in: Instruction): Boolean = {
    val instrThatPuts64Bit = Set[Class[_]](
      // long
      classOf[InstrLCONST], classOf[InstrLLOAD], classOf[InstrLADD], classOf[InstrLSUB],
      classOf[InstrLDIV], classOf[InstrLNEG], classOf[InstrLUSHR], classOf[InstrLAND],
      classOf[InstrI2L],
      //        classOf[InstrLALOAD], classOf[InstrLMUL], classOf[InstrLREM], classOf[InstrLSHL],
      //        classOf[InstrLSHR], classOf[InstrLOR], classOf[InstrLXOR], classOf[InstrF2L],
      //        classOf[InstrD2L],
      // double
      classOf[InstrDLOAD]
      //        classOf[InstrDCONST], classOf[InstrDADD], classOf[InstrDSUB],
      //        classOf[InstrDDIV], classOf[InstrDNEG],
      //        classOf[InstrI2D],
      //        classOf[InstrDALOAD], classOf[InstrDMUL], classOf[InstrDREM],
      //        classOf[InstrF2D],
      //        classOf[InstrL2D],
    )
    val methodReturns64Bit: Boolean = in match {
      case i: InstrINVOKEINTERFACE => i.desc.getReturnType.exists(_.is64Bit)
      case i: InstrINVOKESPECIAL => i.desc.getReturnType.exists(_.is64Bit)
      case i: InstrINVOKESTATIC => i.desc.getReturnType.exists(_.is64Bit)
      case i: InstrINVOKEVIRTUAL => i.desc.getReturnType.exists(_.is64Bit)
      case _ => false
    }
    instrThatPuts64Bit.contains(in.getClass) || methodReturns64Bit
  }
}

/**
  * DUP instruction
  */
case class InstrDUP() extends StackInstructions {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(DUP)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    //TODO, when applied to LONG, use the int one instead of the 2byte one
    mv.visitInsn(DUP)
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): UpdatedFrame = {
    val (v, prev, frame1) = s.pop()
    val frame2 = frame1.push(v, prev)
    val frame3 = frame2.push(v, prev)
    (frame3, Set())
  }
}

/**
  * Duplicate the top two slots of the operand stack values.
  */
case class InstrDUP2() extends StackInstructions {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(DUP2)
  }

  /**
    * Lifting means the top value on operand stack is a category 2 value
    */
  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this))
      mv.visitInsn(DUP)
    else
      mv.visitInsn(DUP2)
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): UpdatedFrame = {
    val (v, prev, frame) = s.pop()
    if (v == LONG_TYPE() || v == DOUBLE_TYPE() || (v == V_TYPE() && isVOf64Bit(prev.head))) {
      if (v == V_TYPE()) env.setLift(this)
      (frame.push(v, prev).push(v, prev), Set())
    }
    else {
      val (v2, prev2, frame2) = frame.pop()
      (frame2.push(v2, prev2).push(v, prev).push(v2, prev2).push(v, prev), Set())
    }
  }

  override def doBacktrack(env: VMethodEnv): Unit = {} // do nothing
}

/**
  * Duplicate the top operand stack value and insert three slots down.
  *
  * The last two slots could be of type long.
  */
case class InstrDUP_X2() extends StackInstructions {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(DUP_X2)
  }

  /**
    * Lifting means the last two slots form a long value
    */
  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      mv.visitInsn(DUP_X1)
    }
    else {
      mv.visitInsn(DUP_X2)
    }
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): UpdatedFrame = {
    val (v1, prev1, frame1) = s.pop()
    val (v2, prev2, frame2) = frame1.pop()
    if (v2 == LONG_TYPE() || v2 == DOUBLE_TYPE()) {
      (frame2.push(v1, prev1).push(v2, prev2).push(v1, prev1), Set())
    }
    else if (v2 == V_TYPE() && isVOf64Bit(prev2.head)) {
      env.setLift(this)
      (frame2.push(v1, prev1).push(v2, prev2).push(v1, prev1), Set())
    }
    else {
      val (v3, prev3, frame3) = frame2.pop()
      (frame3.push(v1, prev1).push(v3, prev3).push(v2, prev2).push(v1, prev1), Set())
    }
  }

  override def doBacktrack(env: VMethodEnv): Unit = {} // do nothing
}

/**
  * Duplicate the top two operand stack slots and insert three slots down
  */
case class InstrDUP2_X1() extends StackInstructions {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(DUP2_X1)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    ???
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): UpdatedFrame = {
    ???
  }
}


/**
  * POP instruction
  */
case class InstrPOP() extends StackInstructions {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(POP)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    mv.visitInsn(POP)
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): UpdatedFrame = {
    val (v, prev, newFrame) = s.pop()
    (newFrame, Set())
  }
}


/**
  * Push byte
  *
  * @param value
  */
case class InstrBIPUSH(value: Int) extends Instruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitIntInsn(BIPUSH, value)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      pushConstant(mv, value)
      mv.visitMethodInsn(INVOKESTATIC, IntClass, "valueOf", s"(I)$IntType", false)
      callVCreateOne(mv, (m) => loadCurrentCtx(m, env, block))
    }
    else
      toByteCode(mv, env, block)
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): UpdatedFrame = {
    val newFrame =
      if (env.shouldLiftInstr(this))
        s.push(V_TYPE(), Set(this))
      else
        s.push(INT_TYPE(), Set(this))
    (newFrame, Set())
  }
}


/**
  * SIPUSH: Push short
  *
  * @param value
  */
case class InstrSIPUSH(value: Int) extends Instruction {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitIntInsn(SIPUSH, value)
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    if (env.shouldLiftInstr(this)) {
      mv.visitIntInsn(SIPUSH, value)
      mv.visitMethodInsn(
        INVOKESTATIC,
        IntClass,
        "valueOf",
        genSign("I", TypeDesc.getInt.toModel),
        false
      )
      callVCreateOne(mv, (m) => loadCurrentCtx(m, env, block))
    }
    else
      toByteCode(mv, env, block)
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): UpdatedFrame = {
    val newFrame =
      if (env.shouldLiftInstr(this))
        s.push(V_TYPE(), Set(this))
      else
        s.push(INT_TYPE(), Set(this))
    (newFrame, Set())
  }
}

/**
  * Duplicate the top operand stack value and insert two values down
  *
  * Operand stack: ..., value2, value1 -> ..., value1, value2, value1
  */
case class InstrDUP_X1() extends StackInstructions {
  override def toByteCode(mv: MethodVisitor, env: MethodEnv, block: Block): Unit = {
    mv.visitInsn(DUP_X1)
  }

  override def updateStack(s: VBCFrame, env: VMethodEnv): (VBCFrame, Set[Instruction]) = {
    val (v1, prev1, frame1) = s.pop()
    val (v2, prev2, frame2) = frame1.pop()
    val frame3 = frame2.push(v1, prev1)
    val frame4 = frame3.push(v2, prev1)
    val frame5 = frame4.push(v1, prev1)
    (frame5, Set())
  }

  override def toVByteCode(mv: MethodVisitor, env: VMethodEnv, block: Block): Unit = {
    mv.visitInsn(DUP_X1)
  }
}
