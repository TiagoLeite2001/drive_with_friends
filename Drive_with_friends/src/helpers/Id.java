package helpers;

public class Id {
    public static int id = 0;

    public static int getID(){
        int oldID = id;
        id++;

        return oldID;
    }
}
