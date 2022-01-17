package others;

import helpers.Location;

public class Main {
    public static void main(String[] args) {
        Location l2 = new Location(3.11,4.11);
        Location l1 = new Location(3.112,4.112);
        Location l3 = new Location(0,0);

        System.out.println(l3.distanceTo(l2));
    }
}
