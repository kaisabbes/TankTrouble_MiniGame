// src/main/java/rmi/IGameClient.java
package rmi;
import shared.GameState;
import java.rmi.Remote;
import java.rmi.RemoteException;

// Callback interface: Serveur -> Client
public interface IGameClient extends Remote {
    void updateGame(GameState state) throws RemoteException;
    void displayGameOver(String message) throws RemoteException;
}