package edu.cmu.cs.vbc.prog;

import edu.cmu.cs.varex.Vint;

/**
 * Created by lukas on 6/13/17.
 */
public class Simple {
    public static void main(String[] args){
        Simple s = new Simple();
        int x = 1;
        int y = x + 1;
        int z  = s.foo(y);
        System.out.println(z);
    }

    public int foo(int x) {
        return bar(x)*2;
    }

    private int bar(int x) {
        return x*2;
    }
}