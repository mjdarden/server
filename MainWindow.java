package gui;

import javax.swing.JFrame;

public class MainWindow
{
    private JFrame window = null;

    public MainWindow()
    {
        window = new JFrame();
        window.setTitle("Server Gui");
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        window.setSize(800, 500);
        window.setLocationRelativeTo(null);
    }

    public void show()
    {
        window.setVisible(true);
    }
}
