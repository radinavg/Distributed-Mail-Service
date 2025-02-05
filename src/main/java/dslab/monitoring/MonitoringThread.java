package dslab.monitoring;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class MonitoringThread extends Thread{
    private DatagramSocket datagramSocket;
    private MonitoringServer monitoringServer;

    public MonitoringThread(DatagramSocket datagramSocket, MonitoringServer monitoringServer)
    {
      this.datagramSocket = datagramSocket;
      this.monitoringServer = monitoringServer;
    }

  @Override
  public void run() {
    byte[] buffer;
    DatagramPacket packet;
    try {
    while(true) {
      buffer = new byte[1024];
      packet = new DatagramPacket(buffer, buffer.length);
      datagramSocket.receive(packet);
      String data = new String(packet.getData(), 0, packet.getLength());
      String[] parts = data.split(" ");
      if (parts.length == 2) {
        String serverInfo = parts[0];
        String emailAddress = parts[1];
        this.monitoringServer.updateServerStats(serverInfo);
        this.monitoringServer.updateAddressStats(emailAddress);
      }
      }
    }catch (IOException e) {
      e.printStackTrace();
    }
  }
}
