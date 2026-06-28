package Ticketservice.Server.src;

import java.rmi.registry.Registry;

public class Main {
    public static void main(String[] args) {
        try {
            Registry reg = java.rmi.registry.LocateRegistry.createRegistry(1099);
            System.out.println("Server erfolgreich gestartet");

            LoginService loginService = new LoginService();
            reg.bind("LoginService", loginService);

            BeschwerdeService beschwerdeService = new BeschwerdeService();
            reg.bind("BeschwerdeService", beschwerdeService);

            KundenService kundenService = new KundenService();
            reg.bind("KundenService", kundenService);

            TicketbearbeitungsService ticketbearbeitungsService = new TicketbearbeitungsService();
            reg.bind("TicketbearbeitungsService", ticketbearbeitungsService);

        } catch (Exception e) {
            System.err.println("Fehler beim Starten des RMI-Servers:");
            e.printStackTrace();
        }
    }
}
