package helpers;

public class MsgToComunity {
    public String msg;
    public Location location;

    public MsgToComunity(String msg, Location location){
        this.msg = msg;
        this.location = location;
    }

    @Override
    public String toString() {
        return "  Mensagem da comunidade: " +
                " " + msg + '\'' +
                ", localização:" + location;
    }
}
