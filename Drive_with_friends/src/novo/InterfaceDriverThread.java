package novo;

import com.google.gson.Gson;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

    public InterfaceDriverThread(Socket socket) {
        try {
            this.socket = socket;
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.scanner = new Scanner(System.in);
            this.input = null;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        super.run();
        try {
            out.println("DRIVER");
            System.out.println("eehe");

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
        out.println(Variables.LOGIN);
        System.out.print("Username: ");
        out.println(scanner.nextLine());
        System.out.print("\nPassword");
        out.println(scanner.nextLine());

        String input = in.readLine();
        System.out.println(input);

        if (input.equals(Variables.VALID_LOGIN)) {
            menu();
        } else {
            System.out.println("Dados inválidos!");
        }
    }

    public void singUp() throws IOException {
        out.println(Variables.SINGUP);
        System.out.println("Nome");
        out.println(scanner.nextLine());
        System.out.println("Username");
        out.println(scanner.nextLine());
        System.out.println("Password");
        out.println(scanner.nextLine());

        String answer = in.readLine();

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
        this.driver = gson.fromJson(in.readLine(), Driver.class);
    }

    public void menu() throws IOException {
        updateDriverInfo();
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
            case "3":
            case "4":
        }
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
        out.println(Variables.LOCATION);
        out.println(Variables.NEW_LOCATION);
        System.out.println("Introduza a\n latitude:");
        String latit = scanner.nextLine();
        out.println(latit);
        System.out.println("longitude: ");
        String longit = scanner.nextLine();
        out.println(longit);
        if(in.readLine().equals(Variables.VALID_LOCATION)){
            System.out.println("Localização atualizada com sucesso");
            this.driver.setCurrentLocation(Double.parseDouble(latit), Double.parseDouble(longit));
        }
        else {
            System.out.println("Erro a alterar localização");
        }

    }

    /**
    public void startDriverInterface() {

        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(null);

        JLabel lUsername = new JLabel("Username");
        lUsername.setBounds(600, 300, 100, 20);

        JTextField username = new JTextField("");
        username.setBounds(600, 320, 100, 20);

        JLabel lPassword = new JLabel("Password");
        lPassword.setBounds(600, 340, 100, 20);

        JTextField password = new JTextField("");
        password.setBounds(600, 360, 100, 20);

        JButton buttonLogin = new JButton("Login");
        buttonLogin.setBounds(600, 380, 100, 20);

        JButton buttonSingup = new JButton("Sing Up");
        buttonSingup.setBounds(600, 400, 100, 20);

        loginPanel.add(lUsername);
        loginPanel.add(lPassword);
        loginPanel.add(username);
        loginPanel.add(password);
        loginPanel.add(buttonLogin);
        loginPanel.add(buttonSingup);

        frame.setResizable(false);

        buttonLogin.addActionListener(e -> {
            login(username.getText(), password.getText(), loginPanel);
        });

        buttonSingup.addActionListener(e -> {

            frame.remove(loginPanel);

            JPanel panelSingup = new JPanel();
            panelSingup.setLayout(null);

            panelSingup.add(username);
            panelSingup.add(lUsername);
            panelSingup.add(password);
            panelSingup.add(lPassword);

            JLabel lName = new JLabel("Nome");
            lName.setBounds(600, 260, 100, 20);

            JTextField name = new JTextField(20);
            name.setBounds(600, 280, 100, 20);

            panelSingup.add(lName);
            panelSingup.add(name);

            JButton back = new JButton("Voltar");
            back.setBounds(600, 420, 100, 20);

            JButton bSingUp = new JButton("Sing Up");
            bSingUp.setBounds(600, 390, 100, 20);

            panelSingup.add(bSingUp);
            panelSingup.add(back);

            frame.add(panelSingup);
            frame.setLocationRelativeTo(null);
            frame.pack();
            frame.setVisible(true);

            back.addActionListener(e13 -> {
                frame.remove(panelSingup);
                frame.add(loginPanel);
                frame.repaint();
            });

            bSingUp.addActionListener(e14 -> {

                //Verificar se algum campo está em branco
                if (!username.getText().trim().equals("") || !name.getText().trim().equals("") || !password.getText().trim().equals("")) {

                    boolean singedUp = false;
                    while (!singedUp) {
                        try {
                            out.println(Variables.SINGUP);
                            out.println(name.getText());
                            out.println(username.getText());
                            out.println(password.getText());
                            String answer = in.readLine();
                            System.out.println(answer);
                            switch (answer) {
                                case Variables.VALID_SINGUP:
                                    singedUp = true;
                                    break;
                                case Variables.INVALID_SINGUP:
                                    JOptionPane.showMessageDialog(frame, "O username já está a ser utilizado!");
                                    break;
                            }

                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                    frame.remove(panelSingup);
                    frame.setVisible(false);

                    JOptionPane.showMessageDialog(frame, "Utilizador registado com sucesso!");
                    System.out.println("Utilizador registado com sucesso!");
                    try {
                        userLoggedIn();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Algum campo está por preencher!");
                    System.out.println("Algum campo está por preencher!");
                }
            });

        });

        frame.add(loginPanel);
        frame.repaint();
        frame.setVisible(true);
        frame.setResizable(true);
        frame.setPreferredSize(new Dimension(1200, 800));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.pack();
    }

    public void login(String username, String password, JPanel loginPanel) {
        try {
            out.println(Variables.LOGIN);
            out.println(username);
            out.println(password);

            String input = in.readLine();
            System.out.println(input);

            if (input.equals(Variables.VALID_LOGIN)) {
                frame.remove(loginPanel);
                frame.repaint();
                frame.setVisible(false);

                userLoggedIn();
            } else {
                JOptionPane.showMessageDialog(frame, "Dados inválidos!");
            }

        } catch (IOException ex) {
        }
    }


    public void userLoggedIn() throws IOException {
        updateDriverInfo();

        GUILocation();
        GUIFriends();
        GUIAlertas();
        GUIChat();
        GUIGroups();
    }

    private void GUILocation() {

        JFrame frameLocation = new JFrame();
        JPanel panelLocation = new JPanel();

        panelLocation.setLayout(new GridLayout(0,1));

        JLabel currentLocation = new JLabel();
        currentLocation.setText("Localização atual: " + driver.getCurrentLocation().toString());
        JLabel newLocation = new JLabel("Nova localização: ");
        JTextField latitude = new JTextField("Latitude");
        JTextField longitude = new JTextField("Longitude");
        JButton changeLocation = new JButton("Alterar localização");

        changeLocation.addActionListener(e1 -> {
            try {
                out.println(Variables.LOCATION);
                out.println(Variables.NEW_LOCATION);
                out.println(latitude.getText());
                out.println(longitude.getText());
                String resposta = in.readLine();
                System.out.println("resposta" + resposta);

                if(resposta.equals(Variables.VALID_LOCATION)){
                    JOptionPane.showMessageDialog(frame, "Localização alterada com sucesso!");
                    currentLocation.setText("A sua localização atual é: " + in.readLine());
                    latitude.setText("Latitude");
                    longitude.setText("Longitude");
                }
                else {
                    JOptionPane.showMessageDialog(frame, "Erro a alterar a localização!");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        panelLocation.add(currentLocation);
        panelLocation.add(newLocation);
        panelLocation.add(latitude);
        panelLocation.add(longitude);
        panelLocation.add(changeLocation);

        frameLocation.add(panelLocation,BorderLayout.CENTER);
        frameLocation.setTitle("Localização");;
        frameLocation.pack();
        frameLocation.setVisible(true);
        frameLocation.repaint();
        frameLocation.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    private void GUIFriends() throws IOException {

        JFrame frame = new JFrame();
        frame.setTitle("Amigos");
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0,1));

        JLabel friends = new JLabel();

        JTextArea friendsArea = new JTextArea("");
        if(!(this.driver.getFriends() == null)){
            friends.setText("Amigos: " + this.driver.getFriends().size());
            for (Object c : this.driver.getFriends()) {

                Driver friend = (Driver) c;
                friendsArea.append( friend.getUsername());
            }

        }



        out.println(Variables.ALL_USERS);

        JTextArea usersArea = new JTextArea("");

        int count = 0;
        String input = in.readLine();
        while (!input.equals(Variables.DONE)){
            usersArea.append("\n"+input);
            count++;
            input = in.readLine();
        }

        JLabel users = new JLabel("Todos os utilizadores: " + count);

        JLabel l = new JLabel("Adicionar um amigo");
        JTextField username = new JTextField("Username");
        JButton add = new JButton("Adicionar");

        add.addActionListener(e -> {
            out.println(Variables.ADD_FRIEND);
            out.println(username.getText());
            try {
                if(in.readLine().equals(Variables.DONE)){
                    JOptionPane.showMessageDialog(frame, "Amigo adicionado");
                    friendsArea.append("\n" + username.getText());
                    friends.setText("Amigos: " + this.driver.getFriends().size());
                    updateDriverInfo();
                }
                else {
                    JOptionPane.showMessageDialog(frame, "Erro a adicionar amigo");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });


        panel.add(friends);
        panel.add(friendsArea);
        panel.add(users);
        panel.add(usersArea);
        panel.add(l);
        panel.add(username);
        panel.add(add);
        frame.add(panel,BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
        frame.repaint();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }



    private void GUIGroups() {

        JFrame frame = new JFrame();
        frame.setTitle("Grupos");
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0,1));

        JLabel label = new JLabel("Lista de grupos: ");

        panel.add(label);
        frame.add(panel,BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
        frame.repaint();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    private void GUIAlertas() {

        JFrame frame = new JFrame();
        frame.setTitle("Alertas");
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0,1));

        JLabel label = new JLabel("Areas de alertas: ");

        panel.add(label);
        frame.add(panel,BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
        frame.repaint();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    private void GUIAreaCircundante() {

        JFrame frame = new JFrame();
        frame.setTitle("Área Circundante");
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0,1));

        JLabel label = new JLabel("Raio: " + this.driver.getRadiusLocalArea());

        panel.add(label);
        frame.add(panel,BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
        frame.repaint();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    private void GUIChat() {

        JFrame frame = new JFrame();
        frame.setTitle("Chat");
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0,1));

        JLabel label = new JLabel("Mensagens: ");

        panel.add(label);
        frame.add(panel,BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
        frame.repaint();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }
     */



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
