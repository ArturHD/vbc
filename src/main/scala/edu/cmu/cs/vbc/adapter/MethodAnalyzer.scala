package edu.cmu.cs.vbc.adapter

import org.objectweb.asm.tree.{LabelNode, MethodNode}
import org.objectweb.asm.tree.analysis.{Analyzer, BasicInterpreter, BasicValue, Frame}

/**
  * Recognize CFG blocks in a method
  *
  * @param mn method under analysis
  */
class MethodAnalyzer(owner: String, mn: MethodNode) extends Analyzer[BasicValue](new BasicInterpreter()) {
  /**
    * Each element of the list represents the first instruction of a block
    */
  var blocks: scala.collection.mutable.SortedSet[Int] = scala.collection.mutable.SortedSet()
  var label2Index = Map[LabelNode, Int]()

  def analyze(): Array[Frame[BasicValue]] = {
    println("Method: " + mn.name)
    analyze(owner, mn)
  }

  override protected def newControlFlowEdge(insn: Int, successor: Int): Unit = {
    if (blocks.isEmpty)
      blocks = blocks + insn // first instruction
    else if (successor != insn + 1) {
      blocks = blocks + (insn + 1) // the instruction after the jump
      blocks = blocks + successor // there is a jump
    }
  }

  def printBlocksIdx = blocks.foreach((x: Int) => {
    println(x)
  })

  /**
    * If the start of a block is a label, construct a map between LabelNode and block index.
    *
    */
  def validate() = blocks.foreach((x: Int) => {
    val i = mn.instructions.get(x)
    if (i.isInstanceOf[LabelNode]) {
      label2Index += (i.asInstanceOf[LabelNode] -> blocks.toVector.indexOf(x))
    }
  })
}