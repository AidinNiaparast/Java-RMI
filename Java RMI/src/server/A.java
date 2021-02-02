package server;

/**
 * Created by Aidin on 7/23/2017.
 */

public class A {
    int i = 0;

    public A(int i) {
        System.out.println("on server: inside A's constructor");
        this.i = i;
    }

    public B addTo(B b) {
        System.out.println("on server: inside A.addTo method");
        return new B(i + b.j);
    }
}