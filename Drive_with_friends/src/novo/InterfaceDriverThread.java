package novo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;

public class InterfaceDriverThread extends Thread {
    static JFrame frame = new JFrame("Drive With Friends");
    static JPanel panelMenu = new JPanel(new GridLayout());

    static Socket socket = null;
    static BufferedReader in;
    static PrintWriter out;
    Driver driver;

    public InterfaceDriverThread(Socket socket) {
        try {
            this.socket = socket;
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
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
            closeEverything(this.socket, this.in, this.out);
        }
    }

    public static void startDriverInterface() {


        JLabel title = new JLabel("Welcome");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(null);
        //loginPanel.setLayout(new BoxLayout(loginPanel, BoxLayout.PAGE_AXIS));
        //loginPanel.setBorder(new EmptyBorder(75, 0, 10, 0));

        JLabel lUsername = new JLabel("Username");
        lUsername.setBounds(500, 100, 100, 20);

        JTextField username = new JTextField();
        username.setBounds(500, 130, 100, 20);

        JLabel lPassword = new JLabel("Password");
        lPassword.setBounds(600, 130, 100, 20);

        JTextField password = new JTextField();
        password.setBounds(600, 130, 100, 20);

        JButton buttonLogin = new JButton("Login");
        //buttonLogin.setPreferredSize(new Dimension(200, 100));
        buttonLogin.setBackground(Color.green);

        JButton buttonSingup = new JButton("Sing Up");
        //buttonSingup.setPreferredSize(new Dimension(100, 100));

        loginPanel.add(lUsername);
        loginPanel.add(lPassword);
        loginPanel.add(username);
        loginPanel.add(password);
        loginPanel.add(buttonLogin);
        loginPanel.add(buttonSingup);

        buttonLogin.addActionListener(e -> {

                try {
                    out.println(Variables.LOGIN);
                    out.println(username.getText());
                    out.println(password.getText());

                    String input = in.readLine();
                    System.out.println(input);

                    if(input.equals(Variables.VALID_LOGIN)){

                        frame.remove(loginPanel);
                        frame.repaint();

                        userLoggedIn();
                    }
                    else {
                        JOptionPane.showMessageDialog(frame, "Dados inválidos!");
                    }

                } catch (IOException ex) {
                }

        });

        buttonSingup.addActionListener(e -> {

            frame.remove(loginPanel);

            JPanel panelSingup = new JPanel();
            panelSingup.setBorder(new EmptyBorder(75, 0, 10, 0));

            JLabel lName = new JLabel("Name");
            //JLabel lUsername = new JLabel("Username");
            //JLabel lPassword = new JLabel("Password");

            JTextField name = new JTextField(20);
            //JTextField username = new JTextField(20);
            //JTextField password = new JTextField(20);

            JButton bSingUp = new JButton("SingUp");
            bSingUp.setPreferredSize(new Dimension(100, 20));

            panelSingup.add(lName);
            panelSingup.add(name);

            panelSingup.add(lUsername);
            panelSingup.add(username);

            panelSingup.add(lPassword);
            panelSingup.add(password);

            JButton back = new JButton("Voltar");
            back.setPreferredSize(new Dimension(80, 20));

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
                            switch (answer){
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
                }else {
                    JOptionPane.showMessageDialog(frame, "Algum campo está por preencher!");
                    System.out.println("Algum campo está por preencher!");
                }
            });

        });

        frame.add(title);
        frame.add(loginPanel);
        frame.setVisible(true);
        frame.setPreferredSize(new Dimension(1200, 800));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        frame.pack();
    }

    private static void userLoggedIn() {

        JToolBar topBar = new JToolBar();

        JButton bLocation = new JButton("Minha Localização");
        topBar.add(bLocation);
        JPanel pLocation = new JPanel(new FlowLayout(FlowLayout.LEFT, 150, 20));

        bLocation.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                out.println(Variables.LOCATION);
                JLabel currentLocation = new JLabel();

                try {
                    currentLocation.setText(in.readLine());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                JLabel newLocation = new JLabel("Nova localização: ");
                JTextField latitude = new JTextField("Latitude");
                JTextField longitude = new JTextField("Longitude");

                JButton changeLocation = new JButton("Alterar localização");
                changeLocation.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            out.println(latitude);
                            out.println(longitude);
                            JOptionPane.showMessageDialog(frame, "Localização alterada com sucesso!");
                            currentLocation.setText("A sua localização atual é: " + in.readLine());
                            latitude.setText("Latitude");
                            longitude.setText("Longitude");
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
                pLocation.add(currentLocation);
                pLocation.add(newLocation);
                pLocation.add(latitude);
                pLocation.add(longitude);
                pLocation.add(changeLocation);
                frame.add(pLocation);
                frame.repaint();
            }
        });

        JButton bAreaAlerts = new JButton("Áreas de alertas");
        topBar.add(bAreaAlerts);
        JPanel pAreaAlerts = new JPanel(new FlowLayout(FlowLayout.LEFT, 150, 20));

        JButton bAlerts = new JButton("Alertas Gerais");
        topBar.add(bAlerts);
        JPanel pAlerts = new JPanel(new FlowLayout(FlowLayout.LEFT, 150, 20));

        JButton bFriends = new JButton("Amigos");
        topBar.add(bFriends);
        JPanel pFriends = new JPanel(new FlowLayout(FlowLayout.LEFT, 150, 20));

        JButton bGroups = new JButton("Grupos");
        topBar.add(bGroups);
        JPanel pGroups = new JPanel(new FlowLayout(FlowLayout.LEFT, 150, 20));
        //bGroups.setPreferredSize(new Dimension(80, 20));

        //jFrame.add(topBar);
        frame.setLayout(new BorderLayout());
        //frame.getContentPane().add(topBar, BorderLayout.PAGE_START);

        frame.repaint();
        frame.add(panelMenu, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

    }


    public void closeEverything(Socket socket, BufferedReader bufferedReader, PrintWriter printWriter){
        try {
            if (bufferedReader != null){
                bufferedReader.close();
            }
            if (printWriter != null){
                printWriter.close();
            }
            if (socket != null){
                socket.close();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
