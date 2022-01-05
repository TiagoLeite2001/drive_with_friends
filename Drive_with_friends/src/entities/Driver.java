package entities;

import helpers.Id;
import others.AlertLocation;
import others.Group;
import others.Location;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Driver {

    static JFrame jFrame = new JFrame("Drive With Friends");

    private static ArrayList<Driver> drivers = new ArrayList<>();

    private int id;
    private String username;
    private String name;
    private String password;
    private Location currentLocation;
    private ArrayList<Driver> friends;
    private ArrayList<Group> groups;
    private ArrayList<AlertLocation> alertsLocations;
    private double radiusLocalArea;
    private Socket socket;
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;

    public Driver(String username, String name, String password) {
        this.username = username;
        this.id = Id.getID();
        this.name = name;
        this.password = password;
        drivers.add(this);

    }

    public Driver(String username) {
        this.username = username;
    }

    public boolean connet(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(double latitude, double longitude) {
        Location currentLocation = new Location(latitude, longitude);

        this.currentLocation = currentLocation;
    }

    public ArrayList<Driver> getFriends() {
        return friends;
    }

    public void setFriends(ArrayList<Driver> friends) {
        this.friends = friends;
    }

    public ArrayList<Group> getGroups() {
        return groups;
    }

    public void setGroups(ArrayList<Group> groups) {
        this.groups = groups;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }

    public ArrayList<AlertLocation> getAlertsLocations() {
        return alertsLocations;
    }

    public void setAlertsLocations(ArrayList<AlertLocation> alertsLocations) {
        this.alertsLocations = alertsLocations;
    }

    public void addAlertLocation(double latitude, double longitude, double radius) {
        Location centerOfLocation = new Location(latitude, longitude);
        AlertLocation alertLocation = new AlertLocation(centerOfLocation, radius);

        this.alertsLocations.add(alertLocation);
    }

    public double getRadiusLocalArea() {
        return radiusLocalArea;
    }

    public void setRadiusLocalArea(double radiusLocalArea) {
        this.radiusLocalArea = radiusLocalArea;
    }

    public boolean login(String password) {
        return this.password.equals(password);
    }

    public void sendMessage() {
        try {
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            Scanner scanner = new Scanner(System.in);
            while (socket.isConnected()) {
                String messageToSend = scanner.nextLine();
                bufferedWriter.write(username + ": " + messageToSend);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void listenForMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String msgFromGroupChat;

                while (socket.isConnected()) {
                    try {
                        msgFromGroupChat = bufferedReader.readLine();
                        System.out.println(msgFromGroupChat);
                    } catch (IOException e) {
                        closeEverything(socket, bufferedReader, bufferedWriter);
                    }
                }
            }
        }).start();
    }

    public void closeSocket() {
        try {
            if (this.socket != null) {
                this.socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Driver getDriver(String username) throws ClassNotFoundException {
        for(Driver driver : drivers){
            if (driver.username.equals(username)){
                return driver;
            }
        }
        throw new ClassNotFoundException();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Driver) {
            Driver driver = (Driver) o;
            if (this.username.equals(driver.username)) {
                return true;
            }
            return false;
        }
        return false;
    }

    public static void init() {
        jFrame.setResizable(false);

        jFrame.setSize(500, 500);
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

        bLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                jFrame.remove(panel);

                JPanel panelLoging = new JPanel(new FlowLayout(FlowLayout.LEFT, 150, 20));
                panelLoging.setBorder(new EmptyBorder(75, 0, 10, 0));

                //Labels
                JLabel lUsername = new JLabel("Username");
                JLabel lPassword = new JLabel("Password");

                //Inputs
                JTextField username = new JTextField(20);
                JTextField password = new JTextField(20);

                //Buttons
                JButton bLogin = new JButton("Login");
                bLogin.setPreferredSize(new Dimension(100, 20));

                panelLoging.add(lUsername);
                panelLoging.add(username);

                panelLoging.add(lPassword);
                panelLoging.add(password);

                panelLoging.add(bLogin);

                JButton back = new JButton("Voltar");
                back.setPreferredSize(new Dimension(80, 20));

                panelLoging.add(back);

                jFrame.add(panelLoging);
                jFrame.setVisible(true);

                back.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        jFrame.remove(panelLoging);
                        jFrame.add(panel);
                        jFrame.repaint();
                    }
                });

                bLogin.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            //Verificar se algum campo está em branco
                            if (username.getText().trim().equals("") || password.getText().trim().equals("")) {
                                JOptionPane.showMessageDialog(jFrame, "Algum campo está por preencher!");
                                throw new IOException();
                            }

                            Driver driver = getDriver(username.getText());

                            if(driver.password.equals(password.getText())){
                                jFrame.remove(panelLoging);
                                jFrame.repaint();
                                userLoggedIn(driver);
                            }else
                            {
                                JOptionPane.showMessageDialog(jFrame, "Os campos estão incorretos ou o utilizador não existe!");
                            }

                        } catch (IOException | ClassNotFoundException ex ) {
                        }
                    }
                });
            }
        });

        bSingup.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                jFrame.remove(panel);

                JPanel panelSingup = new JPanel(new FlowLayout(FlowLayout.LEFT, 150, 20));
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


                back.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        jFrame.remove(panelSingup);
                        jFrame.add(panel);
                        jFrame.repaint();
                    }
                });

                bSingUp.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {

                        try {
                            //Verificar se algum campo está em branco
                            if (username.getText().trim().equals("") || name.getText().trim().equals("") || password.getText().trim().equals("")) {
                                JOptionPane.showMessageDialog(jFrame, "Algum campo está por preencher!");
                                throw new IOException();
                            }
                            if(!drivers.contains(new Driver(username.getText()) )){
                                Driver driver = new Driver(username.getText(), name.getText(), password.getText());

                                jFrame.remove(panelSingup);
                                jFrame.add(panel);
                                jFrame.repaint();

                                JOptionPane.showMessageDialog(jFrame, "Utilizador registado com sucesso!");
                            }
                            else{
                                JOptionPane.showMessageDialog(jFrame, "O username já está a ser utilizado!");
                                username.setText("");
                                name.setText("");
                                password.setText("");
                            }


                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }

                    }
                });


            }
        });
        jFrame.setPreferredSize(new Dimension(500, 500));
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setResizable(false);
        jFrame.pack();
        jFrame.setVisible(true);
        jFrame.setLocationRelativeTo(null);
    }

    private static void userLoggedIn(Driver driver) {
        //conectar o utilizador
        try {
            Socket socket = new Socket("localhost", 1234);
            driver.connet(socket);
        } catch (IOException e) {
            e.printStackTrace();
        }

        JToolBar leftBar = new JToolBar();

        JButton bLocation = new JButton("Minha Localização");
        bLocation.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

        JButton bAreaAlerts = new JButton("Áreas de alertas");
        JButton bAlerts = new JButton("Alertas Gerais");
        JButton bFriends = new JButton("Amigos");
        JButton bGroups = new JButton("Grupos");
        bGroups.setPreferredSize(new Dimension(80, 20));

        leftBar.add(bLocation);
        leftBar.add(bAreaAlerts);
        leftBar.add(bAlerts);
        leftBar.add(bFriends);
        leftBar.add(bGroups);

        jFrame.setLayout(new BorderLayout());
        jFrame.getContentPane().add(leftBar, BorderLayout.PAGE_START);
        jFrame.setSize(500, 500);
        jFrame.repaint();

    }

    public static void main(String[] args) throws IOException {

        Driver driver = new Driver("t", "Tiago Leite", "t");
        init();


        //Scanner scanner = new Scanner(System.in);
        //System.out.println("Enter your username for the group chat: ");
        //String username = scanner.nextLine();
        //Socket socket = new Socket("localhost", 1234);
        //Driver client = new Driver(socket, username, name, pa);
        //client.listenForMessage();
        //client.sendMessage();
    }
}
