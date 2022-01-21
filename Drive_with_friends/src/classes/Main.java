package classes;

import helpers.Location;

public class Main {
    public static void main(String[] args) {
        Location l = new Location(3.1, 3.1);
        Location l1 = new Location(3.1, 3.2);

        System.out.println(l.distanceTo(l1));
    }


}
