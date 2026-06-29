package Ticketservice.Server.src.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;


public interface IKundenService extends Remote {
    String getSupportAntwort(int tID) throws RemoteException;
    String getStatus(int tID) throws RemoteException;
    void istZufrieden(int tID, boolean z) throws RemoteException;
    List<String[]> getKundenTickets(int kundenID) throws RemoteException;
}
