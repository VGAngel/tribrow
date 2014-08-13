package com.pro.game.example.test.jme3test.network;

import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.*;

/**
 * A simple test chat server.  When SM implements a set
 * of standard chat classes this can become a lot simpler.
 *
 * @author Paul Speed
 * @version $Revision: 8843 $
 */
public class TestChatClient extends JFrame {

    private Client client;
    private JEditorPane chatLog;
    private StringBuilder chatMessages = new StringBuilder();
    private JTextField nameField;
    private JTextField messageField;

    public TestChatClient(String host) throws IOException {
        super("jME3 Test Chat Client - to:" + host);

        // Build out the UI       
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(800, 600);

        chatLog = new JEditorPane();
        chatLog.setEditable(false);
        chatLog.setContentType("text/html");
        chatLog.setText("<html><body>");

        getContentPane().add(new JScrollPane(chatLog), "Center");

        // A crude form       
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(new JLabel("Name:"));
        nameField = new JTextField(System.getProperty("user.name", "yourname"));
        Dimension d = nameField.getPreferredSize();
        nameField.setMaximumSize(new Dimension(120, d.height + 6));
        p.add(nameField);
        p.add(new JLabel("  Message:"));
        messageField = new JTextField();
        p.add(messageField);
        p.add(new JButton(new SendAction(true)));
        p.add(new JButton(new SendAction(false)));

        getContentPane().add(p, "South");

        client = Network.connectToServer(TestChatServer.NAME, TestChatServer.VERSION,
                host, TestChatServer.PORT, TestChatServer.UDP_PORT);
        client.addMessageListener(new ChatHandler(), TestChatServer.ChatMessage.class);
        client.start();
    }

    public static String getString(Component owner, String title, String message, String initialValue) {
        return (String) JOptionPane.showInputDialog(owner, message, title, JOptionPane.PLAIN_MESSAGE,
                null, null, initialValue);
    }

    public static void main(String... args) throws Exception {
        TestChatServer.initializeClasses();

        // Grab a host string from the user
        String s = getString(null, "Host Info", "Enter chat host:", "localhost");
        if (s == null) {
            System.out.println("User cancelled.");
            return;
        }

        TestChatClient test = new TestChatClient(s);
        test.setVisible(true);
    }

    private class ChatHandler implements MessageListener<Client> {

        public void messageReceived(Client source, Message m) {
            TestChatServer.ChatMessage chat = (TestChatServer.ChatMessage) m;

            System.out.println("Received:" + chat);

            // One of the least efficient ways to add text to a
            // JEditorPane
            chatMessages.append("<font color='#00a000'>" + (m.isReliable() ? "TCP" : "UDP") + "</font>");
            chatMessages.append(" -- <font color='#000080'><b>" + chat.getName() + "</b></font> : ");
            chatMessages.append(chat.getMessage());
            chatMessages.append("<br />");
            String s = "<html><body>" + chatMessages + "</body></html>";
            chatLog.setText(s);

            // Set selection to the end so that the scroll panel will scroll
            // down.
            chatLog.select(s.length(), s.length());
        }
    }

    private class SendAction extends AbstractAction {

        private boolean reliable;

        public SendAction(boolean reliable) {
            super(reliable ? "TCP" : "UDP");
            this.reliable = reliable;
        }

        public void actionPerformed(ActionEvent evt) {
            String name = nameField.getText();
            String message = messageField.getText();

            TestChatServer.ChatMessage chat = new TestChatServer.ChatMessage(name, message);
            chat.setReliable(reliable);
            System.out.println("Sending:" + chat);
            client.send(chat);
        }
    }
}
