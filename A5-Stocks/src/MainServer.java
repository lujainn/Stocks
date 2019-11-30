import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MainServer {

    public static void main(String[] args) {
        try {
            SimpleDataSource.init("src/database.properties");
        }catch(IOException e ) {
            e.printStackTrace();
        } catch(ClassNotFoundException e) {
            e.printStackTrace();
        }
        try (Connection conn = SimpleDataSource.getConnection())  {
            ResultSet rs = conn.getMetaData().getTables(null,null,"TRACKED", new String[] {"TABLE"});
            if(!rs.next()) {
                System.out.println("CREATING TABLE");
                Statement stat = conn.createStatement();
                String createSQL = "CREATE TABLE Tracked ( NameUser VARCHAR(10), Tickers VARCHAR(1000) )";
                stat.execute(createSQL);
            }
        } catch(SQLException e ) {
            e.printStackTrace();
        }
        try {
            ServerSocket serverSocket = new ServerSocket(10051);
            while(true){
                Socket socket= serverSocket.accept();
                Thread thread= new Thread(new ClientHandler(socket));
                thread.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }



}
