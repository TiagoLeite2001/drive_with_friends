package classes;

import com.google.gson.Gson;
import helpers.*;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerDriverHandler extends Thread {

    static ArrayList<ServerDriverHandler> driverHandlersList = new ArrayList<>();

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
        driverHandlersList.add(this);
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
                        location(gson.fromJson(r.msg, Location.class));
                        break;
                    case (Variables.AREA_ALERTS):
                        areaAlerts(gson.fromJson(r.msg, AreaAlert.class));
                        break;
                    case (Variables.AREA_CIRCUNDANTE):
                        areaCircundante(gson.fromJson(r.msg, AreaCircundante.class));
                        break;
                    case (Variables.ALL_USERS):
                        sendAllUsers();
                        break;
                    case (Variables.GROUPS):
                        addGroup(gson.fromJson(r.msg, AddGroup.class));
                        break;
                    case (Variables.ADD_FRIEND):
                        addFriend(gson.fromJson(r.msg, AddFriend.class));
                        break;
                    case (Variables.MSG_TO_COMUNITY):
                        msgToComunity(gson.fromJson(r.msg, MsgToComunity.class));
                        break;
                    case (Variables.MSG_TO_FRIEND):
                        msgToUser(gson.fromJson(r.msg, Message.class));
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean singUp(Singup s) throws IOException {

        for (Object c : drivers.getAll()) {
            Driver driver = (Driver) c;
            if (driver.getUsername().equals(s.username)) {
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

    public void addGroup(AddGroup ad){
        //this.driver.addGroup;
    }

    public void msgToComunity(MsgToComunity mtc) throws IOException {
        DatagramPacket packetGroupMulticast = new DatagramPacket(mtc.msg.getBytes(), mtc.msg.getBytes().length, addressBroadcast, Variables.PORT_BROADCAST);
        this.socketBroadcast.send(packetGroupMulticast);
    }

    public void msgToUser(Message m) throws IOException {
        System.out.println("Size" + driverHandlersList.size());

        for (ServerDriverHandler sdh : driverHandlersList) {
            System.out.println(sdh.driver.getUsername());
            if (sdh.driver.getUsername().equals(m.to)) {

                Request r = new Request(Variables.MSG_FROM_FRIEND, gson.toJson(m));
                sdh.out.println(gson.toJson(r));
            }
        }
    }

    public void areaCircundante(AreaCircundante ac) {
        this.driver.setRadius(ac.radius);
    }

    public void location(Location location) {
        this.driver.setCurrentLocation(location);
    }

    public void areaAlerts(AreaAlert al) {
        try {
            String area = al.local;
            switch (area) {
                case ("NORTH"):
                    MulticastSocket socketBroadcastNorth = new MulticastSocket(Variables.PORT_MULTICAST_NORTE);
                    InetAddress groupAdressNorte = InetAddress.getByName(Variables.IP_MULTICAST_NORTE);
                    socketBroadcastNorth.joinGroup(groupAdressNorte);
                    Thread threadBroadCastNorth = new Thread(new ThreadMulticast(socketBroadcastNorth));
                    threadBroadCastNorth.start();

                case ("CENTER"):
                    MulticastSocket socketBroadcastCenter = new MulticastSocket(Variables.PORT_MULTICAST_CENTRO);
                    InetAddress groupAdressCenter = InetAddress.getByName(Variables.IP_MULTICAST_CENTRO);
                    socketBroadcastCenter.joinGroup(groupAdressCenter);
                    Thread threadBroadCastCenter = new Thread(new ThreadMulticast(socketBroadcastCenter));
                    threadBroadCastCenter.start();

                case ("SOUTH"):
                    MulticastSocket socketBroadcastSouth = new MulticastSocket(Variables.PORT_MULTICAST_SUL);
                    InetAddress groupAdressSouth = InetAddress.getByName(Variables.IP_MULTICAST_SUL);
                    socketBroadcastSouth.joinGroup(groupAdressSouth);
                    Thread threadBroadCastSouth = new Thread(new ThreadMulticast(socketBroadcastSouth));
                    threadBroadCastSouth.start();

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

    public void addFriend(AddFriend ad) throws IOException {
        String username = ad.userToAdd;
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