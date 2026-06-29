package Ticketservice.Client;

import Ticketservice.Server.src.interfaces.ITicketbearbeitungsService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

public class MitarbeiterGUI extends JFrame {
    private int mitarbeiterId;
    private ITicketbearbeitungsService ticketService;
    private DefaultTableModel tableModel;
    private JTable ticketTable;

    public MitarbeiterGUI(int mitarbeiterId, String name) {
        this.mitarbeiterId = mitarbeiterId;
        
        // Connect to RMI Server
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            ticketService = (ITicketbearbeitungsService) registry.lookup("TicketbearbeitungsService");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "RMI Server connection failed!", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        setTitle("Mitarbeiter-Dashboard");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Header
        JLabel willkommenLabel = new JLabel("Angemeldet als Mitarbeiter: " + name + " (ID: " + mitarbeiterId + ")", SwingConstants.CENTER);
        willkommenLabel.setFont(new Font("Arial", Font.BOLD, 16));
        willkommenLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(willkommenLabel, BorderLayout.NORTH);

        // Ticket Table
        String[] columnNames = {"Ticket ID", "Beschreibung", "Erstellt am"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Prevent manual editing in the table
            }
        };
        ticketTable = new JTable(tableModel);
        add(new JScrollPane(ticketTable), BorderLayout.CENTER);

        // Buttons Panel
        JPanel buttonPanel = new JPanel();
        
        JButton refreshBtn = new JButton("Meine Tickets aktualisieren");
        refreshBtn.addActionListener(e -> loadTickets());
        
        JButton openTicketBtn = new JButton("Ticket bearbeiten");
        openTicketBtn.addActionListener(e -> openSelectedTicket());

        buttonPanel.add(refreshBtn);
        buttonPanel.add(openTicketBtn);
        add(buttonPanel, BorderLayout.SOUTH);

        // Initial load
        loadTickets();
    }

    private void loadTickets() {
        if (ticketService == null) return;
        
        tableModel.setRowCount(0); // Clear current table
        try {
            List<String[]> tickets = ticketService.getMitarbeiterTickets(mitarbeiterId);
            for (String[] ticket : tickets) {
                tableModel.addRow(ticket);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Fehler beim Laden der Tickets.", "Fehler", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void openSelectedTicket() {
        int selectedRow = ticketTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Bitte wählen Sie zuerst ein Ticket aus der Liste aus.", "Hinweis", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get Ticket Details from the selected row
        int ticketId = Integer.parseInt((String) tableModel.getValueAt(selectedRow, 0));
        String beschreibung = (String) tableModel.getValueAt(selectedRow, 1);

        // Open Dialog
        JDialog dialog = new JDialog(this, "Ticket #" + ticketId + " bearbeiten", true);
        dialog.setSize(500, 400);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setLocationRelativeTo(this);

        // Info Panel
        JTextArea infoArea = new JTextArea("Kundenbeschreibung:\n" + beschreibung);
        infoArea.setEditable(false);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setBackground(new Color(240, 240, 240));
        dialog.add(new JScrollPane(infoArea), BorderLayout.NORTH);

        // Answer Panel
        JPanel answerPanel = new JPanel(new BorderLayout());
        answerPanel.add(new JLabel("Support-Antwort:"), BorderLayout.NORTH);
        JTextArea answerArea = new JTextArea();
        answerArea.setLineWrap(true);
        answerArea.setWrapStyleWord(true);
        answerPanel.add(new JScrollPane(answerArea), BorderLayout.CENTER);
        dialog.add(answerPanel, BorderLayout.CENTER);

        // Save Button
        JButton saveBtn = new JButton("Antwort speichern");
        saveBtn.addActionListener(e -> {
            String antwort = answerArea.getText().trim();
            if (antwort.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Die Antwort darf nicht leer sein.", "Fehler", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                // Calls server to save answer and set status to 2 ("in Überprüfung")
                ticketService.beendeBearbeitung(ticketId, mitarbeiterId, antwort);
                JOptionPane.showMessageDialog(dialog, "Antwort erfolgreich übermittelt. Ticket geht in Überprüfung.");
                dialog.dispose();
                loadTickets(); // Refresh the list so it disappears
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Fehler beim Speichern der Antwort.", "Fehler", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(saveBtn);
        dialog.add(bottomPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
}