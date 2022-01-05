package entities;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private ServerSocket serverSocket;

    public Server(ServerSocket serverSocket){
        this.serverSocket = serverSocket;
    }

    public void startServer(){
        try {
            while (!serverSocket.isClosed()){
                Socket socket = serverSocket.accept();
                System.out.println("A new client has connected");
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
