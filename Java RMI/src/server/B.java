package server;

/**
 * Created by Aidin on 7/23/2017.
 */

public class B {
    int j = 30;

    public B() {
        System.out.println("on server: inside B's constructor");
    }

    public B(int j) {
        System.out.println("on server: inside B's constructor with parameter");
        this.j = j;
    }

    @Override
    public String toString() {
        return "B(j=" + j + ")";
    }
}