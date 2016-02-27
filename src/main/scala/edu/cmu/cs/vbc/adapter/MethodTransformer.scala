package edu.cmu.cs.vbc.adapter

import edu.cmu.cs.vbc.instructions._
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree._
import org.objectweb.asm.Opcodes._

import scala.collection.immutable.TreeSet
import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer


object MethodTransformer {

  var variables: Array[Variable] = null

  val transformMethod = (cn: ClassNode, isLift: Boolean) => {
    println("Transforming Class: " + cn.name)
    for (i <- 0 until cn.methods.size()) {
      val m = cn.methods.get(i)
      println("\tMethod: " + m.name)
      val a = new MethodAnalyzer(cn.name, m)
      a.analyze()
      a.validate()
      val ordered = (TreeSet[Int]() ++ a.blocks).toArray :+ m.instructions.size()
      variables = getVariableArray(m)

      val blocks = (for (i <- 0 to ordered.length - 2) yield createBlock(m, ordered(i), ordered(i+1),a)).toArray
      val blocks2 = addFieldInit(blocks, cn.name, m.name)
      val exceptions: Array[String] = m.exceptions.asScala.toArray
      val newM = new MyMethodNode(
        m.access, m.name, m.desc, m.signature, exceptions, new CFG(blocks2.toList), isLift
      )

      val lb = getLocalVarListBuffer()
      val rewritten = rewrite(isLift, newM, lb)
      val env = createMethodEnv(isLift, rewritten, lb.toList)
      rewritten.transform(env)
      cn.methods.set(i, rewritten)
    }
  }

  def createMethodEnv(isLift: Boolean, mtd: MyMethodNode, lv: List[LocalVar]): MethodEnv = {
    if (isLift) new VMethodEnv(mtd, lv)
    else new MethodEnv(mtd, lv)
  }

  def rewrite(isLift: Boolean, mtd: MyMethodNode, lb: ListBuffer[LocalVar]): MyMethodNode =
    if (isLift) Rewrite.rewrite(mtd, lb) else mtd

  def getLocalVarListBuffer():ListBuffer[LocalVar] = {
    val lb = new ListBuffer[LocalVar]
    for (v <- variables.toList if v.isInstanceOf[LocalVar]) lb += v.asInstanceOf[LocalVar]
    lb
  }

  def getVariableArray(mn: MethodNode) = {
    val variables = new Array[Variable](mn.maxLocals)
    for (i <- 0 until mn.instructions.size()) {
      val inst = mn.instructions.get(i)
      inst match {
        case node: VarInsnNode =>
          val idx = node.`var`
          if (variables(idx) == null){
            if (idx == 0) variables(idx) = new Parameter(0)
            else variables(idx) = new LocalVar()
          }
        case _ =>
      }
    }
    variables
  }

  def createBlock(mn: MethodNode, start: Int, end: Int, a: MethodAnalyzer): Block = {
    val instrList = for (i <- start until end) yield transformBytecode(mn.instructions.get(i), a)
    new Block(removeNOP(instrList.toArray): _*)
  }

  def addFieldInit(blocks: Array[Block], cls: String, mtd: String): Array[Block] = {
    if (mtd == "<init>") {
      val lastBlock = blocks.last
      assert(lastBlock.instr.last.isReturnInstr, "last instr of <init> is not return")
      val ret = lastBlock.instr.last
      val newInstrs = lastBlock.instr.dropRight(1) :+ InstrINIT_FIELDS(cls) :+ ret
      blocks.dropRight(1) :+ new Block(newInstrs: _*)
    }
    else {
      blocks
    }
  }

  def removeNOP(instList: Array[Instruction]): Array[Instruction] = {
    for (i <- instList if !i.isInstanceOf[InstrNOP]) yield i
  }


  //TODO: a is only used to get block idx, using an object could avoid passing a around
  def transformBytecode(inst: AbstractInsnNode, a: MethodAnalyzer): Instruction = inst.getOpcode match {
    case NOP => UNKNOWN()
    case ACONST_NULL => UNKNOWN(ACONST_NULL)
    case ICONST_M1 => InstrICONST(-1)
    case ICONST_0 => InstrICONST(0)
    case ICONST_1 => InstrICONST(1)
    case ICONST_2 => InstrICONST(2)
    case ICONST_3 => InstrICONST(3)
    case ICONST_4 => InstrICONST(4)
    case ICONST_5 => InstrICONST(5)
    case LCONST_0 => UNKNOWN(LCONST_0)
    case LCONST_1 => UNKNOWN(LCONST_1)
    case FCONST_0 => UNKNOWN(FCONST_0)
    case FCONST_1 => UNKNOWN(FCONST_1)
    case FCONST_2 => UNKNOWN(FCONST_2)
    case DCONST_0 => UNKNOWN(DCONST_0)
    case DCONST_1 => UNKNOWN(DCONST_1)
    case BIPUSH => {
      val i = inst.asInstanceOf[IntInsnNode]
      InstrBIPUSH(i.operand)
    }
    case SIPUSH => {
      val i = inst.asInstanceOf[IntInsnNode]
      InstrSIPUSH(i.operand)
    }
    case LDC => {
      val i = inst.asInstanceOf[LdcInsnNode]
      InstrLDC(i.cst)
    }
    case ILOAD => {
      val i = inst.asInstanceOf[VarInsnNode]
      InstrILOAD(variables(i.`var`))
    }
    case LLOAD => UNKNOWN(LLOAD)
    case FLOAD => UNKNOWN(FLOAD)
    case DLOAD => UNKNOWN(DLOAD)
    case ALOAD => {
      val i = inst.asInstanceOf[VarInsnNode]
      InstrALOAD(variables(i.`var`))
    }
    case IALOAD => UNKNOWN(IALOAD)
    case LALOAD => UNKNOWN(LALOAD)
    case FALOAD => UNKNOWN(FALOAD)
    case DALOAD => UNKNOWN(DALOAD)
    case AALOAD => UNKNOWN(AALOAD)
    case BALOAD => UNKNOWN(BALOAD)
    case CALOAD => UNKNOWN(CALOAD)
    case SALOAD => UNKNOWN(SALOAD)
    case ISTORE => {
      val i = inst.asInstanceOf[VarInsnNode]
      InstrISTORE(variables(i.`var`))
    }
    case LSTORE => UNKNOWN(LSTORE)
    case FSTORE => UNKNOWN(FSTORE)
    case DSTORE => UNKNOWN(DSTORE)
    case ASTORE => {
      val i = inst.asInstanceOf[VarInsnNode]
      InstrASTORE(variables(i.`var`))
    }
    case IASTORE => UNKNOWN(IASTORE)
    case LASTORE => UNKNOWN(LASTORE)
    case FASTORE => UNKNOWN(FASTORE)
    case DASTORE => UNKNOWN(DASTORE)
    case AASTORE => UNKNOWN(AASTORE)
    case BASTORE => UNKNOWN(BASTORE)
    case CASTORE => UNKNOWN(CASTORE)
    case SASTORE => UNKNOWN(SASTORE)
    case POP => InstrPOP()
    case POP2 => UNKNOWN(POP2)
    case DUP => InstrDUP()
    case DUP_X1 => UNKNOWN(DUP_X1)
    case DUP_X2 => UNKNOWN(DUP_X2)
    case DUP2 => UNKNOWN(DUP2)
    case DUP2_X1 => UNKNOWN(DUP2_X1)
    case DUP2_X2 => UNKNOWN(DUP2_X2)
    case SWAP => UNKNOWN(SWAP)
    case IADD => InstrIADD()
    case LADD => UNKNOWN(LADD)
    case FADD => UNKNOWN(FADD)
    case DADD => UNKNOWN(DADD)
    case ISUB => InstrISUB()
    case LSUB => UNKNOWN(LSUB)
    case FSUB => UNKNOWN(FSUB)
    case DSUB => UNKNOWN(DSUB)
    case IMUL => InstrIMUL()
    case LMUL => UNKNOWN(LMUL)
    case FMUL => UNKNOWN(FMUL)
    case DMUL => UNKNOWN(DMUL)
    case IDIV => InstrIDIV()
    case LDIV => UNKNOWN(LDIV)
    case FDIV => UNKNOWN(FDIV)
    case DDIV => UNKNOWN(DDIV)
    case IREM => UNKNOWN(IREM)
    case LREM => UNKNOWN(LREM)
    case FREM => UNKNOWN(FREM)
    case DREM => UNKNOWN(DREM)
    case INEG => UNKNOWN(INEG)
    case LNEG => UNKNOWN(LNEG)
    case FNEG => UNKNOWN(FNEG)
    case DNEG => UNKNOWN(DNEG)
    case ISHL => UNKNOWN(ISHL)
    case LSHL => UNKNOWN(LSHL)
    case ISHR => UNKNOWN(ISHR)
    case LSHR => UNKNOWN(LSHR)
    case IUSHR => UNKNOWN(IUSHR)
    case LUSHR => UNKNOWN(LUSHR)
    case IAND => UNKNOWN(IAND)
    case LAND => UNKNOWN(LAND)
    case IOR => UNKNOWN(IOR)
    case LOR => UNKNOWN(LOR)
    case IXOR => UNKNOWN(IXOR)
    case LXOR => UNKNOWN(LXOR)
    case IINC => UNKNOWN(IINC)
    case I2L => UNKNOWN(I2L)
    case I2F => UNKNOWN(I2F)
    case I2D => UNKNOWN(I2D)
    case L2I => UNKNOWN(L2I)
    case L2F => UNKNOWN(L2F)
    case L2D => UNKNOWN(L2D)
    case F2I => UNKNOWN(F2I)
    case F2L => UNKNOWN(F2L)
    case F2D => UNKNOWN(F2D)
    case D2I => UNKNOWN(D2I)
    case D2L => UNKNOWN(D2L)
    case D2F => UNKNOWN(D2F)
    case I2B => UNKNOWN(I2B)
    case I2C => UNKNOWN(I2C)
    case I2S => UNKNOWN(I2S)
    case LCMP => UNKNOWN(LCMP)
    case FCMPL => UNKNOWN(FCMPL)
    case FCMPG => UNKNOWN(FCMPG)
    case DCMPL => UNKNOWN(DCMPL)
    case DCMPG => UNKNOWN(DCMPG)
    case IFEQ => {
      val insIFEQ = inst.asInstanceOf[JumpInsnNode]
      val label = insIFEQ.label
      InstrIFEQ(a.label2BlockIdx(label))
    }
    case IFNE => {
      val i = inst.asInstanceOf[JumpInsnNode]
      val label = i.label
      InstrIFNE(a.label2BlockIdx(label))
    }
    case IFLT => UNKNOWN(IFLT)
    case IFGE => {
      val i = inst.asInstanceOf[JumpInsnNode]
      InstrIFGE(a.label2BlockIdx(i.label))
    }
    case IFGT => {
      val i = inst.asInstanceOf[JumpInsnNode]
      InstrIFGT(a.label2BlockIdx(i.label))
    }
    case IFLE => UNKNOWN(IFLE)
    case IF_ICMPEQ => {
      val i = inst.asInstanceOf[JumpInsnNode]
      InstrIF_ICMPEQ(a.label2BlockIdx(i.label))
    }
    case IF_ICMPNE => UNKNOWN(IF_ICMPNE)
    case IF_ICMPLT => {
      val i = inst.asInstanceOf[JumpInsnNode]
      InstrIF_ICMPLT(a.label2BlockIdx(i.label))
    }
    case IF_ICMPGE => {
      val i = inst.asInstanceOf[JumpInsnNode]
      InstrIF_ICMPGE(a.label2BlockIdx(i.label))
    }
    case IF_ICMPGT => UNKNOWN(IF_ICMPGT)
    case IF_ICMPLE => UNKNOWN(IF_ICMPLE)
    case GOTO => {
      val i = inst.asInstanceOf[JumpInsnNode]
      InstrGOTO(a.label2BlockIdx(i.label))
    }
    case JSR => UNKNOWN(JSR)
    case RET => UNKNOWN(RET)
    case TABLESWITCH => UNKNOWN(TABLESWITCH)
    case LOOKUPSWITCH => UNKNOWN(LOOKUPSWITCH)
    case IRETURN => InstrIRETURN()
    case LRETURN => UNKNOWN(LRETURN)
    case FRETURN => UNKNOWN(FRETURN)
    case DRETURN => UNKNOWN(DRETURN)
    case ARETURN => UNKNOWN(ARETURN)
    case RETURN => InstrRETURN()
    case GETSTATIC => {
      val i = inst.asInstanceOf[FieldInsnNode]
      InstrGETSTATIC(i.owner, i.name, i.desc)
    }
    case PUTSTATIC => {
      val i = inst.asInstanceOf[FieldInsnNode]
      InstrPUTSTATIC(i.owner, i.name, i.desc)
    }
    case GETFIELD => {
      val i = inst.asInstanceOf[FieldInsnNode]
      InstrGETFIELD(i.owner, i.name, i.desc)
    }
    case PUTFIELD => {
      val i = inst.asInstanceOf[FieldInsnNode]
      InstrPUTFIELD(i.owner, i.name, i.desc)
    }
    case INVOKEVIRTUAL => {
      val i = inst.asInstanceOf[MethodInsnNode]
      InstrINVOKEVIRTUAL(i.owner, i.name, i.desc, i.itf)
    }
    case INVOKESPECIAL => {
      val i = inst.asInstanceOf[MethodInsnNode]
      InstrINVOKESPECIAL(i.owner, i.name, i.desc, i.itf)
    }
    case INVOKESTATIC => {
      val i = inst.asInstanceOf[MethodInsnNode]
      InstrINVOKESTATIC(i.owner, i.name, i.desc, i.itf)
    }
    case INVOKEINTERFACE => UNKNOWN(INVOKEINTERFACE)
    case INVOKEDYNAMIC => UNKNOWN(INVOKEDYNAMIC)
    case NEW => {
      val i = inst.asInstanceOf[TypeInsnNode]
      InstrNEW(i.desc)
    }
    case NEWARRAY => UNKNOWN(NEWARRAY)
    case ANEWARRAY => UNKNOWN(ANEWARRAY)
    case ARRAYLENGTH => UNKNOWN(ARRAYLENGTH)
    case ATHROW => UNKNOWN(ATHROW)
    case CHECKCAST => UNKNOWN(CHECKCAST)
    case INSTANCEOF => UNKNOWN(INSTANCEOF)
    case MONITORENTER => UNKNOWN(MONITORENTER)
    case MONITOREXIT => UNKNOWN(MONITOREXIT)
    case MULTIANEWARRAY => UNKNOWN(MULTIANEWARRAY)
    case IFNULL => UNKNOWN(IFNULL)
    case IFNONNULL => UNKNOWN(IFNONNULL)
    case -1 => InstrNOP() // special nodes in ASM such as LineNumberNode and LabelNode
    case _ => {
      UNKNOWN()
    }
  }
}
