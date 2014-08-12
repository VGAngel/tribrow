package com.pro.webstart;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 * User: vgangel
 * Date: 8/22/13
 * Time: 1:17 PM
 */

//1. Компилируем программу
//2. Создаем файл манифеста
//        Manifest-Version: 1.0
//        Main-Class: webstart.WebStartTest
//3.Создаем jar файл TestU.jar
//4.Создаем файл запуска testU.jnlp
public class WebStartTest {

    public static void main(String args[]) {
        TestFrame.setDefaultLookAndFeelDecorated(true);
        JFrame frame = new TestFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null);
        frame.show();
    }

    static class TestFrame extends JFrame {
        private JMenuBar bar;
        private JMenu menu;
        private JMenuItem open;
        private JMenuItem save;
        private JMenuItem saveAs;
        private JMenuItem exit;
        private JScrollPane pane;
        private JTextArea text;
        private JFileChooser chooser;
        private String currFileName = null;

        public TestFrame() {
            //setIconImage(new ImageIcon(getClass().getResource("JHelp.gif")).getImage());
            setTitle("WebStartTest");
            open = new JMenuItem("Открыть");
            open.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    chooser = new JFileChooser();
                    int r = chooser.showOpenDialog(null);
                    if (r == JFileChooser.APPROVE_OPTION) {
                        currFileName = chooser.getSelectedFile().getPath();
                        openFile(currFileName);
                    }
                }
            });
            save = new JMenuItem("Сохранить");
            save.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    saveFile();
                }
            });
            saveAs = new JMenuItem("Сохранить как");
            saveAs.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    saveAsFile();
                }
            });
            exit = new JMenuItem("Выход");
            exit.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            });
            menu = new JMenu("Файл");
            menu.add(open);
            menu.add(save);
            menu.add(saveAs);
            menu.add(exit);
            bar = new JMenuBar();
            bar.add(menu);
            setJMenuBar(bar);
            pane = new JScrollPane();
            text = new JTextArea();
            pane.getViewport().add(text, null);
            getContentPane().add(pane, BorderLayout.CENTER);
        }

        /**
         * openFile
         *
         * @param fileName String
         */
        void openFile(String fileName) {
            try {
                File file = new File(fileName);
                int size = (int) file.length();
                int chars_read = 0;
                FileReader in = new FileReader(file);
                char[] data = new char[size];
                while (in.ready()) {
                    chars_read += in.read(data, chars_read, size - chars_read);
                }
                in.close();
                text.setText(new String(data, 0, chars_read));
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Ошибка открытия файла "
                        + fileName, "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }

        boolean saveFile() {
            if (currFileName == null) {
                return saveAsFile();
            }
            try {
                File file = new File(currFileName);
                FileWriter out = new FileWriter(file);
                String txt = text.getText();
                out.write(txt);
                out.close();
                return true;
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Ошибка сохранения "
                        + currFileName, "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
            return false;
        }

        /**
         * saveAsFile
         *
         * @return boolean
         */
        boolean saveAsFile() {
            this.repaint();
            if (JFileChooser.APPROVE_OPTION == chooser.showSaveDialog(this)) {
                currFileName = chooser.getSelectedFile().getPath();
                this.repaint();
                return saveFile();
            } else {
                this.repaint();
                return false;
            }
        }
    }
}