package dslab.mailbox;

import dslab.transfer.DMTP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ReceivingThread extends Thread{
  private Socket socket;
  private MailboxServer mailboxServer;

  public ReceivingThread(Socket socket, MailboxServer mailboxServer)
  {
    this.socket = socket;
    this.mailboxServer = mailboxServer;
  }

  public void run() {
    boolean isQuit = false;
    while (!socket.isClosed() && !isQuit) {
      try {
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

        writer.println("ok DMTP");

        DMTP dmpt = new DMTP();

        String instruction;
        while ((instruction = reader.readLine()) != null) {
          String[] parts = instruction.split(" ");

          String command = parts[0].toLowerCase();
          switch (command) {
            case "begin":
              if (!dmpt.getBegin()) {
                dmpt.setBegin(true);
                writer.println("ok");
              } else {
                writer.println("error second begin");
              }
              break;
            case "to":
              if (dmpt.getBegin()) {
                if (parts.length > 1) {
                  dmpt.getTo().clear();
                  String joined = "";
                  for (int i = 1; i < parts.length; i++) {
                    joined += parts[i];
                  }
                  String[] joinedSplit = joined.split(",");
                  for(int i = 0; i < joinedSplit.length; i++)
                  {
                    String[] emailParts = joinedSplit[i].trim().split("@");
                    if (emailParts.length == 2) {
                      if (this.mailboxServer.findUser(emailParts[0]) && emailParts[1].equals(this.mailboxServer.getDomain())) {
                        dmpt.getTo().add(joinedSplit[i]);
                      } else {
                        writer.println("error unknown recipient " + emailParts[0]);
                      }
                    } else {
                      writer.println("error invalid recipient");
                    }
                  }
                writer.println("ok " + dmpt.getTo().size());
                } else {
                writer.println("error missing recipient");
                }
              } else {
                writer.println("error missing begin");
              }
              break;
            case "from":
              if (dmpt.getBegin()) {
                if (parts.length > 1) {
                  dmpt.setFrom(parts[1]);
                  writer.println("ok");
                } else {
                  writer.println("error missing sender");
                }
              } else {
                writer.println("error missing begin");
              }
              break;
            case "subject":
              if (dmpt.getBegin()) {
                if (parts.length > 1) {
                  String joined = "";
                  for (int i = 1; i < parts.length; i++) {
                    if(i == 1)
                    {
                      joined += parts[i];
                    } else {
                      joined += " " + parts[i];
                    }
                  }
                  dmpt.setSubject(joined);
                  writer.println("ok");
                } else {
                  writer.println("error missing subject");
                }
              } else {
                writer.println("error missing begin");
              }
              break;
            case "data":
              if (dmpt.getBegin()) {
                if (parts.length > 1) {
                  String joined = "";
                  for (int i = 1; i < parts.length; i++) {
                    if(i == 1)
                    {
                      joined += parts[i];
                    } else {
                      joined += " " + parts[i];
                    }
                  }
                  dmpt.setData(joined);
                  writer.println("ok");
                } else {
                  writer.println("error missing data");
                }
              } else {
                writer.println("error missing begin");
              }
              break;
            case "send":
              if (!dmpt.getBegin() || dmpt.getFrom() == null || dmpt.getTo().isEmpty()) {
                writer.println("error incomplete message");
              } else {
                String user = dmpt.getTo().get(0);
                String[] emailParts = user.split("@");
                this.mailboxServer.getDMAP(emailParts[0]).addDMTP(dmpt);
                writer.println("ok");
              }
              break;
            case "quit":
              isQuit = false;
              writer.println("ok bye");

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