package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Aidin on 7/23/2017.
 */
public class Server {
    static int counter = 1;
    static Map<Integer, Object> objects = new HashMap<>();

    public static void main(String[] args){
        try{
            ServerSocket listener=new ServerSocket(9001);
            while(true) {
                try {
                    new Thread(new RequestHandler(listener.accept())).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
