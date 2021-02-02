package client;

/**
 * Created by Aidin on 7/26/2017.
 */

public class SampleClient {
    public static void main(String[] args) {
        String server = "127.0.0.1";//server host name or ip
        int port = 9001;//server port
        System.out.println("connecting to server");
        RemoteMethodInvokation rmi = new RemoteMethodInvokation(server, port);
        System.out.println("creating A");
        ARemote a = rmi.newRemoteInstance(ARemote.class, 10);
        System.out.println("creating B");
        BRemote b1 = rmi.newRemoteInstance(BRemote.class);
        System.out.println("calling remote method addTo");
        BRemote b2 = a.addTo(b1);
        System.out.println(b2);
        System.out.println("releasing object on server");

        rmi.releaseRemoteInstance(a);
        rmi.releaseRemoteInstance(b1);
        rmi.releaseRemoteInstance(b2);
    }
}
