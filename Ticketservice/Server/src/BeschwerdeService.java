package Ticketservice.Server.src;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.time.LocalDate;

import Ticketservice.Server.config.DBConnector;
import Ticketservice.Server.src.interfaces.IBeschwerdeService;

public class BeschwerdeService extends UnicastRemoteObject implements IBeschwerdeService {
    private int bID;

    public BeschwerdeService() throws RemoteException {

    }

    @Override
    public String erfasseBeschwerde(int kundenID, String k1, String k2, String k3, String beschreibung) throws RemoteException {
        return klassifiziereBeschwerde(k1, k2, k3);
        
    }

    public String klassifiziereBeschwerde(String k1, String k2, String k3) {
        try {
            String abfrage = "SELECT wissensantwort.Inhalt, stichwort.Begriff FROM wissensantwort, hat, stichwort WHERE wissensantwort.WissensantwortID = hat.WissensantwortID AND hat.StichwortID = stichwort.StichwortID AND stichwort.Begriff IN (?, ?, ?)";
            Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(abfrage);
            ps.setString(1, k1);
            ps.setString(2, k2);
            ps.setString(3, k3);
            ResultSet res = ps.executeQuery();
            if (res.next()) {
            return res.getString("Inhalt");
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int neueBeschwerde(int kID, String b) throws RemoteException{
        int tmp = 0;
        try {
            String abfrage = "SELECT MAX(BeschwerdeID) AS maxId FROM beschwerde";
            Connection con = DBConnector.getConnection();
            Statement stat = con.createStatement();
            ResultSet res = stat.executeQuery(abfrage);
            if(res.next()) {
                tmp = res.getInt("maxId");
            }
            bID = tmp + 1;
        } catch(SQLException e) {
            e.printStackTrace();
        }
        
        try {
            String abfrage1 = "INSERT INTO beschwerde (BeschwerdeID, Beschreibung, Datum, KundenID) VALUES (?, ?, ?, ?)";
            Connection con1 = DBConnector.getConnection();
            PreparedStatement stat1 = con1.prepareStatement(abfrage1);
            stat1.setInt(1, bID);
            stat1.setString(2, b);
            stat1.setObject(3, LocalDate.now());
            stat1.setInt(4, kID);
            stat1.executeUpdate();
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return bID;
    }
}