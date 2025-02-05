package dslab.transfer;

import at.ac.tuwien.dsg.orvell.Shell;
import at.ac.tuwien.dsg.orvell.StopShellException;
import at.ac.tuwien.dsg.orvell.annotation.Command;
import dslab.util.Config;

import java.io.InputStream;
import java.io.PrintStream;

public class TransferShell implements Runnable{
  private Config config;
  private Shell shell;
  private TransferServer transferServer;

  public TransferShell(String componentId, Config config, InputStream inputStream, PrintStream outputStream, TransferServer transferServer) {
    this.config = config;
    this.transferServer = transferServer;

    shell = new Shell(inputStream, outputStream);
    shell.register(this);
    shell.setPrompt(componentId + "> ");
  }

  @Override
  public void run() {
    shell.run();
    System.out.println("Exiting the Transfer Server shell, goodbye!");
  }

  @Command
  public void shutdown() {
    transferServer.shutdown();
    throw new StopShellException();
  }
}
