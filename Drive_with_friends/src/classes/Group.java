package classes;

import java.net.InetAddress;

public class Group {
    private InetAddress ip;
    private String name;

    public Group(InetAddress ip,String name){
        this.ip = ip;
        this.name =  name;
    }

    public Group(String name){
        this.name =  name;
    }

    public String getIpS() {
        return ip.getHostName();
    }

    public InetAddress getIp() {
        return ip;
    }

    public void setIp(InetAddress ip) {
        this.ip = ip;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Group group = (Group) o;

        return name != null ? name.equals(group.name) : group.name == null;
    }

}
