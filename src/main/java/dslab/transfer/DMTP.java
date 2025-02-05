package dslab.transfer;

import java.util.ArrayList;

public class DMTP {

  private boolean begin;
  private ArrayList<String> to = new ArrayList<>();
  private String from;
  private String subject;
  private String data;

  public boolean getBegin() {
    return begin;
  }

  public void setBegin(boolean begin) {
    this.begin = begin;
  }

  public ArrayList<String> getTo() {
    return to;
  }

  public void setTo(ArrayList<String> to) {
    this.to = to;
  }

  public String getFrom() {
    return from;
  }

  public void setFrom(String from) {
    this.from = from;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }

  @Override
  public String toString() {
    String dmtp = "from " + this.from + '\n';
    for(String toElement : to)
    {
      dmtp = dmtp + "to " + toElement + " ";
    }
    dmtp += '\n' + "subject " + subject + '\n' + "data " + data;
    return dmtp;
  }
}
