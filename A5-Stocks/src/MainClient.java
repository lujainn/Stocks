import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.SocketHandler;

public class MainClient {
    public static void main(String[] args) {
        try {
            Socket clientSocket= new Socket("127.0.0.1", 10051);
            Scanner scanner = new Scanner(clientSocket.getInputStream());
            scanner.useDelimiter("!");
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
            Scanner inputScanner = new Scanner(System.in);
            while(inputScanner.hasNextLine()) {
                writer.print(inputScanner.nextLine());
                writer.flush();
                while(!scanner.hasNext()){Thread.sleep(10);}
                System.out.println(scanner.next() + "!");
            }
            clientSocket.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
