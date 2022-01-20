package classes;

import com.google.gson.Gson;
import helpers.*;
import others.Group;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class DriverHandler extends Thread {

    static ArrayList<DriverHandler> driverHandlersList = new ArrayList<>();

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

    Driver driver = null;

    Gson gson;
    String request;

    MulticastSocket socketMulticastComunity;
    InetAddress groupAdressComunity;

    SharedObject sharedObject;

    public DriverHandler(Socket socketClient, MulticastSocket socketBroadcast,
                         MulticastSocket socketNorte, MulticastSocket socketCentro, MulticastSocket socketSul,
                         InetAddress addressBroadcast, InetAddress addressNorte, InetAddress addressCentro,
                         InetAddress addressSul, SharedObject sharedObject)
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
        this.gson = new Gson();

        this.socketMulticastComunity = null;
        this.groupAdressComunity = null;

        this.sharedObject = sharedObject;
    }

    @Override
    public void run() {

        boolean login = false;

        while (!login) {
            try {
                String re = in.readLine();
                Request r = gson.fromJson(re, Request.class);
                request = r.request;

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

                System.out.println("REuqest: " + request);

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
                    case (Variables.GROUP_JOIN):
                        joinGroup(gson.fromJson(r.msg, Group.class));
                        break;
                    case (Variables.GROUP_LEAVE):
                        //leaveGroup(gson.fromJson(r.msg, Group.class));
                        break;
                    case (Variables.GROUP_CREATE):
                        createGroup(gson.fromJson(r.msg, Group.class));
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

        for (Object c : this.sharedObject.getDrivers()) {
            Driver driver = (Driver) c;
            if (driver.getUsername().equals(s.username)) {
                out.println(Variables.INVALID_SINGUP);
                return false;
            }
        }

        Driver newDriver = new Driver(s.name, s.username, s.password);
        this.driver = newDriver;

        this.sharedObject.addDriver(newDriver);

        out.println(Variables.VALID_SINGUP);

        return true;
    }

    public boolean login(Login login) throws IOException {

        if (this.sharedObject.getDrivers().isEmpty()) {
            out.println(Variables.INVALID_LOGIN);
            return false;
        }
        for (Object c : this.sharedObject.getDrivers()) {
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

    public void joinGroup(Group group){
        if(getGroup(group) != null){
            //this.driver.
        }
    }

    public void createGroup(Group group) throws UnknownHostException {
        if(!existsGroup(group)){
            InetAddress ip = InetAddress.getByName(Variables.IP_MULTICAST_COMUNITY + Variables.IP_MULTICAST_COMUNITY_VALUE++);
            Group g = new Group(ip, group.getName());

            this.sharedObject.addGroup(g);
        }
    }

    public boolean existsGroup(Group group){
        for (Object o : this.sharedObject.getGroups()) {
            Group g = (Group) o;
            if (g.getName().equals(group.getName())) {return true;}
        }
        return false;
    }

    public Group getGroup(Group group){
        for (Object o : this.sharedObject.getGroups()) {
            Group g = (Group) o;
            if (g.getName().equals(group.getName())) {return g;}
        }
        return null;
    }

    public void msgToEveryone(MsgToComunity mtc) throws IOException {
        DatagramPacket packetGroupMulticast = new DatagramPacket(mtc.msg.getBytes(), mtc.msg.getBytes().length, addressBroadcast, Variables.PORT_BROADCAST);
        this.socketBroadcast.send(packetGroupMulticast);
    }

    public void msgToComunity(MsgToComunity mtc) throws IOException {
        int port = Variables.PORT_MULTICAST_COMUNITY++;
        MulticastSocket socketMulticastComunity = new MulticastSocket(port);
        InetAddress groupAdressComunity = InetAddress.getByName(Variables.IP_MULTICAST_COMUNITY + Variables.IP_MULTICAST_COMUNITY_VALUE++);

        for (DriverHandler sdh : driverHandlersList) {
            if (!(sdh.driver == null) && !(sdh.driver.getUsername().equals(this.driver.getUsername()))&&(sdh.driver.getCurrentLocation().distanceTo(mtc.location) <= 1)) {
                System.out.println("km: " + sdh.driver.getCurrentLocation().distanceTo(mtc.location) );
                sdh.socketMulticastComunity = socketMulticastComunity;
                sdh.socketMulticastComunity.joinGroup(groupAdressComunity);
                sdh.starThreadMulticastComunity();
                Thread threadBroadCastComunity = new Thread(new ThreadMulticast(sdh.socketMulticastComunity));
                threadBroadCastComunity.start();
            }
        }

        String data = gson.toJson(mtc);

        DatagramPacket packetGroupMulticastComunity = new DatagramPacket(data.getBytes(),
                data.getBytes().length, groupAdressComunity, port);

        this.socketMulticastComunity.send(packetGroupMulticastComunity);
    }

    public void starThreadMulticastComunity(){
        Thread broadcastThread = new Thread(new ThreadMulticast(this.socketMulticastComunity));
        broadcastThread.start();
    }

    public void msgToUser(Message m) throws IOException {
        System.out.println("Size" + driverHandlersList.size());

        for (DriverHandler sdh : driverHandlersList) {
            System.out.println(sdh.driver.getUsername());
            if (sdh.driver.getUsername().equals(m.to)) {
                Request r = new Request(Variables.MSG_FROM_FRIEND, gson.toJson(m));
                sdh.out.println(gson.toJson(r));
            }
        }
    }

    public void areaCircundante(AreaCircundante ac) {
        this.driver.setRadius(ac.radius);
        out.println(Variables.AREA_CIRCUNDANTE_CHANGED);
    }

    public void location(Location location) {
        this.driver.setCurrentLocation(location);
        out.println(Variables.VALID_LOCATION);

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
        if (!this.sharedObject.getDrivers().isEmpty()) {
            for (Object c : this.sharedObject.getDrivers()) {
                Driver driver = (Driver) c;
                out.println(driver.getUsername());
            }
        }
        out.println(Variables.DONE);
    }

    public void addFriend(AddFriend ad) throws IOException {
        String username = ad.userToAdd;
        boolean found = false;

        if (!this.sharedObject.getDrivers().isEmpty()) {
            for (Object c : this.sharedObject.getDrivers()) {
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
