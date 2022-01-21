package helpers;

public class MsgToArea {
    public String msg;
    public String area;

    public MsgToArea(String msg, String area){
        this.msg = msg;
        this.area = area;
    }

    @Override
    public String toString() {
        return "  Mensagem da proteção civil para a área " + area +": "+ msg;
    }
}
