package classes;

import com.google.gson.Gson;
import helpers.Login;
import helpers.MsgToArea;
import helpers.Request;
import helpers.Variables;

import java.io.*;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class InterfaceCP extends Thread{

    private MulticastSocket socketBroadcast;
    private InetAddress addressBroadcast;

    private Socket socket;

    private BufferedReader in;
    private PrintWriter out;

    private Gson gson;

    ScheduledExecutorService scheduler;

    private Scanner scanner;
    private String input;

    public InterfaceCP(Socket socket) {
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

        this.out.println(Variables.PC);

        try {
            startCPInterface();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void  startCPInterface() throws IOException {

        boolean login = false;
        while (!login){
            System.out.println("Proteção civil     (username:PC, password:PC)");

            System.out.println("Username: ");
            input= scanner.nextLine();

            System.out.println("Password: ");
            String pass= scanner.nextLine();

            Login l = new Login(input, pass);

            Request r = new Request(Variables.LOGIN, gson.toJson(l));
            this.out.println(gson.toJson(r));

            Request r2 = gson.fromJson(in.readLine(), Request.class);
            if (r2.request.equals(Variables.VALID_LOGIN)){
                login = true;
            }
        }

        menu();

        closeEverything();
    }

    public void menu() throws IOException {
        while (true){
            System.out.println("" +
                    "\n 1 - Enviar alerta para uma localidade" +
                    "\n 2 - Enviar alerta para toda a gente");

            input = scanner.nextLine();

            switch (input){
                case "1":
                    sendAlert();
                    break;
                case "2":
                    sendMessageToEveryone();
                    break;
            }
        }
    }

    public void sendAlert() throws IOException {
        System.out.println("\n  Localizações disponiveis:" +
                "\nNorte" +
                "\nCentro" +
                "\nSul");
        System.out.println("\n Introduza a localização: ");
        this.input = scanner.nextLine().toUpperCase(Locale.ROOT);

        System.out.println("\n Introduza a mensagem: ");
        String msg = scanner.nextLine();

        MsgToArea m = new MsgToArea(msg, input);

        Request r = new Request(Variables.MSG_TO_LOCATION, gson.toJson(m));
        this.out.println(gson.toJson(r));
    }

    public void sendMessageToEveryone(){
        System.out.println("Introduza a mensagem: ");

        String msg = " Proteção civil: " + scanner.nextLine();

        Request r = new Request(Variables.MSG_TO_COMUNITY, msg);

        this.out.println(gson.toJson(r));
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
