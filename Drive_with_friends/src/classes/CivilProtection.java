package classes;

import helpers.Variables;

import java.io.IOException;
import java.net.Socket;

public class CivilProtection {
    public static void main(String[] args) {
        Socket socket;

        try {
            socket = new Socket(Variables.IP_USER, Variables.PORT_SERVER);
            new InterfaceCP(socket).start();
        } catch (IOException e) {
            System.err.println("Connection refused.");
        }
    }
}
