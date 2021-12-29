import java.lang.reflect.Constructor;
import java.util.List;

public class Driver {
    private int id;
    private String username;
    private String name;
    private String password;
    private Location currentLocation;
    private List<Driver> friends;
    private List<Group> groups;

    public Driver(String username,String name, String password){
        this.username = username;
        this.id = id;
        this.name = name;
        this.password = password;
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

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }

    public List<Driver> getFriends() {
        return friends;
    }

    public void setFriends(List<Driver> friends) {
        this.friends = friends;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
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
}
