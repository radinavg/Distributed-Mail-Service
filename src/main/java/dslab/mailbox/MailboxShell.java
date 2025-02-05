package dslab.mailbox;

import at.ac.tuwien.dsg.orvell.Shell;
import at.ac.tuwien.dsg.orvell.StopShellException;
import at.ac.tuwien.dsg.orvell.annotation.Command;
import dslab.util.Config;

import java.io.InputStream;
import java.io.PrintStream;

public class MailboxShell implements Runnable{
  private Config config;
  private Shell shell;
  private MailboxServer mailboxServer;

  public MailboxShell(String componentId, Config config, InputStream inputStream, PrintStream outputStream, MailboxServer mailboxServer)
  {
    this.config = config;
    this.mailboxServer = mailboxServer;

    shell = new Shell(inputStream, outputStream);
    shell.register(this);
    shell.setPrompt(componentId + "> ");
  }

  @Override
  public void run() {
    shell.run();
    System.out.println("Exiting the Mailbox Server shell, goodbye!");
  }

  @Command
  public void shutdown() {
    mailboxServer.shutdown();
    throw new StopShellException();
  }
}
