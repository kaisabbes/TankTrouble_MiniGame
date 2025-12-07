// src/main/java/client/GameController.java
package client;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import rmi.IGameServer;
import java.rmi.RemoteException;

public class GameController {
    private final IGameServer server;
    private final int myId;
    private boolean up, down, left, right;

    public GameController(Scene scene, IGameServer server, int myId) {
        this.server = server;
        this.myId = myId;

        scene.setOnKeyPressed(e -> handleKey(e.getCode(), true));
        scene.setOnKeyReleased(e -> handleKey(e.getCode(), false));
    }

    private void handleKey(KeyCode code, boolean pressed) {
        boolean changed = false;
        switch (code) {
            case W: case UP: up = pressed; changed = true; break;
            case S: case DOWN: down = pressed; changed = true; break;
            case A: case LEFT: left = pressed; changed = true; break;
            case D: case RIGHT: right = pressed; changed = true; break;
            case SPACE:
                if (pressed) shoot();
                break;
        }
        if (changed) sendInput();
    }

    private void shoot() {
        try { server.shoot(myId); } catch (RemoteException e) { e.printStackTrace(); }
    }

    private void sendInput() {
        try {
            server.updateInput(myId, up, down, left, right);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void sendChat(String msg) {
        try { server.sendChat("Player " + myId + ": " + msg); } catch (RemoteException e) { e.printStackTrace(); }
    }
}