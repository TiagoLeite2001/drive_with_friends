package others;

import classes.ThreadMulticast;
import com.google.gson.Gson;
import helpers.Location;
import helpers.Variables;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Main {
    public static void main(String[] args) throws IOException {
        Location l2 = new Location(3.11,4.11);
        Location l1 = new Location(3.112,4.112);
        Location l3 = new Location(0,0);

        Gson g = new Gson();

        MulticastSocket m = new MulticastSocket(Variables.PORT_MULTICAST);



        MulticastSocket ms = new MulticastSocket(Variables.PORT_MULTICAST);
        InetAddress n = InetAddress.getByName("230.22.22.2");


        String msg = "OLAAAA";
        DatagramPacket packetGroup = new DatagramPacket(msg.getBytes(), msg.getBytes().length, n, Variables.PORT_MULTICAST);


        Thread multicastThread = new Thread(new ThreadMulticast(ms));
        multicastThread.start();



        ms.joinGroup(n);

        m.send(packetGroup);

    }
}
