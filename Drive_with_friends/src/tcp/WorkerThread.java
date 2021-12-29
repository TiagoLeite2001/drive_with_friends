package tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class WorkerThread extends Thread{
    private Socket socket = null;
    private ArrayList msg;

    public WorkerThread(Socket socket){
        super("WorkerThred");
        this.socket = socket;
        this.msg = new ArrayList();
    }

    public ArrayList receive(){
        return this.msg;
    }

    public void run(){
        try{
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader( new InputStreamReader(
                    socket.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null){
                InetAddress ipAddress = socket.getInetAddress();
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                LocalDateTime now = LocalDateTime.now();
                String frase = inputLine + "; IP: " + ipAddress + " ; Date and Time: " + dtf.format(now);
                msg.add(frase);
                out.println(inputLine + "; IP: " + ipAddress + " ; Date and Time: " + dtf.format(now));
                if(inputLine.equals("Bye"))
                    break;
            }
            out.close();
            in.close();
            socket.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

}
