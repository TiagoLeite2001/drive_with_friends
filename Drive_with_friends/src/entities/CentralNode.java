package entities;

import others.Location;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class CentralNode {
    private static ArrayList<Driver> driversList = new ArrayList<>();

    private int numbOfDrivers;

    public CentralNode(){
        this.numbOfDrivers = 0;
    }

    public void singUp(String username, String name, String password) throws Exception {
        Driver driver = new Driver(username);

        //verificar se existe algum condutor com o mesmo username
        if(!driversList.contains(driver)){
            Socket socket = new Socket();
            driver = new Driver(socket, username, name, password);
            driversList.add(driver);
        }
        else{throw new Exception("O username inserido já existe!");}
    }

    public boolean login(String username, String password){
        Driver driver = new Driver(username);

        if (driversList.contains(driver)){
            driver = driversList.get(driversList.indexOf(driver));
            return driver.login(password);
        }
        return false;
    }

    //radius -> km
    public double densityOfDriversInArea(double latitude, double longitude, double radius){
        Location area = new Location(latitude, longitude);
        int numbOfDrivers = 0;

        for(Driver driver : driversList) {
            if(driver.getCurrentLocation().distanceTo(area) <= radius){
                numbOfDrivers++;
            }
        }

        return numbOfDrivers/(3.14*radius*radius);
    }

    public String alertProtecaoCivil(String msg){
        return msg;
    }


}
