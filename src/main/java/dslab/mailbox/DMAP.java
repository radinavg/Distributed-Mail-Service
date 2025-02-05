package dslab.mailbox;

import dslab.transfer.DMTP;

import java.util.HashMap;
import java.util.Map;

public class DMAP {
  private boolean login;
  private String username;
  private String password;
  private HashMap<Integer, DMTP> dmtps = new HashMap<>();

  public DMAP(String username, String password) {
    this.username = username;
    this.password = password;
  }

  public boolean isLogin() {
    return login;
  }

  public void setLogin(boolean login) {
    this.login = login;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public void addDMTP (DMTP dmtp){
    int id = this.dmtps.size() + 1;
    this.dmtps.put(id, dmtp);
  }

  public String list()
  {
    String dmtps = "";
    if(this.dmtps.isEmpty())
    {
      return "no messages";
    }
    for(Map.Entry<Integer, DMTP> dmtpEntry : this.dmtps.entrySet())
    {
      dmtps += dmtpEntry.getKey() + " " + dmtpEntry.getValue().getFrom() + " " + dmtpEntry.getValue().getSubject() + '\n';
    }
    return dmtps;
  }

  public String show(int id)
  {
    return this.dmtps.get(id).toString();
  }

  public String delete(int id)
  {
    if(dmtps.size() >= id)
    {
      this.dmtps.remove(id);
      return "ok";
    }
    else
      return "error unknown message id";
  }
}

