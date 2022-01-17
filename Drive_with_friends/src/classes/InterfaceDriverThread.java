package classes;

import com.google.gson.Gson;
import helpers.Login;
import helpers.Request;
import helpers.Singup;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.Scanner;

public class InterfaceDriverThread extends Thread {
    static JFrame frame = new JFrame("Drive With Friends");
    static JPanel leftPanel = new JPanel();
    static JPanel rightPanel = new JPanel();

    static Socket socket = null;
    public static BufferedReader in;
    public static PrintWriter out;
    private Driver driver;
    Scanner scanner;
    String input;

    Gson gson;

    public InterfaceDriverThread(Socket socket) {
        try {
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
            MulticastSocket socketBroadcast = new MulticastSocket(Variables.PORT_BROADCAST);
            InetAddress groupAddress = InetAddress.getByName(Variables.IP_BROADCAST);
            socketBroadcast.joinGroup(groupAddress);
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

        switch (input){
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

        System.out.println("l94:" + input);

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
        System.out.println("l:130");
        System.out.println("Login efetuado como " + this.driver.getUsername());

        System.out.println("\n 0 - Sair" +
                "\n 1 - Localização" +
                "\n 2 - Amigos" +
                "\n 3 - Alertas" +
                "\n 4 - ");

        input=scanner.nextLine();
        switch (input){
            case "1":
                location();
            case "2":
                friends();
            case "3":
            case "4":
        }
    }

    public void friends(){
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
        while (!back){
            System.out.println("A sua localização atual é: " + this.driver.getCurrentLocation());
            System.out.println("-------------" +
                    "\n 1 - Nova localização" +
                    "\n 0 - Voltar");
            input = scanner.nextLine();
            switch (input){
                case "0":
                    back = true;
                    break;
                case "1":
                    newLocation();
            }
        }
    }

    public void newLocation() throws IOException {

        System.out.println("Introduza a latitude:");
        String latit = scanner.nextLine();

        System.out.println("Introduza a longitude: ");
        String longit = scanner.nextLine();

        out.println(Variables.LOCATION);
        out.println(latit);
        out.println(longit);

        if(in.readLine().equals(Variables.VALID_LOCATION)){
            System.out.println("Localização atualizada com sucesso");
            this.driver.setCurrentLocation(Double.parseDouble(latit), Double.parseDouble(longit));
        }
        else {
            System.out.println("Erro a alterar localização");
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
            if (this.socket != null) {
                this.socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
