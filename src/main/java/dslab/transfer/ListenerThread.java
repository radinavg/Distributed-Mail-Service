package dslab.transfer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.net.Socket;

public class ListenerThread extends Thread{
  private Socket socket;
  private TransferServer transferServer;


  public ListenerThread(Socket socket, TransferServer transferServer) {
    this.socket = socket;
    this.transferServer = transferServer;
  }

  public void run()
  {
      while (socket.isConnected())
      {
        try {
          BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
          PrintWriter writer = new PrintWriter(socket.getOutputStream(),true);

          writer.println("ok DMTP");
          DMTP dmpt = new DMTP();

          String instruction;
          while ((instruction = reader.readLine()) != null) {
            String[] parts = instruction.split(" ");

            String command = parts[0].toLowerCase();
            switch (command) {
              case "begin":
                if(!dmpt.getBegin())
                {
                  dmpt.setBegin(true);
                  writer.println("ok");
                }
                else {
                  writer.println("error second begin");
                }
                break;
              case "to":
                if(dmpt.getBegin())
                {
                  if (parts.length > 1) {
                    dmpt.getTo().clear();
                    String joined = "";
                    for (int i = 1; i < parts.length; i++) {
                      joined += parts[i];
                    }
                    String[] joinedSplit = joined.split(",");
                    for (int i = 0; i < joinedSplit.length; i++) {
                      dmpt.getTo().add(joinedSplit[i].trim());
                    }
                    writer.println("ok " + dmpt.getTo().size());
                    writer.flush();
                  } else {
                    writer.println("error missing recipient");
                  }
                } else {
                  writer.println("error missing begin");
                }
                break;
              case "from":
                if(dmpt.getBegin())
                {
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
                if(dmpt.getBegin())
                {
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
                if(dmpt.getBegin())
                {
                  if(parts.length > 1)
                  {
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
                  }
                  else {
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
                  this.transferServer.addDMTP(dmpt);
                  dmpt = new DMTP();
                  writer.println("ok");
                }
                break;
              case "quit":
                writer.println("ok bye");
                socket.close();
                break;
              default:
                writer.println("error protocol error");
                break;
            }
          }
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
  }
}
