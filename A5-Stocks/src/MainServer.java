import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MainServer {

    public static void main(String[] args) {

        try {
            ServerSocket serverSocket = new ServerSocket(1001);
            while(true){
                Socket socket= serverSocket.accept();
                Thread thread= new Thread(new ClientHandler(socket));
                serverSocket.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }



}
