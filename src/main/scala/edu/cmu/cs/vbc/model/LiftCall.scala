package edu.cmu.cs.vbc.model

import edu.cmu.cs.vbc.utils.LiftUtils._
import edu.cmu.cs.vbc.utils.LiftingPolicy
import edu.cmu.cs.vbc.vbytecode.{MethodDesc, MethodName, Owner, TypeDesc}
import org.objectweb.asm.Type

/**
  * Handle method calls, especially methods that we don't lift (e.g. java/lang/)
  *
  * @author chupanw
  */
object LiftCall {

  /**
    * Takes the method signature and returns the lifted method signature
    *
    * @param owner
    * @param name
    * @param desc
    * @return String -> lifted owner name,
    *         String -> lifted method name,
    *         String -> lifted method description
    *
    */
  def liftCall(hasVArgs: Boolean, owner: Owner, name: MethodName, desc: MethodDesc): (Owner, MethodName, MethodDesc, Boolean) = {
    val shouldLiftMethod = LiftingPolicy.shouldLiftMethodCall(owner, name, desc)
    if (shouldLiftMethod) {
      /*
       * VarexC is going to lift this method
       *
       * owner: no need to change because VarexC is going to lift this method
       * name: same as above
       * desc: needs to be replaced with V, because this is the way VarexC lifts method signature
       * todo: could have type erasure problem
       */
      (LiftingPolicy.liftClassName(owner), name, replaceWithVs(desc), true)
    }
    else if (hasVArgs) {
      /*
       * Calling V methods from model classes, all arguments should be V, and type information will be
       * encoded into the method name to avoid type erasure.
       *
       * owner needs to be lifted in case we are calling methods from jre
       * encode the type information into the method name
       * desc should be replaced by V type
       */
      (LiftingPolicy.liftClassName(owner), encodeTypeInName(name, desc), replaceWithVs(desc), true)
    }
    else {
      /*
       * Most likely, we are calling library methods that we shouldn't lift.
       *
       * owner should be updated with model classes
       * name should be the same
       * desc should be updated with model classes
       */
      (LiftingPolicy.liftClassName(owner), name, replaceLibCls(desc), false)
    }
  }

  /**
    * Scan and replace java library classes with model classes
    */
  private def replaceLibCls(desc: MethodDesc): MethodDesc = {
    val liftType: Type => String =
      (t: Type) => if (t == Type.VOID_TYPE) t.getDescriptor else LiftingPolicy.liftClassType(TypeDesc(t.toString))
    val mtype = Type.getMethodType(desc)
    MethodDesc(
      mtype.getArgumentTypes.map(liftType).mkString("(", "", ")") +
      liftType(Type.getType(primitiveToObjectType(mtype.getReturnType.toString)))
    )
  }

  /**
    * Replace all the none-void parameter types with V types, also add FE to the end of parameter list
    */
  private def replaceWithVs(desc: MethodDesc): MethodDesc = {
    val liftType: Type => String =
      (t: Type) => if (t == Type.VOID_TYPE) t.getDescriptor else "Ledu/cmu/cs/varex/V;"
    val mtype = Type.getMethodType(desc)
    MethodDesc(
      (mtype.getArgumentTypes.map(liftType) :+ "Lde/fosd/typechef/featureexpr/FeatureExpr;").mkString("(", "", ")") +
      liftType(mtype.getReturnType)
    )
  }

  def encodeTypeInName(mn: MethodName, desc: MethodDesc): MethodName = {
    mn.name match {
      case "<init>" => mn
      case _ => MethodName(mn.name + desc.replace('/', '_').replace('(', '$').replace(')', '$').replace(";", "").replace("[", "Array_"))
    }
  }

}
