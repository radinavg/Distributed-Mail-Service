package dslab.mailbox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

public class ClientThread extends Thread{
  private Socket socket;
  private MailboxServer mailboxServer;

  public ClientThread(Socket socket, MailboxServer mailboxServer) {
    this.socket = socket;
    this.mailboxServer = mailboxServer;
  }

  @Override
  public void run() {
    boolean isQuit = false;
    while(!socket.isClosed() && !isQuit)
    {
      try {
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter writer = new PrintWriter(socket.getOutputStream(),true);
        writer.println("ok DMAP");
        DMAP dmap = new DMAP(null,null);

        String instruction;
        while ((instruction = reader.readLine()) != null) {
          String[] parts = instruction.split(" ");
          String command = parts[0].toLowerCase();
          switch (command) {
            case "login":
              if(parts.length == 3)
              {
                if(this.mailboxServer.findUser(parts[1])){
                  String password = this.mailboxServer.getDMAP(parts[1]).getPassword();
                  if(parts[2].equals(password))
                  {
                    dmap = this.mailboxServer.getDMAP(parts[1]);
                    dmap.setLogin(true);
                    writer.println("ok");
                  } else {
                    writer.println("error wrong password");
                  }
                } else {
                  writer.println("error unknown user");
                }
              } else {
                writer.println("error invalid instruction");
              }
              break;
            case "list":
              if(dmap.isLogin())
              {
                String message = dmap.list();
                String[] messageParts = message.split("\n");
                for(int i = 0; i < messageParts.length; i++)
                {
                  writer.println(messageParts[i]);
                }
              } else {
                writer.println("error not logged in");
              }
              break;
            case "show":
              if(dmap.isLogin()){
                if(parts.length == 2)
                {
                  int id = Integer.parseInt(parts[1]);
                  String message = dmap.show(id);
                  String[] messageParts = message.split("\n");
                  for(int i = 0; i < messageParts.length; i++)
                  {
                    writer.println(messageParts[i]);
                  }
                } else {
                  writer.println("error invalid instruction");
                }
              } else {
                writer.println("error not logged in");
              }
              break;
            case "delete":
              if(dmap.isLogin()){
                if(parts.length == 2)
                {
                  int id = Integer.parseInt(parts[1]);
                  String message = this.mailboxServer.getDMAP(dmap.getUsername()).delete(id);
                  writer.println(message);
                } else {
                  writer.println("error invalid instruction");
                }
              } else {
                writer.println("error not logged in");
              }
              break;
            case "logout":
              if(dmap.isLogin())
              {
                dmap.setLogin(false);
                writer.println("ok");
              } else {
                writer.println("error logout without login");
              }
              break;
            case "quit":
              writer.println("ok bye");
              isQuit = true;
              break;
            default:
              writer.println("error protocol error");
              break;
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        try {
          socket.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }
}
