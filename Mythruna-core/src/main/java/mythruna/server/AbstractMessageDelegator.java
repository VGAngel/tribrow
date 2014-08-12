package mythruna.server;

import com.jme3.network.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public abstract class AbstractMessageDelegator<S> implements MessageListener<S> {

    private Class delegateType;
    private Map<Class, Method> methods = new HashMap<>();

    protected AbstractMessageDelegator(Class delegateType, boolean automap) {
        this.delegateType = delegateType;
        if (automap) {
            automap();
        }
    }

    public void register(Server server) {
        for (Class type : this.methods.keySet()) {
            server.addMessageListener((MessageListener<? super HostedConnection>) this, new Class[]{type});
            //server.addMessageListener(this, new Class[]{type});
        }

    }

    public void register(Client client) {
        for (Class type : this.methods.keySet()) {
            client.addMessageListener((MessageListener<? super Client>) this, new Class[]{type});
            //client.addMessageListener(this, new Class[]{type});
        }
    }

    protected Method findDelegate(String name, Class parameterType) {
        for (Method m : this.delegateType.getDeclaredMethods()) {
            if (m.getName().equals(name)) {
                Class[] parms = m.getParameterTypes();
                if (parms.length == 2) {
                    if (MessageConnection.class.isAssignableFrom(parms[0])) {
                        if (parms[1].isAssignableFrom(parameterType)) {
                            return m;
                        }
                    }
                }
            }
        }
        return null;
    }

    protected boolean allowName(String name) {
        return true;
    }

    public AbstractMessageDelegator<S> automap() {
        for (Method m : this.delegateType.getDeclaredMethods()) {
            if (allowName(m.getName())) {
                Class[] parms = m.getParameterTypes();
                if (parms.length == 2) {
                    if (MessageConnection.class.isAssignableFrom(parms[0])) {
                        if (Message.class.isAssignableFrom(parms[1])) {
                            this.methods.put(parms[1], m);
                        }
                    }
                }
            }
        }
        return this;
    }

    public AbstractMessageDelegator<S> map(String[] methodNames) {
        Set names = new HashSet(Arrays.asList(methodNames));

        for (Method m : this.delegateType.getDeclaredMethods()) {
            if (names.contains(m.getName())) {
                Class[] parms = m.getParameterTypes();
                if (parms.length == 2) {
                    if (MessageConnection.class.isAssignableFrom(parms[0])) {
                        if (Message.class.isAssignableFrom(parms[1])) {
                            this.methods.put(parms[1], m);
                        }
                    }
                }
            }
        }
        return this;
    }

    public AbstractMessageDelegator<S> map(Class messageType, String methodName) {
        Method m = findDelegate(methodName, messageType);
        if (m == null) {
            throw new RuntimeException("Method:" + methodName + " not found matching signature (MessageConnection, " + messageType.getName() + ")");
        }

        this.methods.put(messageType, m);

        return this;
    }

    protected Method getMethod(Class c) {
        Method m = (Method) this.methods.get(c);
        if (m != null) {
            return m;
        }

        return m;
    }

    protected abstract Object getSourceDelegate(S paramS);

    public void messageReceived(S source, Message msg) {
        if (msg == null) {
            return;
        }
        Object delegate = getSourceDelegate(source);
        if (delegate == null) {
            return;
        }

        Method m = getMethod(msg.getClass());
        if (m == null) {
            throw new RuntimeException("Method now found for class:" + msg.getClass());
        }
        try {
            m.invoke(delegate, new Object[]{source, msg});
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error executing:" + m, e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Error executing:" + m, e.getCause());
        }
    }
}