package entities;

import classes.Driver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    static ArrayList<Driver> drivers;
    private ServerSocket serverSocket;

    public Server(ServerSocket serverSocket){
        this.drivers = new ArrayList<>();
        this.serverSocket = serverSocket;
    }

    public void startServer(){
        try {
            while (!serverSocket.isClosed()){
                Socket socket = serverSocket.accept();
                System.out.println("Cliente connectado");
                ClientHandler clientHandler = new ClientHandler(socket);

                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void closeServerSocket(){
        try {
            if (serverSocket != null){
                serverSocket.close();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static String login(String username, String password){
        Driver driver = getDriver(username);
        if(driver != null && driver.getPassword().equals(password)){ return username; }
        return "error";
    }

    //Apagar?
    public static boolean existsClient(String username){
        Driver driver = new Driver(username);

        if(drivers.contains(driver)){
            return true;
        }
        return false;
    }

    public static Driver getDriver(String username){
        for(Driver driver : drivers){
            if(driver.getUsername().equals(username)){
                return driver;
            }
        }
        return null;
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(1234);
        Server server = new Server(serverSocket);
        server.startServer();
    }

    /**
     * private final static int UDP_PORT = 8100;
     *     private final static int TCP_PORT = 8200;
     *
     *     public static void main(String[] args) {
     *         new Thread(() -> executeTcpServer()).start();
     *         new Thread(() -> executeUdpServer()).start();
     *     }
     *
     *     public static void executeTcpServer() {
     *         try (ServerSocket serverSocket = new ServerSocket(TCP_PORT)) {
     *             while (true) {
     *                 System.out.println("waiting for TCP connection...");
     *                 // Blocks until a connection is made
     *                 final Socket socket = serverSocket.accept();
     *                 final InputStream inputStream = socket.getInputStream();
     *                 String text = new BufferedReader(
     *                         new InputStreamReader(inputStream, StandardCharsets.UTF_8))
     *                         .lines()
     *                         .collect(Collectors.joining("\n"));
     *                 System.out.println(text);
     *             }
     *         } catch (Exception exception) {
     *             exception.printStackTrace();
     *         }
     *     }
     *
     *     public static void executeUdpServer() {
     *         try (DatagramSocket socket = new DatagramSocket(UDP_PORT)) {
     *             while (true) {
     *                 byte[] packetBuffer = new byte[2024];
     *                 final DatagramPacket packet = new DatagramPacket(packetBuffer, packetBuffer.length);
     *                 System.out.println("waiting for UDP packet...");
     *                 // Blocks until a packet is received
     *                 socket.receive(packet);
     *                 final String receivedPacket = new String(packet.getData()).trim();
     *                 System.out.println(receivedPacket);
     *             }
     *         } catch (Exception exception) {
     *             exception.printStackTrace();
     *         }
     *     }
     */
}
