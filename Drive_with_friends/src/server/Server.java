package server;

import java.sql.Driver;
import java.util.List;

public class Server {
    private List<Driver> driversList;
    private int numbOfDrivers;

    public Server(){
        this.numbOfDrivers = 0;
    }

    public void singUp(Driver driver) throws Exception {
        //verificar se existe algum condutor com o mesmo username
        if(!driversList.contains(driver)){
            driversList.add(driver);
        }
        else{throw new Exception("O username inserido já existe!");}
    }


}
