package mythruna.client.ui;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CommandList<T> implements Command<T> {

    private List<Command<? super T>> list = new CopyOnWriteArrayList();

    public CommandList() {
    }

    public void addCommand(Command<? super T> c) {
        this.list.add(c);
    }

    public void addCommands(Command<? super T>[] commands) {
        this.list.addAll(Arrays.asList(commands));
    }

    public void removeCommand(Command<? extends T> c) {
        this.list.remove(c);
    }

    public void execute(Object source, T action) {
        for (Command c : this.list) {
            System.out.println("execute:" + c);
            c.execute(source, action);
        }
    }
}