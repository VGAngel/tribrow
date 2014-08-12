package mythruna.client.net;

import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import mythruna.client.ClientOptions;
import mythruna.client.shell.DefaultConsole;
import mythruna.client.shell.ResetOptionsCommand;
import mythruna.client.ui.MessageLog;
import mythruna.msg.ConsoleMessage;
import mythruna.shell.MemoryCommand;
import mythruna.shell.UptimeCommand;
import mythruna.shell.WhoCommand;
import org.progeeks.tool.console.AbstractShellCommand;
import org.progeeks.tool.console.ShellEnvironment;

public class RemoteConsole extends DefaultConsole {

    private Client client;

    public RemoteConsole(Client client) {
        this.client = client;

        DefaultConsole.MessageShell shell = getShell();

        setLocalVariable("opts", ClientOptions.getInstance());

        shell.registerCommand("who", new RemoteShellCommand("who", "Lists the players that are currently online.", WhoCommand.HELP));

        shell.registerCommand("where", new RemoteShellCommand("where", "Lists the players that are currently online.", WhoCommand.HELP));

        shell.registerCommand("serverMem", new RemoteShellCommand("serverMem", "Displays current server memory usage.", MemoryCommand.HELP));

        shell.registerCommand("uptime", new RemoteShellCommand("uptime", "Shows server up time.", UptimeCommand.HELP));

        shell.registerCommand("resetopts", new ResetOptionsCommand());

        client.addMessageListener(new ConsoleObserver(), new Class[]{ConsoleMessage.class});
    }

    protected void execute(String cmd) {
        getShell().execute(cmd);
    }

    protected void executeAdmin(String cmd) {
        executeRemote(cmd);
    }

    protected void say(String s) {
        s = s.replaceAll(">:\\(", "{");
        s = s.replaceAll(":\\(", "}");
        s = s.replaceAll(":\\)", "©");
        s = s.replaceAll(";\\)", "®");

        ConsoleMessage msg = new ConsoleMessage(0L, this.client.getId(), null, "say " + s);
        System.out.println("Sending:" + msg);
        this.client.send(msg);
    }

    protected void executeRemote(String s) {
        ConsoleMessage msg = new ConsoleMessage(0L, this.client.getId(), null, s);
        System.out.println("Sending:" + msg);
        this.client.send(msg);
    }

    protected class RemoteShellCommand extends AbstractShellCommand {
        private String cmd;

        public RemoteShellCommand(String cmd, String description, String[] help) {
            super(description, help);
            this.cmd = cmd;
        }

        public int execute(ShellEnvironment sEnv, String args) {
            RemoteConsole.this.executeRemote(this.cmd + " " + args);
            return 0;
        }

        public boolean isSimple() {
            return true;
        }
    }

    protected class ConsoleObserver
            implements MessageListener<Client> {
        protected ConsoleObserver() {
        }

        public void messageReceived(Client client, Message m) {
            ConsoleMessage msg = (ConsoleMessage) m;

            if (msg.getFrom() == null) {
                RemoteConsole.this.println("> " + msg.getMessage());
            } else {
                String s = msg.getFrom() + ":" + msg.getMessage();
                MessageLog.addMessage(s, true);

                System.out.println("chat-" + s);
            }
        }
    }
}
