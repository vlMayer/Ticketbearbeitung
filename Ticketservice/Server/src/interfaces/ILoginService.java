package Ticketservice.Server.src.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ILoginService extends Remote {
    boolean authentifiziereKunde(int id, String name) throws RemoteException;
    boolean authentifiziereMitarbeiter(int id, String name) throws RemoteException;
}