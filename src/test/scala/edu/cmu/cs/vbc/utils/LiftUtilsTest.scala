package edu.cmu.cs.vbc.utils

import edu.cmu.cs.vbc.DiffMethodTestInfrastructure
import org.scalatest.{FunSuite, Matchers}


class LiftUtilsTest extends FunSuite with Matchers with DiffMethodTestInfrastructure {

  import edu.cmu.cs.vbc.utils.LiftUtils._

  test("liftDesc") {
    liftMethodDescription("()V") should equal("(" + fexprclasstype + ")V")
    liftMethodDescription("(I)V") should equal("(" + vclasstype + fexprclasstype + ")V")
    liftMethodDescription("(II)V") should equal("(" + vclasstype + vclasstype + fexprclasstype + ")V")
    liftMethodDescription("(II)I") should equal("(" + vclasstype + vclasstype + fexprclasstype + ")" + vclasstype)
  }

  test("liftSignature") {
    liftMethodSignature("()V", None) should equal(None)
    liftMethodSignature("()V", Some("()V")) should equal(Some("(" + fexprclasstype + ")V"))
    liftMethodSignature("", Some("()V")) should equal(Some("(" + fexprclasstype + ")V"))
    liftMethodSignature("", Some("()I")) should equal(Some("(" + fexprclasstype + ")L" + vclassname + s"<$IntType>;"))
    liftMethodSignature("(I)V", None) should equal(Some("(L" + vclassname + s"<$IntType>;" + fexprclasstype + ")V"))
    liftMethodSignature("", Some("(Ljava/lang/Object;)V")) should equal(Some("(L" + vclassname + "<Ljava/lang/Object;>;" + fexprclasstype + ")V"))
    liftMethodSignature("", Some("(Ljava/lang/Object;Ljava/lang/Object;)V")) should equal(Some("(L" + vclassname + "<Ljava/lang/Object;>;L" + vclassname + "<Ljava/lang/Object;>;" + fexprclasstype + ")V"))
    liftMethodSignature("", Some("([Ljava/lang/Object;)V")) should equal(Some("(L" + vclassname + "<[Ljava/lang/Object;>;" + fexprclasstype + ")V"))
    liftMethodSignature("", Some("(Ljava/util/List<Ljava/lang/Object;>;)V")) should equal(Some("(L" + vclassname + "<Ljava/util/List<Ljava/lang/Object;>;>;" + fexprclasstype + ")V"))
    liftMethodSignature("(I)I", None) should equal(Some("(L" + vclassname + s"<$IntType>;" + fexprclasstype + ")L" + vclassname + s"<$IntType>;"))
  }

}