import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Server {

    // netwroking variable
    ServerSocket server;
    Socket socket;

    // fileIO variable
    DataInputStream dis; // to recieve the message
    DataOutputStream dos; // to send the message

    Thread rThread, wThread; // thread for read and write message

    public Server() {
        // serverSocket can throw error if port is busy
        try {
            server = new ServerSocket(8080);

            System.out.println("Server started");
            System.out.println("Waiting...");

            socket = server.accept(); // waiting for client when connected client socket object goes in the socket

            System.out.println("Connected");

            dis = new DataInputStream(socket.getInputStream()); // for reading the message

            dos = new DataOutputStream(socket.getOutputStream());// for writing the messages

            // reading and writing method so that they can be done simultaneously
            startReading();

            startWriting();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startReading() {
        // message reading via a thread
        // lamba is used here because run is SAM of runnable
        Runnable r1 = () -> {
            System.out.println("Started reading");
            try {
                while (true) {

                    String msg = dis.readUTF();
                    if (msg.equalsIgnoreCase("bye")) {
                        System.out.println("Chat Closed");
                        break;
                    }

                    System.out.println("Client : " + msg);

                }
            }
            catch(SocketException se){
                System.out.println("Connection closed");
            } 
            catch (Exception e) {
                e.printStackTrace();
            }
            // when loop breaks it goes in the finally and closes all resuources
            finally {
                closeResources();
            }
        };

        // when start is called wThread will call the above run method
        rThread = new Thread(r1);
        rThread.start();
    }

    public void startWriting() {
        // message writing via another thread
        Runnable r2 = () -> {
            System.out.println("Sever Writing");
            // so that br1 close automaticaly due to autocloseable
            try (BufferedReader br1 = new BufferedReader(new InputStreamReader(System.in))) {
                while (true) {

                    String content = br1.readLine();

                    if (content.isEmpty()) {
                        continue;
                    }

                    dos.writeUTF(content);
                    dos.flush();

                    // if bye is recieved then it close the connection
                    if (content.equalsIgnoreCase("bye")) {
                        break;
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            // when loop breaks it goes in the finally and closes all resuources
            finally {
                closeResources();
            }

        };

        // when start is called wThread will call the above run method
        wThread = new Thread(r2);
        wThread.start();
    }

    public void closeResources() {
        try {
            // Checks all the resources then closig them for proper resourve management
            if (dis != null)
                dis.close();

            if (dos != null)
                dos.close();

            if (socket != null)
                socket.close();

            if (server != null)
                server.close();

            if (rThread != null && !rThread.isInterrupted())
                rThread.interrupt();

            if (wThread != null && !wThread.isInterrupted())
                wThread.interrupt();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Welcome to the chatApp server");

        new Server();
    }
}
