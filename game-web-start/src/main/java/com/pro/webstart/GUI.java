 package com.pro.webstart;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import javax.jnlp.BasicService;
import javax.jnlp.ServiceManager;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

public class GUI extends JFrame{

    private static String codeBase;

    public GUI() {
        super();
        setSize(300, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("7room test frame");
        codeBase = identifyCodeBase();
        try {
            setIconImage(new ImageIcon(new URL(codeBase+"/resources/icon/7room.png")).getImage());
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
    }

    public static String identifyCodeBase() {
        try {
            BasicService bs = (BasicService) ServiceManager.lookup("javax.jnlp.BasicService");
            return bs.getCodeBase().toString();
        } catch (Exception ex) {
            try {
                return new File("").toURI().toURL().toString();
            } catch (Exception ex2) {
                return "";
            }
        }
    }

    public static String getCodeBase() {
        return codeBase;
    }

}
