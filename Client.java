import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

public class Client implements Runnable 
{

  // The client socket
  private static Socket clientSocket = null;
  // The output stream
  private static PrintStream os = null;
  // The input stream
  private static DataInputStream is = null;
  private static FileOutputStream fos = null;
  private static BufferedOutputStream bos = null;
  private static FileInputStream fis = null;
  private static BufferedInputStream bis = null;
  private static BufferedReader inputLine = null;
  private static boolean closed = false;
  private static OutputStream outstream = null;
  public static void main(String[] args) 
  {

    // The default port.
    int portNumber = 6669;
    // The default host.
    String host = "localhost";
    if (args.length < 2) 
    {
      //System.out.println("Usage: java MultiThreadChatClient <host> <portNumber>\n" + "Now using host=" + host + ", portNumber=" + portNumber);
    } 
    else 
    {
      host = args[0];
      portNumber = Integer.valueOf(args[1]).intValue();
    }
    // Open a socket on a given host and port. Open input and output streams.
    try 
    {
      clientSocket = new Socket(host, portNumber);
      inputLine = new BufferedReader(new InputStreamReader(System.in));
      os = new PrintStream(clientSocket.getOutputStream());
      is = new DataInputStream(clientSocket.getInputStream());
    } 
    catch (UnknownHostException e)
    {
      System.err.println("Don't know about host " + host);
    } 
    catch (IOException e) 
    {
      System.err.println("Couldn't get I/O for the connection to the host " + host);
    }

    if (clientSocket != null && os != null && is != null) 
    {
      try 
      {
        //Create a thread to read from the server
        new Thread(new Client()).start();
        while (!closed) 
        {
          //WRITING to server
          String temp = inputLine.readLine();
          if (temp.startsWith("bFILE"))
          {
            // bFILE /swapnil/image.jpg
            os.println(temp);
            FileInputStream fis_1 = null;
            BufferedInputStream bis_1 = null;
            OutputStream os_1 = null;
            ServerSocket servsock_1 = null;
            Socket sock_1 = null;
            int portNumber_1 = 6666;
            try
            {
              servsock_1 = new ServerSocket(portNumber_1);
              try
              {
                sock_1 = servsock_1.accept();
                String[] words = temp.split("\\s", 2);
                //words[1] contains the path for the file
                File myFile = new File ("/home/swapnil/Desktop/java_code" + words[1]);
                byte [] mybytearray  = new byte [(int)myFile.length()];
                fis_1 = new FileInputStream(myFile);
                bis_1 = new BufferedInputStream(fis_1);
                bis_1.read(mybytearray,0,mybytearray.length);
                os_1 = sock_1.getOutputStream();
                os_1.write(mybytearray,0,mybytearray.length);
                os_1.flush();
                System.out.println("File sent to everyone !!");
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
          else if (temp.startsWith("uFILE"))
          {
            // uFILE anurag /swapnil/image.jpg
            os.println(temp);
            FileInputStream fis_2 = null;
            BufferedInputStream bis_2 = null;
            OutputStream os_2 = null;
            ServerSocket servsock_2 = null;
            Socket sock_2 = null;
            int portNumber_2 = 3333;
            try
            {
              servsock_2 = new ServerSocket(portNumber_2);
              try
              {
                sock_2 = servsock_2.accept();
                String[] words = temp.split("\\s", 3);
                //words[1] contains the path for the file
                File myFile = new File ("/home/swapnil/Desktop/java_code" + words[2]);
                byte [] mybytearray  = new byte [(int)myFile.length()];
                fis_2 = new FileInputStream(myFile);
                bis_2 = new BufferedInputStream(fis_2);
                bis_2.read(mybytearray,0,mybytearray.length);
                os_2 = sock_2.getOutputStream();
                os_2.write(mybytearray,0,mybytearray.length);
                os_2.flush();
                System.out.println("File sent to " + words[1]);
              }
              finally 
              {
                if (bis_2 != null) bis_2.close();
                if (os_2 != null) os_2.close();
                if (sock_2 != null) sock_2.close();
              }
            }
            finally 
            {
              if (servsock_2 != null) servsock_2.close();
            }
          }
          else
          {
            os.println(temp);
          }
        }
        os.close();
        is.close();
        bis.close();
        clientSocket.close();
      } 
      catch (IOException e) 
      {
        System.err.println("IOException:  " + e);
      }
    }
  }

  //Create a thread to read from the server. (non-Javadoc)
  public void run() 
  {
    String line;
    try 
    {
      while ((line = is.readLine()) != null) 
      {
        System.out.println(line);
        if (line.indexOf("##### Bye") != -1)
          break;

        if (line.startsWith("RECEIVING"))
        {
          String[] words = line.split("\\s", 5);
          int bytesRead;
          int current = 0;
          FileOutputStream fos_1 = null;
          BufferedOutputStream bos_1 = null;
          Socket sock_1 = null;
          int FILE_SIZE = 21474836; 
          try
          {
            sock_1 = new Socket("localhost", 8000);
            byte [] mybytearray  = new byte [FILE_SIZE];
            InputStream is_1 = sock_1.getInputStream();
            fos_1 = new FileOutputStream("/home/swapnil/Desktop/java_code/" + words[3] + "/" + words[1]);
            bos_1 = new BufferedOutputStream(fos_1);
            bytesRead = is_1.read(mybytearray,0,mybytearray.length);
            current = bytesRead;

            do 
            {
              bytesRead = is_1.read(mybytearray, current, (mybytearray.length-current));
              if(bytesRead >= 0) current += bytesRead;
            } while(bytesRead > -1);

            bos_1.write(mybytearray, 0 , current);
            bos_1.flush();
          }
          finally
          {
            if (fos_1 != null) fos_1.close();
            if (bos_1 != null) bos_1.close();
            if (sock_1 != null) sock_1.close();
          }
        }   
      }
      closed = true;
    } 
    catch (IOException e) 
    {
      System.err.println("IOException:  " + e);
    }
  }
}
