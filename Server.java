package server;

import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

public class Server implements Runnable
{
    private ServerSocket m_socket = null;
    private boolean m_running = false;
    private final int QUEUE_SIZE = 10;
    private Map<String, Connection> m_nameToConnection = new HashMap<String, Connection>();
    private Map<String, String> m_usernameToName = new HashMap<String, String>();
    private Server m_server = null;

    public Server()
    {
        m_server = this;
        try
        {
            // Open config file and load props
            String configFilePath = "src/config.properties";
            FileInputStream propsInput = new FileInputStream(configFilePath);
            Properties props = new Properties();
            props.load(propsInput);
            String acceptIp = props.getProperty("ACCEPT_IP", "127.0.0.1");
            int acceptPort = Integer.parseInt(props.getProperty("ACCEPT_PORT", "10001"));

            // open datagram socket
            InetAddress addr = InetAddress.getByName(acceptIp);
            m_socket = new ServerSocket(acceptPort, QUEUE_SIZE, addr);
            m_socket.setReuseAddress(true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void run()
    {
        Thread acceptConnectionThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    m_running = true;
                    while (m_running)
                    {
                        try
                        {
                            Socket socket = m_socket.accept();
                            String name = UUID.randomUUID().toString();
                            Connection connection = new Connection(socket, name, m_server);
                            m_nameToConnection.put(name, connection);
                            Thread thread = new Thread(connection);
                            thread.setDaemon(false);
                            thread.start();
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    m_running = false;
                }
            }
        });

        acceptConnectionThread.setDaemon(false);
        acceptConnectionThread.start();
    }

    public void addToUsernameMap(String username, String name)
    {
        m_usernameToName.put(username, name);
    }

    public void sendToSpecificClient(String sender, String receiver, String message)
    {
        if (m_usernameToName.containsKey(receiver))
        {
            String receiverName = m_usernameToName.get(receiver);
            Connection receiverConnection = m_nameToConnection.get(receiverName);
            receiverConnection.processMessageFromAnotherConnection(receiver, message);
        }
        else
        {
            String senderName = m_usernameToName.get(sender);
            Connection senderConnection = m_nameToConnection.get(senderName);
            String rtnStr = "Error, user not found: " + receiver;
            senderConnection.processMessageFromAnotherConnection("Server", rtnStr);
        }
    }

    public void connectionStopped(String username)
    {
        if (m_usernameToName.containsKey(username))
        {
            String name = m_usernameToName.get(username);
            m_nameToConnection.remove(name);
            m_usernameToName.remove(username);
        }
    }

    public void stop()
    {
        m_running = false;
    }

    public boolean getIsRunning()
    {
        return m_running;
    }
}
