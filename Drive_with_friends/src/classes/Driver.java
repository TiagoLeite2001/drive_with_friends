package classes;

import helpers.Variables;
import helpers.Location;

import java.io.*;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class Driver implements Serializable{

    private String username;
    private String name;
    private String password;
    private Location currentLocation;
    private ArrayList<String> friends;
    private ArrayList<Group> groups;
    private ArrayList<String> alertsLocations;
    private double radiusLocalArea;
    private ConcurrentHashMap<String, String> messages;


    public Driver(String username, String name, String password) {
        this.username = username;
        this.name = name;
        this.password = password;
        this.currentLocation = new Location(0,0);
        this.friends = new ArrayList<>();
        this.groups = new ArrayList<>();
        this.alertsLocations = new ArrayList<>();
        this.radiusLocalArea = 5;
        this.messages = new ConcurrentHashMap<>();
    }


    public ConcurrentHashMap<String, String> getMessages() {
        return messages;
    }

    public String getMsgFromDriver(String driver){
        return this.messages.get(driver);
    }

    public void addMsg(String driver, String msg){
        String msg1 = this.messages.get(driver) + "\n " +  msg;
        this.messages.put(driver, msg1);
    }

    public void setMessages(ConcurrentHashMap<String, String> messages) {
        this.messages = messages;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public ArrayList<String> getFriends() {
        return friends;
    }

    public void addFriend(String friend) {
        this.friends.add(friend);
    }

    public void setFriends(ArrayList<String> friends) {
        this.friends = friends;
    }

    public ArrayList<Group> getGroups() {
        return groups;
    }

    public void addGroup(Group group) {
        this.groups.add(group);
    }

    public void leaveGroup(Group group) {
        this.groups.remove(group);
    }

    public boolean containsGroup(Group group){
        return this.groups.contains(group);
    }

    public void setGroups(ArrayList<Group> groups) {
        this.groups = groups;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }

    public void startComunityMulticast(MulticastSocket ms, InetAddress ia){

    }

    public boolean addAlertLocation(String al){
        if(!this.alertsLocations.contains(al)){
            this.alertsLocations.add(al);
            return true;
        }
        return false;
    }

    public double getRadiusLocalArea() {
        return radiusLocalArea;
    }

    public void setRadius(double radiusLocalArea) {
        this.radiusLocalArea = radiusLocalArea;
    }

    public boolean login(String password) {
        return this.password.equals(password);
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

    public static void main(String[] args) {
        Socket socketDriver;

        try {
            socketDriver = new Socket(Variables.IP_USER, Variables.PORT_SERVER);
            new InterfaceDriver(socketDriver).start();
        } catch (IOException e) {
            System.err.println("Connection refused.");
        }
    }

}
