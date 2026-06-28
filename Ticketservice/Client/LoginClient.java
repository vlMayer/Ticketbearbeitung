package Ticketservice.Client;

import Ticketservice.Server.src.interfaces.ILoginService;

import javax.swing.*;
import java.awt.*;
import java.rmi.Naming;

public class LoginClient extends JFrame {
    private JTextField idField;
    private JTextField nameField;
    private JComboBox<String> rolleBox;
    private JButton loginButton;
    private ILoginService loginService;

    public LoginClient() {
        verbindeMitServer();

        setTitle("Ticketservice - Login");
        setSize(350, 220);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 2, 10, 10));

        add(new JLabel("  Benutzer-ID:"));
        idField = new JTextField();
        add(idField);

        add(new JLabel("  Name:"));
        nameField = new JTextField();
        add(nameField);

        add(new JLabel("  Rolle:"));
        rolleBox = new JComboBox<>(new String[] {"Kunde", "Mitarbeiter"});
        add(rolleBox);

        add(new JLabel(""));
        loginButton = new JButton("Login");
        add(loginButton);

        loginButton.addActionListener(e -> fuehreLoginAus());
    }

    private void verbindeMitServer() {
        try {
            loginService = (ILoginService) Naming.lookup("rmi://localhost:1099/LoginService");
            System.out.println("Erfolgreich mit dem Server verbunden.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Fehler bei der Serververbindung:\n" + e.getMessage(),
                "Verbindungsfehler", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void fuehreLoginAus() {
        try {
            int id = Integer.parseInt(idField.getText().trim());
            String name = nameField.getText().trim();
            String gewaehlteRolle = (String) rolleBox.getSelectedItem();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Bitte einen Namen eingeben.");
                return;
            } else if (gewaehlteRolle == "Mitarbeiter") {
                boolean erfolgreich = loginService.authentifiziereMitarbeiter(id, name);
                if (erfolgreich) {
                    new MitarbeiterGUI(id, name).setVisible(true);
                    this.dispose();
                }
            } else if (gewaehlteRolle == "Kunde") {
                boolean erfolgreich = loginService.authentifiziereKunde(id, name);
                if (erfolgreich) {
                    new KundenGUI(id, name).setVisible(true);
                    this.dispose();
                }
            } else {
                JOptionPane.showMessageDialog(this,
                    "Login fehlgeschlagen. ID, Name oder Rolle stimmt nicht.",
                    "Fehler", JOptionPane.WARNING_MESSAGE);
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Die ID muss eine gültige Zahl sein!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Fehler bei der Kommunikation mit dem Server.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginClient().setVisible(true));
    }
}