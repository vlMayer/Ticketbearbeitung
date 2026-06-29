package Ticketservice.Server.src.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ITicketbearbeitungsService extends Remote {
    void starteBearbeitung(int tID, int mitarbeiterID) throws RemoteException;
    void FristVermerken(int tID, int mID) throws RemoteException;
    void beendeBearbeitung(int tID, int mID, String antwortInhalt) throws RemoteException;
    void aktualisiereStatus(int status, int tID) throws RemoteException;
    void behandleKundenzufriedenheit(boolean zufrieden, int tID) throws RemoteException;
    void neuesTicket(int kID, String b) throws RemoteException;
    List<String[]> getMitarbeiterTickets(int mID) throws RemoteException;
}
