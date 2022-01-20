package helpers;

public class Message {
    public String from;
    public String to;
    public String msg;

    public Message(String from, String to, String msg){
        this.from = from;
        this.to = to;
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "Message from: " + from + ", " + msg + "\n";
    }
}
