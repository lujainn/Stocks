import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

public class ClientHandler implements Runnable{

    Socket socket;
    List<String> tickers;
    String username;

    PrintWriter writer;

    public ClientHandler(Socket socket){
        this.socket=socket;
        tickers= new ArrayList<>();
    }
    @Override
    public void run() {
        try {
        Scanner scanner= new Scanner(socket.getInputStream());
        scanner.useDelimiter("!");
        writer = new PrintWriter(socket.getOutputStream());

        while(true){
                while (scanner.hasNext()){
                    String fullCommand = scanner.next();
                    System.out.println("received " + fullCommand);
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

    private String getStock(String stock) {
        try {
            HttpClient client = HttpClient.newBuilder().build();
            URI uri = new URI("https://financialmodelingprep.com/api/v3/stock/real-time-price/" + stock);
            HttpRequest request = HttpRequest.newBuilder(uri).build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString(Charset.defaultCharset()));

            System.out.println(response.body());
            return response.body();
        } catch (IOException | URISyntaxException | InterruptedException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void userLogIn(String username){
        this.username = username;
        String tickersSQL = "SELECT * FROM Tracked WHERE NameUser = ?";

        try (Connection conn = SimpleDataSource.getConnection())  {
            PreparedStatement statement = conn.prepareStatement(tickersSQL);

            statement.setString(1, username);
            try {
                ResultSet resultSet =statement.executeQuery();
                if(resultSet.next()){
                    tickers = Arrays.asList(resultSet.getString("Tickers").split(","));
                } else {
                    tickers = new ArrayList<>();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            writer.print("ok!");
            writer.flush();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void portfolio(){
        if(tickers.size() == 0) writer.print("none");
        for(String ticker : tickers) {
            System.out.println(ticker);
            double price = StockPriceParser.parsePrice(getStock(ticker));
            writer.println( ticker+ " " + price);
        }
        writer.print("!");
        writer.flush();
    }

    private void track(String ticker){
        if(getStock(ticker).equals("{ }")) {
            writer.print("error!");
        } else {
            tickers.add(ticker);
            writer.print("ok!");
        }
        insert();

        writer.flush();
    }
    private void insert() {
        try (Connection conn = SimpleDataSource.getConnection()) {
            String query = "SELECT * FROM Tracked WHERE NameUser = ?";

            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setString(1, username);
            ResultSet rs = preparedStatement.executeQuery();
            StringBuilder singleBuilder = new StringBuilder();
            int i = 1;
            for(String tracked : tickers) {
                singleBuilder.append(tracked);
                if(i != tickers.size()) {
                    singleBuilder.append(",");
                }
                i++;
            }
            if(rs.next()) {
                System.out.println("UPDATING");
                String updateStr = "UPDATE Tracked SET Tickers = ? WHERE NameUser = ?";
                PreparedStatement update = conn.prepareStatement(updateStr);

                update.setString(1, singleBuilder.toString());
                update.setString(2, username);

                update.executeUpdate();
            }
            else {
                System.out.println("INSERTING");

                String insertStr = "INSERT INTO Tracked VALUES (?, ?)";
                PreparedStatement insert = conn.prepareStatement(insertStr);

                insert.setString(1, username);
                insert.setString(2, singleBuilder.toString());
                insert.execute();
            }
        }catch(SQLException e) {
            e.printStackTrace();
        }
    }
    private void forget(String ticker){
        tickers.remove(ticker);
        insert();
        writer.print("ok!");
        writer.flush();
    }

    public List<String> getTicker() {
        return tickers;
    }
    public String getUsername() {
        return username;
    }





}
