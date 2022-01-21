package classes;

import com.google.gson.Gson;
import helpers.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class InterfaceDriver extends Thread {
    private MulticastSocket socketBroadcast;
    private InetAddress addressBroadcast;

    private Socket socket;

    private BufferedReader in;
    private PrintWriter out;

    private Driver driver;
    private Scanner scanner;
    private String input;

    private Gson gson;

    private SharedObject sharedObject;

    ScheduledExecutorService scheduler;

    public InterfaceDriver(Socket socket) {
        try {
            this.socketBroadcast = new MulticastSocket(Variables.PORT_MULTICAST);
            this.addressBroadcast = InetAddress.getByName(Variables.IP_MULTICAST);
            this.socketBroadcast.joinGroup(addressBroadcast);

            this.socket = socket;
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            this.scanner = new Scanner(System.in);
            this.input = null;
            this.gson = new Gson();

            this.scheduler = Executors.newScheduledThreadPool(1);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        super.run();
        try {
            this.out.println(Variables.DRIVER);

            Thread broadcastThread = new Thread(new ThreadMulticast(socketBroadcast));
            broadcastThread.start();

            startDriverInterface();

        } catch (IOException ex1) {
            closeEverything();
        }
    }

    public void startDriverInterface() throws IOException {
        boolean login =false;

        while(!login){
            System.out.println("Bem vindo" +
                    "\n" +
                    "\n 0 - Sair" +
                    "\n 1 - Login" +
                    "\n 2 - SIngUp" +
                    "\n" +
                    "\n");
            this.input = this.scanner.nextLine();

            switch (this.input) {
                case "0":
                    break;
                case "1":
                    login = login();
                    break;
                case "2":
                    login = singUp();
                    break;
            }
        }
        menu();
    }

    public boolean login() throws IOException {
        System.out.print("Username: ");
        String username = this.scanner.nextLine();

        System.out.print("Password:");
        String pass = this.scanner.nextLine();

        Login l = new Login(username, pass);
        Request request = new Request(Variables.LOGIN, gson.toJson(l));

        this.out.println(gson.toJson(request));

        String input = this.in.readLine();

        if (input.equals(Variables.VALID_LOGIN)) {
            return true;
        } else {
            System.out.println("Dados inválidos!");
        }
        return false;
    }

    public boolean singUp() throws IOException {
        System.out.println("Nome");
        String name = scanner.nextLine();

        System.out.println("Username");
        String username = scanner.nextLine();

        System.out.println("Password");
        String password = scanner.nextLine();

        Singup singup = new Singup(name, username, password);
        Request request = new Request(Variables.SINGUP, gson.toJson(singup));

        out.println(gson.toJson(request));

        String answer = in.readLine();

        switch (answer) {
            case Variables.VALID_SINGUP:
                return true;
            case Variables.INVALID_SINGUP:
                System.out.println("O username já está a ser utilizado!");
                return false;
        }
        return false;
    }

    public void joinGroup(String ip) {
        try {
            this.socketBroadcast.joinGroup(InetAddress.getByName(ip));
            System.out.println("Adicionado ao grupo");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void joinGroupC(String ip) {
        try {
            this.socketBroadcast.joinGroup(InetAddress.getByName(ip));
        } catch (IOException e) {
        }
    }

    public void getDriverInfo() throws IOException {
        String input = in.readLine();
        this.driver = gson.fromJson(input, Driver.class);
    }

    public void startBroadcastGroupsAndAlertsLocations() throws IOException {
        ArrayList<Group> groups = this.driver.getGroups();
        if(this.driver.getGroups().size() > 0){
            for (Group g:groups) {
                this.socketBroadcast.joinGroup(g.getIp());
            }
        }
    }

    public void updateDriverInfo() {
        Request r = new Request(Variables.SHARED, "");
        this.out.println(gson.toJson(r));
    }

    public void menu() throws IOException {
        getDriverInfo();
        startBroadcastGroupsAndAlertsLocations();

        System.out.println("Login efetuado como " + this.driver.getUsername());

        Request r = new Request(Variables.SHARED, "");
        out.println(gson.toJson(r));

        processIncomingMsg();
        newRequest();
    }

    public void processIncomingMsg() {
        new Thread(() -> {
            while (socket.isConnected()) {
                try {
                    String s = in.readLine();
                    System.out.println(s);
                    Request r = gson.fromJson(s, Request.class);

                    switch (r.request) {
                        case Variables.RESPONSE:
                            System.out.println(r.msg);
                            updateDriverInfo();
                            break;
                        case Variables.MSG_FROM_FRIEND:
                            Message m = gson.fromJson(r.msg, Message.class);
                            System.out.println(m.toString());
                            updateDriverInfo();
                            break;
                        case Variables.SHARED:
                            this.sharedObject = gson.fromJson(r.msg, SharedObject.class);
                            this.driver = this.sharedObject.getDriver(this.driver);
                            break;
                        case Variables.GROUP_JOIN:
                            joinGroup(r.msg);
                            updateDriverInfo();
                            break;
                        case Variables.GROUP_JOIN_COMMUNITY:
                            joinGroupC(r.msg);
                            updateDriverInfo();
                            break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public void newRequest() throws IOException {

        boolean goOn = true;
        while (goOn) {
            System.out.println("\n " +
                    "\n0 - Sair" +
                    "\n 1 - Localização" +
                    "\n 2 - Amigos" +
                    "\n 3 - Alertas" +
                    "\n 4 - Mensagens" +
                    "\n 5 - Grupos" +
                    "\n" +
                    "");

            this.input = this.scanner.nextLine();

            switch (this.input) {
                case "0":
                    goOn = false;
                    break;
                case "1":
                    location();
                    updateDriverInfo();
                    break;
                case "2":
                    friends();
                    updateDriverInfo();
                    break;
                case "3":
                    alerts();
                    updateDriverInfo();
                    break;
                case "4":
                    sendMessage();
                    updateDriverInfo();
                    break;
                case "5":
                    groups();
                    updateDriverInfo();
                    break;
            }
        }
        System.exit(-1);
    }

    public void alerts() throws IOException {
        boolean goOn = true;
        while (goOn) {
            System.out.println("\n " +
                    "\n Alertas" +
                    "\n 0 - Sair" +
                    "\n 1 - Adicionar localização" +
                    "\n 2 - Remover localização" +
                    "");

            this.input = this.scanner.nextLine();

            switch (this.input) {
                case "0":
                    goOn = false;
                    updateDriverInfo();
                    break;
                case "1":
                    addAlert();
                    updateDriverInfo();
                    break;
                case "2":
                    removeAlert();
                    updateDriverInfo();
                    break;
            }
        }
    }

    public void addAlert() throws IOException {
        System.out.println("\n Adicionar uma localização aos alertas");
        System.out.println("\n  Localizações disponiveis:" +
                "\n Norte" +
                "\n Centro" +
                "\n Sul");
        System.out.println("\n Introduza a localização: ");

        this.input = scanner.nextLine().toUpperCase(Locale.ROOT);

        switch (this.input) {
            case "NORTE":
                this.socketBroadcast.joinGroup(InetAddress.getByName(Variables.IP_MULTICAST_NORTE));
                if(this.driver.addAlertLocation(input)){
                    addedLocation(input);
                }
                else {
                    System.out.println("A localização já nos seus alertas");
                }
                break;
            case "CENTRO":
                this.socketBroadcast.joinGroup(InetAddress.getByName(Variables.IP_MULTICAST_CENTRO));
                this.driver.addAlertLocation(input);
                if(this.driver.addAlertLocation(input)){
                    addedLocation(input);
                }
                else {
                    System.out.println("A localização já nos seus alertas");
                }
            case "SUL":
                this.socketBroadcast.joinGroup(InetAddress.getByName(Variables.IP_MULTICAST_SUL));
                this.driver.addAlertLocation(input);
                if(this.driver.addAlertLocation(input)){
                    addedLocation(input);
                }
                else {
                    System.out.println("A localização já nos seus alertas");
                }
            default:break;
        }
    }

    public void addedLocation(String l){
        Request r = new Request(Variables.AREA_ALERTS, l);
        out.println(gson.toJson(r));
        System.out.println(" Alertas do " + l + " adicionados.");
    }

    public void removedLocation(String l){
        System.out.println(" Alertas do " + l + " removidos.");
    }

    public void removeAlert() throws IOException {
        System.out.println("\n Remover uma localização dos alertas");
        System.out.println("\n  Localizações disponiveis:" +
                "\nNorte" +
                "\nCentro" +
                "\nSul");
        System.out.println("\n Introduza a localização: ");

        this.input = scanner.nextLine().toUpperCase(Locale.ROOT);

        switch (this.input) {
            case "NORTE" -> {
                this.socketBroadcast.leaveGroup(InetAddress.getByName(Variables.IP_MULTICAST_NORTE));
                removedLocation(input);
            }
            case "CENTRO" -> {
                this.socketBroadcast.leaveGroup(InetAddress.getByName(Variables.IP_MULTICAST_CENTRO));
                removedLocation(input);
            }
            case "SUL" -> {
                this.socketBroadcast.leaveGroup(InetAddress.getByName(Variables.IP_MULTICAST_SUL));
                removedLocation(input);
            }
            default -> {
            }
        }
    }

    public void sendMessage() throws IOException {
        boolean goOn = true;
        while (goOn) {
            System.out.println("\n " +
                    " Mensagens" +
                    "0 - Sair" +
                    "\n 1 - Para um amigo" +
                    "\n 2 - Para um grupo" +
                    "\n 3 - Para a comunidade" +
                    "\n 4 - Ver mensagens de um amigo" +
                    "");

            input = scanner.nextLine();

            switch (input) {
                case "0" -> {
                    goOn = false;
                    updateDriverInfo();
                }
                case "1" -> {
                    msgToFriend();
                    updateDriverInfo();
                }
                case "2" -> {
                    msgToGroup();
                    updateDriverInfo();
                }
                case "3" -> {
                    msgToComunity();
                    updateDriverInfo();
                }
                case "4" -> {
                    msgFromFriend();
                    updateDriverInfo();
                }
            }
        }
    }

    public void groups() throws IOException {
        boolean goOn = true;
        while (goOn) {
            System.out.println("\n " +
                    " Grupos" +
                    "\n 0 - Sair" +
                    "\n 1 - Adicionar grupo" +
                    "\n 2 - Remover grupo" +
                    "\n 3 - Criar grupo" +
                    "");

            input = scanner.nextLine();

            switch (input) {
                case "0" -> {
                    goOn = false;
                    updateDriverInfo();
                }
                case "1" -> {
                    addGroup();
                    updateDriverInfo();
                }
                case "2" -> {
                    removeGroup();
                    updateDriverInfo();
                }
                case "3" -> {
                    createGroup();
                    updateDriverInfo();
                }
            }
        }
    }

    public void createGroup(){
        System.out.println("Introduza o nome do grupo");
        String name = scanner.nextLine();

        Group group = new Group(name);

        Request r = new Request(Variables.GROUP_CREATE, gson.toJson(group));

        out.println(gson.toJson(r));
    }


    public void addGroup(){
        ArrayList<Group> gs = this.sharedObject.getGroups();
        if(gs!= null){
            System.out.println("Lista de grupos");
            for (Group g : gs) {
                System.out.println(g.getName());
            }
        }

        System.out.println("\n Juntar a um grupo");
        System.out.println("\n Introduza o nome do grupo: ");

        String nome = scanner.nextLine();
        Group g = new Group(nome);

        Request r = new Request(Variables.GROUP_JOIN, gson.toJson(g));

        out.println(gson.toJson(r));
    }

    public void removeGroup(){
        ArrayList<Group> gs = this.sharedObject.getGroups();
        if(gs!= null){
            System.out.println("Lista de grupos");
            for (Group g : gs) {
                System.out.println(g.getName());
            }
        }

        System.out.println("\n Remover um grupo");
        System.out.println("\n Introduza o nome do grupo: ");

        String nome = scanner.nextLine();
        Group g = new Group(nome);

        Request r = new Request(Variables.GROUP_LEAVE, gson.toJson(g));

        out.println(gson.toJson(r));
    }

    public void msgToGroup() {
        System.out.println("");
        System.out.println("Introduza o nome do grupo:");
        String grupo = scanner.nextLine();

        System.out.println("");
        System.out.println("Introduza a mensagem:");
        String msg = scanner.nextLine();


        Message m = new Message(this.driver.getUsername(), grupo, msg);
        Request r = new Request(Variables.MSG_TO_GROUP, gson.toJson(m));

        out.println(gson.toJson(r));
    }

    public void msgFromFriend(){
        System.out.println("");
        System.out.println("Introduza o username do amigo:");
        String username = scanner.nextLine();

        String msg = this.driver.getMsgFromDriver(username);

        if (msg != null){
            System.out.println(" Mensagens de " + username + "\n" + msg);
        }
        else {
            System.out.println("Não foram encontradas mensagens desse utilizador");
        }
    }

    public void msgToFriend() {
        System.out.println("");
        System.out.println("Introduza o username do amigo:");
        String username = scanner.nextLine();

        System.out.println("");
        System.out.println("Introduza a mensagem:");
        String msg = scanner.nextLine();


        Message m = new Message(this.driver.getUsername(), username, msg);
        Request r = new Request(Variables.MSG_TO_FRIEND, gson.toJson(m));

        out.println(gson.toJson(r));
    }

    //MSG PARA TODOS
    public void msgToALL() throws IOException {
        System.out.println("Introduza a mensagem para a comunidade: ");
        String msg = this.driver.getUsername() + " : " + scanner.nextLine();
        DatagramPacket packetGroupMulticast = new DatagramPacket(msg.getBytes(),
                msg.getBytes().length, this.addressBroadcast, Variables.PORT_MULTICAST);
        this.socketBroadcast.send(packetGroupMulticast);
    }

    public void msgToComunity() {
        System.out.println("Introduza a mensagem para a comunidade: ");
        String msg = scanner.nextLine();

        MsgToComunity mtc = new MsgToComunity(msg, this.driver.getCurrentLocation());
        Request r = new Request(Variables.MSG_TO_COMUNITY, gson.toJson(mtc));

        out.println(gson.toJson(r));
    }

    public void friends() {
        System.out.println("Lista de amigos:");

        for (String username:this.driver.getFriends()) {
            System.out.println("- " + username);
        }

        System.out.println("\n 0 - Voltar" +
                "\n 1 - Adicionar amigo" +
                "\n 2 - Remover amigo");

        input = scanner.nextLine();
        switch (input) {
            case "0":
                break;
            case "1":
                addFriend();
                break;
            case "2":
                removeFriend();
            default:
                break;
        }

    }

    public void addFriend(){
        System.out.println("Introduza a username do utilizador:");
        String username = scanner.nextLine();

        AddFriend ad = new AddFriend(username);
        Request r = new Request(Variables.ADD_FRIEND, gson.toJson(ad));

        out.println(gson.toJson(r));
    }

    public void removeFriend(){

    }

    public void location() {

        boolean back = false;
        while (!back) {
            System.out.println("A sua localização atual é: " + this.driver.getCurrentLocation());
            System.out.println("\n" +
                    "\n 0 - Voltar" +
                    "\n 1 - Nova localização" +
                    "\n 2 - Definir raio circudante" +
                    "\n ");
            input = scanner.nextLine();
            switch (input) {
                case "0":
                    back = true;
                    updateDriverInfo();
                    break;
                case "1":
                    newLocation();
                    updateDriverInfo();
                    break;
                case "2":
                    updateRadius();
                    updateDriverInfo();
                default:
                    break;
            }
        }
    }

    public void updateRadius(){
        System.out.println("Introduza o raio:");
        String radius = scanner.nextLine();

        Request r = new Request(Variables.AREA_CIRCUNDANTE, radius);

        this.out.println(gson.toJson(r));
    }

    public void newLocation() {
        System.out.println("Introduza a latitude:");
        String latit = scanner.nextLine();
        System.out.println("Introduza a longitude: ");
        String longit = scanner.nextLine();

        Location l = new Location(Double.parseDouble(latit), Double.parseDouble(longit));
        Request r = new Request(Variables.LOCATION, gson.toJson(l));

        out.println(gson.toJson(r));
    }


    public void closeEverything() {
        try {
            if (this.in != null) {
                this.in.close();
            }
            if (this.out != null) {
                this.out.close();
            }
            if (this.socket != null) {
                this.socket.close();
            }
            if (this.socketBroadcast != null){
                this.socketBroadcast.close();
            }
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
