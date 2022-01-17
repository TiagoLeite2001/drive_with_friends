package helpers;

public class Message {
    public String from;
    public String to;
    public String msg;

    public Message(String from, String to, String msg){
        this.to = to;
        this.from = from;
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "Message :" +
                "From='" + from + '\'' +
                ", " + msg + "\"";
    }
}
