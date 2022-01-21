package classes;

import com.google.gson.Gson;
import helpers.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Locale;

public class CPHandler extends Thread{
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private MulticastSocket socketBroadcast;

    private Gson gson;
    private String request;

    public CPHandler(Socket socket, MulticastSocket socketBroadcast) throws IOException {
        this.socket = socket;
        this.out = new PrintWriter(this.socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        this.socketBroadcast = socketBroadcast;
        this.gson = new Gson();
    }

    @Override
    public void run() {

        while (socket.isConnected()) {
            try {
                System.out.println("Entao");
                String re = in.readLine();
                Request r = gson.fromJson(re, Request.class);
                request = r.request;

                switch (request) {
                    case Variables.LOGIN:
                        login(gson.fromJson(r.msg, Login.class));
                        break;
                    default:
                        break;
                }
            } catch (IOException ex) {
                closeEverything();
            }
        }
        System.out.println("CP connected");
    }

    public void login(Login login) throws IOException {
        if (login.username.equals("PC") && login.password.equals("PC")) {
            Request r = new Request(Variables.VALID_LOGIN, "");
            out.println(gson.toJson(r));
            menu();
        }else {
            Request r = new Request(Variables.INVALID_LOGIN, "Login inválido");
            out.println(gson.toJson(r));
        }
    }

    public void menu(){
        while (this.socket.isConnected()){
            try {
                String re = in.readLine();
                Request r = gson.fromJson(re, Request.class);
                request = r.request;

                switch (request) {
                    case Variables.MSG_TO_COMUNITY:
                        msgToEveryone(r.msg);
                        break;
                    case Variables.MSG_TO_LOCATION:
                        msgToLocation(gson.fromJson(r.msg, MsgToArea.class));
                    default:
                        break;
                }
            } catch (IOException ex) {
                closeEverything();
            }
        }
    }

    public void msgToEveryone(String msg) throws IOException {
        DatagramPacket packetGroupMulticast = new DatagramPacket(msg.getBytes(), msg.getBytes().length,
                InetAddress.getByName(Variables.IP_MULTICAST), Variables.PORT_MULTICAST);
        this.socketBroadcast.send(packetGroupMulticast);
    }

    public void msgToLocation(MsgToArea msg) throws IOException {
        switch (msg.area) {
            case "NORTE":
                DatagramPacket packetGroupMulticastN = new DatagramPacket(msg.msg.getBytes(), msg.msg.getBytes().length,
                        InetAddress.getByName(Variables.IP_MULTICAST_NORTE), Variables.PORT_MULTICAST);
                this.socketBroadcast.send(packetGroupMulticastN);
                break;
            case "CENTRO":
                DatagramPacket packetGroupMulticastC = new DatagramPacket(msg.msg.getBytes(), msg.msg.getBytes().length,
                        InetAddress.getByName(Variables.IP_MULTICAST_CENTRO), Variables.PORT_MULTICAST);
                this.socketBroadcast.send(packetGroupMulticastC);
                break;
            case "SUL":
                DatagramPacket packetGroupMulticastS = new DatagramPacket(msg.msg.getBytes(), msg.msg.getBytes().length,
                        InetAddress.getByName(Variables.IP_MULTICAST_SUL), Variables.PORT_MULTICAST);
                this.socketBroadcast.send(packetGroupMulticastS);
                break;
            default:break;
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
