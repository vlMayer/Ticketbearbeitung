package Ticketservice.Client;

import Ticketservice.Server.src.interfaces.IBeschwerdeService;
import Ticketservice.Server.src.interfaces.IKundenService;
import Ticketservice.Server.src.interfaces.ITicketbearbeitungsService;

import javax.swing.*;
import java.awt.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

public class KundenGUI extends JFrame {

    private final int kundenId;
    private final String name;
    private IBeschwerdeService beschwerdeService;
    private IKundenService kundenService;
    private ITicketbearbeitungsService ticketbearbeitungsService;

    private JPanel ticketPanel;
    private JScrollPane scrollPane;

    public KundenGUI(int kundenId, String name) {
        this.kundenId = kundenId;
        this.name = name;

        // RMI-Verbindung aufbauen
        try {
            Registry reg = LocateRegistry.getRegistry("localhost", 1099);
            beschwerdeService = (IBeschwerdeService) reg.lookup("BeschwerdeService");
            kundenService = (IKundenService) reg.lookup("KundenService");
            ticketbearbeitungsService = (ITicketbearbeitungsService) reg.lookup("TicketbearbeitungsService");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "RMI-Verbindung fehlgeschlagen: " + e.getMessage(), "Fehler", JOptionPane.ERROR_MESSAGE);
        }

        setTitle("Kunden-Dashboard");
        setSize(900, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel willkommenLabel = new JLabel(
            "Willkommen, " + name + " (ID: " + kundenId + ")",
            SwingConstants.CENTER
        );
        willkommenLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(willkommenLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));

        JButton neueBeschwerdeBtn = new JButton("Neue Beschwerde erstellen");
        neueBeschwerdeBtn.addActionListener(e -> neueBeschwerdeErstellen());

        JButton aktualisiereBtn = new JButton("Meine Tickets aktualisieren");
        aktualisiereBtn.addActionListener(e -> ladeTickets());

        JButton antwortenEinsehenBtn = new JButton("Antworten zu Tickets einsehen");
        antwortenEinsehenBtn.addActionListener(e -> antwortenAnzeigen());

        buttonPanel.add(neueBeschwerdeBtn);
        buttonPanel.add(aktualisiereBtn);
        buttonPanel.add(antwortenEinsehenBtn);
        add(buttonPanel, BorderLayout.CENTER);

        ticketPanel = new JPanel();
        ticketPanel.setLayout(new BoxLayout(ticketPanel, BoxLayout.Y_AXIS));

        scrollPane = new JScrollPane(ticketPanel);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Meine Tickets"));
        add(scrollPane, BorderLayout.SOUTH);

        ladeTickets();
    }

    private JPanel erstelleHeaderZeile() {
        JPanel header = new JPanel(new GridLayout(1, 4, 10, 5));
        header.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        header.setBorder(BorderFactory.createLineBorder(Color.BLACK, 4));

        header.add(erstelleFeld("TicketID", true));
        header.add(erstelleFeld("Status", true));
        header.add(erstelleFeld("Beschreibung", true));
        header.add(erstelleFeld("Eröffnungszeitpunkt", true));

        return header;
    }

    private JPanel erstelleTicketZeile(int ticketId, String status, String beschreibung, String eroeffnungszeitpunkt, String supportantwort) {
        JPanel row = new JPanel(new GridLayout(1, 4, 10, 5));
        row.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        row.add(erstelleFeld(String.valueOf(ticketId), false));
        row.add(erstelleFeld(status, false));
        row.add(erstelleFeld(beschreibung, false));
        row.add(erstelleFeld(eroeffnungszeitpunkt, false));
        return row;
    }

    private JLabel erstelleFeld(String text, boolean fett) {
        JLabel label = new JLabel(text);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        label.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        if (fett == true) {
            label.setFont(label.getFont().deriveFont(Font.BOLD));
        } else {
            label.setFont(label.getFont().deriveFont(Font.PLAIN));
        }

        return label;
    }

    private void ladeTickets() {
        ticketPanel.removeAll();
        ticketPanel.add(erstelleHeaderZeile());

        if (kundenService == null) {
            JLabel fehler = new JLabel("Keine Verbindung zum Server (KundenService).");
            fehler.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            ticketPanel.add(fehler);
            ticketPanel.revalidate();
            ticketPanel.repaint();
            return;
        }

        try {
            // RMI-Call an das Backend!
            List<String[]> tickets = kundenService.getKundenTickets(kundenId);

            if (tickets.isEmpty()) {
                JLabel leer = new JLabel("Keine Tickets vorhanden.");
                leer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                ticketPanel.add(leer);
            } else {
                for (String[] t : tickets) {
                    int ticketId = Integer.parseInt(t[0]);
                    String status = t[1];
                    String beschreibung = t[2];
                    String eroeffnungszeitpunkt = t[3];
                    String supportantwort = "test"; // Wie vorher hardgecodet

                    ticketPanel.add(erstelleTicketZeile(ticketId, status, beschreibung, eroeffnungszeitpunkt, supportantwort));
                }
            }

        } catch (Exception e) {
            JLabel fehler = new JLabel("Fehler beim Laden der Tickets: " + e.getMessage());
            fehler.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            ticketPanel.add(fehler);
            e.printStackTrace();
        }

        ticketPanel.revalidate();
        ticketPanel.repaint();
    }

    private void neueBeschwerdeErstellen() {
        if (beschwerdeService == null) {
            JOptionPane.showMessageDialog(this,
                "Keine Verbindung zum Server.",
                "Fehler", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JTextField k1Field = new JTextField();
        JTextField k2Field = new JTextField();
        JTextField k3Field = new JTextField();
        JTextArea beschreibungArea = new JTextArea(4, 20);

        Object[] felder = {
            "Stichwort 1:", k1Field,
            "Stichwort 2:", k2Field,
            "Stichwort 3:", k3Field,
            "Beschreibung:", new JScrollPane(beschreibungArea)
        };

        int ergebnis = JOptionPane.showConfirmDialog(
            this, felder, "Neue Beschwerde", JOptionPane.OK_CANCEL_OPTION);

        if (ergebnis != JOptionPane.OK_OPTION) return;

        String k1 = k1Field.getText().trim();
        String k2 = k2Field.getText().trim();
        String k3 = k3Field.getText().trim();
        String beschreibung = beschreibungArea.getText().trim();

        try {
            String wissensantwort = beschwerdeService.erfasseBeschwerde(kundenId, k1, k2, k3, beschreibung);

            if (wissensantwort != null && !wissensantwort.isEmpty()) {
                JTextArea antwortAnzeige = new JTextArea(wissensantwort, 4, 30);
                antwortAnzeige.setLineWrap(true);
                antwortAnzeige.setWrapStyleWord(true);
                antwortAnzeige.setEditable(false);
                antwortAnzeige.setBackground(UIManager.getColor("Panel.background"));

                String[] optionen = {"Bitte auswählen", "Ja, ich bin zufrieden", "Nein, ich bin nicht zufrieden"};
                JComboBox<String> zufriedenheitDropdown = new JComboBox<>(optionen);

                Object[] antwortFelder = {
                    "Mögliche Lösung für dein Anliegen:", new JScrollPane(antwortAnzeige),
                    " ",
                    "Hilft dir diese Antwort weiter?", zufriedenheitDropdown
                };

                int antwortErgebnis = JOptionPane.showConfirmDialog(
                    this, antwortFelder,
                    "Wissensantwort gefunden", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);

                if (antwortErgebnis != JOptionPane.OK_OPTION) return;

                String auswahl = (String) zufriedenheitDropdown.getSelectedItem();

                if ("Bitte auswählen".equals(auswahl)) {
                    JOptionPane.showMessageDialog(this,
                        "Bitte wähle aus, ob du mit der Antwort zufrieden bist.",
                        "Hinweis", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if ("Ja, ich bin zufrieden".equals(auswahl)) {
                    JOptionPane.showMessageDialog(this,
                        "Schön, dass dir geholfen werden konnte!",
                        "Abgeschlossen", JOptionPane.INFORMATION_MESSAGE);
                        zufriedenheitDropdown.setEnabled(false);
                } else {
                    ticketbearbeitungsService.neuesTicket(kundenId, beschreibung);
                    

                        JOptionPane.showMessageDialog(this,
                        "Ein neues Ticket wurde für dich erstellt und zur Bearbeitung weitergeleitet.",
                        "Ticket erstellt", JOptionPane.INFORMATION_MESSAGE);
                        zufriedenheitDropdown.setEnabled(false);
                        
                }

            } else {
                ticketbearbeitungsService.neuesTicket(kundenId, beschreibung);
                
                JOptionPane.showMessageDialog(this,
                    "Keine automatische Lösung gefunden.\nEin Ticket wurde erstellt.",
                    "Ticket erstellt", JOptionPane.INFORMATION_MESSAGE);
            }

            ladeTickets();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Fehler beim Einreichen: " + ex.getMessage(),
                "Fehler", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void antwortenAnzeigen() {
        if (beschwerdeService == null) {
            JOptionPane.showMessageDialog(this,
            "Keine Verbindung zum Server.",
            "Fehler", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFrame antworten = new JFrame("Antworten");
        antworten.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        antworten.setSize(1000, 600);
        antworten.setLocationRelativeTo(this);
        antworten.setLayout(new BorderLayout(10, 10));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        JPanel header = new JPanel(new GridLayout(1, 2, 10, 5));
        header.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        header.add(erstelleFeld("TicketID", true));
        header.add(erstelleFeld("Antwort", true));
        
        try {
            java.util.List<String[]> tickets = kundenService.getKundenTickets(kundenId);

            if (tickets.isEmpty()) {
                JPanel leerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                leerPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
                leerPanel.add(new JLabel("Du hast noch keine Tickets erstellt."));
                contentPanel.add(leerPanel);
            } else {
                for (String[] t : tickets) {
                    int ticketId = Integer.parseInt(t[0]);
                    
                    String antwort = kundenService.getSupportAntwort(ticketId);

                    if (antwort == null || antwort.trim().isEmpty()) {
                        antwort = "Noch keine Antwort vorhanden oder Ticket ist noch in Bearbeitung.";
                    }

                    JPanel inhalt = new JPanel(new GridLayout(1, 2, 10, 5));
                    inhalt.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
                    
                    inhalt.add(erstelleFeld(String.valueOf(ticketId), false));
                    
                    JTextArea antwortArea = new JTextArea(antwort);
                    antwortArea.setWrapStyleWord(true);
                    antwortArea.setLineWrap(true);
                    antwortArea.setOpaque(false);
                    antwortArea.setEditable(false);
                    antwortArea.setFont(new Font("Arial", Font.PLAIN, 12));
                    antwortArea.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

                    JPanel antwortContainer = new JPanel(new BorderLayout());
                    antwortContainer.add(antwortArea, BorderLayout.CENTER);
                    if (!antwort.equals("Noch keine Antwort vorhanden oder Ticket noch in Bearbeitung.")) {
                        JPanel feedbackPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                        feedbackPanel.add(new JLabel("War diese Antwort hilfreich?"));

                        String[] optionen = {"Bitte wählen", "Ja, zufrieden", "Nein, unzufrieden"};
                        JComboBox<String> zufriedenDropdown = new JComboBox<>(optionen);
                        
                        // Action Listener für das Dropdown
                        zufriedenDropdown.addActionListener(e -> {
                            String auswahl = (String) zufriedenDropdown.getSelectedItem();
                            if (!auswahl.equals("Bitte wählen")) {
                                boolean isZufrieden = auswahl.equals("Ja, zufrieden");
                                
                                try {
                                    // 1. RMI Call an das Backend
                                    ticketbearbeitungsService.behandleKundenzufriedenheit(isZufrieden, ticketId);
                                    
                                    // 2. Dropdown sofort locken, damit der User nicht spammen kann
                                    zufriedenDropdown.setEnabled(false);
                                    
                                    // 3. Dem User Feedback geben
                                    if (isZufrieden) {
                                        JOptionPane.showMessageDialog(antworten, "Vielen Dank! Dein Ticket wurde erfolgreich abgeschlossen.");
                                    } else {
                                        JOptionPane.showMessageDialog(antworten, "Tut uns leid! Das Ticket wurde erneut geöffnet und einem anderen Mitarbeiter zugewiesen.");
                                    }
                                } catch (Exception ex) {
                                    JOptionPane.showMessageDialog(antworten, "Fehler beim Speichern: " + ex.getMessage(), "Fehler", JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        });

                        feedbackPanel.add(zufriedenDropdown);
                        antwortContainer.add(feedbackPanel, BorderLayout.SOUTH);
                    }

                    inhalt.add(antwortContainer); // Container statt nur die Area adden
                    contentPanel.add(inhalt);
                    
                    inhalt.add(antwortArea);
                    contentPanel.add(inhalt);
                }
            }

        } catch (Exception ex) {
             JOptionPane.showMessageDialog(this,
                "Fehler beim Laden der Antworten: " + ex.getMessage(),
                "Fehler", JOptionPane.ERROR_MESSAGE);
             ex.printStackTrace();
        }

        JScrollPane scrollPaneAntworten = new JScrollPane(contentPanel);
        scrollPaneAntworten.setBorder(BorderFactory.createTitledBorder("Antworten zu meinen Tickets"));
        antworten.add(scrollPaneAntworten, BorderLayout.CENTER);

        antworten.setVisible(true);
    }
}