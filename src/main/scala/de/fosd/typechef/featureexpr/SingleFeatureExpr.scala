package de.fosd.typechef.featureexpr

import de.fosd.typechef.featureexpr.bdd.BDDFeatureExpr
import edu.cmu.cs.varex.mtbdd.MTBDD

class SingleFeatureExpr(override val bdd: MTBDD[Boolean]) extends BDDFeatureExpr(bdd) {
  def feature: String = ???
}
