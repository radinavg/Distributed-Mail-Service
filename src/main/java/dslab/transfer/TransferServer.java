package dslab.transfer;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dslab.ComponentFactory;
import dslab.util.Config;

public class TransferServer implements ITransferServer, Runnable {

    private final String componentId;
    private final Config config;
    private final InputStream in;
    private final PrintStream out;
    private final ServerSocket serverSocket;
    private final BlockingQueue<DMTP> dmtps = new ArrayBlockingQueue<>(10);
    private final ExecutorService listenerThreadPool = Executors.newFixedThreadPool(10);
    private final ExecutorService forwardingThreadPool = Executors.newFixedThreadPool(2);
    private TransferShell transferShell;
    /**
     * Creates a new server instance.
     *
     * @param componentId the id of the component that corresponds to the Config resource
     * @param config the component config
     * @param in the input stream to read console input from
     * @param out the output stream to write console output to
     */
    public TransferServer(String componentId, Config config, InputStream in, PrintStream out) {
        // TODO
        this.componentId = componentId;
        this.config = config;
        this.in = in;
        this.out = out;

        try {
            int dmtpPort = config.getInt("tcp.port");
            serverSocket = new ServerSocket(dmtpPort);
        } catch (IOException e) {
            throw new UncheckedIOException("Error while setting up server", e);
        }
    }

    @Override
    public void run() {
        // TODO
        transferShell = new TransferShell(componentId, config, in, out, this);
        Thread shellThread = new Thread(transferShell);
        shellThread.start();
        forwardingThreadPool.submit(new ForwardingThread(this));
        while(!serverSocket.isClosed())
        {
            try {
                Socket socket = serverSocket.accept();
                listenerThreadPool.submit(new ListenerThread(socket,this));
            } catch (IOException e) {
                throw new UncheckedIOException("Error while accepting thread", e);
            }
        }
    }

    @Override
    public void shutdown() {
        // TODO
        try {
            listenerThreadPool.shutdown();
            forwardingThreadPool.shutdown();
            if (serverSocket != null){
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error while closing server socket: " + e.getMessage());
        }
    }

    public void addDMTP(DMTP dmpt) throws InterruptedException {
        this.dmtps.put(dmpt);
    }

    public DMTP takeDMTP() throws InterruptedException {
        return this.dmtps.take();
    }

    public Config getConfig() {
        return config;
    }

    public ExecutorService getForwardingThreadPool() {
        return forwardingThreadPool;
    }

    public static void main(String[] args) throws Exception {
        ITransferServer server = ComponentFactory.createTransferServer(args[0], System.in, System.out);
        server.run();
    }

}
