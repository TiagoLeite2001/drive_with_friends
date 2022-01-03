package tcp;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Client {

    private static ArrayList<Client> clients = new ArrayList<>();

    private Socket socket;
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;
    private String username;
    private String password;

    public Client(Socket socket, String username){
        try {
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.username = username;
        } catch (IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public Client(String username, String password){
        this.username = username;
        this.password = password;
        clients.add(this);
    }

    public Client(String username, String password, int login){
        this.username = username;
        this.password = password;
        clients.add(this);
    }

    public void sendMessage(){
        try {
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            Scanner scanner = new Scanner(System.in);
            while (socket.isConnected()){
                String messageToSend = scanner.nextLine();
                bufferedWriter.write(username + ": " + messageToSend);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void listenForMessage(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String msgFromGroupChat;

                while(socket.isConnected()){
                    try {
                        msgFromGroupChat = bufferedReader.readLine();
                        System.out.println(msgFromGroupChat);
                    } catch (IOException e){
                        closeEverything(socket, bufferedReader, bufferedWriter);
                    }
                }
            }
        }).start();
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
        try {
            if (bufferedReader != null){
                bufferedReader.close();
            }
            if (bufferedWriter != null){
                bufferedWriter.close();
            }
            if (socket != null){
                socket.close();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Client client = (Client) o;

        if (password != null ? password.equals(client.password) : client.password == null) return false;

        return username != null ? username.equals(client.username) : client.username == null;
    }


    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\nLogin -> 1\n" + "Registar -> 2");
        int input = 0;

        while(true){
            boolean done = false;
            while (!done) {
                try {
                    input = scanner.nextInt();
                    if (input > 0 && input < 3){
                        break;
                    }
                    System.out.println("Introduza um valor válido!");
                } catch (InputMismatchException e) {
                    e.printStackTrace();
                }
            }

            switch (input){
                case 1:
                    //limpar o /n que o nextInt() deixa
                    scanner.nextLine();
                    System.out.println("Username: ");
                    String username = scanner.nextLine();

                    System.out.println("Password: ");
                    String password = scanner.nextLine();

                    Client client = new Client(username, password, 0);

                    if (clients.contains(client)){
                        System.out.println("Login efetuado com sucesso!");
                        Socket socket = new Socket("localhost", 1234);
                        client = new Client(socket, username);
                        client.listenForMessage();
                        client.sendMessage();
                    }
                    else {
                        //limpar o /n que o nextInt() deixa
                        scanner.nextLine();
                        System.out.println("O username e/ou a password estão incorretos!");
                        break;
                    }
                case 2:
                    System.out.println("/n Regisar novo utilizador/n");

                    System.out.println("Username: ");
                    username = scanner.nextLine();

                    System.out.println("Password: ");
                    password = scanner.nextLine();

                    client = new Client(username, password);
                    clients.add(client);

                    System.out.println("Utilizador registado com sucesso!");
                    break;

                default:
                    break;

            }
        }





    }
}
