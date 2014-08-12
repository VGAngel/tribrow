package mythruna.client.shell;

import mythruna.PlayerContext;
import mythruna.client.ui.MessageLog;
import mythruna.shell.*;
import org.progeeks.tool.console.PrintCommand;
import org.progeeks.tool.console.Shell;
import org.progeeks.tool.console.ShellCommand;

public class DefaultConsole implements Console {

    private MessageShell shell = new MessageShell();

    private String lastCommand = null;

    public DefaultConsole() {
        this.shell.registerCommand("exit", null);

        this.shell.registerCommand("print", new PrintCommand());
        this.shell.registerCommand("mem", new MemoryCommand());
        this.shell.registerCommand("gc", new GcCommand());
        this.shell.registerCommand("threads", new ShowThreadsCommand());
        this.shell.registerCommand("set", new SetCommand());
        this.shell.registerCommand("props", new ShowPropertiesCommand());
        this.shell.registerCommand("report", new ReportCommand());
    }

    public void setPlayerContext(PlayerContext context) {
        ((PlayerShellCommandProcessor) this.shell.getShellEnvironment()).setPlayerContext(context);
    }

    public void echo(Object o) {
        this.shell.echo(o);
    }

    public void setLocalVariable(String name, Object value) {
        this.shell.getShellEnvironment().getVariables().put(name, value);
    }

    public void registerCommand(String name, ShellCommand cmd) {
        this.shell.registerCommand(name, cmd);
    }

    public MessageShell getShell() {
        return this.shell;
    }

    protected void execute(String cmd) {
        this.shell.execute(cmd);
    }

    protected void executeAdmin(String cmd) {
        this.shell.execute(cmd);
    }

    protected void say(String s) {
        System.out.println("say:" + s);
        MessageLog.addMessage(s);
    }

    protected void println(String s) {
        System.out.println(s);
        MessageLog.addMessage(s);
    }

    public void runCommand(String cmd) {
        if ((cmd.startsWith("/")) || (cmd.startsWith("~"))) {
            this.lastCommand = cmd;
            char c = cmd.charAt(0);
            cmd = cmd.substring(1);
            if (c == '/')
                execute(cmd);
            else
                executeAdmin(cmd);
        } else if ((".".equals(cmd)) && (this.lastCommand != null) && (!".".equals(this.lastCommand))) {
            runCommand(this.lastCommand);
        } else {
            say(cmd);
            this.lastCommand = cmd;
        }
    }

    protected class MessageShell extends Shell {
        public MessageShell() {
            super("", new PlayerShellCommandProcessor(null));
        }

        public void errorCommandNotFound(String name, String args) {
            MessageLog.addMessage("!!! command not found:" + name);
        }

        public void echo(Object s) {
            MessageLog.addMessage("> " + s);
        }
    }
}
