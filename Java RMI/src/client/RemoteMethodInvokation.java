package client;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import java.net.Socket;

/**
 * Created by Aidin on 7/23/2017.
 */

public class RemoteMethodInvokation {
    private final String host;
    private final int port;

    public RemoteMethodInvokation(String host, int port) {
        this.host=host;
        this.port=port;
        Runtime.runFinalizersOnExit(true);
    }

    public static <T extends RemoteInterface> String getServerClassName(Class<T> a){
        Annotation annotation = a.getAnnotation(ImplementedBy.class);
        String serverClassName = null;
        if (annotation != null)
            serverClassName = ((ImplementedBy) annotation).value();
        return serverClassName;
    }

    public <T extends RemoteInterface> T newRemoteInstance(Class<T> a, Object... params) {
        try(Socket socket = new Socket(host, port);
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());) {

            String serverClassName=getServerClassName(a);

            if (serverClassName != null) {
                output.writeObject("Create Object");
                output.writeObject(serverClassName);
                if(params!=null)
                    output.writeObject(params.length);
                else
                    output.writeObject(0);


                for (Object param : params) {
                    if (param instanceof RemoteInterface) {
                        if(( (MyInvocationHandler) Proxy.getInvocationHandler( param ) ).isRealised )
                            return null;
                        output.writeObject(true);
                        output.writeObject(((MyInvocationHandler) Proxy.getInvocationHandler(param)).ID);
                    } else {
                        output.writeObject(false);
                        output.writeObject(param);
                    }
                }

                output.flush();

                while(true) {
                    try {
                        int ID = (int) input.readObject();
                        return (T) Proxy.newProxyInstance(a.getClassLoader(), new Class[]{a}, new MyInvocationHandler(host, port, a, ID));
                    } catch (EOFException e) {
                        e.printStackTrace();
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void releaseRemoteInstance(RemoteInterface remoteObject) {
        try(Socket socket = new Socket(host, port);
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());){
            output.writeObject("Release Object");
            output.writeObject( ((MyInvocationHandler) Proxy.getInvocationHandler( remoteObject )).ID);
            ((MyInvocationHandler) Proxy.getInvocationHandler( remoteObject )).isRealised = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
