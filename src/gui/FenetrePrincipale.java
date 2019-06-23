package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;

// Il ne faut pas oublier d'ajouter la bibliothéque Connector/J au projet.
// Et de demarrer le serveur de bdd mysql.
public class FenetrePrincipale extends JFrame {

  private DefaultListModel<Client> clientListModel;
  private String userName;
  private String password;

  public FenetrePrincipale(String userName, String password) throws HeadlessException {
    this.userName = userName;
    this.password = password;

    setTitle("Magasin");
    setSize(640, 480);
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    JTextFieldX nomTF = new JTextFieldX();
    nomTF.setPlaceHolder("Entrez le nom du client");

    clientListModel = new DefaultListModel<>();
    JList<Client> clientList = new JList<>(clientListModel);
    clientList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    JScrollPane scrollPane = new JScrollPane(clientList);
    scrollPane.setPreferredSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

    JLabel hint1 = new JLabel("Pour ajouter : remplir le champ et appuyer sur entrée.");
    JLabel hint2 = new JLabel("Pour modifier : selectionner puis remplir le champ et appuyer sur ctrl+entrée.");
    JLabel hint3 = new JLabel("Pour supprimer : selectionner puis appyuer sur suppr (ou del).");

    Box clientPanel = Box.createVerticalBox();
    clientPanel.add(hint1);
    clientPanel.add(hint2);
    clientPanel.add(hint3);
    clientPanel.add(Box.createVerticalStrut(20));
    clientPanel.add(nomTF);
    clientPanel.add(Box.createVerticalStrut(20));
    clientPanel.add(scrollPane);
    clientPanel.setBorder(
            BorderFactory.createEmptyBorder(
                    20, 20, 20, 20
            )
    );
//    clientPanel.setOpaque(true);
//    clientPanel.setBackground(Color.red);

    clientList.addListSelectionListener((e) -> {
      Client client = clientList.getSelectedValue();
      if (client != null) {
        nomTF.setText(client.getNom());
      }
    });
    clientList.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DELETE) {
          Client client = clientList.getSelectedValue();
          if (client != null) {
            deleteUser(client);
          }
        }
      }
    });

    nomTF.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() != KeyEvent.VK_ENTER) {
          return;
        }
        String nom = nomTF.getText();
        if (e.isControlDown()) {
          Client client = clientList.getSelectedValue();
          if (client != null) {
            updateUser(client, nom);
          } else {
            showErrorDialog("Aucun élement selectionner.");
          }
        } else {
          createUser(nom);
        }
      }
    });

    JPanel commandePanel = new JPanel();
//    commandePanel.setBackground(Color.blue);

    JLabel todo = new JLabel("@TODO: créer un JTable pour les commandes et"
            + " implementer les 4 opérations (CRUD, Create Read Update Delete).");
    commandePanel.add(todo);

    JSplitPane splitPane = new JSplitPane();
    splitPane.setLeftComponent(clientPanel);
    splitPane.setRightComponent(commandePanel);
    splitPane.setOneTouchExpandable(true);

    add(splitPane, BorderLayout.CENTER);

    loadAllClients();

    setLocationRelativeTo(this);
    setVisible(true);
  }

  private void deleteUser(Client client) {
    try (Connection connection = getConnection(userName, password)) {
      PreparedStatement statement = connection.prepareStatement(
              "DELETE FROM Client WHERE ID = ?"
      );
      statement.setInt(1, client.getId());
      if (statement.executeUpdate() != 1) {
        showErrorDialog("Impossible de supprimer le client ID=" + client.getId() + ".");
      } else {
        clientListModel.removeElement(client);
      }
    } catch (SQLException ex) {
      showErrorDialog("Impossible de supprimer le client ID=" + client.getId() + ".");
    }
  }

  private void updateUser(Client client, String nouvNom) {
    try (Connection connection = getConnection(userName, password)) {
      PreparedStatement statement = connection.prepareStatement(
              "UPDATE Client SET Nom = ? WHERE ID = ?"
      );
      statement.setInt(2, client.getId());
      statement.setString(1, nouvNom);
      if (statement.executeUpdate() != 1) {
        showErrorDialog("Impossible de mettre à jour le client ID=" + client.getId() + ".");
      } else {
        client.setNom(nouvNom);
//                clientList.repaint();
        repaint();
      }
    } catch (SQLException ex) {
      showErrorDialog("Impossible de mettre à jour le client ID=" + client.getId() + ".");
    }
  }

  private void createUser(String nom) {
    try (Connection connection = getConnection(userName, password)) {
      PreparedStatement statement = connection.prepareStatement(
              "INSERT INTO Client(Nom) VALUES (?)",
              PreparedStatement.RETURN_GENERATED_KEYS
      );
      statement.setString(1, nom);
      if (statement.executeUpdate() != 1) {
        showErrorDialog("Impossible de créer le client \"" + nom + "\".");
      } else {
        ResultSet set = statement.getGeneratedKeys();
        int id = 0;
        if (set.next()) {
          id = set.getInt(1);
        }
        clientListModel.addElement(new Client(id, nom));
      }
    } catch (SQLException ex) {
      showErrorDialog("Impossible de créer le client \"" + nom + "\".");
    }
  }

  private void loadAllClients() {
    try (Connection connection = getConnection(userName, password)) {
      Statement statement = connection.createStatement();
      ResultSet set = statement.executeQuery("SELECT * FROM Client");
      while (set.next()) {
        clientListModel.addElement(
                new Client(set.getInt("ID"), set.getString("Nom"))
        );
      }
    } catch (SQLException ex) {
      showErrorDialog("Impossible de charger les clients.");
    }
  }

  private void showErrorDialog(String errorMessage) {
    JOptionPane.showMessageDialog(this, errorMessage,
            "Une erreur c'est produite", JOptionPane.ERROR_MESSAGE);
  }

  private static Connection getConnection(String userName, String password)
          throws SQLException {
    String host = "localhost";
    String port = "3306";
    String dbName = "Magasin";

    return DriverManager.getConnection("jdbc:mysql://"
            + host + ":" + port
            + "/" + dbName + "?useSSL=false",
            userName, password);
  }

  public static void main(String[] args) {
    new FenetrePrincipale("root", "");
  }

}
