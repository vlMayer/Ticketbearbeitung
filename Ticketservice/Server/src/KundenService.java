package Ticketservice.Server.src;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import Ticketservice.Server.config.DBConnector;
import Ticketservice.Server.src.interfaces.IKundenService;

public class KundenService implements IKundenService, Serializable{
    
    @Override
    public String getSupportAntwort(int tID) {
        try{
        String abfrage = "SELECT ticket.TicketID, ticketbearbeitung.TicketID, ticketbearbeitung.BearbeitungsID, supportantwort.BearbeitungsID, supportantwort.Inhalt FROM ticket, ticketbearbeitung, supportantwort WHERE ticket.TicketID = ticketbearbeitung.TicketID AND ticketbearbeitung.BearbeitungsID = supportantwort.BearbeitungsID AND ticket.TicketID = " + tID;
        Connection con = DBConnector.getConnection();
        Statement stat = con.createStatement();
        ResultSet res = stat.executeQuery(abfrage);
        if(res.next()) {
            return res.getString("Inhalt");
        } else {
            return null;
        }
        } catch(SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getStatus(int tID) {
        try{
        String abfrage = "SELECT Status FROM ticket WHERE TicketID = " + tID;
        Connection con = DBConnector.getConnection();
        Statement stat = con.createStatement();
        ResultSet res = stat.executeQuery(abfrage);
        if(res.next()) {
            return res.getString("BeschwerdeID");
        } else {
            return null;
        }
        } catch(SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void istZufrieden(int tID, boolean z) throws RemoteException{
        if(z == true) {
            Ticket.setStatus(3, tID);
        } else {
            TicketbearbeitungsService t = new TicketbearbeitungsService();
            t.starteBearbeitung(tID, 1);
        }
    }

    @Override
    public List<String[]> getKundenTickets(int kundenId) throws RemoteException {
        List<String[]> tickets = new ArrayList<>();
        String sql = "SELECT ticket.TicketID, ticket.Status, beschwerde.Beschreibung, ticket.Eröffnungszeitpunkt FROM ticket, beschwerde WHERE ticket.BeschwerdeID = beschwerde.BeschwerdeID AND beschwerde.KundenID = ? ORDER BY ticket.TicketID DESC";

        try (Connection con = DBConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, kundenId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String[] ticketData = new String[4];
                    ticketData[0] = String.valueOf(rs.getInt("TicketID"));
                    ticketData[1] = rs.getString("Status");
                    ticketData[2] = rs.getString("Beschreibung");
                    ticketData[3] = String.valueOf(rs.getObject("Eröffnungszeitpunkt"));
                    tickets.add(ticketData);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RemoteException("Fehler beim Laden der Tickets aus der Datenbank", e);
        }
        return tickets;
    }
}
