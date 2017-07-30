package edu.cmu.cs.varex;

import de.fosd.typechef.featureexpr.FeatureExpr;

import java.io.PrintStream;

/**
 * Created by ckaestne on 1/16/2016.
 */
public class VOps {

    public static V<? extends Integer> IADD(V<? extends Integer> a, V<? extends Integer> b, FeatureExpr ctx) {
        return a.sflatMap(ctx, (fe, aa) -> b.smap(fe, bb -> aa.intValue() + bb.intValue()));
    }

    public static V<? extends Integer> IINC(V<? extends Integer> a, int increment, FeatureExpr ctx) {
        return a.smap(ctx, aa -> aa.intValue() + increment);
    }

    /**
     * Called by lifted bytecode, compare with 0
     *
     * @param a
     * @return
     */
    public static FeatureExpr whenEQ(V<?> a) {
        return a.when(v -> {
            if (v instanceof Boolean)
                return !(Boolean) v ;
            else if (v instanceof Integer)
                return (Integer) v == 0;
            else
                throw new RuntimeException("Unsupported whenEQ type");
        }, true);
    }

    /**
     * Called by lifted bytecode, compare with 0
     *
     * @param a
     * @return
     */
    public static FeatureExpr whenNE(V<?> a) {
        return a.when(v -> {
            if (v instanceof Boolean)
                return (Boolean) v;
            else if (v instanceof Integer)
                return (Integer) v != 0;
            else
                throw new RuntimeException("Unsupported whenNE type");
        }, true);
    }

    public static FeatureExpr whenGT(V<? extends Integer> a) {
        return a.when(v -> v > 0, true);
    }

    public static FeatureExpr whenGE(V<? extends Integer> a) {
        return a.when(v -> v >= 0, true);
    }

    public static FeatureExpr whenLT(V<? extends Integer> a) {
        return a.when(v -> v < 0, true);
    }

    public static FeatureExpr whenLE(V<? extends Integer> a) {
        return a.when(v -> v <= 0, true);
    }

    public static FeatureExpr whenNONNULL(V<? extends Object> a) {
        return a.when(v -> v != null, false);
    }

    public static FeatureExpr whenNULL(V<? extends Object> a) {
        return a.when(v -> v == null, false);
    }

    public static FeatureExpr whenIEQ(V<? extends Integer> a, V<? extends Integer> b) {
        V<? extends Integer> sub = compareInt(a, b);
        return whenEQ(sub);
    }

    public static FeatureExpr whenIGE(V<? extends Integer> a, V<? extends Integer> b) {
        V<? extends Integer> sub = compareInt(a, b);
        return whenGE(sub);
    }

    public static FeatureExpr whenILT(V<? extends Integer> a, V<? extends Integer> b) {
        V<? extends Integer> sub = compareInt(a, b);
        return whenLT(sub);
    }

    public static FeatureExpr whenILE(V<? extends Integer> a, V<? extends Integer> b) {
        V<? extends Integer> sub = compareInt(a, b);
        return whenLE(sub);
    }

    public static FeatureExpr whenINE(V<? extends Integer> a, V<? extends Integer> b) {
        V<? extends Integer> sub = compareInt(a, b);
        return whenNE(sub);
    }

    public static FeatureExpr whenIGT(V<? extends Integer> a, V<? extends Integer> b) {
        V<? extends Integer> sub = compareInt(a, b);
        return whenGT(sub);
    }

    private static V<? extends Integer> compareInt(V<? extends Integer> a, V<? extends Integer> b) {
        return a.flatMap(aa -> {
            if (aa == null)
                return V.one(null);
            else
                return b.map(bb -> {
                    if (bb == null)
                        return null;
                    else {
                        // avoid Integer overflow
                        if (aa.intValue() > 0 && bb.intValue() < 0)
                           return 1;
                        else if (aa.intValue() < 0 && bb.intValue() > 0)
                            return -1;
                        else
                            return aa.intValue() - bb.intValue();
                    }
                });
        });
    }

    public static V<? extends Integer> ISUB(V<? extends Integer> a, V<? extends Integer> b, FeatureExpr ctx) {
        return a.sflatMap(ctx, (fe, aa) -> b.smap(fe, bb -> aa.intValue() - bb.intValue()));
    }


    public static V<? extends Integer> IMUL(V<? extends Integer> a, V<? extends Integer> b, FeatureExpr ctx) {
        return a.sflatMap(ctx, (fe, aa) -> b.smap(fe, bb -> aa.intValue() * bb.intValue()));
    }

    public static V<? extends Integer> IDIV(V<? extends Integer> a, V<? extends Integer> b) {
        return a.flatMap(aa -> b.map(bb -> aa.intValue() / bb.intValue()));
    }

    public static V<? extends Integer> i2c(V<? extends Integer> a, FeatureExpr ctx) {
        return a.smap((v -> {
            int i = v.intValue();
            char c = (char)i;
            return (int) c;
        }), ctx);
    }

    public static FeatureExpr whenAEQ(V<?> a, V<?> b) {
        V<? extends Boolean> compare = a.flatMap(aa -> {
            return b.map(bb -> {
                return aa == bb;
            });
        });
        return compare.when(c -> c, true);
    }

    public static FeatureExpr whenANE(V<?> a, V<?> b) {
        V<? extends Boolean> compare = a.flatMap(aa -> {
            return b.map(bb -> {
                return aa != bb;
            });
        });
        return compare.when(c -> c, true);
    }

    public static V<? extends Integer> iushr(V<? extends Integer> value1, V<? extends Integer> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1 >>> v2));
    }

    public static V<? extends Integer> irem(V<? extends Integer> value1, V<? extends Integer> value2) {
        return value1.flatMap(v1 -> value2.map(v2 -> v1 % v2));
    }

    public static V<? extends Integer> ior(V<? extends Integer> value1, V<? extends Integer> value2) {
        return value1.flatMap(v1 -> value2.map(v2 -> v1 | v2));
    }

    public static V<? extends Integer> iand(V<? extends Integer> value1, V<? extends Integer> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1 & v2));
    }

    public static V<? extends Integer> ixor(V<? extends Integer> value1, V<? extends Integer> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1 ^ v2));
    }

    public static V<? extends Long> ldiv(V<? extends Long> value1, V<? extends Long> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1.longValue() / v2.longValue()));
    }

    public static V<? extends Integer> l2i(V<? extends Long> value1, FeatureExpr ctx) {
        return value1.smap(ctx, x -> (int) x.longValue());
    }

    public static V<? extends Integer> i2b(V<? extends Integer> value1, FeatureExpr ctx) {
        return value1.smap(ctx, x -> (int) (byte) x.intValue());
    }

    public static V<? extends Integer> i2s(V<? extends Integer> value1, FeatureExpr ctx) {
        return value1.smap(ctx, x -> (int) (short) x.intValue());
    }

    public static V<? extends Long> i2l(V<? extends Integer> value1, FeatureExpr ctx) {
        return value1.smap(ctx, x -> (long) x.intValue());
    }

    public static V<? extends Long> ladd(V<? extends Long> value1, V<? extends Long> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1.longValue() + v2.longValue()));
    }

    public static V<? extends Long> land(V<? extends Long> value1, V<? extends Long> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1.longValue() & v2.longValue()));
    }

    public static V<? extends Long> lushr(V<? extends Long> value1, V<? extends Integer> value2, FeatureExpr ctx) {
        return value1.sflatMap(ctx, (fe, v1) -> value2.smap(fe, v2 -> v1.longValue() >>> v2.intValue()));
    }

    //////////////////////////////////////////////////
    // Special println that prints configuration as well
    //////////////////////////////////////////////////
    public static void println(PrintStream out, String s, FeatureExpr ctx) {
        out.println("[" + ctx + "]: " + s);
    }
    public static void println(PrintStream out, int i, FeatureExpr ctx) {
        out.println("[" + ctx + "]: " + i);
    }
    public static void println(PrintStream out, Object o, FeatureExpr ctx) {
        out.println("[" + ctx + "]: " + o);
    }
    public static void println(PrintStream out, char c, FeatureExpr ctx) {
        out.println("[" + ctx + "]: " + c);
    }
    public static void println(PrintStream out, boolean b, FeatureExpr ctx) {
        out.println("[" + ctx + "]: " + b);
    }

    //////////////////////////////////////////////////
    // Truncating primitive types to int
    //////////////////////////////////////////////////
    public static Integer truncB(Integer o) {
        return (int) (byte) o.intValue();
    }
    public static Integer truncC(Integer o) {
        return (int) (char) o.intValue();
    }
    public static Integer truncZ(Integer o) {
        return (int) (byte) o.intValue();   // same as byte according to the spec of BASTORE
    }
    public static Integer truncS(Integer o) {
        return (int) (short) o.intValue();
    }
}
