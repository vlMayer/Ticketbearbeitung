package Ticketservice.Server.src;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;

import Ticketservice.Server.config.DBConnector;
import Ticketservice.Server.src.interfaces.ILoginService;

public class LoginService extends UnicastRemoteObject implements ILoginService {
    private static final long serialVersionUID = 1L;

    public LoginService() throws RemoteException {
        
    }

    @Override
    public boolean authentifiziereKunde(int id, String name) throws RemoteException {

        String kundenAbfrage = "SELECT KundenID FROM kunde WHERE KundenID = ? AND Name = ?";
        try (Connection con = DBConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(kundenAbfrage)) {
            
            ps.setInt(1, id);
            ps.setString(2, name);
            
            try (ResultSet res = ps.executeQuery()) {
                if (res.next()) {
                    System.out.println("Erfolgreich authentifiziert.");
                    return true;
                } else {
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RemoteException("Datenbankfehler bei der Überprüfung", e);
        }
    }

    @Override
    public boolean authentifiziereMitarbeiter(int id, String name) throws RemoteException {

        String mitarbeiterAbfrage = "SELECT MitarbeiterID FROM mitarbeiter WHERE MitarbeiterID = ? AND Name = ?";
        try (Connection con = DBConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(mitarbeiterAbfrage)) {
            
            ps.setInt(1, id);
            ps.setString(2, name);
            
            try (ResultSet res = ps.executeQuery()) {
                if (res.next()) {
                    System.out.println("Erfolgreich authentifiziert.");
                    return true;
                } else {
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RemoteException("Datenbankfehler bei der Überprüfung", e);
        }
    }
}