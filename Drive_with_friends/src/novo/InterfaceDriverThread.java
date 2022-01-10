package novo;

import com.google.gson.Gson;

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

public class InterfaceDriverThread extends Thread {
    static JFrame frame = new JFrame("Drive With Friends");
    static JPanel panelMenu = new JPanel(new GridLayout());

    static Socket socket = null;
    static BufferedReader in;
    static PrintWriter out;
    private Driver driver;

    public InterfaceDriverThread(Socket socket) {
        try {
            this.socket = socket;
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
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
            closeEverything(this.socket, this.in, this.out);
        }
    }

    public void startDriverInterface() {

        JLabel title = new JLabel("Welcome");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(null);
        //loginPanel.setLayout(new BoxLayout(loginPanel, BoxLayout.PAGE_AXIS));
        //loginPanel.setBorder(new EmptyBorder(75, 0, 10, 0));

        JLabel lUsername = new JLabel("Username");
        lUsername.setFont(new Font("", Font.PLAIN, 20));
        lUsername.setBounds(500, 300, 100, 20);

        JTextField username = new JTextField("t");
        username.setBounds(600, 300, 100, 20);

        JLabel lPassword = new JLabel("Password");
        lPassword.setFont(new Font("", Font.PLAIN, 20));
        lPassword.setBounds(500, 330, 100, 20);

        JTextField password = new JTextField("t");
        password.setFont(new Font("", Font.PLAIN, 20));
        password.setBounds(600, 330, 100, 20);

        JButton buttonLogin = new JButton("Login");
        buttonLogin.setBackground(Color.green);
        buttonLogin.setBounds(500, 360, 90, 30);

        JButton buttonSingup = new JButton("Sing Up");
        buttonSingup.setBackground(Color.blue);
        buttonSingup.setBounds(600, 360, 100, 30);

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

        System.out.println(this.driver.getCurrentLocation().toString());

        JToolBar topBar = new JToolBar();
        topBar.setBounds(100, 100, 500, 200);

        JPanel menuPanel = new JPanel();

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

        JPanel chat = new JPanel();
        //chat.setBackground(Color.gray);
        chat.setBounds(800, 100, 350, 600);

        JLabel label = new JLabel("User X\n User z");
        JLabel label2 = new JLabel("User X");
        label.setSize(70,20);

        JList list = new JList();

        list.add( label);
        list.add( label2);

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBounds(800, 100, 500, 500);
        scroll.setBackground(Color.blue);
        chat.add(scroll);

        menuPanel.add(topBar);

        frame.add(chat);
        frame.add(menuPanel);
        frame.getContentPane().add(topBar, BorderLayout.PAGE_START);
        frame.setResizable(true);
        frame.repaint();
        frame.add(panelMenu, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        buttonLocation.addActionListener(e -> {


            JPanel panelLocation = new JPanel();
            panelLocation.setBackground(Color.green);
            panelLocation.setLayout(null);
            panelLocation.setSize(500, 300);

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
            frame.add(panelLocation);
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


    public void closeEverything(Socket socket, BufferedReader bufferedReader, PrintWriter printWriter) {
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (printWriter != null) {
                printWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
