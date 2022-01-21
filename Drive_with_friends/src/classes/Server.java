package classes;

import com.google.gson.Gson;
import helpers.Variables;

import javax.swing.plaf.synth.SynthOptionPaneUI;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.*;

class Server {

    static SharedObject sharedObject = new SharedObject();
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    ServerSocket serverSocket;

    MulticastSocket socketBroadcast;
    InetAddress addressBroadcast;

    static Gson gson;


    public Server() throws IOException {
        this.gson = new Gson();
        this.start();
    }

    public void start() throws IOException {

        Reader reader = Files.newBufferedReader(Paths.get("data.json"));

        sharedObject= gson.fromJson(reader, SharedObject.class);

        serverSocket = new ServerSocket(Variables.PORT_SERVER);
        socketBroadcast = new MulticastSocket(Variables.PORT_MULTICAST);
        addressBroadcast = InetAddress.getByName(Variables.IP_MULTICAST);
        socketBroadcast.joinGroup(addressBroadcast);

        saveData();

        System.out.println("Server waiting for connection.");

        while (!serverSocket.isClosed()) {
            Socket socketClient = serverSocket.accept();
            System.out.println("User connected");
            String clientType = new ClientType(socketClient).getClientType();

            switch (clientType) {
                case Variables.DRIVER:
                    new UserHandler(socketClient, socketBroadcast, sharedObject).start();
                    break;
                case Variables.PC:
                    new CPHandler(socketClient, socketBroadcast).start();
                    break;
                default:
                    break;
            }
        }

        socketBroadcast.leaveGroup(addressBroadcast);
        socketBroadcast.close();

    }

    public void saveData() {
        Runnable save = () -> {
            try {
                saveDataa();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        ScheduledFuture<?> beeperHandle =
                scheduler.scheduleAtFixedRate(save, 10, 10, SECONDS);
        Runnable canceller = () -> beeperHandle.cancel(false);
        scheduler.schedule(canceller, 1, HOURS);
    }

    public void saveDataa() throws IOException {
        String s = gson.toJson(sharedObject);
        File file = new File("data.json");
        file.createNewFile();
        FileWriter writer = new FileWriter(file);
        writer.write(s);
        writer.flush();
        writer.close();
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server();


    }
}
