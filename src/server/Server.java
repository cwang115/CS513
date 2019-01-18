package server;

import client.PublicClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.net.Socket;
import java.util.HashSet;

/**
 * @author Chu Wang
 * The server system in the Client-Server application
 *
 *1.Server operations (such as connect requests and disconnect requests) are printed out by the server in the console.
 *2.The server handles connections / disconnections without disruption of other services.
 *3.Each client has unique nicknames, duplicates are resolved before allowing a client to be connected.
 *4.All clients are informed of changes in the list of connected users.
 */
public class Server {
    /**
     * HashSet clientThreads stores all the client thread objects
     */
    private static HashSet<Threads> clientThreads = new HashSet<>();
    /**
     * HashSet clientNames stores current client names to avoid duplicate names
     */
    private static ArrayList<String> clientNames = new ArrayList<>();
    /**
     * HashSet PrintWriter stores all the printwriters for the clients
     */
    private static HashSet<PrintWriter> outputs = new HashSet<>();



    public void startServer() throws IOException {
        /**
         * Function startServer initialize server at port number 6000
         */
        ServerSocket serverSocket = new ServerSocket(6000);
        System.out.println("Server started...");
        try{
            while(true) {
                Socket socket = serverSocket.accept();
                System.out.println("Server connected with " + PublicClient.class.getName());
                //For each request, create a new Threads instance
                Threads clientThread = new Threads(socket);
                clientThreads.add(clientThread);
                //start the thread
                clientThread.start();
            }
        }catch (IOException e) {
            System.out.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        } finally {
            serverSocket.close();
        }
    }

    public void publicMessage(String clientName, String message) throws Exception {
        /**
         * publicMessage sends message to all users
         */
        for(Threads th : clientThreads) {
            th.output.println(clientName + ":" + message);
        }

    }

    /**
     * Threads class listens to client requests
     */
    class Threads extends Thread {
        private Socket clientSocket;
        private String clientName;
        BufferedReader input;
        PrintWriter output;
        boolean newUser = false;
        String privateChatgp = null;
        String fromClient = null;
        String toClient = null;


        /**
         * Threads object constructor
         * @param socket
         * @throws IOException
         */
        public Threads(Socket socket) throws IOException {
            this.clientSocket = socket;
            //get input from the client
            this.input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            // return the output to the client
            this.output = new PrintWriter(clientSocket.getOutputStream(),true);
            this.clientName = input.readLine();

            /**
             * First check if the conversation is private
             */
            if (clientName.contains("#")) {
                String newName = clientName;
                newName = newName.replace("#", ", ");
                String[] currentUsers = newName.split(", ");
                //referencing the list
                fromClient = currentUsers[0];
                toClient = currentUsers[1];
                privateChatgp = clientName;
                System.out.println("Private chat from " + fromClient + " to " + toClient + "\n");
            } else {
                /**
                 * Update users list and send public messages
                 */
                    if(!clientNames.contains(clientName)){
                        clientNames.add(clientName);
                        System.out.println("New user: " + clientName + "\n");
                        newUser = true;
                        //Notify clients with new users
                        newUserAlert(clientName);
                    } else {
                        //prevents making new client instances
                        output.println("#duplicate");
                        return;
                    }
                output.println("PUB" + clientNames);
                newUsersListAlert(clientNames);
                }
            }


        public String getClientName() {
            return clientName;
        }
        public String getGroupName() {
            return privateChatgp;
        }

        public void newUserAlert(String user) {
            for (Threads cf : clientThreads) {
                    cf.output.println(user + " has entered the chat room.\n");
            }
        }

        /**
         * Update all users with the list of current users
         * @param users
         */
        public void newUsersListAlert(ArrayList<String> users) {
            String message = "PUB" + users;
            for (Threads cf : clientThreads) {
                cf.output.println(message);
            }
        }

        public void sendMessage(String username, String message) {
            output.println(username + ":" + message);
        }

        public void publicMessage(String user, String message) {
            for (Threads ct : clientThreads) {
                    ct.output.println(user + ":" + message);
                }
            }

        /**
         * Closes socket and streams
         * @throws Exception
         */
        public void CloseSocket() throws Exception{
            if (output != null) { output.close(); }
            if (input != null) {input.close(); }
            if (clientSocket != null) { clientSocket.close();}
        }

        @Override
        public void run() {
            String line;
            try{
                while(true) {
                    line =input.readLine();
                    if (line.equals("EXIT")){
                        for (Threads clientThread:clientThreads){
                            clientThread.output.println( clientName + " has left the chat room.\n");
                        }
                        System.out.println("Client " + clientName + " has left the chat room.\n");
                        clientThreads.remove(this);
                        //clientNames.add(clientName);
                        CloseSocket();
                        output.flush();
                        break;
                    } /**
                        * Handles private disconnection with the keyword "end"
                        **/
                    else if (line.equals("end")) {
                        for (Threads th : clientThreads) {
                            if (th.getGroupName() != null) {
                                /**
                                 * notify the other user in the private chat
                                 */
                                if (th.getGroupName().equals(toClient + "#" + fromClient)) {
                                    th.output.println(fromClient + " has left the private chat.\n");
                                }
                            }
                        }
                        System.out.println(fromClient + " has left the private chat.\n");
                        clientThreads.remove(this);
                        CloseSocket();
                        break;
                    }
                    /**
                     * private conversation between specified users with the special character "->"
                     */
                    else if (line.contains("->")) {
                        boolean privateTalk = true;
                        for (Threads th : clientThreads) {
                            if (th.getGroupName() != null)
                            {
                                if (th.getGroupName().equals(toClient + "#" + fromClient)) {
                                    privateTalk = false;
                                    th.sendMessage(fromClient, line);
                                }
                            }
                        }
                        if (privateTalk) {
                            for (Threads th : clientThreads) {
                                if (th.getClientName().equals(toClient)) {
                                    th.sendMessage(fromClient, line);
                                }
                            }
                        }
                    } else{
                        publicMessage(clientName, line);
                    }
                }
            } catch (Exception e) {
                System.out.println("Exception: " + e.getMessage());
            }finally {
                if(clientName != null) {
                    clientNames.remove(clientName);
                }
                if (output != null) {
                    outputs.remove(output);
                }
                try{
                    CloseSocket();
                }catch (Exception e){
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

}
