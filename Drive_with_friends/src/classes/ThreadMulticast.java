package classes;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;

public class ThreadMulticast extends Thread {
    MulticastSocket multicastSocket;
    DatagramPacket packageReceived;
    String message = null;

    public ThreadMulticast(MulticastSocket multicastSocket) {
        this.multicastSocket = multicastSocket;
    }

    @Override
    public void run() {
        // Recebe mensagens do grupo multicast
        while (!multicastSocket.isClosed()) {
            byte[] buf = new byte[256];

            packageReceived = new DatagramPacket(buf, buf.length);

            try {
                multicastSocket.receive(packageReceived);
            } catch (IOException ex) {
                multicastSocket.close();
            }

            message = new String(packageReceived.getData(), 0, packageReceived.getLength());

            System.out.println(message);
        }
    }
}
