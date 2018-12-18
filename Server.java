import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.File;
import java.net.Socket;
import java.net.ServerSocket;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import java.util.*;

public class Server 
{
  // The server socket to accept connections from clients.
  private static ServerSocket serverSocket = null;
  // The client socket to send/receive data from clients.
  private static Socket clientSocket = null;
  // This chat server can accept up to maxClientsCount clients' connections.
  private static final int maxClientsCount = 100;
  private static final clientThread[] threads = new clientThread[maxClientsCount];

  public static void main(String args[])
  {
    // The default port number.
    int portNumber = 6669;
    String host = "localhost";
    File f = new File("server");
    f.mkdir();
    if (args.length < 1)
    {
      System.out.println("Server is using port number = " + portNumber);
    } 
    else
    {
      portNumber = Integer.valueOf(args[0]).intValue();
    }

    try
    {
      //open a server port to listen for client connections
      serverSocket = new ServerSocket(portNumber);
    }
    catch (IOException e)
    {
      System.out.println(e);
    }

    while (true) 
    {
      System.out.println("Listening...");
      try
      {
        clientSocket = serverSocket.accept();
        int i = 0;
        for (i = 0; i < maxClientsCount; i++)
        {
          if (threads[i] == null)
          {
            (threads[i] = new clientThread(clientSocket, threads)).start();
            break;
          }
        }
        if (i == maxClientsCount)
        {
          PrintStream os = new PrintStream(clientSocket.getOutputStream());
          os.println("Server too busy. Try later.");
          os.close();
          clientSocket.close();
        }
      } 
      catch (IOException e)
      {
        System.out.println(e);
      }
    }
  }
}


class clientThread extends Thread 
{

  private String clientName = null;
  private String clientName_block = null;
  private DataInputStream is = null;
  private PrintStream os = null;
  private Socket clientSocket = null;
  private final clientThread[] threads;
  private int maxClientsCount;
  private FileInputStream fis = null;
  private BufferedInputStream bis = null;
  private OutputStream pos = null;
  private static FileOutputStream fos = null;
  private static BufferedOutputStream bos = null;

  public clientThread(Socket clientSocket, clientThread[] threads) 
  {
    this.clientSocket = clientSocket;
    this.threads = threads;
    maxClientsCount = threads.length;
  }

  public void run()
  {
    int maxClientsCount = this.maxClientsCount;
    clientThread[] threads = this.threads;
    int count = 0;
    try
    {
      //Create input and output streams for this client.
      is = new DataInputStream(clientSocket.getInputStream());
      os = new PrintStream(clientSocket.getOutputStream());
      String name;
      while (true)
      {
        os.println("#####   Enter your username : #####");
        name = is.readLine().trim();
        if (name.indexOf('@') == -1)
        {
          break;
        }
        else 
        {
          os.println("The name should not contain '@' character.");
        }
        if (name.indexOf('!') == -1)
        {
          break;
        }
        else 
        {
          os.println("The name should not contain '!' character.");
        }
      }

      //welcome the new client
      os.println("HELLO " + name + " !!!");
      os.println("\n");
      os.println("#########   CHAT APPLCATION   #########");
      os.println("(a) To send a Broadcast message - simply type on the terminal window");
      os.println("(b) To send a Unicast message - use format --> '@username message'");
      os.println("(c) To send a Blockcast message - use format --> '!username message'");
      os.println("(d) To send a Broadcast file - use format --> 'bFILE /sender_username/filename.extension'");
      os.println("(e) To send a Unicast file - use format --> 'uFILE recipient_username /sender_username/filename.extension'");
      os.println("\n");
      os.println("To exit the application type EXIT in a new line");

      //Create a directory for every new member
      File f = new File(name);
      f.mkdir();
      
      synchronized (this) 
      {
        for (int i = 0; i < maxClientsCount; i++) 
        {
          if (threads[i] != null && threads[i] == this)
          {
            clientName = "@" + name;
            clientName_block = "!" + name;
            break;
          }
        }
        for (int i = 0; i < maxClientsCount; i++)
        {
          if (threads[i] != null && threads[i] != this)
          {
            threads[i].os.println("*** A new user " + name + " has logged in !!! ***");
          }
        }     
      }
      //Start the conversation
      while (true) 
      {
        String line = is.readLine();
        if (line.startsWith("EXIT"))
        {
          break;
        }
        //If the message is private send it to the given client
        if (line.startsWith("@"))
        {
          String[] words = line.split("\\s", 2);
          if (words.length > 1 && words[1] != null)
          {
            words[1] = words[1].trim();
            if (!words[1].isEmpty())
            {
              synchronized (this) 
              {
                for (int i = 0; i < maxClientsCount; i++) 
                {
                  if (threads[i] != null && threads[i] != this && threads[i].clientName != null && threads[i].clientName.equals(words[0])) 
                  {
                    //send private message
                    threads[i].os.println("<<<" + name + ">>> " + words[1]);
                    //Echo this message for the sender.
                    this.os.println(">>>" + name + ">>> " + words[1]);
                    break;
                  }
                }
              }
            }
          }
        }
        //If the message is blockcast - do not send to the specified client
        else if (line.startsWith("!"))
        {
          String[] words = line.split("\\s", 2);
          if (words.length > 1 && words[1] != null)
          {
            words[1] = words[1].trim();
            if (!words[1].isEmpty())
            {
              synchronized (this)
              {
                for (int i = 0; i < maxClientsCount; i++)
                {
                  if (threads[i] != null && threads[i] != this && threads[i].clientName_block != null && !threads[i].clientName_block.equals(words[0]))
                  {
                    threads[i].os.println("<<<" + name + ">>> " + words[1]);
                  }
                }
                this.os.println(">>>" + name + ">>> " + words[1]);
              }

            }
          }
        }
        //broadcast the file to all clients
        else if (line.startsWith("bFILE"))
        {
          String[] words = line.split("\\s", 2);
          if (words.length > 1 && words[1] != null)
          {
            words[1] = words[1].trim();
            if (!words[1].isEmpty())
            {
              synchronized (this)
              {
                int FILE_SIZE = 21474836; 
                String host = "localhost";
                int portNumber_file = 6666;
                FileOutputStream fos_1 = null;
                BufferedOutputStream bos_1 = null;
                Socket sock = null;
                sock = new Socket(host, portNumber_file);
                String[] bits = words[1].split("/");
                String lastone = bits[bits.length - 1];
                try
                {
                  int current = 0;
                  int bytesRead;
                  File myFile = new File ("/home/swapnil/Desktop/java_code/server/" + lastone);
                  byte [] mybytearray = new byte [FILE_SIZE];
                  InputStream instream = sock.getInputStream(); 
                  fos_1 = new FileOutputStream(myFile);
                  bos_1 = new BufferedOutputStream(fos_1);
                  bytesRead = instream.read(mybytearray,0,mybytearray.length);
                  current = bytesRead;
                  do 
                  {
                    bytesRead = instream.read(mybytearray, current, (mybytearray.length - current));
                    if (bytesRead >= 0)
                    {
                      current += bytesRead;
                    }
                  } while (bytesRead > -1);
                  bos_1.write(mybytearray, 0, current);
                  bos_1.flush();
                }              
                finally 
                {
                  if (fos_1 != null) fos_1.close();
                  if (bos_1 != null) bos_1.close();
                  if (sock != null) sock.close();
                }
              
                for (int i = 0; i < maxClientsCount; i++)
                {
                  if (threads[i] != null && threads[i].clientName != null && threads[i] != this) 
                  {
                    FileInputStream fis_1 = null;
                    BufferedInputStream bis_1 = null;
                    OutputStream os_1 = null;
                    ServerSocket servsock_1 = null;
                    Socket sock_1 = null;
                    try
                    {
                      threads[i].os.println("RECEIVING " + lastone + " to " + threads[i].clientName.substring(1) + " folder");
                      servsock_1 = new ServerSocket(8000);
                      try
                      {
                        sock_1 = servsock_1.accept();
                        File sendFile = new File ("/home/swapnil/Desktop/java_code/server/" + lastone);
                        byte [] mybytearray  = new byte [(int)sendFile.length()];
                        fis_1 = new FileInputStream(sendFile);
                        bis_1 = new BufferedInputStream(fis_1);
                        bis_1.read(mybytearray,0,mybytearray.length);
                        os_1 = sock_1.getOutputStream();
                        os_1.write(mybytearray,0,mybytearray.length);
                        os_1.flush();
                      }
                      finally
                      {
                        if (bis_1 != null) bis_1.close();
                        if (os_1 != null) os_1.close();
                        if (sock_1 != null) sock_1.close();
                      }
                    }
                    finally
                    {
                      if (servsock_1 != null) servsock_1.close();
                    }
                  }
                }
              }
            }
          }
        }
        else if (line.startsWith("uFILE"))
        {
          //System.out.println(line);
          String[] words = line.split("\\s", 3);
          // @FILE anurag /swapnil/song.mp3
          if (words.length > 1 && words[1] != null && words[2] !=null)
          {
            words[1] = words[1].trim();
            words[2] = words[2].trim();
            if (!words[1].isEmpty() && !words[2].isEmpty())
            {
              synchronized (this)
              {
                int FILE_SIZE = 21474836; 
                String host = "localhost";
                int portNumber_file = 3333;
                FileOutputStream fos_1 = null;
                BufferedOutputStream bos_1 = null;
                Socket sock = null;
                sock = new Socket(host, portNumber_file);
                //the file is now in words[2]
                String recv = "@" + words[1]; // contains the value of unicast recipient
                String[] bits = words[2].split("/");
                String lastone = bits[bits.length - 1];
                try
                {
                  int current = 0;
                  int bytesRead;
                  File myFile = new File ("/home/swapnil/Desktop/java_code/server/" + lastone);
                  byte [] mybytearray = new byte [FILE_SIZE];
                  InputStream instream = sock.getInputStream(); 
                  fos_1 = new FileOutputStream(myFile);
                  bos_1 = new BufferedOutputStream(fos_1);
                  bytesRead = instream.read(mybytearray,0,mybytearray.length);
                  current = bytesRead;
                  do 
                  {
                    bytesRead = instream.read(mybytearray, current, (mybytearray.length - current));
                    if (bytesRead >= 0)
                    {
                      current += bytesRead;
                    }
                  } while (bytesRead > -1);
                  bos_1.write(mybytearray, 0, current);
                  bos_1.flush();
                }              
                finally 
                {
                  if (fos_1 != null) fos_1.close();
                  if (bos_1 != null) bos_1.close();
                  if (sock != null) sock.close();
                }
              
                for (int i = 0; i < maxClientsCount; i++)
                {
                  if (threads[i] != null && threads[i].clientName != null && threads[i] != this && threads[i].clientName.equals(recv)) 
                  {
                    FileInputStream fis_1 = null;
                    BufferedInputStream bis_1 = null;
                    OutputStream os_1 = null;
                    ServerSocket servsock_1 = null;
                    Socket sock_1 = null;
                    try
                    {
                      threads[i].os.println("RECEIVING " + lastone + " to " + threads[i].clientName.substring(1) + " folder");
                      servsock_1 = new ServerSocket(8000);
                      try
                      {
                        sock_1 = servsock_1.accept();
                        File sendFile = new File ("/home/swapnil/Desktop/java_code/server/" + lastone);
                        byte [] mybytearray  = new byte [(int)sendFile.length()];
                        fis_1 = new FileInputStream(sendFile);
                        bis_1 = new BufferedInputStream(fis_1);
                        bis_1.read(mybytearray,0,mybytearray.length);
                        os_1 = sock_1.getOutputStream();
                        os_1.write(mybytearray,0,mybytearray.length);
                        os_1.flush();
                      }
                      finally
                      {
                        if (bis_1 != null) bis_1.close();
                        if (os_1 != null) os_1.close();
                        if (sock_1 != null) sock_1.close();
                      }
                    }
                    finally
                    {
                      if (servsock_1 != null) servsock_1.close();
                    }
                  }
                }
              }
            }
          }

        }
        else 
        {
          //The message is public, broadcast it to all other clients. 
          synchronized (this) 
          {
            for (int i = 0; i < maxClientsCount; i++) 
            {
              if (threads[i] != null && threads[i].clientName != null) 
              {
                threads[i].os.println("<<<" + name + ">>> " + line);
              }

            }
            System.out.println("<<<" + name + ">>> " + line);
          }
        }
      }
      synchronized (this) 
      {
        for (int i = 0; i < maxClientsCount; i++) 
        {
          if (threads[i] != null && threads[i] != this && threads[i].clientName != null) 
          {
            threads[i].os.println("##### " + name + " is exiting the chat application #####");
          }
        }
      }
      os.println("##### Bye " + name + " #####");

      //cleanup for the left client
      synchronized (this) 
      {
        for (int i = 0; i < maxClientsCount; i++)
        {
          if (threads[i] == this) 
          {
            threads[i] = null;
          }
        }
      }
      
      //Close the output stream, close the input stream, close the socket.
      is.close();
      os.close();
      fos.close();
      bos.close();
      bis.close();
      clientSocket.close();
    } 
    catch (IOException e) 
    {

    }
  }
}
