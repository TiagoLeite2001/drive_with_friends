package classes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientProtocol {
    String clientType;
    BufferedReader bufferedReader;

    public ClientProtocol(Socket socket){
        try {
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientType = bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getClientType() throws IOException {
        return clientType;
    }
}
