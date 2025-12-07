package client;

import rmi.IGameClient;
import shared.GameState;
import ui.GameUI;
import javafx.application.Platform;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
public class GameClientImpl extends UnicastRemoteObject implements IGameClient{
    private static final long serialVersionUID = 1L;

    private GameUI ui;
    private int playerId = 0;

    public GameClientImpl(GameUI ui) throws RemoteException {
        super();
        this.ui = ui;
    }

    public void setPlayerId(int id) {
        this.playerId = id;
    }

    @Override
    public void updateGame(GameState state) throws RemoteException {
        Platform.runLater(() -> ui.render(state));
    }

    // >>> MÉTHODE À IMPLÉMENTER/VÉRIFIER <<<
    @Override
    public void displayGameOver(String message) throws RemoteException {
        // 1. Affichage dans la console (pour le débogage/trace)
        System.out.println("----------------------------------------------");
        System.out.println("********** GAME OVER! " + message + " **********");
        System.out.println("----------------------------------------------");

        // 2. Affichage graphique (via JavaFX Thread)
        Platform.runLater(() -> ui.showNotification("DÉFAITE", message)); // Assurez-vous que GameUI a une méthode showNotification
    }
}
