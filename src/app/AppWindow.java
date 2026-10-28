package app;

import javax.swing.JFrame;

public class AppWindow extends JFrame {

    public AppWindow() {
        setTitle("apgr assignment 01");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        DrawPanel panel = new DrawPanel(800, 600);
        add(panel);
        pack();
        setLocationRelativeTo(null);
        new Controller(panel);
    }
}
