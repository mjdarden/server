package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class Connection implements Runnable
{
    private Socket m_socket = null;
    private String m_name = null;
    private String m_userName = null;
    private BufferedReader m_bufferedInputReader = null;
    private BufferedWriter m_bufferedOutputWriter = null;
    private Server m_server = null;

    public Connection (Socket socket, String name, Server server)
    {
        m_socket = socket;
        m_name = name;
        m_server = server;
        try
        {
            m_bufferedInputReader = new BufferedReader(new InputStreamReader(m_socket.getInputStream()));
            m_bufferedOutputWriter = new BufferedWriter(new OutputStreamWriter(m_socket.getOutputStream()));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void run()
    {
        try
        {
            System.out.println("Starting while loop");
            while (m_socket.isConnected())
            {
                System.out.println("chencking if reader ready");
                if (m_bufferedInputReader.ready())
                {
                    String inputStr = m_bufferedInputReader.readLine();
                    System.out.println("Name: " + m_name + ", received: " + inputStr);
                    m_bufferedOutputWriter.write("received: " + inputStr);
                    m_bufferedOutputWriter.newLine();
                    m_bufferedOutputWriter.flush();
                    processMessageFromMyClient(inputStr);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            System.out.println("stopping.....");
            stop();
        }
    }

    private void processMessageFromMyClient(String inputStr)
    {
        if (inputStr.contains("username"))
        {
            String[] tokens = inputStr.split(":");
            if (tokens.length == 2)
            {
                m_userName = tokens[1];
                m_server.addToUsernameMap(m_userName, m_name);
            }
            else
            {
                System.out.println("Error: Incorrect username format on message - " + inputStr);
            }
        }
        else if (inputStr.contains("sendTo"))
        {
            String[] tokens = inputStr.split(":");
            if (tokens.length == 3)
            {
                String receiver = tokens[1];
                String message = tokens[2];
                m_server.sendToSpecificClient(m_userName, receiver, message);
            }
            else
            {
                System.out.println("Error: Incorrect sendTo format on message - " + inputStr);
            }
        }
        else if (inputStr.equalsIgnoreCase("bye"))
        {
            stop();
        }
    }

    public void processMessageFromAnotherConnection(String senderName, String message)
    {
        System.out.println();
        try
        {
            m_bufferedOutputWriter.write(senderName + " says: " + message);
            m_bufferedOutputWriter.newLine();
            m_bufferedOutputWriter.flush();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void stop()
    {
        try
        {
            if (m_bufferedInputReader != null)
            {
                m_bufferedInputReader.close();
            }
            if (m_bufferedOutputWriter != null)
            {
                m_bufferedOutputWriter.close();
            }
            if (m_socket != null)
            {
                m_socket.close();
            }
            m_server.connectionStopped(m_userName);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public String getName()
    {
        return m_name;
    }

}
