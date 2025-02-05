package dslab.mailbox;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LogInThread extends Thread{
  private final ServerSocket dmapSocket;
  private final MailboxServer mailboxServer;
  private final ExecutorService clientThreadPool = Executors.newFixedThreadPool(10);

  public LogInThread(ServerSocket dmapSocket, MailboxServer mailboxServer)
  {
    this.dmapSocket = dmapSocket;
    this.mailboxServer = mailboxServer;
  }

  @Override
  public void run() {
    while(!dmapSocket.isClosed()){
      try {
        Socket socket = dmapSocket.accept();
        clientThreadPool.submit(new ClientThread(socket,mailboxServer));
      } catch (IOException e) {
        throw new UncheckedIOException("Error while accepting thread", e);
      }
    }
  }

  public void shutdown() {
    clientThreadPool.shutdown();
  }
}
