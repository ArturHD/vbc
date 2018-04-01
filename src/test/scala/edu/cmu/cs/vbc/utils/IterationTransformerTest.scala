package edu.cmu.cs.vbc.utils

import edu.cmu.cs.vbc.loader.Loader
import edu.cmu.cs.vbc.vbytecode.instructions._
import edu.cmu.cs.vbc.vbytecode._
import org.objectweb.asm.tree._
import org.objectweb.asm._
import org.objectweb.asm.Opcodes._
import org.scalatest.{FunSuite, Matchers}

import scala.collection.JavaConversions._
import PartialFunction.cond
import edu.cmu.cs.vbc.utils.LiftUtils.{fexprclassname, fexprclasstype, vclassname, vclasstype}

class IterationTransformerTest extends FunSuite with Matchers {
  val objectClassType = "Ljava/lang/Object;"

  def equal(a: Block, b: Block): Boolean = {
    // No value equality for instrs by design, so this is an ugly workaround
    val aStr = a.instr.toList.toString
    val bStr = b.instr.toList.toString
    if (aStr == bStr) {
      true
    } else {
      print("Left:\t" + aStr + "\nRight:\t" + bStr)
      false
    }
  }
  def equal(a: List[Block], b: List[Block]): Boolean = {
    val sizeEqual = a.size == b.size
    val blocksEqual = a.zip(b).foldRight(true)((blocks: (Block, Block), matchSoFar: Boolean) => matchSoFar && equal(blocks._1, blocks._2))
    sizeEqual && blocksEqual
  }
  def equal(a: CFG, b: CFG): Boolean = equal(a.blocks, b.blocks)

  val cfg_1loop = CFG(List(
    Block(InstrLDC("orig 1"), InstrPOP(), InstrDUP(), InstrGOTO(1)), // 0
    Block(InstrLDC("orig 2"), InstrDUP(), InstrICONST(1), InstrGOTO(2)), // 1
    Block(InstrLDC("orig 3"), InstrSWAP(), InstrPOP(), // 2
      InstrINVOKEVIRTUAL(Owner("List"), MethodName("iterator"), MethodDesc("()Ljava_util_Iterator;"), true),
      InstrDUP(), InstrGOTO(3)),
    Block(InstrLDC("orig 4"), InstrDUP(), InstrPOP(), InstrGOTO(4)), // 3: loop entry
    Block(InstrLDC("orig 5"), // 4
      InstrINVOKEINTERFACE(Owner("Iterator"), MethodName("next"), MethodDesc("()Ljava_util_object;"), true),
      InstrDUP(), InstrPOP(), InstrGOTO(5)),
    Block(InstrLDC("orig 6"), InstrDUP(), InstrPOP(), InstrIFEQ(3)), // 5: loop ^
    Block(InstrLDC("orig 7"), InstrDUP(), InstrPOP(), InstrGOTO(7)), // 6
    Block(InstrLDC("orig 8"), InstrDUP(), InstrPOP(), InstrGOTO(7)) // 7
  ))
  val cfg_2loop = CFG(List(
    Block(InstrLDC("orig 1"), InstrPOP(), InstrDUP(), InstrGOTO(1)), // 0
    Block(InstrLDC("orig 2"), InstrDUP(), InstrICONST(1), InstrGOTO(2)), // 1
    Block(InstrLDC("orig 3"), InstrSWAP(), InstrPOP(), // 2
      InstrINVOKEVIRTUAL(Owner("List"), MethodName("iterator"), MethodDesc("()Ljava_util_Iterator;"), true),
      InstrDUP(), InstrGOTO(3)),
    Block(InstrLDC("orig 4"), InstrDUP(), InstrPOP(), InstrGOTO(4)), // 3: loop entry
    Block(InstrLDC("orig 5"), // 4
      InstrINVOKEINTERFACE(Owner("Iterator"), MethodName("next"), MethodDesc("()Ljava_util_object;"), true),
      InstrDUP(), InstrPOP(), InstrGOTO(5)),
    Block(InstrLDC("orig 6"), InstrDUP(), InstrPOP(), InstrIFEQ(3)), // 5: loop ^
    Block(InstrLDC("orig 7"), InstrDUP(), // 6
      InstrINVOKEVIRTUAL(Owner("List"), MethodName("iterator"), MethodDesc("()Ljava_util_Iterator;"), true),
      InstrPOP(), InstrGOTO(7)),
    Block(InstrLDC("orig 8"), InstrDUP(), InstrPOP(), InstrGOTO(8)), // 7: loop entry
    Block(InstrLDC("orig 9"), // 8
      InstrINVOKEINTERFACE(Owner("Iterator"), MethodName("next"), MethodDesc("()Ljava_util_object;"), true),
      InstrDUP(), InstrPOP(), InstrGOTO(9)),
    Block(InstrLDC("orig 10"), InstrDUP(), InstrPOP(), InstrIFEQ(11)), // 9
    Block(InstrLDC("orig 11"), InstrDUP(), InstrPOP(), InstrGOTO(7)), // 10: loop ^
    Block(InstrLDC("orig 12"), InstrDUP(), InstrPOP(), InstrGOTO(11)) // 11
    ))

  val valid_cfg_2loop = CFG(List(
    Block(InstrLDC("orig 1"), InstrPOP(), InstrICONST(1), InstrDUP(), InstrGOTO(1)), // 0
    Block(InstrLDC("orig 2"), InstrPOP(), InstrPOP(), InstrICONST(2), InstrGOTO(2)), // 1
    Block(InstrLDC("orig 3"), InstrPOP(), InstrSWAP(), InstrPOP(), // 2
      InstrINVOKEVIRTUAL(Owner("List"), MethodName("iterator"), MethodDesc("()Ljava_util_Iterator;"), true),
      InstrPOP(), InstrGOTO(3)),
    Block(InstrLDC("orig 4"), InstrPOP(), InstrICONST(1), InstrGOTO(4)), // 3: loop entry
    Block(InstrLDC("orig 5"), InstrPOP(), // 4
      InstrINVOKEINTERFACE(Owner("Iterator"), MethodName("next"), MethodDesc("()Ljava_util_object;"), true),
      InstrDUP(), InstrPOP(), InstrIFEQ(6)),
    Block(InstrLDC("orig 6"), InstrPOP(), InstrGOTO(3)), // 5: loop ^
    Block(InstrLDC("orig 7"), InstrPOP(), InstrICONST(3), // 6
      InstrINVOKEVIRTUAL(Owner("List"), MethodName("iterator"), MethodDesc("()Ljava_util_Iterator;"), true),
      InstrPOP(), InstrGOTO(7)),
    Block(InstrLDC("orig 8"), InstrPOP(), InstrICONST(7), InstrGOTO(8)), // 7: loop entry
    Block(InstrLDC("orig 9"), InstrPOP(), // 8
      InstrINVOKEINTERFACE(Owner("Iterator"), MethodName("next"), MethodDesc("()Ljava_util_object;"), true),
      InstrDUP(), InstrPOP(), InstrGOTO(9)),
    Block(InstrLDC("orig 10"), InstrPOP(), InstrIFEQ(11)), // 9
    Block(InstrLDC("orig 11"), InstrPOP(), InstrICONST(8), InstrPOP(), InstrGOTO(7)), // 10: loop ^
    Block(InstrLDC("orig 12"), InstrPOP(), InstrICONST(9), InstrPOP(), InstrRETURN()) // 11
  ))

  val real_cfg_2loop = {
    val llist_name = Owner("java/util/LinkedList")
    val list_name = Owner("java/util/List")
    val it_name = Owner("java/util/Iterator")
    val it_desc = MethodDesc("()Ljava/util/Iterator;")
    val int_name = Owner("java/lang/Integer")

    def instrLListInit = InstrINVOKESPECIAL(llist_name, MethodName("<init>"), MethodDesc("()V"), false)
    def instrListIterator = InstrINVOKEINTERFACE(list_name, MethodName("iterator"), it_desc, true)
    def instrHasNext = InstrINVOKEINTERFACE(it_name, MethodName("hasNext"), MethodDesc("()Z"), true)
    def instrNext = InstrINVOKEINTERFACE(it_name, MethodName("next"), MethodDesc("()Ljava/lang/Object;"), true)
    def instrIntValue = InstrINVOKEVIRTUAL(int_name, MethodName("intValue"), MethodDesc("()I"), false)
    def instrValueOf = InstrINVOKESTATIC(int_name, MethodName("valueOf"), MethodDesc("(I)Ljava/lang/Integer;"), false)

    val var1 = new LocalVar("1", "Ljava/util/object;")
    val var2 = new LocalVar("2", "Ljava/util/object;")
    val var3 = new LocalVar("3", "Ljava/util/object;")
    val var4 = new LocalVar("4", "Ljava/util/object;")
    val l0 = 0
    val l1 = 1
    val l2 = 2
    val l3 = 3
    val l4 = 7
    val l5 = 5
    val l6 = 6
    val l7 = 8
    val l8 = 12

    CFG(List(
      Block(InstrNEW(llist_name), // 0 = l0
        InstrDUP(),
        instrLListInit,
        InstrASTORE(var1)),
      Block(InstrNEW(llist_name),  // 1 = l1
        InstrDUP(),
        instrLListInit,
        InstrASTORE(var2)),
      Block(InstrALOAD(var1),  // 2 = l2
        instrListIterator,
        InstrASTORE(var3)),
      Block(InstrALOAD(var3), // 3 = l3 : loop 1 entry
        instrHasNext,
        InstrIFEQ(l4)),
      Block(InstrALOAD(var3), // 4 = no label
        instrNext,
        InstrCHECKCAST(int_name),
        InstrASTORE(var4)),
      Block(InstrALOAD(var4), // 5 = l5
        instrIntValue,
        InstrICONST(1),
        InstrIADD(),
        instrValueOf,
        InstrASTORE(var4)),
      Block(InstrGOTO(l3)), // 6 = l6 : loop 1 ^
      Block(InstrALOAD(var2), // 7 = l4
        instrListIterator,
        InstrASTORE(var3)),
      Block(InstrALOAD(var3), // 8 = l7 : loop 2 entry
        instrHasNext,
        InstrIFEQ(l8)),
      Block(InstrALOAD(var3), // 9 = no label
        instrNext,
        InstrCHECKCAST(int_name),
        InstrASTORE(var4)),
      Block(InstrALOAD(var4), // 10 = l9
        instrIntValue,
        InstrICONST(1),
        InstrIADD(),
        instrValueOf,
        InstrASTORE(var4)),
      Block(InstrGOTO(l7)), // 11 = l10 : loop 2 ^
      Block(InstrRETURN()) // 12 = l8
    ))
  }

  val real_cfg_2loop2 = {
    var mv = new MethodNode(ACC_PUBLIC, "test", "()V", null, null)
    mv.visitCode
    val labels = List.range(0, 12).map(l => new Label("L" + l))
    mv.visitLabel(labels(0))
    mv.visitLineNumber(73, labels(0))
    mv.visitTypeInsn(NEW, "java/util/LinkedList")
    mv.visitInsn(DUP)
    mv.visitMethodInsn(INVOKESPECIAL, "java/util/LinkedList", "<init>", "()V", false)
    mv.visitVarInsn(ASTORE, 1)
    mv.visitLabel(labels(1))
    mv.visitLineNumber(74, labels(1))
    mv.visitTypeInsn(NEW, "java/util/LinkedList")
    mv.visitInsn(DUP)
    mv.visitMethodInsn(INVOKESPECIAL, "java/util/LinkedList", "<init>", "()V", false)
    mv.visitVarInsn(ASTORE, 2)
    mv.visitLabel(labels(2))
    mv.visitLineNumber(75, labels(2))
    mv.visitVarInsn(ALOAD, 1)
    mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "iterator", "()Ljava/util/Iterator;", true)
    mv.visitVarInsn(ASTORE, 3)
    mv.visitLabel(labels(3))
    mv.visitFrame(Opcodes.F_APPEND, 3, Array[AnyRef]("java/util/List", "java/util/List", "java/util/Iterator"), 0, null)
    mv.visitVarInsn(ALOAD, 3)
    mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z", true)
    mv.visitJumpInsn(IFEQ, labels(4))
    mv.visitVarInsn(ALOAD, 3)
    mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;", true)
    mv.visitTypeInsn(CHECKCAST, "java/lang/Integer")
    mv.visitVarInsn(ASTORE, 4)
    mv.visitLabel(labels(5))
    mv.visitLineNumber(76, labels(5))
    mv.visitVarInsn(ALOAD, 4)
    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false)
    mv.visitInsn(ICONST_1)
    mv.visitInsn(IADD)
    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false)
    mv.visitVarInsn(ASTORE, 4)
    mv.visitLabel(labels(6))
    mv.visitLineNumber(77, labels(6))
    mv.visitJumpInsn(GOTO, labels(3))
    mv.visitLabel(labels(4))
    mv.visitLineNumber(78, labels(4))
    mv.visitFrame(Opcodes.F_CHOP, 1, null, 0, null)
    mv.visitVarInsn(ALOAD, 2)
    mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "iterator", "()Ljava/util/Iterator;", true)
    mv.visitVarInsn(ASTORE, 3)
    mv.visitLabel(labels(7))
    mv.visitFrame(Opcodes.F_APPEND, 1, Array[AnyRef]("java/util/Iterator"), 0, null)
    mv.visitVarInsn(ALOAD, 3)
    mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z", true)
    mv.visitJumpInsn(IFEQ, labels(8))
    mv.visitVarInsn(ALOAD, 3)
    mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;", true)
    mv.visitTypeInsn(CHECKCAST, "java/lang/Integer")
    mv.visitVarInsn(ASTORE, 4)
    mv.visitLabel(labels(9))
    mv.visitLineNumber(79, labels(9))
    mv.visitVarInsn(ALOAD, 4)
    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false)
    mv.visitInsn(ICONST_1)
    mv.visitInsn(IADD)
    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false)
    mv.visitVarInsn(ASTORE, 4)
    mv.visitLabel(labels(10))
    mv.visitLineNumber(80, labels(10))
    mv.visitJumpInsn(GOTO, labels(7))
    mv.visitLabel(labels(8))
    mv.visitLineNumber(81, labels(8))
    mv.visitFrame(Opcodes.F_CHOP, 1, null, 0, null)
    mv.visitInsn(RETURN)
    mv.visitLabel(labels(11))
    mv.visitLocalVariable("el", "Ljava/lang/Integer;", null, labels(5), labels(6), 4)
    mv.visitLocalVariable("el", "Ljava/lang/Integer;", null, labels(9), labels(10), 4)
    mv.visitLocalVariable("this", "Ledu/cmu/cs/vbc/prog/IterationExample;", null, labels(0), labels(11), 0)
    mv.visitLocalVariable("l1", "Ljava/util/List;", "Ljava/util/List<Ljava/lang/Integer;>;", labels(1), labels(11), 1)
    mv.visitLocalVariable("l2", "Ljava/util/List;", "Ljava/util/List<Ljava/lang/Integer;>;", labels(2), labels(11), 2)
    mv.visitMaxs(2, 5)
    mv.visitEnd()

    val loader = new Loader()
    loader.adaptMethod(Owner("testclass"), mv).body
  }










  // ===== insertElementSatisfiabilityConditional =====
  test("insertElementSatisfiabilityConditional works for one loop") {
    val itt = new IterationTransformer()
    val loopEntry = cfg_1loop.blocks(3)
    val loopBody = Set(cfg_1loop.blocks(4), cfg_1loop.blocks(5))
    val (newCFG, blockUpdates) = itt.insertElementSatisfiabilityConditional(cfg_1loop, List(Loop(loopEntry, loopBody)))
    assert(equal(newCFG, CFG(List(
      Block(InstrLDC("orig 1"), InstrPOP(), InstrDUP(), InstrGOTO(1)), // 0
      Block(InstrLDC("orig 2"), InstrDUP(), InstrICONST(1), InstrGOTO(2)), // 1
      Block(InstrLDC("orig 3"), InstrSWAP(), InstrPOP(), // 2
        InstrINVOKEVIRTUAL(Owner("List"), MethodName("iterator"), MethodDesc("()Ljava_util_Iterator;"), true),
        InstrDUP(), InstrGOTO(3)),
      Block(InstrLDC("orig 4"), InstrDUP(), InstrPOP(), InstrGOTO(4)), // 3: loop entry
      Block(InstrLDC("orig 5"), // 4
        InstrINVOKEINTERFACE(Owner("Iterator"), MethodName("next"), MethodDesc("()Ljava_util_object;"), true),
        InstrIFEQ(7)),
      Block(InstrDUP(), InstrPOP(), InstrGOTO(6)), // 5: second split-half
      Block(InstrLDC("orig 6"), InstrDUP(), InstrPOP(), InstrIFEQ(3)), // 6: loop ^
      Block(InstrGOTO(3)), // 7
      Block(InstrLDC("orig 7"), InstrDUP(), InstrPOP(), InstrGOTO(9)), // 8
      Block(InstrLDC("orig 8"), InstrDUP(), InstrPOP(), InstrGOTO(9)) // 9
    ))), "Block splitting doesn't work as expected")
  }

  test("insertElementSatisfiabilityConditional works for multiple loops") {
    val itt = new IterationTransformer()
    val loop1Entry = cfg_2loop.blocks(3)
    val loop1Body = Set(cfg_2loop.blocks(4), cfg_2loop.blocks(5))
    val loop2Entry = cfg_2loop.blocks(7)
    val loop2Body = Set(cfg_2loop.blocks(8), cfg_2loop.blocks(9), cfg_2loop.blocks(10))
    val (newCFG, blockUpdates) = itt.insertElementSatisfiabilityConditional(cfg_2loop,
      List(Loop(loop1Entry, loop1Body), Loop(loop2Entry, loop2Body)))
    assert(equal(newCFG, CFG(List(
      Block(InstrLDC("orig 1"), InstrPOP(), InstrDUP(), InstrGOTO(1)), // 0
      Block(InstrLDC("orig 2"), InstrDUP(), InstrICONST(1), InstrGOTO(2)), // 1
      Block(InstrLDC("orig 3"), InstrSWAP(), InstrPOP(), // 2
        InstrINVOKEVIRTUAL(Owner("List"), MethodName("iterator"), MethodDesc("()Ljava_util_Iterator;"), true),
        InstrDUP(), InstrGOTO(3)),
      Block(InstrLDC("orig 4"), InstrDUP(), InstrPOP(), InstrGOTO(4)), // 3: loop entry
      Block(InstrLDC("orig 5"), // 4
        InstrINVOKEINTERFACE(Owner("Iterator"), MethodName("next"), MethodDesc("()Ljava_util_object;"), true),
        InstrIFEQ(7)),
      Block(InstrDUP(), InstrPOP(), InstrGOTO(6)), // 5: second split-half
      Block(InstrLDC("orig 6"), InstrDUP(), InstrPOP(), InstrIFEQ(3)), // 6: loop ^
      Block(InstrGOTO(3)), // 7
      Block(InstrLDC("orig 7"), InstrDUP(), // 8
        InstrINVOKEVIRTUAL(Owner("List"), MethodName("iterator"), MethodDesc("()Ljava_util_Iterator;"), true),
        InstrPOP(), InstrGOTO(9)),
      Block(InstrLDC("orig 8"), InstrDUP(), InstrPOP(), InstrGOTO(10)), // 9: loop entry
      Block(InstrLDC("orig 9"), // 10
        InstrINVOKEINTERFACE(Owner("Iterator"), MethodName("next"), MethodDesc("()Ljava_util_object;"), true),
        InstrIFEQ(14)),
      Block(List(InstrDUP(), InstrPOP(), InstrGOTO(12)), Nil), // 11: second split-half
      Block(InstrLDC("orig 10"), InstrDUP(), InstrPOP(), InstrIFEQ(15)), // 12
      Block(InstrLDC("orig 11"), InstrDUP(), InstrPOP(), InstrGOTO(14)), // 13: loop ^
      Block(InstrGOTO(9)), // 14
      Block(InstrLDC("orig 12"), InstrDUP(), InstrPOP(), InstrGOTO(15)) // 15
    ))), "Block splitting doesn't work as expected")
  }

  test("insertElementSatisfiabilityConditional returns valid map to updated block indices") {
    val itt = new IterationTransformer()
    val loop1Entry = cfg_2loop.blocks(3)
    val loop1Body = Set(cfg_2loop.blocks(4), cfg_2loop.blocks(5))
    val loop1 = Loop(loop1Entry, loop1Body)
    val loop2Entry = cfg_2loop.blocks(7)
    val loop2Body = Set(cfg_2loop.blocks(8), cfg_2loop.blocks(9), cfg_2loop.blocks(10))
    val loop2 = Loop(loop2Entry, loop2Body)
    val (newCFG, blockUpdates) = itt.insertElementSatisfiabilityConditional(cfg_2loop,
      List(loop1, loop2))
    assert(blockUpdates(0) == 0)
    assert(blockUpdates(1) == 1)
    assert(blockUpdates(2) == 2)
    assert(blockUpdates(3) == 3)
    assert(blockUpdates(4) == 4)
    assert(blockUpdates(5) == 6)
    assert(blockUpdates(6) == 8)
    assert(blockUpdates(7) == 9)
    assert(blockUpdates(8) == 10)
    assert(blockUpdates(9) == 12)
    assert(blockUpdates(10) == 13)
    assert(blockUpdates(11) == 15)
  }

  test("insertElementSatisfiabilityConditional updates loop structure correctly") {
    val itt = new IterationTransformer()
    val loop1Entry = valid_cfg_2loop.blocks(3)
    val loop1Body = Set(valid_cfg_2loop.blocks(4), valid_cfg_2loop.blocks(5))
    val loop2Entry = valid_cfg_2loop.blocks(7)
    val loop2Body = Set(valid_cfg_2loop.blocks(8), valid_cfg_2loop.blocks(9), valid_cfg_2loop.blocks(10))
    val (newCFG, blockUpdates) = itt.insertElementSatisfiabilityConditional(valid_cfg_2loop,
      List(Loop(loop1Entry, loop1Body), Loop(loop2Entry, loop2Body)))

    assert(newCFG.blocks.size == valid_cfg_2loop.blocks.size + 4)
    assert(equal(newCFG, CFG(List(
      Block(InstrLDC("orig 1"), InstrPOP(), InstrICONST(1), InstrDUP(), InstrGOTO(1)), // 0
      Block(InstrLDC("orig 2"), InstrPOP(), InstrPOP(), InstrICONST(2), InstrGOTO(2)), // 1
      Block(InstrLDC("orig 3"), InstrPOP(), InstrSWAP(), InstrPOP(), // 2
        InstrINVOKEVIRTUAL(Owner("List"), MethodName("iterator"), MethodDesc("()Ljava_util_Iterator;"), true),
        InstrPOP(), InstrGOTO(3)),
      Block(InstrLDC("orig 4"), InstrPOP(), InstrICONST(1), InstrGOTO(4)), // 3: loop entry
      Block(InstrLDC("orig 5"), InstrPOP(), // 4
        InstrINVOKEINTERFACE(Owner("Iterator"), MethodName("next"), MethodDesc("()Ljava_util_object;"), true),
        InstrIFEQ(7)),
      Block(InstrDUP(), InstrPOP(), InstrIFEQ(8)), // 5
      Block(InstrLDC("orig 6"), InstrPOP(), InstrGOTO(7)), // 6: loop ^
      Block(InstrGOTO(3)), // 7
      Block(InstrLDC("orig 7"), InstrPOP(), InstrICONST(3), // 8
        InstrINVOKEVIRTUAL(Owner("List"), MethodName("iterator"), MethodDesc("()Ljava_util_Iterator;"), true),
        InstrPOP(), InstrGOTO(9)),
      Block(InstrLDC("orig 8"), InstrPOP(), InstrICONST(7), InstrGOTO(10)), // 9: loop entry
      Block(InstrLDC("orig 9"), InstrPOP(), // 10
        InstrINVOKEINTERFACE(Owner("Iterator"), MethodName("next"), MethodDesc("()Ljava_util_object;"), true),
        InstrIFEQ(14)),
      Block(InstrDUP(), InstrPOP(), InstrGOTO(12)), // 11
      Block(InstrLDC("orig 10"), InstrPOP(), InstrIFEQ(15)), // 12
      Block(InstrLDC("orig 11"), InstrPOP(), InstrICONST(8), InstrPOP(), InstrGOTO(14)), // 13: loop ^
      Block(InstrGOTO(9)), // 14
      Block(InstrLDC("orig 12"), InstrPOP(), InstrICONST(9), InstrPOP(), InstrRETURN()) // 15
    ))))
  }

  // ===== createSimplifyLambda =====
  test("createSimplifyLambda creates lambda") {
    val itt = new IterationTransformer()
    val cw = new MyClassWriter(ClassWriter.COMPUTE_FRAMES)

    val lambdaName = "lambda$INVOKEVIRTUAL$simplifyCtxList"
    val lambdaDesc = s"(${itt.ctxListClassType})V"

    itt.createSimplifyLambda(cw, lambdaName, lambdaDesc)

    val cr = new ClassReader(cw.toByteArray)
    val classNode = new ClassNode(ASM5)
    cr.accept(classNode, 0)
    def isTheLambda(mn: MethodNode) = mn.name == lambdaName && mn.desc == lambdaDesc

    assert(classNode.methods.toList.exists(isTheLambda))

    val insns = new InsnList()
    insns.add(new VarInsnNode(ALOAD, 0))
    val invOwner = itt.ctxListClassName
    val invName = "simplify____V"
    val invDesc = "()V"
    insns.add(new MethodInsnNode(INVOKEVIRTUAL, invOwner, invName, invDesc, false))
    insns.add(new InsnNode(RETURN))

    for { mn <- classNode.methods.find(isTheLambda) }
      yield {
        assert(cond(mn.instructions.get(0)) {
          case v: VarInsnNode => v.getOpcode == ALOAD && v.`var` == 0
        })
        assert(cond(mn.instructions.get(1)) {
          case m: MethodInsnNode =>
            m.getOpcode == INVOKEVIRTUAL && m.owner == invOwner && m.name == invName && m.desc == invDesc
        })
        assert(cond(mn.instructions.get(2)) {
          case r: InsnNode => r.getOpcode == RETURN
        })
      }
    // todo: figure out checking JVM compliance of lambda inserted
  }

//
  // ===== transformLoopPredecessor =====
  test("transformLoopPredecessor works") {
    val className = "testclass"
    val vbcMtdNode = VBCMethodNode(0, "test", "()V", None, List.empty, valid_cfg_2loop)
    val vbcClazz = VBCClassNode(0, 0, className, None, "java/util/Object", List.empty, List.empty, List(vbcMtdNode))
    val env = new VMethodEnv(vbcClazz, vbcMtdNode)

    val itt = new IterationTransformer()
    val cw = new MyClassWriter(ClassWriter.COMPUTE_FRAMES)

    val loopPredecessor = valid_cfg_2loop.blocks(2)

    def valid_cfg_2loop_insnIdx(insn: Instruction) = valid_cfg_2loop.blocks.flatMap(_.instr).indexWhere(_ eq insn)
    val blockTrans = itt.transformLoopPredecessor(loopPredecessor, env, cw, valid_cfg_2loop_insnIdx)

    val lambdaName = "lambda$INVOKEVIRTUAL$simplifyCtxList"
    val lambdaDesc = s"(${itt.ctxListClassType})V"
    val consumerName = "java/util/function/Consumer"
    val consumerType = s"L$consumerName;"
    val ctxListClassName = "model/java/util/CtxList"
    val ctxListClassType = s"L$ctxListClassName;"

    assert(blockTrans.newVars.isEmpty)
    assert(equal(blockTrans.newBlocks, List(Block(
      InstrLDC("orig 3"),
      InstrPOP(),
      InstrSWAP(),
      InstrPOP(), // 13

      // --- Begin inserted ---
      InstrDUP(),

      // For using simplify call mapped over V wrapping CtxList
//      InstrINVOKEDYNAMIC(Owner(consumerName), MethodName("accept"), MethodDesc(s"()$consumerType"),
//        new Handle(H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory",
//          "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;"),
//        Type.getType("(Ljava/lang/Object;)V"),
//        new Handle(H_INVOKESTATIC, className, lambdaName, lambdaDesc),
//        Type.getType(lambdaDesc)),
//      InstrINVOKEINTERFACE(Owner(vclassname), MethodName("foreach"), MethodDesc(s"($consumerType)V"), true),

      // For using simplify call after getOne
      InstrINVOKEINTERFACE(Owner(vclassname), MethodName("getOne"), MethodDesc("()Ljava/lang/Object;"), true),
      InstrCHECKCAST(Owner(ctxListClassName)),
      InstrINVOKEVIRTUAL(Owner(ctxListClassName), MethodName("simplify____V"), MethodDesc("()V"), false),
      // --- End inserted ---

      InstrINVOKEVIRTUAL(Owner("List"), MethodName("iterator"), MethodDesc("()Ljava_util_Iterator;"), true),
      InstrPOP(),
      InstrGOTO(3)
    ))))
    val iteratorIndex = 14
    val newInsns = 3
    assert(blockTrans.newInsnIndeces == List.range(iteratorIndex, iteratorIndex + newInsns + 1))
  }



  // ===== transformBodyStartBlock =====
  test("transformBodyStartBlock works") {
    val itt = new IterationTransformer()

    val loop1Entry = valid_cfg_2loop.blocks(3)
    val loop1Body = Set(valid_cfg_2loop.blocks(4), valid_cfg_2loop.blocks(5))
    val loop2Entry = valid_cfg_2loop.blocks(7)
    val loop2Body = Set(valid_cfg_2loop.blocks(8), valid_cfg_2loop.blocks(9), valid_cfg_2loop.blocks(10))
    val (newCFG, blockUpdates) = itt.insertElementSatisfiabilityConditional(valid_cfg_2loop,
      List(Loop(loop1Entry, loop1Body), Loop(loop2Entry, loop2Body)))

    val bodyStartBlock = newCFG.blocks(blockUpdates(4))
    val lastBodyBlockIdx = blockUpdates(5)
    def newCFG_insnIdx(insn: Instruction) = newCFG.blocks.flatMap(_.instr).indexWhere(_ eq insn)
    val elementOneVar = new LocalVar("element$one$var", vclasstype)
    val blockTrans = itt.transformBodyStartBlock(bodyStartBlock, newCFG_insnIdx, elementOneVar)

    assert(blockTrans.newVars.size == 1)
    assert(equal(blockTrans.newBlocks, List(Block(
      InstrLDC("orig 5"),
      InstrPOP(),
      InstrINVOKEINTERFACE(Owner("Iterator"), MethodName("next"), MethodDesc("()Ljava_util_object;"), true),

      // stack: ..., One(FEPair)
      InstrINVOKEINTERFACE(Owner(vclassname), MethodName("getOne"), MethodDesc("()Ljava/lang/Object;"), true),
      // ..., FEPair
      InstrCHECKCAST(Owner(itt.fePairClassName)),
      InstrDUP(),
      // ..., FEPair, FEPair
      InstrGETFIELD(Owner(itt.fePairClassName), FieldName("v"), TypeDesc(itt.objectClassType)),
      // ..., FEPair, v
      InstrSWAP(),
      // ..., v, FEPair
      InstrGETFIELD(Owner(itt.fePairClassName), FieldName("ctx"), TypeDesc(fexprclasstype)),
      // ..., v, ctx
      InstrDUP(),
      // ..., v, ctx, ctx
      InstrLOAD_LOOP_CTX(),
      // ..., v, ctx, ctx, loopCtx
      InstrINVOKEINTERFACE(Owner(fexprclassname), MethodName("and"),
        MethodDesc(s"(${fexprclasstype})${fexprclasstype}"), true),

      // ..., v, FEctx, FEctx&loopCtx
      InstrDUP(),
      // ..., v, FEctx, FEctx&loopCtx, FEctx&loopCtx
      InstrINVOKEINTERFACE(Owner(fexprclassname), MethodName("isSatisfiable"), MethodDesc("()Z"), true),
      // ..., v, FEctx, FEctx&loopCtx, isSat?
      InstrINVOKESTATIC(Owner("java/lang/Integer"), MethodName("valueOf"), MethodDesc("(I)Ljava/lang/Integer;"), true),
      // ..., v, FEctx, FEctx&loopCtx, Integer<isSat?>
      InstrICONST(0),
      // ..., v, FEctx, FEctx&loopCtx, Integer<isSat?>, 0
      InstrINVOKESTATIC(Owner("java/lang/Integer"), MethodName("valueOf"), MethodDesc("(I)Ljava/lang/Integer;"), true),
      // ..., v, FEctx, FEctx&loopCtx, Integer<isSat?>, Integer<0>

      InstrINVOKESTATIC(Owner(vclassname), MethodName("choice"), MethodDesc(s"($fexprclasstype$objectClassType$objectClassType)$vclasstype"), true),
      // ..., v, FEctx, V<isSat?>
      InstrDUP_X2(),
      // ..., V<isSat?>, v, FEctx, V<isSat?>
      InstrPOP(),
      // ..., V<isSat?>, v, FEctx
      InstrSWAP(),
      // ..., V<isSat?>, FEctx, v
      InstrINVOKESTATIC(Owner(vclassname), MethodName("one"), MethodDesc(s"($fexprclasstype$objectClassType)$vclasstype"), true),
      // ..., V<isSat?>, One<v>
      InstrASTORE(elementOneVar),
      // ..., V<isSat?> -- to be checked on the jump inserted by insertElementSatisfiabilityConditional()

      // ..... here:
      InstrIFEQ(lastBodyBlockIdx + 1)
    ))))
    val nextInvIndex = newCFG.blocks.flatMap(_.instr).indexWhere(itt.isIteratorNextInvocation)
    assert(blockTrans.newInsnIndeces == List.range(nextInvIndex + 1, nextInvIndex + 20 + 1))
  }

  // ===== transformBodyStartBlockAfterSplit =====
  test("transformBodyStartBlockAfterSplit") {
    val itt = new IterationTransformer()

    val loop1Entry = valid_cfg_2loop.blocks(3)
    val loop1Body = Set(valid_cfg_2loop.blocks(4), valid_cfg_2loop.blocks(5))
    val loop2Entry = valid_cfg_2loop.blocks(7)
    val loop2Body = Set(valid_cfg_2loop.blocks(8), valid_cfg_2loop.blocks(9), valid_cfg_2loop.blocks(10))
    val (newCFG, blockUpdates) = itt.insertElementSatisfiabilityConditional(valid_cfg_2loop,
      List(Loop(loop1Entry, loop1Body), Loop(loop2Entry, loop2Body)))

    val bodyStartBlockAfterSplit = newCFG.blocks(blockUpdates(5) - 1)
    def newCFG_insnIdx(insn: Instruction) = newCFG.blocks.flatMap(_.instr).indexWhere(_ eq insn)
    val elementOneVar = new LocalVar("element$one$var", vclasstype)
    val blockTrans = itt.transformBodyStartBlockAfterSplit(bodyStartBlockAfterSplit, newCFG_insnIdx, elementOneVar)

    assert(blockTrans.newVars.isEmpty)
    assert(equal(blockTrans.newBlocks, List(Block(
      InstrALOAD(elementOneVar),

      InstrDUP(),
      InstrPOP(),
      InstrIFEQ(blockUpdates(6)))
    )))
    val firstInsn = newCFG.blocks(blockUpdates(5) - 1).instr.head
    val blockStartInsnIndex = newCFG_insnIdx(firstInsn)
    assert(blockTrans.newInsnIndeces == List.range(blockStartInsnIndex, blockStartInsnIndex + 1))
  }




  // ===== transformListIteration =====
  test("transformListIteration works") {
    val itt = new IterationTransformer()

    val className = "testclass"
    val vbcMtdNode = VBCMethodNode(0, "test", "()V", None, List.empty, real_cfg_2loop2)
    val vbcClazz = VBCClassNode(0, 0, className, None, "java/util/Object", List.empty, List.empty, List(vbcMtdNode))
    val env = new VMethodEnv(vbcClazz, vbcMtdNode)
    val cw = new MyClassWriter(ClassWriter.COMPUTE_FRAMES)

    val (newCFG, newEnv) = itt.transformListIteration(real_cfg_2loop2, env, cw)

    def tagPreserveAppliedToCorrectInstructions(env: VMethodEnv) = {
      def haveTagPreserve(indices: Iterable[Int]) = indices.foldRight(true)((i, hasTagSoFar) =>
        hasTagSoFar && ((newEnv.instructionTags(i) & newEnv.TAG_PRESERVE) != 0))
      def dontHaveTagPreserve(indices: Iterable[Int]) = indices.foldRight(true)((i, noTagSoFar) => {
        val insnUntagged = (newEnv.instructionTags(i) & newEnv.TAG_PRESERVE) == 0
        if (!insnUntagged) println(s"Instr $i: ${env.instructions(i)} has preserve tag!")
        noTagSoFar && insnUntagged
      })

      val loopPredecessors = newCFG.blocks.filter(_.instr.exists(cond(_) {
        case itInvoke: InstrINVOKEINTERFACE => itInvoke.name.name == "iterator"
        case itInvoke: InstrINVOKEVIRTUAL => itInvoke.name.name == "iterator"
      }))
      // For using simplify call mapped over CtxList wrapper V
//      val loopPredecessorIndices = loopPredecessors.flatMap(loopPredecessor => {
//        val invDynamic = loopPredecessor.instr.find(cond(_) {
//          case i: InstrINVOKEDYNAMIC => i.name.name == "accept"
//        })
//        val invDynamicIndex = newEnv.getInsnIdx(invDynamic.get)
//        List.range(invDynamicIndex - 1, invDynamicIndex - 1 + 3)
//      })
      // For using simplify call after getOne
      val loopPredecessorIndices = loopPredecessors.flatMap(loopPredecessor => {
        val getOne = loopPredecessor.instr.find(cond(_) {
          case i: InstrINVOKEINTERFACE => i.name.name == "getOne"
        })
        val getOneIndex = newEnv.getInsnIdx(getOne.get)
        List.range(getOneIndex - 1, getOneIndex + 3)
      })
      // For using simplify call disabled
//      val loopPredecessorIndices = List.empty[Int]
      val loopPredecessorHasTag = haveTagPreserve(loopPredecessorIndices)

      val bodyStarts = newCFG.blocks.filter(_.instr.exists(cond(_) {
        case nextInvoke: InstrINVOKEINTERFACE => nextInvoke.name.name == "next"
        case nextInvoke: InstrINVOKEVIRTUAL => nextInvoke.name.name == "next"
      }))
      val bodyStartIndices = bodyStarts.flatMap(bodyStart => {
        val nextInv = bodyStart.instr.find(cond(_) {
          case i => itt.isIteratorNextInvocation(i)
        })
        val nextInvIndex = newEnv.getInsnIdx(nextInv.get)
        List.range(nextInvIndex + 1, nextInvIndex + 1 + 20)
      })
      val bodyStartHasTag = haveTagPreserve(bodyStartIndices)

      val bodyAfterSplits = bodyStarts.map(b => newCFG.blocks(newCFG.blocks.indexOf(b) + 1))
      val bodyAfterSplitIndices = bodyAfterSplits.flatMap(bodyAfterSplit => {
        bodyAfterSplit.instr.slice(0, 1).map(newEnv.getInsnIdx)
      })
      val bodyAfterSplitHasTag = haveTagPreserve(bodyAfterSplitIndices)

      val expectedPreserveTagsArePresent = loopPredecessorHasTag && bodyStartHasTag && bodyAfterSplitHasTag
      val unexpectedPreserveTagsAreNotPresent = dontHaveTagPreserve(List.range(0, env.instructions.size)
        .diff(loopPredecessorIndices)
        .diff(bodyStartIndices)
        .diff(bodyAfterSplitIndices))

      expectedPreserveTagsArePresent && unexpectedPreserveTagsAreNotPresent
    }

    // todo: check newCFG and newEnv are correct
    // though this is actually implied by the other tests, so maybe not necessary
    // otherwise look into refactoring so I can reuse the checks in other tests
    assert(newCFG.blocks.size == real_cfg_2loop2.blocks.size + 4)
    assert(tagPreserveAppliedToCorrectInstructions(newEnv))
  }
}