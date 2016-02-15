package edu.cmu.cs.vbc.instructions

/**
  * rewrites of methods as part of variational lifting.
  * note that ASTs are immutable; returns a new (rewritten) method.
  *
  * rewrites happen before toVByteCode is called on a method;
  * the MethodEnv can be used for the transformation
  */
object Rewrite {


    def rewrite(m: MyMethodNode): MyMethodNode =
        ensureUniqueReturnInstr(m)


    private def ensureUniqueReturnInstr(m: MyMethodNode): MyMethodNode = {
        //if the last instruction in the last block is the only return statement, we are happy
        val returnInstr = for (block <- m.body.blocks; instr <- block.instr if instr.isReturnInstr) yield instr
        assert(returnInstr.nonEmpty, "no return instruction found in method")
        assert(returnInstr.distinct.size == 1, "inconsistency: different kinds of return instructions found in method")
        if (returnInstr.size == 1 && returnInstr.head == m.body.blocks.last.instr.last)
            m
        else unifyReturnInstr(m: MyMethodNode, returnInstr.head)
    }

    private def unifyReturnInstr(method: MyMethodNode, returnInstr: Instruction): MyMethodNode = {
        val returnVariable = new LocalVar()

        var newReturnBlockInstr = List(returnInstr)
        if (!method.returnsVoid())
            newReturnBlockInstr ::= InstrILOAD(returnVariable) //TODO generalize to different types of variables, based on return type
        val newReturnBlock = new Block(newReturnBlockInstr: _*)
        val newReturnBlockIdx = method.body.blocks.size

        var substituteInstr: List[Instruction] = List(new InstrGOTO(newReturnBlockIdx))
        if (!method.returnsVoid())
            substituteInstr ::= InstrISTORE(returnVariable) //TODO generalize to different types of variables, based on return type

        val rewrittenBlocks = method.body.blocks.map(block =>
            new Block(block.instr.flatMap(instr =>
                if (instr.isReturnInstr) substituteInstr else List(instr)
            ): _*))

        method.copy(body =
            CFG(
                rewrittenBlocks :+ newReturnBlock
            )
        )

    }


}
