package model.java.vutil;

import de.fosd.typechef.featureexpr.FeatureExpr;
import edu.cmu.cs.varex.V;

/**
 * @author chupanw
 */
public interface Collection {
    V<?> getVCopies(FeatureExpr ctx);
}
