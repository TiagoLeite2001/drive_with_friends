package novo;

import others.AlertLocation;
import others.Group;
import others.Location;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class Driver {

    private String username;
    private String name;
    private String password;
    private Location currentLocation;
    private ArrayList<Driver> friends;
    private ArrayList<Group> groups;
    private ArrayList<AlertLocation> alertsLocations;
    private double radiusLocalArea;


    public Driver(String username, String name, String password) {
        this.username = username;
        this.name = name;
        this.password = password;
    }

    public Driver(String username) {
        this.username = username;
    }

    public Driver() {

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
            socketDriver = new Socket(Variables.IP_DRIVER, Variables.PORT_SERVER);
            new InterfaceDriverThread(socketDriver).start();
        } catch (IOException e) {
            System.err.println("Connection refused.");
        }

    }

}
