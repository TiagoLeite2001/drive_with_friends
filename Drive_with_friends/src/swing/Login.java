package swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Login {
    public static void main(String[] args) {

        JPanel card1 = new JPanel();

        JPanel card2 = new JPanel();

        final String BUTTONPANEL = "Card with JButtons";
        final String TEXTPANEL = "Card with JTextField";

        //Create the panel that contains the "cards".
        JPanel cards = new JPanel(new CardLayout());
        cards.add(card1, BUTTONPANEL);
        cards.add(card2, TEXTPANEL);

        cards.setSize(1000,100);    
        cards.setVisible(true);



        /**
         * JFrame frame=new JFrame();
         *         JPanel panelLogin = new JPanel();
         *
         *
         *         //frame.getContentPane().removeAll();
                  //frame.getContentPane().add(new JPanel());
        JButton loginB=new JButton("Login");//creating instance of JButton
        loginB.setBounds(130,100,100, 40);//x axis, y axis, width, height

        JButton singUpB=new JButton("SingUp");//creating instance of JButton
        singUpB.setBounds(130,200,100, 40);

        panelLogin.add(loginB);//adding button in JFrame
        panelLogin.add(singUpB);

        frame.setSize(400,500);//400 width and 500 height
        frame.setLayout(null);//using no layout managers
        frame.setVisible(true);//making the frame visible

        frame.getContentPane().add(panelLogin);
         */


    }
}