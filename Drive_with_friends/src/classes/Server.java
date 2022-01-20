package classes;

import helpers.Location;
import helpers.Variables;
import others.Group;

import java.io.IOException;
import java.net.*;

class Server {

    static SharedObject sharedObject = new SharedObject();

    ServerSocket serverSocket;

    MulticastSocket socketBroadcast;
    InetAddress addressBroadcast;


    public Server() throws IOException {
        this.start();
    }

    public void start() throws IOException {

        serverSocket = new ServerSocket(Variables.PORT_SERVER);
        socketBroadcast = new MulticastSocket(Variables.PORT_BROADCAST);
        addressBroadcast = InetAddress.getByName(Variables.IP_BROADCAST);
        socketBroadcast.joinGroup(addressBroadcast);

        Driver driver = new Driver("t", "t", "t");
        driver.setCurrentLocation(new Location(3.11, 4.11));

        Driver driver2 = new Driver("driver 2", "t2", "t");
        Driver driver3 = new Driver("ti", "t3", "t");
        driver3.setCurrentLocation(new Location(3.112, 4.112));
        sharedObject.addDriver(driver);
        sharedObject.addDriver(driver2);
        sharedObject.addDriver(driver3);


        Group group1 = new Group(InetAddress.getByName("230.10.1.2"), "grupo 1");
        Group group2 = new Group(InetAddress.getByName("230.10.1.3"), "grupo 2");
        Group group3 = new Group(InetAddress.getByName("230.10.1.4"), "grupo 3");

        sharedObject.addGroup(group1);
        sharedObject.addGroup(group2);
        sharedObject.addGroup(group3);


        System.out.println("Server waiting for connection.");

        while (!serverSocket.isClosed()) {
            Socket socketClient = serverSocket.accept();
            System.out.println("User connected");
            String clientType = new ClientType(socketClient).getClientType();

            switch (clientType) {
                case Variables.DRIVER:
                    new DriverHandler(socketClient, socketBroadcast, sharedObject).start();
                    break;
                case Variables.PC:
                    break;
                default:
                    break;
            }
        }


        socketBroadcast.leaveGroup(addressBroadcast);
        socketBroadcast.close();

    }

    public static void main(String[] args) throws IOException {
        Server server = new Server();
    }
}
