package classes;

import helpers.Location;
import helpers.Variables;
import others.Group;

import java.io.IOException;
import java.net.*;

public class Server {

    static SharedObject sharedObject = new SharedObject();

    ServerSocket serverSocket = null;

    MulticastSocket socketBroadcast = null;
    MulticastSocket socketNorte = null;
    MulticastSocket socketSul = null;
    MulticastSocket socketCentro = null;

    InetAddress addressBroadcast = null;
    InetAddress addressNorte = null;
    InetAddress addressCentro = null;
    InetAddress addressSul = null;

    public Server(){
        this.start();
    }

    public void start(){
        try {
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
        driver.setCurrentLocation(new Location(3.11,4.11));

        Driver driver2 = new Driver("driver 2","t2", "t");
        Driver driver3 = new Driver("ti","t3", "t");
        driver3.setCurrentLocation(new Location(3.112,4.112));
        sharedObject.addDriver(driver);
        sharedObject.addDriver(driver2);
        sharedObject.addDriver(driver3);

        try {
            Group group1 = new Group(InetAddress.getByName("230.10.1.2"), "grupo 1");
            Group group2 = new Group(InetAddress.getByName("230.10.1.3"), "grupo 2");
            Group group3 = new Group(InetAddress.getByName("230.10.1.4"), "grupo 3");

            sharedObject.addGroup(group1);
            sharedObject.addGroup(group2);
            sharedObject.addGroup(group3);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        System.out.println("Server waiting for connection.");

        try {
            while (!serverSocket.isClosed()) {
                Socket socketClient = serverSocket.accept();
                System.out.println("User connect");
                String clientType = new ClientType(socketClient).getClientType();

                switch (clientType) {
                    case Variables.DRIVER:
                        System.out.println("Passei aqui");
                        new DriverHandler(socketClient, socketBroadcast,
                                socketNorte, socketCentro, socketSul,
                                addressBroadcast, addressNorte, addressCentro,
                                addressSul, sharedObject).start();
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

        try {
            socketBroadcast.leaveGroup(addressBroadcast);
            socketBroadcast.close();
            socketNorte.leaveGroup(addressNorte);
            socketNorte.close();
            socketCentro.leaveGroup(addressCentro);
            socketCentro.close();
            socketSul.leaveGroup(addressSul);
            socketSul.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws IOException {
        Server server = new Server();
    }
}
