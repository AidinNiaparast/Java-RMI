package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.Socket;

/**
 * Created by Aidin on 7/23/2017.
 */

public class MyInvocationHandler implements InvocationHandler {
    private final int port;
    private final String serverAddress;
    private Class classType;
    int ID;
    boolean isRealised = false;

    public MyInvocationHandler(String serverAddress, int port, Class a, int ID) {
        classType = a;
        this.serverAddress = serverAddress;
        this.port = port;
        this.ID = ID;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        try (Socket socket = new Socket(serverAddress, port);
             ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream input = new ObjectInputStream(socket.getInputStream());) {
            output.writeObject("Release Object");
            output.writeObject(ID);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if(isRealised)
            return null;

        try(Socket socket = new Socket(serverAddress, port);
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());) {

            /*if(method.getName().equals("finalize")){
                output.writeObject("Release Object");
                output.writeObject(ID);
                return null;
            }*/

            String serverClassName = RemoteMethodInvokation.getServerClassName(classType);

            if(serverClassName != null)
            {
                output.writeObject("Call Method");
                output.writeObject(serverClassName);
                output.writeObject(ID);
                output.writeObject(method.getName());

                if(args != null)
                    output.writeObject(args.length);
                else
                    output.writeObject(0);

                if (args != null) {
                    for (Object arg: args) {
                        if(arg instanceof RemoteInterface) {
                            if(( (MyInvocationHandler) Proxy.getInvocationHandler( arg ) ).isRealised )
                                return null;

                            output.writeObject(true);
                            output.writeObject(((MyInvocationHandler) Proxy.getInvocationHandler( arg ) ).ID);
                        }
                        else {
                            output.writeObject(false);
                            output.writeObject(arg);
                        }
                    }
                }

                output.flush();

                if(method.getAnnotation( RemoteReturn.class ) != null){
                    output.writeObject(true);
                    output.flush();
                    int newID = (int) input.readObject();
                    return method.getReturnType().cast(Proxy.newProxyInstance(method.getReturnType().getClassLoader(), new Class[]{method.getReturnType()},
                            new MyInvocationHandler(serverAddress, port, method.getReturnType(), newID)) );
                }else {
                    output.writeObject(false);
                    output.flush();
                    return input.readObject();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
