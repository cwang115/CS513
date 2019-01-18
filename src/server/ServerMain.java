package server;

import java.io.IOException;

public class ServerMain {
    /**
     * Entry point for server system.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        System.out.println("Starting server..");
        new Server().startServer();
    }
}
