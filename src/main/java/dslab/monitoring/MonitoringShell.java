package dslab.monitoring;

import at.ac.tuwien.dsg.orvell.Shell;
import at.ac.tuwien.dsg.orvell.StopShellException;
import at.ac.tuwien.dsg.orvell.annotation.Command;
import dslab.transfer.TransferServer;
import dslab.util.Config;

import java.io.InputStream;
import java.io.PrintStream;

public class MonitoringShell implements Runnable {
  private Config config;
  private Shell shell;
  private MonitoringServer monitoringServer;

  public MonitoringShell(String componentId, Config config, InputStream inputStream, PrintStream outputStream, MonitoringServer monitoringServer) {
    this.config = config;
    this.monitoringServer = monitoringServer;

    shell = new Shell(inputStream, outputStream);
    shell.register(this);
    shell.setPrompt(componentId + "> ");
  }

  @Override
  public void run() {
    shell.run();
    System.out.println("Exiting the Monitoring Server shell, goodbye!");
  }

  @Command
  public void shutdown() {
    monitoringServer.shutdown();
    throw new StopShellException();
  }

  @Command
  public void addresses() {
    monitoringServer.addresses();
  }

  @Command
  public void servers() {
    monitoringServer.servers();
  }
}
