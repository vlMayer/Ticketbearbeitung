package Ticketservice.Server.src.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IBeschwerdeService extends Remote {
    String erfasseBeschwerde(int kundenID, String k1, String k2, String k3, String beschreibung) throws RemoteException;
    int neueBeschwerde(int kID, String Beschreibung) throws RemoteException;
}
