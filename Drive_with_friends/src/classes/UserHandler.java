package classes;

import com.google.gson.Gson;
import helpers.*;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class UserHandler extends Thread {

    static ArrayList<UserHandler> driverHandlersList = new ArrayList<>();

    private Socket socketUser;
    private BufferedReader in;
    private PrintWriter out;

    private MulticastSocket socketBroadcast;

    private Driver driver;

    private Gson gson;
    private String request;

    private SharedObject sharedObject;

    public UserHandler(Socket socketUser, MulticastSocket socketBroadcast, SharedObject sharedObject) throws IOException {
        driverHandlersList.add(this);
        this.socketUser = socketUser;
        this.out = new PrintWriter(this.socketUser.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(this.socketUser.getInputStream()));
        this.socketBroadcast = socketBroadcast;
        this.driver = null;
        this.gson = new Gson();
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
        String driver = gson.toJson(this.driver);

        out.println(driver);

        try {
            while (socketUser.isConnected()) {
                String srequest = in.readLine();

                Request r = gson.fromJson(srequest, Request.class);
                request = r.request;

                System.out.println("REuqest: " + request);

                switch (request) {
                    case (Variables.LOCATION):
                        location(gson.fromJson(r.msg, Location.class));
                        break;
                    case (Variables.AREA_ALERTS):
                        addAreaAlert(r.msg);
                        break;
                    case (Variables.AREA_CIRCUNDANTE):
                        areaCircundante(Double.parseDouble(r.msg));
                        break;
                    case (Variables.GROUP_JOIN):
                        joinGroup(gson.fromJson(r.msg, Group.class));
                        break;
                    case (Variables.GROUP_LEAVE):
                        leaveGroup(gson.fromJson(r.msg, Group.class));
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
                    case (Variables.MSG_TO_GROUP):
                        msgToGroup(gson.fromJson(r.msg, Message.class));
                        break;
                    case (Variables.MSG_TO_FRIEND):
                        msgToFriend(gson.fromJson(r.msg, Message.class));
                        break;
                    case (Variables.SHARED):
                        sendSharedObject();
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addAreaAlert(String l) {
        this.driver.addAlertLocation(l);
    }

    private void msgToGroup(Message m) throws IOException {
        Group g = getGroup(new Group(m.to));

        if (this.driver.containsGroup(g)) {
            String msg = " Mensagem no grupo: " + g.getName() + ", de: " + m.from + ": " + m.msg;

            System.out.println("IP: " + g.getIpS());

            DatagramPacket packetGroup = new DatagramPacket(msg.getBytes(), msg.getBytes().length, g.getIp(), Variables.PORT_MULTICAST);
            this.socketBroadcast.send(packetGroup);
        } else {
            Request r = new Request(Variables.RESPONSE, "Não pertence ao grupo!");
            this.out.println(gson.toJson(r));
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

    public void joinGroup(Group group) throws IOException {
        Group g = getGroup(group);

        if (g != null) {
            if (!this.driver.containsGroup(g)) {
                this.driver.addGroup(g);
                //this.socketBroadcast.joinGroup(g.getIp());
                Request r = new Request(Variables.GROUP_JOIN, g.getIpS());
                out.println(gson.toJson(r));
            } else {
                Request r = new Request(Variables.RESPONSE, "Voce ja se encontra no grupo");
                out.println(gson.toJson(r));
            }
        } else {
            Request r = new Request(Variables.RESPONSE, "O grupo nao existe");
            out.println(gson.toJson(r));
        }
    }

    public void leaveGroup(Group group) throws IOException {
        Group g = getGroup(group);

        if (g != null) {
            if (this.driver.containsGroup(g)) {
                this.driver.leaveGroup(g);
                this.socketBroadcast.leaveGroup(g.getIp());
                Request r = new Request(Variables.RESPONSE, "Groupo removido");
                out.println(gson.toJson(r));
            } else {
                Request r = new Request(Variables.RESPONSE, "Voce ja não se encontra no grupo");
                out.println(gson.toJson(r));
            }
        } else {
            Request r = new Request(Variables.RESPONSE, "O grupo nao existe");
            out.println(gson.toJson(r));
        }
    }

    public void sendSharedObject() {
        String sh = this.gson.toJson(this.sharedObject);

        Request r = new Request(Variables.SHARED, sh);

        out.println(gson.toJson(r));
    }

    public void createGroup(Group group) throws UnknownHostException {
        if (!existsGroup(group)) {
            String ip = SharedObject.IP_MULTICAST_COMUNITY + SharedObject.IP_MULTICAST_COMUNITY_LAST_VALUE++;
            InetAddress ipn = InetAddress.getByName(ip);
            Group g = new Group(ipn, group.getName());

            this.sharedObject.addGroup(g);

            Request r = new Request(Variables.RESPONSE, "Grupo criado com sucesso");
            out.println(gson.toJson(r));
        }else {
            Request r = new Request(Variables.RESPONSE, "Grupo com o mesmo nome já existe");
            out.println(gson.toJson(r));
        }
    }

    public boolean existsGroup(Group group) {
        for (Object o : this.sharedObject.getGroups()) {
            Group g = (Group) o;
            if (g.getName().equals(group.getName())) {
                return true;
            }
        }
        return false;
    }

    public Group getGroup(Group group) {
        for (Object o : this.sharedObject.getGroups()) {
            Group g = (Group) o;
            if (g.getName().equals(group.getName())) {
                return g;
            }
        }
        return null;
    }

    public void msgToComunity(MsgToComunity mtc) throws IOException {
        String ip = SharedObject.IP_MULTICAST_COMUNITY + SharedObject.IP_MULTICAST_COMUNITY_LAST_VALUE++;
        InetAddress address = InetAddress.getByName(ip);

        for (UserHandler sdh : driverHandlersList) {
            if(!(sdh.driver == null)){
                if ((!(sdh.driver.getUsername().equals(this.driver.getUsername())) &&
                        (sdh.driver.getCurrentLocation().distanceTo(mtc.location) <= 1))||
                        (!(sdh.driver.getUsername().equals(this.driver.getUsername())) &&
                                (sdh.driver.getCurrentLocation().distanceTo(mtc.location) <= sdh.driver.getRadiusLocalArea()))) {
                    //System.out.println("km: " + sdh.driver.getCurrentLocation().distanceTo(mtc.location));

                    Request r = new Request(Variables.GROUP_JOIN_COMMUNITY, ip);
                    sdh.out.println(gson.toJson(r));
                }
            }
        }

        String data = gson.toJson(mtc);
        DatagramPacket packetGroupMulticastCommunity = new DatagramPacket(data.getBytes(), data.getBytes().length,
                address, Variables.PORT_MULTICAST);

        this.socketBroadcast.send(packetGroupMulticastCommunity);
    }


    public void msgToFriend(Message m) throws IOException {
        boolean found = false;
        for (UserHandler sdh : driverHandlersList) {

            if (sdh.driver != null) {
                if (sdh.driver.getUsername().equals(m.to)) {
                    found = true;
                    if (sdh.driver.getFriends().contains(this.driver.getUsername())) {
                        sdh.driver.addMsg(m.from, m.msg);

                        Request r = new Request(Variables.MSG_FROM_FRIEND, gson.toJson(m));
                        sdh.out.println(gson.toJson(r));

                        r = new Request(Variables.RESPONSE, "Mensagem enviada");
                        out.println(gson.toJson(r));
                        break;
                    } else {
                        Request r = new Request(Variables.RESPONSE, gson.toJson("O utilizador não o tem como amigo!"));
                        out.println(gson.toJson(r));
                        break;
                    }
                }
            }
        }

        if (!found) {
            //Se o utilizador não está online
            for (Driver driver : sharedObject.getDrivers()) {
                if (driver != null) {
                    if (driver.getUsername().equals(m.to)) {
                        found = true;
                        if (driver.getFriends().contains(this.driver)) {
                            driver.addMsg(m.from, m.msg);
                            Request r = new Request(Variables.RESPONSE, gson.toJson("Mensagem enviada"));
                            out.println(gson.toJson(r));
                            break;
                        } else {
                            Request r = new Request(Variables.RESPONSE, gson.toJson("O utilizador não o tem como amigo!"));
                            out.println(gson.toJson(r));
                            break;
                        }
                    }
                }
            }
        }
        if (!found) {
            Request r = new Request(Variables.RESPONSE, gson.toJson("O utilizador não encontrado!"));
            out.println(gson.toJson(r));
        }
    }

    public void areaCircundante(Double radius) {
        this.driver.setRadius(radius);

        Request r = new Request(Variables.RESPONSE, "Raio da área circundante alterado com sucesso!");
        this.out.println(r);
    }

    public void location(Location location) {
        this.driver.setCurrentLocation(location);

        Request r = new Request(Variables.RESPONSE, "Localização alterada com sucesso!");
        this.out.println(gson.toJson(r));
    }

    public void addFriend(AddFriend ad) throws IOException {
        String username = ad.userToAdd;
        boolean found = false;

        for (Driver driver : this.sharedObject.getDrivers()) {
            if (driver.getUsername().equals(username)) {
                found = true;
                if (!this.driver.getFriends().contains(driver)) {
                    this.driver.addFriend(driver.getUsername());

                    Request r = new Request(Variables.RESPONSE, "Amigo adiconado");
                    out.println(gson.toJson(r));
                    break;

                } else {
                    Request r = new Request(Variables.RESPONSE, "O utilizador já se encontra nos seus amigos");
                    out.println(gson.toJson(r));
                }
            }
        }

        if (!found) {
            Request r = new Request(Variables.RESPONSE, "O utilizador não existe");
            out.println(gson.toJson(r));
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
            if (this.socketUser != null) {
                this.socketUser.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
