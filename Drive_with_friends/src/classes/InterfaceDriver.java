package classes;

import com.google.gson.Gson;
import helpers.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Scanner;

public class InterfaceDriver extends Thread {
    MulticastSocket socketBroadcast;
    InetAddress addressBroadcast;

    static Socket socket = null;
    public static BufferedReader in;
    public static PrintWriter out;
    private Driver driver;
    Scanner scanner;
    String input;

    Gson gson;

    public InterfaceDriver(Socket socket) {
        try {
            this.socketBroadcast = new MulticastSocket(Variables.PORT_BROADCAST);
            this.addressBroadcast = InetAddress.getByName(Variables.IP_BROADCAST);
            this.socketBroadcast.joinGroup(this.addressBroadcast);

            this.socket = socket;
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            this.scanner = new Scanner(System.in);
            this.input = null;
            this.gson = new Gson();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        super.run();
        try {
            out.println("DRIVER");

            //Connect to BRODCAST GROUP
            Thread broadcastThread = new Thread(new ThreadMulticast(socketBroadcast));
            broadcastThread.start();

            startDriverInterface();

        } catch (IOException ex1) {
            closeEverything();
        }
    }

    public void startDriverInterface() throws IOException {
        System.out.println("Bem vindo" +
                "\n---------" +
                "\n 0 - Sair" +
                "\n 1 - Login" +
                "\n 2 - SIngUp" +
                "\n" +
                "\n Escolha a opção:");

        input = scanner.nextLine();

        switch (input) {
            case "0":
                break;
            case "1":
                login();
            case "2":
                singUp();
        }

    }

    public void login() throws IOException {
        System.out.print("Username: ");
        String username = scanner.nextLine();

        System.out.print("\nPassword");
        String pass = scanner.nextLine();

        Login l = new Login(username, pass);
        Request request = new Request(Variables.LOGIN, gson.toJson(l));

        out.println(gson.toJson(request));

        String input = in.readLine();

        if (input.equals(Variables.VALID_LOGIN)) {
            menu();
        } else {
            System.out.println("Dados inválidos!");
        }
    }

    public void singUp() throws IOException {
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
        System.out.println("l129 " + answer);

        switch (answer) {

            case Variables.VALID_SINGUP:
                menu();
            case Variables.INVALID_SINGUP:
                System.out.println("O username já está a ser utilizado!");
                break;
        }
    }

    public void updateDriverInfo() throws IOException {
        Gson gson = new Gson();
        String input = in.readLine();
        System.out.println("140" + input);
        this.driver = gson.fromJson(input, Driver.class);
    }

    public void menu() throws IOException {

        updateDriverInfo();
        System.out.println("Login efetuado como " + this.driver.getUsername());

        processIncomingMsg();
        newRequest();
    }

    public void processIncomingMsg() {
        new Thread(() -> {
            while (socket.isConnected()) {
                try {
                    Request r = gson.fromJson(in.readLine(), Request.class);

                    System.out.println(r.request);

                    switch (r.request) {
                        case Variables.OK:
                            System.out.println("Concluido com sucesso");
                            break;
                        case Variables.ERROR:
                            System.out.println("Erro");
                        case Variables.MSG_FROM_FRIEND:
                            System.out.println("Passei");
                            Message m = gson.fromJson(r.msg, Message.class);
                            System.out.println(m);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }


    public void newRequest() throws IOException {
        boolean goOn = true;
        while (goOn){
            System.out.println("\n " +
                    "0 - Sair" +
                    "\n 1 - Localização" +
                    "\n 2 - Amigos" +
                    "\n 3 - Alertas" +
                    "\n 4 - Mensagem para a comunidade" +
                    "\n 5 - Mensagem para amigo");

            input = scanner.nextLine();

            switch (input) {
                case "0":
                    goOn = false;
                    break;
                case "1":
                    location();
                    break;
                case "2":
                    friends();
                    break;
                case "3":
                    break;
                case "4":
                    msgToComunity();
                    break;
                case "5":
                    msgToFriend();
                    break;
            }
        }
    }

    public void msgToFriend() {
        System.out.println("");
        System.out.println("Introduza o username do amigo:");
        String username = scanner.nextLine();

        System.out.println("");
        System.out.println("Introduza a mensagem:");
        String msg = scanner.nextLine();


        Message m = new Message( this.driver.getUsername(),username, msg);
        Request r = new Request(Variables.MSG_TO_FRIEND, gson.toJson(m));

        out.println(gson.toJson(r));
    }

    //MSG PARA TODOS
    public void msgToALL() throws IOException {
        System.out.println("Introduza a mensagem para a comunidade: ");
        String msg = this.driver.getUsername() + " : " + scanner.nextLine();
        DatagramPacket packetGroupMulticast = new DatagramPacket(msg.getBytes(),
                msg.getBytes().length, this.addressBroadcast, Variables.PORT_BROADCAST);
        this.socketBroadcast.send(packetGroupMulticast);
    }

    public void msgToComunity() throws IOException {
        System.out.println("Introduza a mensagem para a comunidade: ");
        String msg = scanner.nextLine();

        MsgToComunity mtc = new MsgToComunity(msg,this.driver.getCurrentLocation());
        Request r = new Request(Variables.MSG_TO_COMUNITY, gson.toJson(mtc));

        out.println(gson.toJson(r));
    }

    public void friends() {
        //Imprimir os amigos

        System.out.println("username para mandar msg");
        String username = scanner.nextLine();
        System.out.println("escreva a msg");
        String msg = scanner.nextLine();

        out.println(Variables.MSG_TO_FRIEND);
        out.println(username);
        out.println(msg);

    }

    public void location() throws IOException {

        boolean back = false;
        while (!back) {
            System.out.println("A sua localização atual é: " + this.driver.getCurrentLocation());
            System.out.println("-------------" +
                    "\n 1 - Nova localização" +
                    "\n 0 - Voltar");
            input = scanner.nextLine();
            switch (input) {
                case "0":
                    back = true;
                    break;
                case "1":
                    newLocation();
                    break;
                default:
                    break;
            }
        }
    }

    public void newLocation() {
        System.out.println("Introduza a latitude:");
        String latit = scanner.nextLine();
        System.out.println("Introduza a longitude: ");
        String longit = scanner.nextLine();

        Location l = new Location(Double.parseDouble(latit), Double.parseDouble(longit));
        Request r = new Request(Variables.NEW_LOCATION, gson.toJson(l));

        this.driver.setCurrentLocation(l);

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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
