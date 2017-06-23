package edu.cmu.cs.vbc.prog;

/**
 * Created by lukas on 6/13/17.
 */
public class Simple {
    int f1 = 5;
    boolean f2 = false;
    char f3 = 'a';
    byte f4 = 1;

    static int sf1 = 1;
    static char sf2 = 'h';
    static byte sf3 = 1;
    public static void main(String[] args){
        boolean a;
        int b;
        String g;
        char p;
        byte r;
        Simple s = new Simple();
        // arithmetic
        int x = 1;
        int y = x + 1;

        // methods, int -> int
        int z  = s.foo(y);
        z = z + 1;

        // static methods
        nop(z);

        // methods, U -> int or int -> U
        a = s.even(1);
        b = s.toN(a);
        g = s.toS(1);
        b = s.toI("");
        p = s.toC(1);
        b = s.toI(p);
        r = s.toB(1);
        b = s.toI(r);

        // fields
        b = s.f1;
        s.f1 = 1;
        a = s.f2;
        s.f2 = true;
        p = s.f3;
        s.f3 = 'a';
        r = s.f4;
        s.f4 = 5;

        // static fields
        b = sf1;
        sf1 = 5;
        p = sf2;
        sf2 = 'c';
        r = sf3;
        sf3 = s.toB(1);
    }

    public int foo(int x) {
        return bar(x)*2;
    }

    private int bar(int x) {
        return x*2;
    }

    public boolean even(int x) {
        return x%2 == 0;
    }

    public static void nop(int x) {
        assert true;
    }

    public int toN(boolean b) {
        return 1;
    }

    public String toS(int x) {
        return "";
    }

    public int toI(String s) {
        return 1;
    }

    public char toC(int x) {
        return 'a';
    }

    public int toI(char c) {
        return 1;
    }

    public byte toB(int x) {
        return 1;
    }

    public int toI(byte b) {
        return 1;
    }
}
