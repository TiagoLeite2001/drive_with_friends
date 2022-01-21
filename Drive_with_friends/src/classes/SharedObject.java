package classes;

import java.util.ArrayList;

public class SharedObject {
    static final String IP_MULTICAST_COMUNITY =  "230.0.0.";
    static int IP_MULTICAST_COMUNITY_LAST_VALUE;

    private SynchronizedArrayList<Group> groupsList;
    private SynchronizedArrayList<Driver> driversList;

    public SharedObject(){
        this.groupsList = new SynchronizedArrayList<>();
        this.driversList = new SynchronizedArrayList<>();
        IP_MULTICAST_COMUNITY_LAST_VALUE = 7;
    }

    public void addGroup(Group g) {
        groupsList.add(g);
    }

    public ArrayList<Group> getGroups() {
        return groupsList.getAll();
    }

    public void addDriver(Driver d) {
        driversList.add(d);
    }

    public ArrayList<Driver> getDrivers() {
        return driversList.getAll();
    }

    public Driver getDriver(Driver driver){
        int index = this.getDrivers().lastIndexOf(driver);
        return this.getDrivers().get(index);
    }
}
