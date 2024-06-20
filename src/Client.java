import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;

public class Client {

    // netwroking variable
    Socket socket;

    DataInputStream dis; // to recieve the message
    DataOutputStream dos; // to send the message

    Thread rThread, wThread; // thread for read and write message

    public Client() {
        try {

            System.out.println("Connecting to the server");

            socket = new Socket("127.0.0.1", 8080);

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

                    System.out.println("Server : " + msg);

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
            System.out.println("Client Writing");
            try (BufferedReader br1 = new BufferedReader(new InputStreamReader(System.in))) {
                while (true) {
                    // so that br1 close automaticaly due to autocloseable

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

            if (rThread != null && !rThread.isInterrupted())
                rThread.interrupt();

            if (wThread != null && !wThread.isInterrupted())
                wThread.interrupt();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println("This is the chatapp client");

        // Client intialized which starts the program
        new Client();
    }
}
