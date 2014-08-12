package mythruna.server;

public class MessageDelegator<S> extends AbstractMessageDelegator<S> {

    private Object delegate;

    public MessageDelegator(Object delegate, boolean automap) {
        super(delegate.getClass(), automap);
        this.delegate = delegate;
    }

    public MessageDelegator<S> automap() {
        return (MessageDelegator) super.automap();
    }

    public MessageDelegator<S> map(String[] methodNames) {
        return (MessageDelegator) super.map(methodNames);
    }

    public MessageDelegator<S> map(Class messageType, String methodName) {
        return (MessageDelegator) super.map(messageType, methodName);
    }

    protected Object getSourceDelegate(S source) {
        return this.delegate;
    }
}