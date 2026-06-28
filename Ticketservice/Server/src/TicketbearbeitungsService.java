package Ticketservice.Server.src;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.Duration;
import java.time.LocalDate;

import Ticketservice.Server.config.DBConnector;
import Ticketservice.Server.src.interfaces.ITicketbearbeitungsService;

public class TicketbearbeitungsService extends UnicastRemoteObject implements ITicketbearbeitungsService {
    private int tmp;
    private int tmp3;
    private int bearID;
    public TicketbearbeitungsService() throws RemoteException {
        
    }

    @Override
    public void starteBearbeitung(int tID, int mitarbeiterID) throws RemoteException {
        int neueBearbeitungsID = 1;
        try{
        String abfrage = "SELECT MAX(BearbeitungsID) as maxId FROM ticketbearbeitung";
        Connection con = DBConnector.getConnection();
        Statement stat = con.createStatement();
        ResultSet res = stat.executeQuery(abfrage);
        if(res.next()){
            neueBearbeitungsID = res.getInt("maxId");
        }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        try {
            String abfrage1 = "INSERT INTO ticketbearbeitung (BearbeitungsID, MitarbeiterID, TicketID, Zuweisungszeitpunkt, Bearbeitungsstart, Endzeitpunkt, HatFristverletzung) VALUES (?, ?, ?, ?, ?, ?, ?)";
            Connection con1 = DBConnector.getConnection();
            PreparedStatement ps1 = con1.prepareStatement(abfrage1);
            ps1.setInt(1, neueBearbeitungsID);
            ps1.setObject(2, mitarbeiterID);
            ps1.setInt(3, tID);
            ps1.setObject(4, LocalDateTime.now());
            ps1.setObject(5, LocalDateTime.now());
            ps1.setObject(6, null);
            ps1.setInt(7, 0);
            ps1.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Ticket.setStatus(1, tID);
    }

    @Override
    public void FristVermerken(int tID, int mID) throws RemoteException {
        try{
        String abfrage1 = "UPDATE ticketbearbeitung SET HatFristverletzung = 1 WHERE TicketID = ? AND MitarbeiterID = ?";
        Connection con1 = DBConnector.getConnection();
        PreparedStatement stat1 = con1.prepareStatement(abfrage1);
        stat1.setInt(1, tID);
        stat1.setInt(2, mID);
        } catch(SQLException e){
            e.printStackTrace();
        }
        try{
        String abfrage2 = "UPDATE mitarbeiter SET Fristverletzungen = Fristverletzungen + 1 WHERE MitarbeiterID = ?";
        Connection con2 = DBConnector.getConnection();
        PreparedStatement stat2 = con2.prepareStatement(abfrage2);
        stat2.setInt(1, mID);
        } catch(SQLException e){
            e.printStackTrace();
        }
    }

    @Override
    public void beendeBearbeitung(int tID, int mID, String antwortInhalt) throws RemoteException {
        try{
        String abfrage1 = "UPDATE ticketbearbeitung SET EndZeitpunkt = ? WHERE TicketID = ? AND MitarbeiterID = ?";
        Connection con1 = DBConnector.getConnection();
        PreparedStatement stat1 = con1.prepareStatement(abfrage1);
        stat1.setObject(1, LocalDateTime.now());
        stat1.setInt(2, tID);
        stat1.setInt(3, mID);
        } catch(SQLException e){
            e.printStackTrace();
        }
        try{
        String abfrage2 = "SELECT Bearbeitungsstart, BearbeitungsID FROM ticketbearbeitung WHERE TicketID = ? AND MitarbeiterID = ?";
        Connection con2 = DBConnector.getConnection();
        PreparedStatement stat2 = con2.prepareStatement(abfrage2);
        stat2.setInt(1, tID);
        stat2.setInt(2, mID);
        ResultSet rs = stat2.executeQuery();
        LocalDateTime tmp = rs.getTimestamp("Bearbeitungsstart").toLocalDateTime();
        bearID = rs.getInt("BearbeitungsID");
        if(Duration.between(tmp, LocalDateTime.now()).toHours() > 72) {
            FristVermerken(tID, mID);
        }
        } catch(SQLException e){
            e.printStackTrace();
        }
        try{
        String abfrage3 = "SELECT AntwortID FROM supportantwort";
        Connection con3 = DBConnector.getConnection();
        Statement stat3 = con3.createStatement();
        ResultSet res3 = stat3.executeQuery(abfrage3);
        while(res3.next()){
            tmp3 = res3.getInt("TicketID");
            tmp3 += 1;
        }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        try {
            String abfrage4 = "INSERT INTO supportantwort (AntwortID, Inhalt, Antwortdatum, KundeZufrieden, BearbeitungsID) VALUES (?, ?, ?, ?, ?)";
            Connection con4 = DBConnector.getConnection();
            PreparedStatement ps4 = con4.prepareStatement(abfrage4);
            ps4.setInt(1, tmp3);
            ps4.setString(2, antwortInhalt);
            ps4.setObject(3, LocalDate.now());
            ps4.setInt(4, 0);
            ps4.setInt(5, bearID);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Ticket.setStatus(2, tID);
    }

    @Override
    public void aktualisiereStatus(int status, int tID) throws RemoteException {
        Ticket.setStatus(status, tID);
    }

    @Override
    public void behandleKundenzufriedenheit(boolean zufrieden, int tID) throws RemoteException {
        if (zufrieden) {
            Ticket.setStatus(3, tID);
        } else {

            int bisherigerMitarbeiter = 1;
             try{
            String abfrage1 = "SELECT MitarbeiterID FROM ticketbearbeitung WHERE TicketID = ? ORDER BY Zuweisungszeitpunkt DESC LIMIT 1";
            Connection con1 = DBConnector.getConnection();
            PreparedStatement ps1 = con1.prepareStatement(abfrage1);
            ps1.setInt(1, tID);
            ResultSet res1 = ps1.executeQuery();
            if (res1.next()) {
                bisherigerMitarbeiter = res1.getInt("MitarbeiterID");
            }
        } catch(SQLException e){
            e.printStackTrace();
        }  
        int neuerMitarbeiter = 1; 
        try {
            String abfrage2 = "SELECT MitarbeiterID FROM mitarbeiter WHERE MitarbeiterID != ? LIMIT 1";
                Connection con2 = DBConnector.getConnection();
                PreparedStatement ps2 = con2.prepareStatement(abfrage2);
                ps2.setInt(1, bisherigerMitarbeiter);
                ResultSet rs2 = ps2.executeQuery();
                
                if (rs2.next()) {
                    neuerMitarbeiter = rs2.getInt("MitarbeiterID");
                } else {
                    neuerMitarbeiter = bisherigerMitarbeiter + 1;//NotfallLösung falls es einen Fehler gab
                }   
        } catch (SQLException e) {
            neuerMitarbeiter = bisherigerMitarbeiter == 1 ? 2 : 1;
        }
        starteBearbeitung(tID, neuerMitarbeiter);
    }
}
    @Override
    public void neuesTicket(int kID, String b) throws RemoteException{
        BeschwerdeService be = new BeschwerdeService();
        new Ticket(be.neueBeschwerde(kID, b));
        int ticketID = 1;
        try {
        String sql = "SELECT MAX(TicketID) as maxId FROM ticket";
        Connection con = DBConnector.getConnection();
        Statement stat = con.createStatement();
        ResultSet res = stat.executeQuery(sql);
        if (res.next()) {
            ticketID = res.getInt("maxId");
        }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        starteBearbeitung(ticketID, 1);
    }
}