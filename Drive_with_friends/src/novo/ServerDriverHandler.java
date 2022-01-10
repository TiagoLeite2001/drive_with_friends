package novo;

import entities.Server;
import others.Location;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;

public class ServerDriverHandler extends Thread {
    Socket socketDriver;
    BufferedReader in;
    PrintWriter out;

    MulticastSocket socketBroadcast;

    MulticastSocket socketNorte;
    MulticastSocket socketSul;
    MulticastSocket socketCentro;

    InetAddress addressBroadcast;
    InetAddress addressNorte;
    InetAddress addressCentro;
    InetAddress addressSul;

    SharedObject sharedObject;

    SynchronizedArrayList<Driver> drivers;
    Driver driver = null;

    DatagramPacket packetBroadcast;
    DatagramPacket packetMulticastNorte;
    DatagramPacket packetMulticastCentro;
    DatagramPacket packetMulticastSul;

    public ServerDriverHandler(Socket socketClient, MulticastSocket socketBroadcast,
                               MulticastSocket socketNorte, MulticastSocket socketCentro, MulticastSocket socketSul,
                               InetAddress addressBroadcast, InetAddress addressNorte, InetAddress addressCentro,
                               InetAddress addressSul, SharedObject sharedObject, SynchronizedArrayList<Driver> drivers)
            throws IOException {

        this.socketDriver = socketClient;
        out = new PrintWriter(socketDriver.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socketDriver.getInputStream()));
        this.socketBroadcast = socketBroadcast;
        this.socketNorte = socketNorte;
        this.socketCentro = socketCentro;
        this.socketSul = socketSul;
        this.addressBroadcast = addressBroadcast;
        this.addressNorte = addressNorte;
        this.addressCentro = addressCentro;
        this.addressSul = addressSul;
        this.sharedObject = sharedObject;
        this.drivers = drivers;
    }

    @Override
    public void run() {
        String request;

        boolean login = false;

        while (!login) {
            try {
                request = in.readLine();
                System.out.println("Request:" + request);

                switch (request){
                    case Variables.SINGUP:
                        login = singUp();
                        break;
                    case Variables.LOGIN:
                        login = login();
                        break;
                    default:
                        break;
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        System.out.println("Client connected" );

        //User is logged in
        try {
            while(socketDriver.isConnected()){
                request = in.readLine();
                switch (request){
                    case (Variables.LOCATION):
                        location();
                        break;
                    case (Variables.AREA_ALERTS):
                        areaAlerts();
                        break;
                    case (Variables.CIRCUNC_ALERTS):
                        break;
                    case (Variables.FRIENDS):
                        friends();
                        break;
                    case (Variables.GROUPS):
                        break;
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public boolean singUp() throws IOException {
        String inputName = in.readLine();
        String inputUsername = in.readLine();
        String inputPassword = in.readLine();

        for (Object c : drivers.getAll()) {
            Driver driver = (Driver) c;
            if (driver.getClass().equals(inputUsername)) {
                out.println(Variables.INVALID_SINGUP);
                return false;
            }
        }

        Driver newDriver = new Driver(inputName,inputUsername, inputPassword);
        this.driver = newDriver;
        drivers.add(newDriver);
        out.println(Variables.VALID_SINGUP);

        return true;
    }

    public boolean login() throws IOException {
        String inputUsername = in.readLine();
        System.out.println(inputUsername);
        String inputPassword = in.readLine();

        if (drivers.getAll().isEmpty()) {
            out.println(Variables.INVALID_LOGIN);
            return false;
        }
            for (Object c : drivers.getAll()) {

                Driver driver = (Driver) c;
                if (driver.getUsername().equals(inputUsername)) {
                    if (driver.getPassword().equals(inputPassword)) {
                        out.println(Variables.VALID_LOGIN);
                        this.driver = driver;
                        return true;
                    }
                }
            }

                out.println(Variables.INVALID_LOGIN);
            return false;
    }


    public void location(){

        if(!this.driver.getCurrentLocation().toString().equals(null)){
            out.println(this.driver.getCurrentLocation().toString());
        }
        else {
            out.println("A sua localização é desconhecida!");
        }

        try {
            if (in.readLine().equals(Variables.NEW_LOCATION)){
                newLocation();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void newLocation(){
        try {
                String sLatit = in.readLine();
                double latit = Double.parseDouble(sLatit);

                String sLongit = in.readLine();
                double longit = Double.parseDouble(sLongit);

                this.driver.setCurrentLocation(latit, longit);
                out.println(Variables.VALID_LOCATION);
                out.println(this.driver.getCurrentLocation().toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void areaAlerts(){

    }

    public void friends(){
        if(!this.driver.getFriends().isEmpty()){
            for (Object c : drivers.getAll()) {

                Driver driver = (Driver) c;
                out.println(driver.getUsername());
            }
        }
    }
}
