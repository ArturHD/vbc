package edu.cmu.cs.vbc.vbytecode.instructions

import edu.cmu.cs.vbc.vbytecode._
import edu.cmu.cs.vbc.{DiffMethodTestInfrastructure, InstrDBGIPrint, InstrLoadConfig}
import org.scalatest.FunSuite

/**
  * @author chupanw
  */
class ExceptionTest extends FunSuite with DiffMethodTestInfrastructure {
  def createException(cls: Owner, msg: String): List[Instruction] = List(
    InstrNEW(cls),
    InstrDUP(),
    InstrLDC(msg),
    InstrINVOKESPECIAL(cls, MethodName("<init>"), MethodDesc(s"(${TypeDesc.getString})V"), itf = false)
  )
  test("terminate with exception") {
    methodWithBlocks(
      Block(createException(Owner.getException, "foo") :+ InstrATHROW(): _*) :: Nil
    )
  }

  ignore("conditionally terminate with exception") {
    methodWithBlocks(
      Block(InstrLoadConfig("A"), InstrIFEQ(2)) ::
      Block(createException(Owner.getException, "foo") :+ InstrATHROW(): _*) ::
      Block(InstrICONST(4), InstrDBGIPrint(), InstrRETURN()) ::
      Nil
    )
  }

  ignore("conditionally terminate with different exception") {
    methodWithBlocks(
      Block(InstrLoadConfig("A"), InstrIFEQ(2)) ::
      Block(createException(Owner.getException, "foo") :+ InstrATHROW(): _*) ::
      Block(InstrLoadConfig("B"), InstrIFEQ(4)) ::
      Block(createException(Owner.getException, "bar") :+ InstrATHROW(): _*) ::
      Block(InstrICONST(4), InstrDBGIPrint(), InstrRETURN()) ::
      Nil
    )
  }

  ignore("terminate with alternative exception") {
    methodWithBlocks(List(
      Block(InstrLoadConfig("A"), InstrIFEQ(2)),
      Block(createException(Owner.getException, "foo") :+ InstrATHROW(): _*),
      Block(createException(Owner.getException, "bar") :+ InstrATHROW(): _*)
    ))
  }

  ignore("terminate with alternative exception on stack") {
    methodWithBlocks(List(
      Block(InstrLoadConfig("A"), InstrIFEQ(2)),
      Block(createException(Owner.getException, "foo") :+ InstrGOTO(3): _*),
      Block(createException(Owner.getException, "bar"): _*),
      Block(InstrATHROW())
    ))
  }

  ignore("terminate with alternative exception in var") {
    val exVar = new LocalVar("ex", TypeDesc.getException)
    methodWithBlocks(List(
      Block(createException(Owner.getException, "foo") :+ InstrASTORE(exVar): _*),
      Block(InstrLoadConfig("A"), InstrIFEQ(3)),
      Block(createException(Owner.getException, "bar") :+ InstrASTORE(exVar): _*),
      Block(InstrLoadConfig("B"), InstrIFEQ(5)),
      Block(InstrALOAD(exVar), InstrATHROW()),
      Block(InstrICONST(4), InstrDBGIPrint(), InstrRETURN())
    ))
  }

  ignore("terminate with exception from atomic instruction") {
    methodWithBlocks(List(
      Block(InstrICONST(0), InstrICONST(0), InstrIDIV(), InstrDBGIPrint(), InstrRETURN())
    ))
  }

  ignore("conditionally terminate with exception from atomic instruction") {
    methodWithBlocks(List(
      Block(InstrLoadConfig("A"), InstrIFEQ(2)),
      Block(InstrICONST(0), InstrICONST(1), InstrIDIV(), InstrDBGIPrint(), InstrRETURN()),
      Block(InstrICONST(0), InstrICONST(0), InstrIDIV(), InstrDBGIPrint(), InstrRETURN())
    ))
  }
}
