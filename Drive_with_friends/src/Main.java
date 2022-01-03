import others.Location;

public class Main {
    public static void main(String[] args) {
        Location casa = new Location(41.321151, -8.228299);
        Location escola = new Location(41.366839,  -8.195088);
        double distance = casa.distanceTo(escola);
        System.out.println(distance);
        System.out.println(casa + " to " + escola);
    }
}
