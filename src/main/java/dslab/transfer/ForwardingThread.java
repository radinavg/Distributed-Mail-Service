package dslab.transfer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class ForwardingThread extends Thread {

  private TransferServer transferServer;
  private DMTP dmtp;
  private HashMap<String, String> mailboxServers = new HashMap<>();

  public ForwardingThread(TransferServer transferServer) {
    this.transferServer = transferServer;
  }

  @Override
  public void run() {
    try {
      BufferedReader domains = new BufferedReader(new FileReader("src/main/resources/domains.properties"));
      String in;
      domains.readLine();
      domains.readLine();
      while ((in = domains.readLine()) != null) {
        String[] parts = in.split("=");
        mailboxServers.put(parts[0], parts[1]);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    while (true) {
      boolean failure = false;
      try {
        dmtp = transferServer.takeDMTP();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      for (String recipient : dmtp.getTo()) {
        String[] parts = recipient.split("@");
        if (parts.length == 2) {
          String domain = parts[1];
          for (Map.Entry<String, String> mailbox : mailboxServers.entrySet()) {
            if(mailboxServers.containsKey(domain)){
            if (domain.equals(mailbox.getKey())) {
              String mailboxServerAddress = mailbox.getValue();
              if (mailboxServerAddress != null) {
                String[] addressParts = mailboxServerAddress.split(":");
                String mailboxServerHost = addressParts[0];
                int mailboxServerPort = Integer.parseInt(addressParts[1]);

                try (Socket mailboxSocket = new Socket(mailboxServerHost, mailboxServerPort);
                     PrintWriter writer = new PrintWriter(mailboxSocket.getOutputStream(), true);
                     BufferedReader reader = new BufferedReader(new InputStreamReader(mailboxSocket.getInputStream()))) {

                  String tos = "";
                  int i=0;
                  for(String to : dmtp.getTo())
                  {
                    if(i==dmtp.getTo().size()){
                      tos += to;
                    } else {
                      tos += to + ", ";
                      i++;
                    }
                  }

                  writer.println("begin");
                  reader.readLine();
                  writer.println("to " + tos);
                  reader.readLine();
                  writer.println("from " + dmtp.getFrom());
                  reader.readLine();
                  writer.println("subject " + dmtp.getSubject());
                  reader.readLine();
                  writer.println("data " + dmtp.getData());
                  reader.readLine();
                  writer.println("send");
                  reader.readLine();

                  String monitoringHost = transferServer.getConfig().getString("monitoring.host");
                  int tcpPort = transferServer.getConfig().getInt("tcp.port");
                  int monitoringPort = transferServer.getConfig().getInt("monitoring.port");

                  String monitoringInfo = monitoringHost + ":" + tcpPort + " " + dmtp.getFrom();

                  try (DatagramSocket socket = new DatagramSocket()) {
                    byte[] data = monitoringInfo.getBytes();
                    DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(monitoringHost), monitoringPort);
                    socket.send(packet);
                  } catch (IOException e) {
                    e.printStackTrace();
                  }
                } catch (IOException e) {
                  throw new UncheckedIOException("Error while accepting thread", e);
                }
              }
            }} else
              failure = true;
          }
        } else
          failure = true;
      }
      if(failure)
      {
        try {
          String ip = InetAddress.getLocalHost().getHostAddress();
          DMTP errorDMTP = new DMTP();
          String transferServerAddress = "mailer@" + ip;
          errorDMTP.setFrom(transferServerAddress);
          errorDMTP.getTo().add(dmtp.getFrom());
          errorDMTP.setSubject("Delivery Failure");
          errorDMTP.setData("Failed to deliver the message");
          transferServer.addDMTP(errorDMTP);
        } catch (UnknownHostException | InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }
}
