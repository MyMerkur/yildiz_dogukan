package app;

import javax.swing.JFrame;

public class AppWindow extends JFrame {

    public AppWindow() {
        setTitle("apgr assignment 01");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        add(new DrawPanel(800, 600));
        pack();
        setLocationRelativeTo(null);
    }
}
