 package com.pro.webstart;

import java.awt.EventQueue;

public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                GUI frame = new GUI();
                frame.setVisible(true);
            }
        });
    }

}
