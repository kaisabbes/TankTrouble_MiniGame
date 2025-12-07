// src/main/java/server/GameServerImpl.java
package server;

import rmi.IGameServer;
import rmi.IGameClient;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class GameServerImpl extends UnicastRemoteObject implements IGameServer {
    private final GameRoom gameRoom;

    public GameServerImpl() throws RemoteException {
        super();
        this.gameRoom = new GameRoom();
    }

    @Override
    public int joinGame(IGameClient client, String playerName) throws RemoteException {
        System.out.println(playerName + " joining...");
        return gameRoom.addPlayer(client);
    }

    @Override
    public void updateInput(int playerId, boolean u, boolean d, boolean l, boolean r) throws RemoteException {
        gameRoom.updateInput(playerId, u, d, l, r);
    }

    @Override
    public void shoot(int playerId) throws RemoteException {
        gameRoom.spawnBullet(playerId);
    }

    @Override
    public void sendChat(String msg) throws RemoteException {
        gameRoom.addChat(msg);
    }
}