package novo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;

public class ServerDriverHandler extends Thread {
    Socket socketDriver;
    BufferedReader in;
    PrintWriter out;

    MulticastSocket socketBroadcast;

    MulticastSocket socketNorte;
    MulticastSocket socketSul;
    MulticastSocket socketCentro;

    InetAddress addressBroadcast;
    InetAddress addressNorte;
    InetAddress addressCentro;
    InetAddress addressSul;

    SharedObject sharedObject;

    SynchronizedArrayList<Driver> drivers;

    DatagramPacket packetBroadcast;
    DatagramPacket packetMulticastNorte;
    DatagramPacket packetMulticastCentro;
    DatagramPacket packetMulticastSul;

    public ServerDriverHandler(Socket socketClient, MulticastSocket socketBroadcast,
                               MulticastSocket socketNorte, MulticastSocket socketCentro, MulticastSocket socketSul,
                               InetAddress addressBroadcast, InetAddress addressNorte, InetAddress addressCentro,
                               InetAddress addressSul, SharedObject sharedObject, SynchronizedArrayList<Driver> drivers)
            throws IOException {

        this.socketDriver = socketClient;
        out = new PrintWriter(socketDriver.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socketDriver.getInputStream()));
        this.socketBroadcast = socketBroadcast;
        this.socketNorte = socketNorte;
        this.socketCentro = socketCentro;
        this.socketSul = socketSul;
        this.addressBroadcast = addressBroadcast;
        this.addressNorte = addressNorte;
        this.addressCentro = addressCentro;
        this.addressSul = addressSul;
        this.sharedObject = sharedObject;
        this.drivers = drivers;
    }

    @Override
    public void run() {
        String request;
        String inputName;
        String inputUsername;
        String inputPassword;

        boolean login = false;

        while (!login) {
            try {
                boolean userExists = false;
                request = in.readLine();
                System.out.println(request);
                inputName = in.readLine();
                inputUsername = in.readLine();
                inputPassword = in.readLine();
                if (request.equals(Variables.SINGUP)) {
                    for (Object c : drivers.getAll()) {
                        Driver driver = (Driver) c;
                        if (driver.getClass().equals(inputUsername)) {
                            out.println(Variables.INVALID_SINGUP);
                            userExists = true;
                        }
                    }
                    if (!userExists) {
                        login = true;
                        Driver newDriver = new Driver(inputName,inputUsername, inputPassword);
                        drivers.add(newDriver);
                        out.println(Variables.VALID_SINGUP);
                    }
                } else if (request.equals(Variables.LOGIN)) {
                    if (drivers.getAll().isEmpty()) {
                        out.println(Variables.INVALID_LOGIN);
                    } else {
                        for (Object c : drivers.getAll()) {

                            Driver driver = (Driver) c;
                            if (driver.getUsername().equals(inputUsername)) {
                                if (driver.getPassword().equals(inputPassword)) {
                                    out.println(Variables.VALID_LOGIN);
                                    login = true;
                                }
                            }
                        }
                        if (login == false) {
                            out.println(Variables.INVALID_LOGIN);
                        }
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        System.out.println("...new client connected" + " UserName: " );
        out.println(Variables.VALID_LOGIN);


    }
}
