package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.Socket;

/**
 * Created by Aidin on 7/23/2017.
 */

public class RequestHandler implements Runnable {
    private Socket socket;

    public RequestHandler(Socket socket) {
        this.socket = socket;
    }

    private Class[] changeWrappersToPrimitives(Class[] parameterTypes) {
        for (int i = 0; i < parameterTypes.length; i++) {
            if (parameterTypes[i].equals(Integer.class))
                parameterTypes[i] = int.class;
            if (parameterTypes[i].equals(Double.class))
                parameterTypes[i] = double.class;
            if (parameterTypes[i].equals(Float.class))
                parameterTypes[i] = float.class;
            if (parameterTypes[i].equals(Long.class))
                parameterTypes[i] = long.class;
            if (parameterTypes[i].equals(Short.class))
                parameterTypes[i] = short.class;
            if (parameterTypes[i].equals(Byte.class))
                parameterTypes[i] = byte.class;
            if (parameterTypes[i].equals(Character.class))
                parameterTypes[i] = char.class;
            if (parameterTypes[i].equals(Boolean.class))
                parameterTypes[i] = boolean.class;
        }
        return parameterTypes;
    }

    @Override
    public void run() {
        try (ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream input = new ObjectInputStream(socket.getInputStream());) {

            String command = (String) input.readObject();
            if (command.equals("Create Object")) {
                String className = (String) input.readObject();
                Class c = Class.forName(className);
                int n = (int) input.readObject();
                Object[] parameters = new Object[n];
                Class[] parameterTypes = new Class[n];

                for (int i = 0; i < n; i++) {
                    boolean isremote = (boolean) input.readObject();
                    if (isremote) {
                    	synchronized (Server.objects) {
                    		parameters[i] = Server.objects.get((int) input.readObject());
						}
                    }
                    else
                        parameters[i] = input.readObject();
                    parameterTypes[i] = parameters[i].getClass();
                }

                parameterTypes=changeWrappersToPrimitives(parameterTypes);
                Constructor constructor = c.getConstructor(parameterTypes);

                output.writeObject(Server.counter);
                output.flush();

                synchronized (Server.objects) {
                	Server.objects.put(Server.counter++, constructor.newInstance(parameters));    
				}
            } 
            else if (command.equals("Call Method")) {
                Class c = Class.forName((String) input.readObject());
                int ID = (int) input.readObject();
                String methodName = input.readObject().toString();
                int n = (int) input.readObject();
                Object[] parameters = new Object[n];
                Class[] parameterTypes = new Class[n];

                for (int i = 0; i < n; i++) {
                    boolean isremote = (boolean) input.readObject();
                    if (isremote) {
                    	synchronized (Server.objects) {
                    		parameters[i] = Server.objects.get((int) input.readObject());
						}
                    }
                    else
                        parameters[i] = input.readObject();

                    parameterTypes[i] = parameters[i].getClass();
                }

                parameterTypes=changeWrappersToPrimitives(parameterTypes);
                Method method = c.getMethod(methodName, parameterTypes);
                Object result;
                synchronized (Server.objects) {
                	 result=method.invoke(Server.objects.get(ID), parameters);
				}
               
                if ((boolean) input.readObject() == true) {
                	synchronized (Server.objects) {
                		output.writeObject(Server.counter);
                        Server.objects.put(Server.counter++, result);
					}
                } else {
                    output.writeObject(result);
                }
            } else if (command.equals("Release Object")) {
                int ID = (int) input.readObject();
                synchronized (Server.objects) {
                	 Server.objects.remove(ID);
				}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
