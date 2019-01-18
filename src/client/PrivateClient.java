package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *@author Chu Wang
 *The client system in the Client-Server application
 *
 *1.A list of online users is displayed on Client’s GUI.
 *2.Connection/disconnection actions of users are displayed on Client’s GUI.
 *3.Messages from the originating user and other users are displayed on each Client.
 *4.Clients are able to receive messages while typing a message
 *5.Clients are able to disconnect without disrupting the server.
 */
public class PrivateClient {
    String fromClient;
    String toClient;
    Socket clientSoc;
    BufferedReader input;
    PrintWriter output;
    ArrayList<String> currentUsers = new ArrayList<>();
    DefaultListModel<String> model = new DefaultListModel<>();

    /**
     * private chat UI
     */
    JFrame clientPrivateWindow;
    JTextField textField;
    JLabel privateChat;
    JButton exitPrivateButton;
    JScrollPane messageScroll;
    private final TextArea conversationArea = new TextArea();

    public PrivateClient(String serverAddress,String toClient, String fromClient, String message) throws Exception {
    privateUI();
    clientPrivateWindow.setTitle(fromClient + " whispers to " + toClient);
    this.fromClient = fromClient;
    this.toClient = toClient;
    if(message!=null) {
        String messageDisplay = message.substring(0);
        messageDisplay = messageDisplay.replace("->", "");
        conversationArea.append(messageDisplay + "\n");
    }
    clientPrivateWindow.setVisible(true);
    conversationArea.setEditable(false);
    clientSoc = new Socket(serverAddress, 6000);
    input = new BufferedReader(new InputStreamReader(clientSoc.getInputStream()));
    output = new PrintWriter(clientSoc.getOutputStream(), true);                   
    output.println(fromClient + "#" + toClient);
    new InputThread().start();
    }

    private void privateUI() {
        clientPrivateWindow = new JFrame();
        clientPrivateWindow.getContentPane().setBackground(Color.ORANGE);
        clientPrivateWindow.setBounds(300, 300, 500, 600);
        clientPrivateWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        clientPrivateWindow.getContentPane().setLayout(null);

        privateChat = new JLabel("Private Chat");
        privateChat.setFont(new Font("Courier New", Font.BOLD, 30));
        privateChat.setBounds(50, 50,300,40);
        clientPrivateWindow.getContentPane().add(privateChat);

        textField = new JTextField();
        textField.setFont(new Font("Courier New", Font.PLAIN, 20));
        textField.setBounds(50, 500, 400, 30);
        clientPrivateWindow.getContentPane().add(textField);
        textField.setColumns(10);

        exitPrivateButton = new JButton("Exit");
        exitPrivateButton.setFont(new Font("Courier New", Font.PLAIN, 20));
        exitPrivateButton.setBounds(50, 530, 80, 30);
        clientPrivateWindow.getContentPane().add(exitPrivateButton);

        messageScroll = new JScrollPane();
    	messageScroll.setBounds(50, 100, 400, 250);
    	clientPrivateWindow.getContentPane().add(messageScroll);
    	conversationArea.setFont(new Font("Times New Roman", Font.PLAIN, 20));
    	messageScroll.setRowHeaderView(conversationArea);


        exitPrivateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    clientPrivateWindow.setVisible(false);
                    JOptionPane.showMessageDialog(null, "You have left the private talk");
                    output.println("end");
                    output.close();
                    input.close();
                    clientSoc.close();
                } catch (IOException ex) {
                    Logger.getLogger(PrivateClient.class.getName()).log(Level.SEVERE,null, ex);
                }
            }
        });
      textField.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
              conversationArea.append(fromClient + ": " + textField.getText() + "\n");
              output.println("->"+textField.getText());
              textField.setText("");   
          }
      });
    }

    private void closeSocketpri() throws Exception{
        if (output != null) { output.close(); }
        if (input != null) {input.close(); }
        if (clientSoc != null) { clientSoc.close();}
        textField.setEditable(false);
        exitPrivateButton.setEnabled(false);
    }
    class InputThread extends Thread {
        @Override
        public void run(){
            String message;
            try{
                while(true) {
                    message = input.readLine();;
                    if(message.contains("->")) {
                        message = message.replace("->","");
                       // System.out.println(fromClient + " whispers to " + toClient + ": " + message + "\n");
                        System.out.println("Private message "+ message+"\n");
                        conversationArea.append(message + "\n");
                    }else if(message.startsWith("PUB")){
                        //TODO checkUsers
                        String checkUsers = message.substring(3);
                        System.out.println("List of current users updated: " + checkUsers);
                        checkUsers = checkUsers.replace("[","");
                        checkUsers = checkUsers.replace("]","");
                        String[] currentUsers = checkUsers.split(",");
                        for(int i = 0; i < currentUsers.length; i++){
                             model.addElement(currentUsers[i]);
                        }
                    }else if(message.contains("???")) {
                        JOptionPane.showMessageDialog(null, "Oops, server has stopped...");
                        closeSocketpri();
                    } else {
                        conversationArea.append(message + "\n");}
                }
            }catch(Exception e) {}

        }

    }

}
