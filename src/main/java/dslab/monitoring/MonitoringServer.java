package dslab.monitoring;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dslab.ComponentFactory;
import dslab.transfer.TransferShell;
import dslab.util.Config;

public class MonitoringServer implements IMonitoringServer {

    private final String componentId;
    private final Config config;
    private final InputStream in;
    private final PrintStream out;
    private final DatagramSocket datagramSocket;
    private final HashMap<String, Integer> addressStats = new HashMap<>();
    private final HashMap<String, Integer> serverStats = new HashMap<>();
    //private final ExecutorService monitoringThreadPool = Executors.newFixedThreadPool(10);
    private MonitoringThread monitoringThread;
    private MonitoringShell monitoringShell;

    /**
     * Creates a new server instance.
     *
     * @param componentId the id of the component that corresponds to the Config resource
     * @param config the component config
     * @param in the input stream to read console input from
     * @param out the output stream to write console output to
     */
    public MonitoringServer(String componentId, Config config, InputStream in, PrintStream out) {
        // TODO
        this.componentId = componentId;
        this.config = config;
        this.in = in;
        this.out = out;

        try {
            int udpPort = config.getInt("udp.port");
            datagramSocket = new DatagramSocket(udpPort);
        } catch (IOException e) {
            throw new UncheckedIOException("Error while setting up server", e);
        }
    }

    @Override
    public void run() {
        // TODO
        monitoringThread = new MonitoringThread(datagramSocket,this);
        monitoringThread.start();
        monitoringShell = new MonitoringShell(componentId, config, in, out, this);
        Thread shellThread = new Thread(monitoringShell);
        shellThread.start();
    }

    @Override
    public void addresses() {
        // TODO
        for (Map.Entry<String, Integer> entry : addressStats.entrySet()) {
            out.println(entry.getKey() + " " + entry.getValue());
        }
    }

    @Override
    public void servers() {
        // TODO
        for (Map.Entry<String, Integer> entry : serverStats.entrySet()) {
            out.println(entry.getKey() + " " + entry.getValue());
        }
    }

    @Override
    public void shutdown() {
        // TODO
        monitoringThread.interrupt();
        datagramSocket.close();
    }

    public void updateServerStats(String serverInfo) {
        if(serverStats.containsKey(serverInfo))
        {
            for (Map.Entry<String, Integer> entry : serverStats.entrySet()) {
                if(entry.getKey().equals(serverInfo))
                {
                    entry.setValue(entry.getValue() + 1);
                }
            }
        } else {
            serverStats.put(serverInfo,1);
        }

    }

    public void updateAddressStats(String emailAddress) {
        if(addressStats.containsKey(emailAddress))
        {
            for (Map.Entry<String, Integer> entry : addressStats.entrySet()) {
                if(entry.getKey().equals(emailAddress))
                {
                    entry.setValue(entry.getValue() + 1);
                }
            }
        } else {
            addressStats.put(emailAddress,1);
        }
    }

    public static void main(String[] args) throws Exception {
        IMonitoringServer server = ComponentFactory.createMonitoringServer(args[0], System.in, System.out);
        server.run();
    }

}
