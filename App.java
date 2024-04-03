import javax.swing.SwingUtilities;

import gui.MainWindow;
import server.Server;

public class App
{
    public static void main(String[] args) throws Exception
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                MainWindow mainWindow = new MainWindow();
                mainWindow.show();
            }
        });
        Server server = new Server();
        Thread myThread = new Thread(server);
        myThread.setDaemon(false);
        myThread.start();
    }
}
