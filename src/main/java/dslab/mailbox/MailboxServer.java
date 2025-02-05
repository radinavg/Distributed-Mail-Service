package dslab.mailbox;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dslab.ComponentFactory;
import dslab.transfer.TransferShell;
import dslab.util.Config;

public class MailboxServer implements IMailboxServer, Runnable {

    private final String componentId;
    private final Config config;
    private final InputStream in;
    private final PrintStream out;
    private final ServerSocket dmtpSocket;
    private final ServerSocket dmapSocket;
    private final ConcurrentHashMap<String,DMAP> dmaps = new ConcurrentHashMap<>();
    private final ExecutorService receivingThreadPool = Executors.newFixedThreadPool(10);
    private final ExecutorService logInTheadPool = Executors.newFixedThreadPool(10);
    private final String domain;
    private MailboxShell mailboxShell;


    /**
     * Creates a new server instance.
     *
     * @param componentId the id of the component that corresponds to the Config resource
     * @param config the component config
     * @param in the input stream to read console input from
     * @param out the output stream to write console output to
     */
    public MailboxServer(String componentId, Config config, InputStream in, PrintStream out) {
        // TODO
        this.componentId = componentId;
        this.config = config;
        this.in = in;
        this.out = out;
        this.domain = config.getString("domain");

        try {
            int dmtpPort = config.getInt("dmtp.tcp.port");
            int dmapPort = config.getInt("dmap.tcp.port");
            dmtpSocket = new ServerSocket(dmtpPort);
            dmapSocket = new ServerSocket(dmapPort);

            String usersProperties = config.getString("users.config");
            BufferedReader users = new BufferedReader(new FileReader("src/main/resources/" + usersProperties));
            String line;
            while ((line = users.readLine()) != null) {
                String[] parts = line.split("=");
                DMAP dmap = new DMAP(parts[0],parts[1]);
                dmaps.put(dmap.getUsername(),dmap);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Error while setting up server", e);
        }
    }

    @Override
    public void run() {
        // TODO
        mailboxShell = new MailboxShell(componentId, config, in, out, this);
        Thread shellThread = new Thread(mailboxShell);
        shellThread.start();
        logInTheadPool.submit(new LogInThread(this.dmapSocket, this));
        while(!dmtpSocket.isClosed())
        {
            try {
                Socket socket = dmtpSocket.accept();
                receivingThreadPool.submit(new ReceivingThread(socket,this));
            } catch (IOException e) {
                throw new UncheckedIOException("Error while accepting thread", e);
            }
        }
    }

    @Override
    public void shutdown() {
        // TODO
        try {
            if (dmtpSocket != null){
                dmtpSocket.close();
            }
            if (dmapSocket != null){
                dmapSocket.close();
            }
            receivingThreadPool.shutdownNow();
            logInTheadPool.shutdownNow();
        } catch (IOException e) {
            System.err.println("Error while closing server socket: " + e.getMessage());
        }
    }

    public void addDMAP(DMAP dmap) {
        this.dmaps.put(dmap.getUsername(),dmap);
    }

    public DMAP getDMAP(String username) {
        return this.dmaps.get(username);
    }

    public boolean findUser(String username)
    {
        return this.dmaps.containsKey(username);
    }

    public Config getConfig() {
        return config;
    }

    public String getDomain() {
        return domain;
    }

    public static void main(String[] args) throws Exception {
        IMailboxServer server = ComponentFactory.createMailboxServer(args[0], System.in, System.out);
        server.run();
    }
}
