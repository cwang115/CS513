package client;

import javax.swing.*;

public class ClientMain {

    /**
     * Entry point for client system
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String clientName = JOptionPane.showInputDialog(null,"Input your nickname :", "Nickname",JOptionPane.PLAIN_MESSAGE);;
        String serverAddress = "127.0.0.1";
        try{
            new PublicClient(clientName,serverAddress);
        }catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
