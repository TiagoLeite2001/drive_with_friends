package novo;

import com.google.gson.Gson;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.lang.reflect.Type;
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

            //new GUI(this).startDriverInterface();
            startDriverInterface();

        } catch (IOException ex1) {
            closeEverything();
        }
    }

    public void startDriverInterface() {
        System.out.println("Bem vindo" +
                "\n 1 - Login\n" +
                " 2 - Registar\n" +
                " 0 - Sair ");

        boolean goOn = true;

        while (goOn){
            input = scanner.nextLine();

            switch (input){
                case "0":
                    goOn = false;
                    break;
                case "1":
                    login();
                case "2":
            }
        }


    }

    public void login(){
        System.out.println("Username: ");
        String username = scanner.nextLine();

        System.out.println("Password: ");
        String password = scanner.nextLine();

        out.println(Variables.LOGIN);
        out.println(username);
        out.println(password);

        String input = null;
        try {
            input = in.readLine();
            if (input.equals(Variables.VALID_LOGIN)) {
                userLoggedIn();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void userLoggedIn() throws IOException {

        Gson gson = new Gson();
        this.driver = gson.fromJson(in.readLine(), Driver.class);

        boolean goOn = true;

        while (goOn){
            System.out.println(" 1 - Localização" +
                    "\n 2 - Amigos" +
                    "\n 3 - Grupos" +
                    "\n 4 - Alertas");
            input = scanner.nextLine();
            switch (input){
                case "0":
                    goOn = false;
                    break;
                case "1":
                    location();
                case "2":
                case "3":
                case "4":
            }
        }
    }

    public void location(){
        System.out.println(" A sua localização é: " + this.driver.getCurrentLocation());

        boolean goOn = true;

        while (goOn){
            System.out.println(" 1 - Definir nova localização" +
                    "\n 0 - Voltar");
            input = scanner.nextLine();
            switch (input){
                case "0":
                    goOn = false;
                    break;
                case "1":
                    newLocation();
            }
        }
    }

    public void newLocation(){
        out.println(Variables.NEW_LOCATION);
        System.out.println("Latitude: ");
        String latit = scanner.nextLine();
        out.println(latit);

        System.out.println("Longitude: ");
        String longit = scanner.nextLine();
        out.println(longit);
    }



        /**

        public void startDriverInterface() {

            JPanel loginPanel = new JPanel(new GridBagLayout());

            loginPanel.setBounds(100,100,100,100);

            JLabel lUsername = new JLabel("Username");
            JTextField username = new JTextField("        ");
            JLabel lPassword = new JLabel("Password");
            JTextField password = new JTextField("         ");

            JButton buttonLogin = new JButton("Login");
            buttonLogin.setSize( 100, 50);

            JButton buttonSingup = new JButton("Sing Up");
            buttonSingup.setSize( 100, 50);

            loginPanel.add(lUsername);
            loginPanel.add(lPassword);
            loginPanel.add(username);
            loginPanel.add(password);
            loginPanel.add(buttonLogin);
            loginPanel.add(buttonSingup);

            buttonLogin.addActionListener(e -> {
                login(username.getText(), password.getText(), loginPanel);
            });

            buttonSingup.addActionListener(e -> {

                frame.remove(loginPanel);

                JPanel panelSingup = new JPanel();
                panelSingup.add(username);
                panelSingup.add(lUsername);
                panelSingup.add(password);
                panelSingup.add(lPassword);

                JLabel lName = new JLabel("Nome");
                lName.setFont(new Font("", Font.PLAIN, 20));
                lName.setBounds(500, 360, 100, 20);

                JTextField name = new JTextField(20);
                name.setFont(new Font("", Font.PLAIN, 20));
                name.setBounds(500, 380, 90, 30);

                panelSingup.add(lName);
                panelSingup.add(name);

                JButton back = new JButton("Voltar");
                back.setPreferredSize(new Dimension(80, 20));

                JButton bSingUp = new JButton("Sing Up");
                buttonSingup.setBackground(Color.blue);
                buttonSingup.setBounds(500, 410, 100, 30);

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
                        frame.add(loginPanel);
                        frame.repaint();

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
            frame.setPreferredSize(new Dimension(1800, 1000));
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(true);
            frame.setVisible(true);
            frame.setLocationRelativeTo(null);
            frame.pack();
        }

        public void login(String username, String password, JPanel loginPanel){
            try {
                out.println(Variables.LOGIN);
                out.println(username);
                out.println(password);

                String input = in.readLine();
                System.out.println(input);

                if (input.equals(Variables.VALID_LOGIN)) {
                    frame.remove(loginPanel);
                    frame.repaint();

                    userLoggedIn();
                } else {
                    JOptionPane.showMessageDialog(frame, "Dados inválidos!");
                }

            } catch (IOException ex) {}
        }

        private void userLoggedIn() throws IOException {

            //Recebe o driver info
            Gson gson = new Gson();
            this.driver = gson.fromJson(in.readLine(), Driver.class);

            JPanel menu = new JPanel(new GridBagLayout());

            JPanel topBar = new JPanel(new FlowLayout(FlowLayout.CENTER));
            topBar.setBounds(100, 100, 500, 200);

            JButton buttonLocation = new JButton("Minha Localização");
            topBar.add(buttonLocation);

            JButton buttonAreaAlerts = new JButton("Áreas de alertas");
            topBar.add(buttonAreaAlerts);
            JPanel panelAreaAlerts = new JPanel(new FlowLayout(FlowLayout.LEFT, 150, 20));

            JButton buttonAlerts = new JButton("Alertas Gerais");
            topBar.add(buttonAlerts);
            JPanel panelAlerts = new JPanel(new FlowLayout(FlowLayout.LEFT, 150, 20));

            JButton buttonFriends = new JButton("Amigos");
            topBar.add(buttonFriends);
            JPanel panelFriends = new JPanel(new FlowLayout(FlowLayout.LEFT, 150, 20));

            JButton buttonGroups = new JButton("Grupos");
            topBar.add(buttonGroups);
            JPanel panelGroups = new JPanel(new FlowLayout(FlowLayout.LEFT, 150, 20));

            JPanel chat = new JPanel(new FlowLayout(FlowLayout.CENTER));
            chat.add(new Label("Chat"));


            menu.add(topBar);
            menu.add(chat);
            frame.add(menu);
            frame.setResizable(true);
            frame.repaint();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);

            buttonLocation.addActionListener(e -> {

                JPanel panelLocation = new JPanel();
                panelLocation.setBackground(Color.green);

                JLabel currentLocation = new JLabel();
                currentLocation.setBounds(500, 500,100,20);
                currentLocation.setText("Localização atual: " + driver.getCurrentLocation().toString());

                JLabel newLocation = new JLabel("Nova localização: ");
                newLocation.setBounds(500,500,100,100);

                JTextField latitude = new JTextField("Latitude");
                latitude.setBounds(500,600,100,100);
                JTextField longitude = new JTextField("Longitude");
                latitude.setBounds(500,700,100,100);

                JButton changeLocation = new JButton("Alterar localização");

                panelLocation.add(currentLocation);
                panelLocation.add(newLocation);
                panelLocation.add(latitude);
                panelLocation.add(longitude);
                panelLocation.add(changeLocation);

                //frame.removeAll();
                menu.add(panelLocation);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.pack();
                frame.setVisible(true);
                frame.repaint();

                changeLocation.addActionListener(e1 -> {
                    try {
                        out.println(Variables.LOCATION);
                        out.println(Variables.NEW_LOCATION);
                        out.println(latitude);
                        out.println(longitude);
                        JOptionPane.showMessageDialog(frame, "Localização alterada com sucesso!");
                        currentLocation.setText("A sua localização atual é: " + in.readLine());
                        latitude.setText("Latitude");
                        longitude.setText("Longitude");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });

            });


            //bGroups.setPreferredSize(new Dimension(80, 20));

            //jFrame.add(topBar);


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
