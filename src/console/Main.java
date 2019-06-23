package console;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

// Il ne faut pas oublier d'ajouter la bibliothéque Connector/J au projet.
public class Main {

  private static final Scanner in = new Scanner(System.in);
  private static final PrintStream out = System.out;

  public static void main(String[] args) {
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException ex) {
      ex.printStackTrace();
      System.exit(-1);
    }

    // "localhost" == "127.0.0.1"
    // on peut mettre google.com si notre bdd est stocké chez google.
    String host = "localhost";
    String port = "3306";
    String dbName = "Magasin";
    String userName = "root";
    String password = "";

    try {
      Connection connection
              = DriverManager.getConnection("jdbc:mysql://"
                      + host + ":" + port
                      + "/" + dbName + "?useSSL=false",
                      userName, password);

      // Ajouter des clients.
      while (true) {
        String nom = in.nextLine();
        if ("exit".equals(nom)) {
          break;
        }

        PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO Client(Nom) VALUES (?)"
        );
        preparedStatement.setString(1, nom);
        preparedStatement.execute();
      }

      // Afficher tout les clients.
      Statement statement = connection.createStatement();
      ResultSet set = statement.executeQuery(
              "SELECT * FROM Client"
      );
      while (set.next()) {
        int id = set.getInt("ID");
        String nom = set.getString("Nom");
        out.println(id + ": " + nom);
      }

      // Supprimer des client avec leur nom.
      while (true) {
        String nom = in.nextLine();
        if ("exit".equals(nom)) {
          break;
        }

        PreparedStatement preparedStatement = connection.prepareStatement(
                "DELETE FROM Client WHERE Nom = ?"
        );
        preparedStatement.setString(1, nom);
        preparedStatement.executeUpdate();
      }
    } catch (SQLException ex) {
      ex.printStackTrace();
    }

  }

}
