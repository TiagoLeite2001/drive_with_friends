package novo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class InterfaceDriverThread extends Thread {
    static JFrame jFrame = new JFrame("Drive With Friends");

    static BufferedReader in;
    static PrintWriter out;
    Driver driver;

    public InterfaceDriverThread(BufferedReader bufferedReader, PrintWriter printWriter) {
        this.in = bufferedReader;
        this.out = printWriter;
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
            ex1.printStackTrace();
        }
    }

    public static void startDriverInterface() {

        jFrame.setSize(1200, 800);
        jFrame.setDefaultCloseOperation(jFrame.EXIT_ON_CLOSE);

        JLabel title = new JLabel("Welcome");
        title.setBorder(new EmptyBorder(20, 0, 10, 0));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(75, 0, 10, 0));

        JButton bLogin = new JButton("Login");
        bLogin.setPreferredSize(new Dimension(200, 100));

        JButton bSingup = new JButton("Sing Up");
        bSingup.setPreferredSize(new Dimension(200, 100));

        panel.add(bLogin);
        panel.add(bSingup);

        jFrame.add(title);
        jFrame.add(panel);
        jFrame.setVisible(true);

        bLogin.addActionListener(e -> {

            jFrame.remove(panel);

            JPanel panelLoging = new JPanel();
            panelLoging.setBorder(new EmptyBorder(75, 0, 10, 0));

            //Labels
            JLabel lUsername = new JLabel("Username");
            JLabel lPassword = new JLabel("Password");

            //Inputs
            JTextField username = new JTextField(20);
            JTextField password = new JTextField(20);

            //Buttons
            JButton bLogin1 = new JButton("Login");
            bLogin1.setPreferredSize(new Dimension(100, 20));

            panelLoging.add(lUsername);
            panelLoging.add(username);

            panelLoging.add(lPassword);
            panelLoging.add(password);

            panelLoging.add(bLogin1);

            JButton back = new JButton("Voltar");
            back.setPreferredSize(new Dimension(80, 20));

            panelLoging.add(back);

            jFrame.add(panelLoging);
            jFrame.setVisible(true);

            back.addActionListener(e1 -> {
                jFrame.remove(panelLoging);
                jFrame.add(panel);
                jFrame.repaint();
            });

            bLogin1.addActionListener(e12 -> {
                try {
                    //Verificar se algum campo está em branco
                    if (username.getText().trim().equals("") || password.getText().trim().equals("")) {
                        JOptionPane.showMessageDialog(jFrame, "Algum campo está por preencher!");
                        throw new IOException();
                    }

                    /**
                     Driver driver = getDriver(username.getText());

                     if(driver.password.equals(password.getText())){
                     jFrame.remove(panelLoging);
                     jFrame.repaint();
                     userLoggedIn(driver);
                     }else
                     {
                     JOptionPane.showMessageDialog(jFrame, "Os campos estão incorretos ou o utilizador não existe!");
                     }
                     */

                } catch (IOException ex) {
                }
            });
        });

        bSingup.addActionListener(e -> {

            jFrame.remove(panel);

            JPanel panelSingup = new JPanel();
            panelSingup.setBorder(new EmptyBorder(75, 0, 10, 0));

            JLabel lName = new JLabel("Name");
            JLabel lUsername = new JLabel("Username");
            JLabel lPassword = new JLabel("Password");

            JTextField name = new JTextField(20);
            JTextField username = new JTextField(20);
            JTextField password = new JTextField(20);

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

            jFrame.add(panelSingup);
            jFrame.setLocationRelativeTo(null);
            jFrame.pack();
            jFrame.setVisible(true);


            back.addActionListener(e13 -> {
                jFrame.remove(panelSingup);
                jFrame.add(panel);
                jFrame.repaint();
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
                                    JOptionPane.showMessageDialog(jFrame, "O username já está a ser utilizado!");
                                    break;
                            }

                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                    jFrame.remove(panelSingup);
                    jFrame.add(panel);
                    jFrame.repaint();

                    JOptionPane.showMessageDialog(jFrame, "Utilizador registado com sucesso!");
                    System.out.println("Utilizador registado com sucesso!");
                }else {
                    JOptionPane.showMessageDialog(jFrame, "Algum campo está por preencher!");
                    System.out.println("Algum campo está por preencher!");
                }
            });

        });
        //jFrame.setPreferredSize(new Dimension(800, 500));
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setResizable(false);
        jFrame.pack();
        jFrame.setVisible(true);
        jFrame.setLocationRelativeTo(null);
    }

    private static void userLoggedIn(Driver driver) {

        JToolBar topBar = new JToolBar();

        JButton bLocation = new JButton("Minha Localização");
        topBar.add(bLocation);
        JPanel pLocation = new JPanel(new FlowLayout(FlowLayout.LEFT, 150, 20));

        bLocation.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JLabel currentLocation = new JLabel();
                if (driver.getCurrentLocation() != null) {
                    currentLocation.setText("A sua localização atual é: " + driver.getCurrentLocation().toString());
                } else {
                    currentLocation.setText("A sua localização atual está por definir");
                }
                JLabel newLocation = new JLabel("Nova localização: ");
                JTextField latitude = new JTextField("Latitude");
                JTextField longitude = new JTextField("Longitude");

                JButton changeLocation = new JButton("Alterar localização");
                changeLocation.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        double latit = 0;
                        double longit = 0;
                        try {
                            latit = Double.parseDouble(latitude.getText());
                            longit = Double.parseDouble(longitude.getText());
                            driver.setCurrentLocation(latit, longit);
                            JOptionPane.showMessageDialog(jFrame, "Localização alterada com sucesso!");
                            currentLocation.setText("A sua localização atual é: " + driver.getCurrentLocation().toString());
                            latitude.setText("Latitude");
                            longitude.setText("Longitude");
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(jFrame, "Latitude e/ou longitude errados!");
                            ex.printStackTrace();
                        }
                    }
                });
                pLocation.add(currentLocation);
                pLocation.add(newLocation);
                pLocation.add(latitude);
                pLocation.add(longitude);
                pLocation.add(changeLocation);
                jFrame.add(pLocation);
                jFrame.repaint();
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
        jFrame.setLayout(new BorderLayout());
        jFrame.getContentPane().add(topBar, BorderLayout.PAGE_START);
        jFrame.setSize(800, 500);
        jFrame.repaint();

    }
}
