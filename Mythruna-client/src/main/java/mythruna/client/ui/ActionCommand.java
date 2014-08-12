package mythruna.client.ui;

public class ActionCommand<T> implements Command<T> {

    private String action;
    private Command<T> command;

    public ActionCommand(String action) {
        this(action, null);
    }

    public ActionCommand(String action, Command<T> command) {
        this.action = action;
        this.command = command;
    }

    public String getName() {
        return this.action;
    }

    public final void execute(Object source) {
        throw new UnsupportedOperationException("Catching the bad guys");
    }

    public void execute(Object source, T a) {
        if (this.command != null)
            this.command.execute(source, a);
    }
}