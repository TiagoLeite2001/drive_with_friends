package helpers;

public class Message {
    public String from;
    public String to;
    public String msg;

    private Message(String from, String to, String msg){
        this.to = to;
        this.from = from;
        this.msg = msg;
    }
}
