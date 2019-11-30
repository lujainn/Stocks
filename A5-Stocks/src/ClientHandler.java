import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

public class ClientHandler implements Runnable{

    Socket socket;
    List<String> tickers;
    String username;

    public ClientHandler(Socket socket){
        this.socket=socket;
        tickers= new ArrayList<>();
    }
    @Override
    public void run() {
        try {
        Scanner scanner= new Scanner(socket.getInputStream());

        while(true){

                scanner.useDelimiter("!");
                while (scanner.hasNext()){
                    String fullCommand = scanner.next();
                    String[] commands= fullCommand.split(" ");
                    switch (commands[0]){
                        case "USER":
                            userLogIn(commands[1]);
                            break;
                        case "PORTFOLIO":
                            portfolio();
                            break;
                        case "TRACK":
                            track(commands[1]);
                            break;
                        case "FORGET":
                            forget(commands[1]);
                            break;
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void userLogIn(String username){
        this.username = username;
    }
    private void portfolio(){}

    private void track(String ticker){
        tickers.add(ticker);
    }
    private void forget(String ticker){
        tickers.remove(ticker);
    }

    public List<String> getTicker() {
        return tickers;
    }
    public String getUsername() {
        return username;
    }





}
