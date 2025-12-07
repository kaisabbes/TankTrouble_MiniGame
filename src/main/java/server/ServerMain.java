// src/main/java/server/ServerMain.java
package server;

import shared.GameConstants;
import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerMain {
    public static void main(String[] args) {
        try {
            // 1. Récupération automatique de l'adresse IP locale
            String ipAddress = InetAddress.getLocalHost().getHostAddress();

            System.out.println("VOTRE IP EST : " + ipAddress);
            System.out.println("Donnez cette IP au joueur client !");

            // 2. Configuration essentielle pour le RMI en réseau
            // Cela force le serveur à utiliser la vraie IP et non localhost
            System.setProperty("java.rmi.server.hostname", ipAddress);

            // 3. Démarrage du serveur
            GameServerImpl server = new GameServerImpl();
            Registry registry = LocateRegistry.createRegistry(GameConstants.SERVER_PORT);
            registry.rebind(GameConstants.SERVER_NAME, server);

            System.out.println("Tank Trouble Server running on port " + GameConstants.SERVER_PORT);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}