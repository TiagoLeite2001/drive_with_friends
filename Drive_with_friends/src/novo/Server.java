package novo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    static SynchronizedArrayList<Driver> drivers = new SynchronizedArrayList<>();
    public static void main(String[] args) throws IOException {


        ServerSocket serverSocket = null;

        MulticastSocket socketBroadcast = null;
        MulticastSocket socketNorte = null;
        MulticastSocket socketSul = null;
        MulticastSocket socketCentro = null;

        InetAddress addressBroadcast = null;
        InetAddress addressNorte = null;
        InetAddress addressCentro = null;
        InetAddress addressSul = null;

        SharedObject sharedObject = null;


        try { // Inicializar ServerSocket para os Clients
            serverSocket = new ServerSocket(Variables.PORT_SERVER);
        } catch (IOException e) {
            System.out.println("Could not listen on port: " + Variables.PORT_SERVER);
            System.exit(-1);
        }

        try{// Inicializar Broadcast para enviar alertas
            socketBroadcast = new MulticastSocket(Variables.PORT_BROADCAST);
            addressBroadcast = InetAddress.getByName(Variables.IP_BROADCAST);
            socketBroadcast.joinGroup(addressBroadcast);
        }catch (IOException e) {
            System.out.println("Could not listen on port: " + Variables.PORT_BROADCAST);
            System.exit(-1);
        }


        try { // Inicializar MulticastSocket para enviar os alertas recebidos para os do Norte
            socketNorte = new MulticastSocket(Variables.PORT_MULTICAST_NORTE);
            addressNorte = InetAddress.getByName(Variables.IP_MULTICAST_NORTE);
            socketNorte.joinGroup(addressNorte);
        } catch (IOException e) {
            System.out.println("Could not listen on port: " + Variables.PORT_MULTICAST_NORTE);
            System.exit(-1);
        }

        try { // Inicializar MulticastSocket para enviar os alertas recebidos para os do Centro
            socketCentro = new MulticastSocket(Variables.PORT_MULTICAST_CENTRO);
            addressCentro = InetAddress.getByName(Variables.IP_MULTICAST_CENTRO);
            socketCentro.joinGroup(addressCentro);
        } catch (IOException e) {
            System.out.println("Could not listen on port: " + Variables.PORT_MULTICAST_CENTRO);
            System.exit(-1);
        }

        try { // Inicializar MulticastSocket para enviar os alertas recebidos para os do SUL
            socketSul = new MulticastSocket(Variables.PORT_MULTICAST_SUL);
            addressSul = InetAddress.getByName(Variables.IP_MULTICAST_SUL);
            socketSul.joinGroup(addressSul);
        } catch (IOException e) {
            System.out.println("Could not listen on port: " + Variables.PORT_MULTICAST_SUL);
            System.exit(-1);
        }

        Driver driver = new Driver("t","t", "t");
        Driver driver2 = new Driver("driver 2","t", "t");
        drivers.add(driver);
        drivers.add(driver2);

        System.out.println("Server waiting for connection.");

        try {
            while (!serverSocket.isClosed()) {
                Socket socketClient = serverSocket.accept();
                System.out.println("User connect");
                String clientType = new ClientProtocol(socketClient).getClientType();

                switch (clientType) {
                    case Variables.DRIVER:
                        System.out.println("Passei aqui");
                        new ServerDriverHandler(socketClient, socketBroadcast,
                                socketNorte, socketCentro, socketSul,
                                addressBroadcast, addressNorte, addressCentro,
                                addressSul, sharedObject, drivers).start();
                        break;
                    case Variables.PC:
                        //new ServidorThreadPC(socket, objSharing, serverSocketMulticast,serverSocketMulticastL,serverSocketBroadcast, groupAddress,groupAddressB,groupAddressL, PC).start();
                        break;
                    default:
                        System.out.println("Tentativa de conexão falhada.");
                        break;
                }
            }

        }catch (IOException e){
            e.printStackTrace();
        }

        socketBroadcast.leaveGroup(addressBroadcast);
        socketBroadcast.close();
        socketNorte.leaveGroup(addressNorte);
        socketNorte.close();
        socketCentro.leaveGroup(addressCentro);
        socketCentro.close();
        socketSul.leaveGroup(addressSul);
        socketSul.close();

    }
}
