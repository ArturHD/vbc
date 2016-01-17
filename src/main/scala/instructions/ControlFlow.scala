package edu.cmu.cs.vbc.instructions

import org.objectweb.asm.Opcodes._
import org.objectweb.asm.{Label, MethodVisitor}


/**
  * the design for control-flow lifting is as follows:
  *
  * every block (node in the CFG) has a condition that
  * is initially FALSE
  *
  * when executing the method, the first block receives
  * the ctx of the method as condition.
  *
  * when executing a block with a contradictory condition,
  * one can jump to the next block
  *
  * after executing a block, this blocks condition is
  * set to FALSE and the successor block's conditions are
  * updated:
  * - if there is an unconditional jump, the successor
  * block's condition is "successor.condition or thisblock.condition"
  * - if there is a condition that evaluates to true
  * under condition A, the then-successor's condition is
  * "then-successor.condition or (thisblock.condition and A)"
  * and the else-successor's condition is
  * "else-successor.condition or (thisblock.condition andNot A)"
  *
  * If either successor is before the current block and has
  * a satisfiable condition, we jump back to the successor that
  * is further back.
  * If neither successor is before the current block, we
  * proceed execution with the next block (not necessarily
  * a successor). Note that only at most one successor can be before
  * the current block.
  *
  *
  *
  * The intuition behind this approach is as follows:
  *
  * - we may execute everything multiple times, but we can
  * split the context and the next block to execute might
  * be in different locations.
  *
  * - two blocks with mutually exclusive conditions can be
  * executed in any order
  *
  * - the current approach always jumps back to the earliest
  * block that has a satisfiable condition. As after
  * execution each block turns FALSE, we can just move
  * forward until we make a block further back satisfiable -- that's
  * where we jump to
  *
  * - we could fork the execution on every decision and
  * execute the function to the end, jumping back from
  * there to the last decision and exploring the next
  * path under a missing condition, but this would forgo
  * any joining.
  *
  * - we could also just go sequentially through the
  * method multiple times until all blocks of a contradictory
  * condition, but we may forgot sharing opportunities;
  * we can also resort blocks for that purpose if we stick
  * to the same strategy;
  * there might also be a more efficient mechanism that
  * we can chose at runtime, for example executing those
  * blocks that are least merged yet, jumping back and forth
  * until all blocks have contradictory conditions; or there might even
  * be a static strategy to predict where joining is most
  * likely
  * TODO study this empirically
  *
  * Notes
  *
  * - each block should have either have more than one successor
  * or more than one predecessor. otherwise blocks can be merged.
  * TODO exploit this for optimization
  *
  */









/**
  * assumptions (for now)
  *
  * the if statement is the last statement in a block, making a decision
  * between the next block or the referenced block
  *
  * for now, jumps can only be made forward, not backward (loops not yet
  * supported)
  *
  * for now, blocks need to be balanced wrt to the stack (not enforced yet)
  */
case class InstrIFEQ(targetBlockIdx: Int) extends Instruction {


    override def toByteCode(mv: MethodVisitor, method: MethodNode, block: Block): Unit = {
        val cfg = method.body
        val targetBlock = cfg.blocks(targetBlockIdx)
        assert(targetBlockIdx > block.idx, "not supporting backward jumps yet")
        assert(targetBlockIdx < cfg.blocks.size, "attempting to jump beyond the last block")
        assert(block.idx < cfg.blocks.size - 1, "attempting to jump from the last block")

        mv.visitJumpInsn(IFEQ, targetBlock.label)
    }

    override def toVByteCode(mv: MethodVisitor, method: MethodNode, block: Block): Unit = {
        val cfg = method.body

        /**
          * creating a variable for the decision
          *
          * on top of the stack is the condition, which should be V[Int];
          * that is, we want to know when that value is different from 0
          *
          * the condition is then stored as feature expression in a new
          * variable. this variable is used at the beginning of the relevant
          * blocks to modify the ctx
          *
          * the actual modification of ctx happens Block.toVByteCode
          */
        //        println("creating variable " + thisBlock.getBlockDecisionVar() + " for block " + thisBlock.idx)

        mv.visitMethodInsn(INVOKESTATIC, vopsclassname, "whenEQ", "(Ledu/cmu/cs/varex/V;)Lde/fosd/typechef/featureexpr/FeatureExpr;", false)
        //        mv.visitInsn(DUP)
        //        mv.visitMethodInsn(INVOKESTATIC, "edu/cmu/cs/vbc/test/TestOutput", "printFE", "(Lde/fosd/typechef/featureexpr/FeatureExpr;)V", false)
        //        mv.visitVarInsn(ASTORE, block.blockConditionVar)


    }
}


case class InstrGOTO(targetBlockIdx: Int) extends Instruction {
    override def toByteCode(mv: MethodVisitor, method: MethodNode, block: Block): Unit = {
        val cfg = method.body
        val targetBlock = cfg.blocks(targetBlockIdx)
        assert(targetBlockIdx > block.idx, "not supporting backward jumps yet")
        assert(targetBlockIdx < cfg.blocks.size, "attempting to jump beyond the last block")
        assert(block.idx < cfg.blocks.size - 1, "attempting to jump from the last block")

        mv.visitJumpInsn(GOTO, targetBlock.label)
    }

    override def toVByteCode(mv: MethodVisitor, method: MethodNode, block: Block): Unit = {

    }

}

case class Block(instr: Instruction*) extends LiftUtils {
    var label: Label = null

    /**
      * variable that refers to a condition/featureExpr
      * with which this block is executed;
      * value is initialized by the method
      */
    var blockConditionVar = -1



    def toByteCode(mv: MethodVisitor, method: MethodNode) = {
        mv.visitLabel(label)
        instr.foreach(_.toByteCode(mv, method, this))
    }

    def toVByteCode(mv: MethodVisitor, method: MethodNode) = {
        mv.visitLabel(label)

        //load block condition (local variable for each block)
        mv.visitVarInsn(ALOAD, blockConditionVar)
        //jump to next block if condition is contradictory
        writeIsContradiction(mv)
        //        mv.visitJumpInsn()

        //generate block code

        //if non-conditional jump
        //- update next block's condition (disjunction with prior value)
        //- set this block's condition to FALSE
        //- if backward jump and target condition satisfiable, jump there

        //if conditional jump (then the last instruction left us a featureexpr on the stack)
        //- update then-block's condition
        //- update else-block's condition (ie. next block)
        //- set this block's condition to FALSE
        //- if then-block is behind and its condition is satisfiable, jump there


        instr.foreach(_.toVByteCode(mv, method, this))
    }


    def isConditionalBlock() = instr.lastOption.map(_.isInstanceOf[InstrIFEQ]).getOrElse(false)


    var successors: List[Block] = Nil
    var predecessors: Set[Block] = Set()
    var idx: Int = -1

    /**
      * returns successors (none if last,
      * two if block ends with an if statement
      * of which the second is the target of the if statement,
      * one otherwise)
      */
    private[instructions] def computeSuccessors(cfg: CFG) = {
        val thisBlockIdx = cfg.blocks.indexOf(this)
        if (thisBlockIdx + 1 < cfg.blocks.size) {
            if (isConditionalBlock())
                successors = List(cfg.blocks(thisBlockIdx + 1), cfg.blocks(instr.last.asInstanceOf[InstrIFEQ].targetBlockIdx))
            else if (instr.lastOption.map(_.isInstanceOf[InstrGOTO]).getOrElse(false))
                successors = List(cfg.blocks(instr.last.asInstanceOf[InstrGOTO].targetBlockIdx))
            else
                successors = List(cfg.blocks(thisBlockIdx + 1))
        } else
            successors = Nil
    }

    /** call after computing all successors */
    private[instructions] def computePredecessors(cfg: CFG) = {
        predecessors = for (block <- cfg.blocks.toSet;
                            if block.successors.filter(_ eq this).nonEmpty)
            yield block
    }





}


case class CFG(blocks: List[Block]) extends LiftUtils {

    blocks.foreach(_.computeSuccessors(this))
    blocks.foreach(_.computePredecessors(this))
    for (i <- 0 until blocks.size) blocks(i).idx = i

    def toByteCode(mv: MethodVisitor, method: MethodNode) = {
        //hack: unfortunately necessary, since otherwise state inside the label is shared
        //across both toByteCode and toVByteCode with leads to obscure errors inside ASM
        blocks.foreach(_.label = new Label())

        blocks.foreach(_.toByteCode(mv, method))
    }


    def toVByteCode(mv: MethodVisitor, method: MethodNode) = {
        //hack: unfortunately necessary, since otherwise state inside the label is shared
        //across both toByteCode and toVByteCode with leads to obscure errors inside ASM
        blocks.foreach(_.label = new Label())
        blocks.foreach(_.blockConditionVar = method.getFreshVariable())

        //TODO initialize all block variables to FALSE, except for the first one which is initialized to the ctx parameter
        mv.visitVarInsn(ALOAD, method.ctxParameter)
        mv.visitVarInsn(ASTORE, blocks.head.blockConditionVar)
        for (block <- blocks.tail) {
            writeConstantFALSE(mv)
            mv.visitVarInsn(ASTORE, block.blockConditionVar)
        }

        blocks.foreach(_.toVByteCode(mv, method))
    }
}