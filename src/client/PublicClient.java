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

public class PublicClient {
    /**
     * ServerMain function to instantiate PublicClient Object
     */
    BufferedReader input;
    PrintWriter output;
    String clientName;
    Socket clientSoc;
    String serverAddress = "127.0.0.1";
    String[] currentUsers;
    String fromClient;
    String toClient;
    /**
     * client GUI initialization
     */
    JFrame frame = new JFrame("Chat Room");
    JTextField textField = new JTextField();
    JTextArea messageArea = new JTextArea();
    JLabel messageLabel;
    JButton disconnectButton;
    JButton privateMessage;
    JScrollPane conversationScroll;
    JTextArea conversationTextarea;
    JLabel conversationLabel;
    JScrollPane usersScroll;
    JLabel userLabel;
    JList userList;
    JLabel chatRoom;
    /**
     * PublicClient constructor
     * @param userName
     * @param serverAddress
     * @throws Exception
     */
    public PublicClient(String userName, String serverAddress) throws Exception {
        chatRoomUI();
        this.clientName = userName;
        this.frame.setTitle(userName);
        this.clientSoc = new Socket(serverAddress, 6000);
        this.input = new BufferedReader(new InputStreamReader(clientSoc.getInputStream()));
        this.output = new PrintWriter(clientSoc.getOutputStream(), true);
        this.output.println(userName);
        this.fromClient = userName;
        String inputName = input.readLine();

        /**
         * Process duplicate input names
         */
        if (inputName.contains("#")) {
            JOptionPane.showMessageDialog(null, "Nickname already taken, please choose another name!");
            String name = getClientName();
            try {
                /**
                 * Instantiate PublicClient object
                 */
                new PublicClient(name, serverAddress);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            /**
             * Start threads to handle concurrent Client operations
             */
        } else {
            frame.setVisible(true);
            setCurrentUsers(inputName);
            new inputThreads().start();
        }
    }

    /**
     * Initialize Chat Room UI
     */

    public void chatRoomUI() {
        frame.getContentPane().setBackground(Color.PINK);
        frame.setBounds(300, 300, 500, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        //Message Label
        messageLabel = new JLabel("Type your message here:");
        messageLabel.setFont(new Font("Courier New", Font.ITALIC, 20));
        messageLabel.setBounds(50, 470, 300, 30);
        frame.getContentPane().add(messageLabel);

        //textField = new JTextField();
        //textField.setFont(new Font("Times New Roman", Font.BOLD, 20));
        textField.setBounds(50, 500, 400, 30);
        frame.getContentPane().add(textField,"South");

        //leave Chat room
        disconnectButton = new JButton("EXIT");
        disconnectButton.setFont(new Font("Courier New", Font.BOLD, 20));
        disconnectButton.setBounds(50, 530, 80, 30);
        frame.getContentPane().add(disconnectButton);

        conversationScroll = new JScrollPane();
        conversationScroll.setBounds(50, 200, 400, 250);
        frame.getContentPane().add(conversationScroll);

        conversationTextarea = new JTextArea();
        conversationTextarea.setFont(new Font("Times New Roman", Font.PLAIN, 20));
        conversationScroll.setViewportView(conversationTextarea);
        conversationTextarea.setEditable(false);

        usersScroll = new JScrollPane();
        usersScroll.setBounds(50, 100, 400 ,80);
        frame.getContentPane().add(usersScroll);

        userList = new JList();
        userList.setFont(new Font("Times New Roman", Font.PLAIN, 20));
        usersScroll.setViewportView(userList);

        userLabel = new JLabel("Current Users");
        userLabel.setFont(new Font("Courier New", Font.PLAIN, 20));
        userLabel.setBounds(50, 60, 250, 35);
        frame.getContentPane().add(userLabel);

        privateMessage = new JButton("WHISPER");
        privateMessage.setFont(new Font("Courier New", Font.BOLD, 20));
        privateMessage.setBounds(270, 530, 150, 30);
        frame.getContentPane().add(privateMessage);

        chatRoom = new JLabel("Chat Room");
        chatRoom.setFont(new Font("Courier New", Font.PLAIN, 45));
        chatRoom.setBounds(50, 10, 500, 69);
        frame.getContentPane().add(chatRoom);

        disconnectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    frame.setVisible(false);
                    JOptionPane.showMessageDialog(null, "You have exited the chatroom.");
                    output.println("EXIT");
                    output.close();
                    input.close();

                } catch (IOException ex) {
                    Logger.getLogger(PublicClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        privateMessage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                   toClient = (String) userList.getSelectedValue();
                   new PrivateClient(serverAddress, toClient, fromClient, null );
                } catch(Exception ex){
                    System.out.println(ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });

        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //output.println(textField.getText());
                if(currentUsers != null) {
                    //conversationTextarea.append(clientName + ": " + textField.getText() + "\n");
                    output.println(textField.getText());
                    textField.setText("");
                }else{
                    conversationTextarea.setText("");
                    textField.setText("");
                    JOptionPane.showMessageDialog(null, "Sorry message cannot be sent!");
                }

            }
        });

    }
    private static String getClientName() {
        return JOptionPane.showInputDialog(null,"Input your nickname", JOptionPane.PLAIN_MESSAGE);
    }

    private void closeSocket() throws Exception{
        if (output != null) { output.close(); }
        if (input != null) {input.close(); }
        if (clientSoc != null) { clientSoc.close();}
        if(userList!= null){userList.removeAll();}
        textField.setEditable(false);
        disconnectButton.setEnabled(false);
        privateMessage.setEnabled(false);
    }
    private void setCurrentUsers(String message) throws Exception {
        String processStr = message.substring(3);
        processStr=processStr.replace("]", "");
        processStr= processStr.replace("[", "");
        currentUsers = null;
        currentUsers = processStr.split(", ");
        System.out.println("Current users list updated: "+processStr +"\n");
            /**
             * Update users to all clients
             */
        ArrayList<String> usersUpdated = new ArrayList<String>();
        for (int i = 0; i < currentUsers.length; i++) {
                if (!currentUsers[i].equals(clientName)) {
                    usersUpdated.add(currentUsers[i]);
                }
        }
        currentUsers = new String[usersUpdated.size()];
        if(usersUpdated!= null){
            for (int j = 0; j < usersUpdated.size(); j++) {
                currentUsers[j] = usersUpdated.get(j); }
        }
        if(currentUsers!= null){
            userList.setListData(currentUsers);}

    }

    private void updateUserList(String message) throws Exception {
        String processStr = message.substring(3);
        processStr = processStr.replace("[", "");
        processStr = processStr.replace("]", "");
        System.out.println("Current users list updated: "+processStr +"\n");
        String[] onlineUsers = processStr.split(", ");
        ArrayList<String> usersUpdated = new ArrayList<String>();
        for(int i=0;i<onlineUsers.length;i++) {
            if(!onlineUsers[i].equals(clientName)) {
                usersUpdated.add(onlineUsers[i]);
            }
        }
        onlineUsers = new String[usersUpdated.size()];
        if(usersUpdated != null) {
            for(int j=0; j < usersUpdated.size();j++) {
                onlineUsers[j] = usersUpdated.get(j);
            }
        }
        if(onlineUsers!=null) {
            userList.setListData(onlineUsers);
        }

    }


    class inputThreads extends Thread {
        public void run() {
            String message;
                try {
                    while(true) {
                        message = input.readLine();
                        /**
                         * public message
                         */
                        if(message.startsWith("PUB")){
                            updateUserList(message);
                       }
                        /**
                         * If message contains -> then instantiate the private client instance
                         */
                        else if(message.contains("->")) {
                            String text = message.substring(0);
                            String[] msg = text.split(":");
                            if(!msg[0].equals(toClient)){
                                try {
                                    toClient = msg[0];
                                    new PrivateClient(serverAddress, toClient,fromClient,message);
                                }
                                catch(Exception e) { e.printStackTrace(); }
                            }
                            /**
                             * handles case where the server has stopped
                             */
                        } else if(message.equals("???")) {
                            JOptionPane.showMessageDialog(null, "Opps, server has stopped...");
                            closeSocket();
                        } else {
                            System.out.println("public message: " +message +"\n");
                            conversationTextarea.append(message + "\n"); }
                    }
                } catch(Exception e) {}
            }
        }








}