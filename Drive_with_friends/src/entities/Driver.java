package entities;

import helpers.Id;
import others.AlertLocation;
import others.Group;
import others.Location;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Driver {
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

    public Driver(Socket socket, String username,String name, String password){
        try {
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.username = username;
            this.id = Id.getID();
            this.name = name;
            this.password = password;
            drivers.add(this);
        } catch (IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public Driver(String username){
        this.username = username;
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

    public boolean login (String password){
        return this.password.equals(password);
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
    public boolean equals(Object o){
        if(o instanceof Driver){
            Driver driver = (Driver) o;
            if (this.username.equals(driver.username)){ return true;}
            return false;
        }
        return false;
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your username for the group chat: ");
        String username = scanner.nextLine();
        Socket socket = new Socket("localhost", 1234);
        //Driver client = new Driver(socket, username, name, pa);
        //client.listenForMessage();
        //client.sendMessage();
    }
}
