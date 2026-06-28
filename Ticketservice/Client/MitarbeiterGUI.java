package Ticketservice.Client;

import javax.swing.*;
import java.awt.*;

public class MitarbeiterGUI extends JFrame {
    public MitarbeiterGUI(int mitarbeiterId, String name) {
        setTitle("Mitarbeiter-Dashboard");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel willkommenLabel = new JLabel("Angemeldet als Mitarbeiter: " + name + " (ID: " + mitarbeiterId + ")", SwingConstants.CENTER);
        willkommenLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(willkommenLabel, BorderLayout.NORTH);

        // Hier kannst du später Buttons für die Ticketbearbeitung hinzufügen
        JPanel panel = new JPanel();
        panel.add(new JButton("Meine Tickets aktualisieren"));
        panel.add(new JButton("Ticket übernehmen"));
        add(panel, BorderLayout.CENTER);
    }
}