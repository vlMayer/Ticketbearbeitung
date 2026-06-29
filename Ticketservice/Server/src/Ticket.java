package Ticketservice.Server.src;


import java.rmi.RemoteException;
import java.sql.*;
import java.time.LocalDateTime;

import Ticketservice.Server.config.DBConnector;

public class Ticket {
    private int tmp1 = 0;
    private int tmp2 = 0;
    private final static String [] statusmöglichkeiten = {"offen", "in Bearbeitung", "in Überprüfung", "geschlossen"};


    public Ticket(int b) throws RemoteException{
        try {
            String abfrage = "SELECT MAX(TicketID) AS maxId FROM ticket";
            Connection con = DBConnector.getConnection();
            Statement stat = con.createStatement();
            ResultSet res = stat.executeQuery(abfrage);
            if(res.next()){
                tmp1 = res.getInt("maxId") + 1;
            } else {
                tmp1 = 1;
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        
        ticketAbspeichern(tmp1, LocalDateTime.now(), statusmöglichkeiten[0], b);
    }

    public void TicketbearbeitungsService() throws RemoteException {
        try {
            String abfrage = "SELECT BearbeitungsID FROM ticketbearbeitung";
            Connection con = DBConnector.getConnection();
            Statement stat = con.createStatement();
            ResultSet res = stat.executeQuery(abfrage);
            while (res.next()) {
                tmp2 = res.getInt("BearbeitungsID");
                tmp2 += 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        bearbeitungAbspeichern(tmp2, 1, tmp1, LocalDateTime.now(), null, false);
    }

    public static int getBeschwerde(int id) {
        try{
        String abfrage = "SELECT BeschwerdeID FROM ticket WHERE TicketID = " + id;
        Connection con = DBConnector.getConnection();
        Statement stat = con.createStatement();
        ResultSet res = stat.executeQuery(abfrage);
        if(res.next()) {
            return res.getInt("BeschwerdeID");
        } else {
            return 0;
        }
        } catch(SQLException e){
            e.printStackTrace();
            return 0;
        }
    }
    public static LocalDateTime getErstelltAm(int id) {
        try{
        String abfrage = "SELECT Eröffnungszeitpunkt FROM ticket WHERE TicketID = " + id;
        Connection con = DBConnector.getConnection();
        Statement stat = con.createStatement();
        ResultSet res = stat.executeQuery(abfrage);
        if(res.next()) {
            return res.getTimestamp("Eröffnungszeitpunkt").toLocalDateTime();
        } else {
            return null;
        }
        } catch(SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    public static String getStatus(int id) {
        try{
        String abfrage = "SELECT Status FROM ticket WHERE TicketID = " + id;
        Connection con = DBConnector.getConnection();
        Statement stat = con.createStatement();
        ResultSet res = stat.executeQuery(abfrage);
        if(res.next()) {
            return res.getString("Status");
        } else {
            return null;
        }
        } catch(SQLException e){
            e.printStackTrace();
            return null;
        }
    }
    public static void setStatus(int v, int id) {
        try{
        String abfrage = "UPDATE ticket SET Status = ? WHERE TicketID = ? AND TicketID = " + id;
        Connection con = DBConnector.getConnection();
        PreparedStatement ps = con.prepareStatement(abfrage);
        ps.setString(1, statusmöglichkeiten[v]);
        ps.setObject(2, id);
        ps.executeUpdate();
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }


    private void ticketAbspeichern(int id, LocalDateTime erstelltAm, String status, int beschwerdeID) {
        try{
        String abfrage = "INSERT INTO ticket (TicketID, Eröffnungszeitpunkt, Status, BeschwerdeID) VALUES (?, ?, ?, ?)";
        Connection con = DBConnector.getConnection();
        PreparedStatement ps = con.prepareStatement(abfrage);
        ps.setInt(1, id);
        ps.setObject(2, erstelltAm);
        ps.setString(3, status);
        ps.setInt(4, beschwerdeID);
        ps.executeUpdate();
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    private void bearbeitungAbspeichern(int bID, int mID, int tID, LocalDateTime Zuweisungszeitpunkt, LocalDateTime Endzeitpunkt, boolean hatFristverletzung) {
        try {
            String abfrage = "INSERT INTO ticketbearbeitung (BearbeitungsID, MitarbeiterID, TicketID, Zuweisungszeitpunkt, Endzeitpunkt, HatFristverletzung) VALUES (?, ?, ?, ?, ?, ?)";
            Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(abfrage);
            ps.setInt(1, bID);
            ps.setObject(2, mID);
            ps.setInt(3, tID);
            ps.setObject(4, Zuweisungszeitpunkt);
            ps.setObject(5, Endzeitpunkt);
            ps.setBoolean(6, hatFristverletzung);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
