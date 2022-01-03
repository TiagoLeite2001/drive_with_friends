package others;

public class AlertLocation {
    private Location location;
    private double radius;

    public AlertLocation(Location location, double radius){
        this.location = location;
        this.radius = radius;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }
}
