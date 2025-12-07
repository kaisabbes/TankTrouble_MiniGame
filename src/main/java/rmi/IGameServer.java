// src/main/java/rmi/IGameServer.java
package rmi;
import java.rmi.Remote;
import java.rmi.RemoteException;

// Interface principale: Client -> Serveur
public interface IGameServer extends Remote {
    int joinGame(IGameClient client, String playerName) throws RemoteException;
    void updateInput(int playerId, boolean up, boolean down, boolean left, boolean right) throws RemoteException;
    void shoot(int playerId) throws RemoteException;
    void sendChat(String msg) throws RemoteException;
}