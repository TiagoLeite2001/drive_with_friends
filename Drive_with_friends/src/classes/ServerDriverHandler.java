package classes;

import com.google.gson.Gson;
import helpers.Login;
import helpers.Request;
import helpers.Singup;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerDriverHandler extends Thread {

    public static ArrayList<ServerDriverHandler> driverHandlersList = new ArrayList<>();

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

    Driver driver = null;
    SharedObject driverShared= null;

    DatagramPacket packetBroadcast;
    DatagramPacket packetMulticastNorte;
    DatagramPacket packetMulticastCentro;
    DatagramPacket packetMulticastSul;

    Gson gson;
    String request;

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
        this.driverShared = new SharedObject();
        this.gson = new Gson();
    }

    @Override
    public void run() {

        boolean login = false;

        while (!login) {
            try {
                String re = in.readLine();
                System.out.println("re:" + re);
                Request r = gson.fromJson(re, Request.class);
                request = r.request;
                System.out.println(request);

                switch (request) {
                    case Variables.SINGUP:
                        login = singUp(gson.fromJson(r.msg, Singup.class));
                        break;
                    case Variables.LOGIN:
                        System.out.println(request);
                        login = login(gson.fromJson(r.msg, Login.class));
                        break;
                    default:
                        break;
                }

            } catch (IOException ex) {
                closeEverything();
            }
        }
        System.out.println("Client connected");


        //User is logged in
        //Send user info
        Gson gson = new Gson();
        String driver = gson.toJson(this.driver);

        out.println(driver);

        /**
        //Ligar multicast das areas de alertas do utilizador
        if (!this.driver.getAlertsLocations().isEmpty()) {
            for (Object o : this.driver.getAlertsLocations()) {
                MulticastSocket ms = (MulticastSocket) o;
                Thread msThread = new Thread(new ThreadMulticast(ms));
                msThread.start();
            }
        }
         */

        try {
            while (socketDriver.isConnected()) {
                String srequest = in.readLine();

                Request r = gson.fromJson(srequest, Request.class);
                request = r.request;

                switch (request) {
                    case (Variables.LOCATION):
                        location();
                        break;
                    case (Variables.AREA_ALERTS):
                        areaAlerts();
                        break;
                    case ("AREA_CIRCUNDANTE"):
                        areaCircundante();
                        break;
                    case (Variables.ALL_USERS):
                        sendAllUsers();
                        break;
                    case (Variables.GROUPS):
                        break;
                    case (Variables.ADD_FRIEND):
                        addFriend();
                        break;
                    case ("MSG_TO_COMUNITY"):
                        msgToComunity();
                    case (Variables.MSG_TO_FRIEND):
                        msgToUser();

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean singUp(Singup s) throws IOException {

        for (Object c : drivers.getAll()) {
            Driver driver = (Driver) c;
            if (driver.getClass().equals(s.username)) {
                out.println(Variables.INVALID_SINGUP);
                return false;
            }
        }

        Driver newDriver = new Driver(s.name, s.username, s.password);
        this.driver = newDriver;
        drivers.add(newDriver);

        System.out.println("nao passei");

        out.println(Variables.VALID_SINGUP);

        return true;
    }

    public boolean login(Login login) throws IOException {

        if (drivers.getAll().isEmpty()) {
            out.println(Variables.INVALID_LOGIN);
            return false;
        }
        for (Object c : drivers.getAll()) {

            Driver driver = (Driver) c;
            if (driver.getUsername().equals(login.username)) {
                if (driver.getPassword().equals(login.password)) {
                    out.println(Variables.VALID_LOGIN);
                    System.out.println(Variables.VALID_LOGIN);
                    this.driver = driver;
                    return true;
                }
            }
        }

        out.println(Variables.INVALID_LOGIN);
        return false;
    }

    public void msgToComunity() throws IOException {
        String msg = this.driver.getUsername() + " : " + in.readLine();
        DatagramPacket packetGroupMulticast = new DatagramPacket(msg.getBytes(), msg.getBytes().length, addressBroadcast, Variables.PORT_BROADCAST);
        this.socketBroadcast.send(packetGroupMulticast);
    }

    public void msgToUser() throws IOException {
        String username = in.readLine();
        String msg = in.readLine();

        for (ServerDriverHandler sdh : driverHandlersList) {
            if (sdh.driver.getUsername().equals(username)) {
                sdh.out.println("Message from: " + this.driver.getUsername() + " : " + msg);
            }
        }
    }

    public void areaCircundante() {
        try {
            double radius = Double.parseDouble(in.readLine());
            this.driver.setRadius(radius);

            out.println("OK");
        } catch (IOException e) {
            out.println("ERROR");
            e.printStackTrace();
        }
    }

    public void location() {

        try {
            String sLatit = in.readLine();
            double latit = Double.parseDouble(sLatit);

            String sLongit = in.readLine();
            double longit = Double.parseDouble(sLongit);

            this.driver.setCurrentLocation(latit, longit);
            out.println(Variables.VALID_LOCATION);
            //out.println(this.driver.getCurrentLocation().toString());

        } catch (Exception e) {
            out.println(Variables.INVALID_LOCATION);
            e.printStackTrace();
        }
    }

    public void areaAlerts() {
        try {
            String input = in.readLine();
            switch (input) {
                case ("NORTH"):
                    MulticastSocket socketBroadcastNorth = new MulticastSocket(Variables.PORT_MULTICAST_NORTE);
                    InetAddress groupAdressNorte = InetAddress.getByName(Variables.IP_MULTICAST_NORTE);
                    socketBroadcastNorth.joinGroup(groupAdressNorte);
                    Thread threadBroadCastNorth = new Thread(new ThreadMulticast(socketBroadcastNorth));
                    threadBroadCastNorth.start();

                    out.println("OK");

                case ("CENTER"):
                    MulticastSocket socketBroadcastCenter = new MulticastSocket(Variables.PORT_MULTICAST_CENTRO);
                    InetAddress groupAdressCenter = InetAddress.getByName(Variables.IP_MULTICAST_CENTRO);
                    socketBroadcastCenter.joinGroup(groupAdressCenter);
                    Thread threadBroadCastCenter = new Thread(new ThreadMulticast(socketBroadcastCenter));
                    threadBroadCastCenter.start();

                    out.println("OK");

                case ("SOUTH"):
                    MulticastSocket socketBroadcastSouth = new MulticastSocket(Variables.PORT_MULTICAST_SUL);
                    InetAddress groupAdressSouth = InetAddress.getByName(Variables.IP_MULTICAST_SUL);
                    socketBroadcastSouth.joinGroup(groupAdressSouth);
                    Thread threadBroadCastSouth = new Thread(new ThreadMulticast(socketBroadcastSouth));
                    threadBroadCastSouth.start();

                    out.println("OK");
            }
        } catch (Exception e) {
            out.println(Variables.ERROR);
        }
    }

    public void sendAllUsers() {
        if (!Server.drivers.getAll().isEmpty()) {
            for (Object c : Server.drivers.getAll()) {
                Driver driver = (Driver) c;
                out.println(driver.getUsername());
            }
        }
        out.println(Variables.DONE);
    }

    public void addFriend() throws IOException {
        String username = in.readLine();
        boolean found = false;

        if (!Server.drivers.getAll().isEmpty()) {
            for (Object c : Server.drivers.getAll()) {
                Driver driver = (Driver) c;
                if (driver.getUsername().equals(username)) {
                    this.driver.addFriend(driver);
                    found = true;
                }
            }
        }
        if (found) {
            out.println(Variables.DONE);
        } else {
            out.println(Variables.ERROR);
        }
    }

    public void closeEverything() {
        try {
            if (this.in != null) {
                this.in.close();
            }
            if (this.out != null) {
                this.out.close();
            }
            if (this.socketDriver != null) {
                this.socketDriver.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
