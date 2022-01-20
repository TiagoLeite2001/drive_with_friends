package classes;

import others.Group;

import java.util.ArrayList;

public class SharedObject {
    private SynchronizedArrayList<Group> groupsList;
    private SynchronizedArrayList<Driver> driversList;

    public SharedObject(){
        this.groupsList = new SynchronizedArrayList<>();
        this.driversList = new SynchronizedArrayList<>();
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
}
