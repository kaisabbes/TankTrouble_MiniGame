// src/main/java/server/GameRoom.java
package server;

import shared.*;
import rmi.IGameClient;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GameRoom implements Runnable {
    private final Map<Integer, IGameClient> clients = new ConcurrentHashMap<>();
    private final GameState state = new GameState();
    private boolean running = true;
    private int nextId = 1;
    private int bulletIdCounter = 1;
    private static final long SHOOT_COOLDOWN_MS = 1000;
    // Définition des 4 points de spawn dans les coins
    // Pour une meilleure lisibilité, utilisons des marges (ex: 50 pixels du bord)
    private static final double MARGIN = GameConstants.TANK_SIZE * 2;

    // Coin 1: Haut Gauche (TL)
    private static final double TL_X = MARGIN;
    private static final double TL_Y = MARGIN;

    // Coin 2: Bas Droite (BR)
    private static final double BR_X = GameConstants.SCREEN_WIDTH - MARGIN;
    private static final double BR_Y = GameConstants.SCREEN_HEIGHT - MARGIN;

    // Coin 3: Bas Gauche (BL)
    private static final double BL_X = MARGIN;
    private static final double BL_Y = GameConstants.SCREEN_HEIGHT - MARGIN;

    // Coin 4: Haut Droite (TR)
    private static final double TR_X = GameConstants.SCREEN_WIDTH - MARGIN;
    private static final double TR_Y = MARGIN;

    public GameRoom() {
        generateMap();
        new Thread(this).start(); // Démarre la boucle de jeu
    }
    private void generateMap() {
        // Cadre extérieur (déjà géré par les limites 0/WIDTH/HEIGHT, mais ajoutons des murs internes)
        state.walls.addAll(MapGenerator.generateRandomMap());
    }

    public synchronized int addPlayer(IGameClient client) {
        if (state.tanks.size() >= 4) {
            System.out.println("Partie pleine (4 joueurs maximum).");
            return 0;
        }

        int id = nextId++;

        // --- 1. DÉTERMINATION DES COORDONNÉES DE SPAWN ---
        double spawnX, spawnY;

        // Utilisez l'ID du joueur pour assigner un coin.
        // Puisque nextId commence à 1, l'ID des joueurs sera 1, 2, 3, 4.

        switch (id) {
            case 1:
                spawnX = TL_X; // Haut Gauche
                spawnY = TL_Y;
                break;
            case 2:
                spawnX = BR_X; // Bas Droite
                spawnY = BR_Y;
                break;
            case 3:
                spawnX = BL_X; // Bas Gauche
                spawnY = BL_Y;
                break;
            case 4:
                spawnX = TR_X; // Haut Droite
                spawnY = TR_Y;
                break;
            default:
                // Si plus de 4 joueurs se connectent, ils apparaissent aléatoirement ou au premier coin
                spawnX = TL_X;
                spawnY = TL_Y;
                break;
        }


        // --- 2. ATTRIBUTION DU SKIN (Basé sur le code précédent) ---
        String color;
        int skinId = id % 4; // Résultat : 1, 2, 3 ou 0

        switch (skinId) {
            case 1: color = "GREEN"; break;
            case 2: color = "BLUE"; break;
            case 3: color = "DARK"; break;
            case 0:
            default: color = "SAND"; break;
        }

        // --- 3. AJOUT DU TANK ---
        // Utilisation des coordonnées fixes calculées ci-dessus
        state.tanks.add(new Tank(id, spawnX, spawnY, color));
        clients.put(id, client);
        System.out.println("Nouveau joueur " + id + " rejoint la partie au coin (" + spawnX + ", " + spawnY + ")");
        return id;
    }

    public synchronized void updateInput(int pid, boolean u, boolean d, boolean l, boolean r) {
        for (Tank t : state.tanks) {
            if (t.id == pid && t.isAlive) {
                t.up = u; t.down = d; t.left = l; t.right = r;
                break;
            }
        }
    }

    public synchronized void spawnBullet(int pid) {
        Tank owner = state.tanks.stream().filter(t -> t.id == pid).findFirst().orElse(null);
        if (owner != null && owner.isAlive) {
            // Récupère l'heure actuelle du système
            long currentTime = System.currentTimeMillis();
            // Vérifie si le temps écoulé depuis le dernier tir est supérieur ou égal au cooldown
            if (currentTime - owner.lastShotTime >= SHOOT_COOLDOWN_MS) {
                // Autorisation de tirer : Spawn de la balle
                state.bullets.add(new Bullet(bulletIdCounter++, pid, owner.x, owner.y, owner.angle));
                // Mise à jour de l'heure du dernier tir pour commencer le cooldown
                owner.lastShotTime = currentTime;
            }
        }
    }

    public synchronized void addChat(String msg) {
        state.chatMessages.add(msg);
        if(state.chatMessages.size() > 10) state.chatMessages.remove(0);
    }

    @Override
    public void run() {
        while (running) {
            long start = System.currentTimeMillis();
            updatePhysics();
            broadcastState();
            long elapsed = System.currentTimeMillis() - start;
            try {
                Thread.sleep(Math.max(0, 16 - elapsed)); // ~60 FPS
            } catch (InterruptedException e) { e.printStackTrace(); }
        }
    }

    private synchronized void updatePhysics() {
        double tankRadius = GameConstants.TANK_SIZE / 2.0;

        // 1. Déplacer les Tanks
        for (Tank t : state.tanks) {
            if (!t.isAlive) continue;

            // Rotation (inchangée)
            if (t.left) t.angle -= GameConstants.ROTATION_SPEED;
            if (t.right) t.angle += GameConstants.ROTATION_SPEED;

            if (t.up || t.down) {
                double speed = t.up ? GameConstants.TANK_SPEED : -GameConstants.TANK_SPEED;
                double dx = Math.cos(Math.toRadians(t.angle)) * speed;
                double dy = Math.sin(Math.toRadians(t.angle)) * speed;

                double nextX = t.x + dx;
                double nextY = t.y + dy;

                boolean collision = false;

                // 1a. Collision avec les limites de l'écran (inchangée)
                if (nextX - tankRadius < 0 || nextX + tankRadius > GameConstants.SCREEN_WIDTH ||
                        nextY - tankRadius < 0 || nextY + tankRadius > GameConstants.SCREEN_HEIGHT) {
                    collision = true;
                }

                // 1b. Collision avec les murs de la carte (inchangée)
                if (!collision) {
                    for (Wall wall : state.walls) {
                        if (wall.intersects(nextX, nextY, tankRadius)) {
                            collision = true;
                            break;
                        }
                    }
                }

                // *** 1c. NOUVEAU: Collision Tank-Tank ***
                if (!collision) {
                    for (Tank other : state.tanks) {
                        // Ne pas vérifier la collision avec soi-même ou un tank mort
                        if (t.id == other.id || !other.isAlive) {
                            continue;
                        }

                        // Calcul de la distance entre le centre du tank actuel et le centre du tank autre
                        double dist = Math.sqrt(Math.pow(nextX - other.x, 2) + Math.pow(nextY - other.y, 2));

                        // Si la distance est inférieure à la somme des rayons (tankRadius * 2)
                        if (dist < tankRadius * 2) {
                            collision = true;
                            break;
                        }
                    }
                }
                // *** FIN Collision Tank-Tank ***

                // Si pas de collision, on applique le mouvement
                if (!collision) {
                    t.x = nextX;
                    t.y = nextY;
                }
            }
        }
        // 2. Déplacer les balles
        Iterator<Bullet> bit = state.bullets.iterator();
        while (bit.hasNext()) {
            Bullet b = bit.next();
            double nextX = b.x + b.dx;
            double nextY = b.y + b.dy;

            boolean hitWall = false;
            double bulletRadius = GameConstants.BULLET_SIZE / 2.0;

            // *** NOUVEAU: Vérification de sortie de l'écran (Destruction immédiate) ***

            if (nextX - bulletRadius <= 0 || nextX + bulletRadius >= GameConstants.SCREEN_WIDTH ||
                    nextY - bulletRadius <= 0 || nextY + bulletRadius >= GameConstants.SCREEN_HEIGHT) {

                // La balle est sortie de l'arène de jeu. Destruction immédiate.
                bit.remove();
                continue;
            }

            // *** FIN de la vérification de sortie ***


            // 2b. Rebond sur les murs de la carte (La seule source de rebond maintenant)
            for (Wall wall : state.walls) {
                if (wall.intersects(nextX, nextY, bulletRadius)) {

                    if (!hitWall) {
                        // Appliquer la logique de réflexion angulaire (inversion des composantes)
                        double wallCenterX = wall.x + wall.width / 2;
                        double wallCenterY = wall.y + wall.height / 2;

                        // Si le mur est plus large que haut (horizontal), on inverse DY
                        if (wall.width > wall.height) {
                            b.dy = -b.dy; // Réflexion sur l'axe X (mur horizontal)
                        }
                        // Sinon (mur vertical ou carré), on inverse DX
                        else {
                            b.dx = -b.dx; // Réflexion sur l'axe Y (mur vertical)
                        }
                    }

                    hitWall = true;
                    break;
                }
            }

            // --- Gestion du Rebond Unique (Appliqué uniquement si hitWall est true) ---

            if (hitWall) {
                if (b.bounces >= 1) {
                    bit.remove(); // Destruction de la balle après le premier rebond (sur un mur interne)
                    continue;
                }
                b.bounces++; // Enregistrement du premier rebond
            }

            // Mise à jour de la position finale
            b.x = nextX;
            b.y = nextY;
            // 3. Collision Balle <-> Tank
            for (Tank t : state.tanks) {
                if (t.isAlive && t.id != b.ownerId) {
                    double dist = Math.sqrt(Math.pow(t.x - b.x, 2) + Math.pow(t.y - b.y, 2));

                    if (dist < GameConstants.TANK_SIZE / 2 + GameConstants.BULLET_SIZE) {
                        t.health -= 25;
                        if (t.health <= 0) {
                            t.isAlive = false;
                            state.explosions.add(new Explosion(t.x, t.y));
                            IGameClient client = clients.get(t.id);
                            if (client != null) {
                                try {
                                    client.displayGameOver("Vous avez perdu !");
                                } catch (RemoteException re) {
                                    System.err.println("Erreur RMI lors de l'envoi du message de défaite au client " + t.id + ": " + re.getMessage());
                                }
                            }
                        }
                        try { bit.remove(); } catch (Exception e) {}
                        break;
                    }
                }
            }
        }
        state.explosions.removeIf(Explosion::isFinished);
    }

    private void broadcastState() {
        // Copie pour éviter concurrence
        GameState snapshot;
        synchronized (this) { snapshot = state; } // Note: Shallow copy en réalité ici, mais OK pour l'exemple

        clients.forEach((id, client) -> {
            try {
                client.updateGame(snapshot);
            } catch (RemoteException e) {
                // Client déconnecté, on pourrait le retirer ici
            }
        });
    }

}