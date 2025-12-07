// src/main/java/ui/GameUI.java
package ui;

import javafx.stage.Stage;
import shared.*;
import client.ClientMain; // Import pour accéder aux images statiques
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.image.Image;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class GameUI {
    private Canvas canvas;
    private GraphicsContext gc;
    private Stage primaryStage;

    public GameUI(Stage stage, Canvas canvas) {
        this.primaryStage = stage;
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
    }

    public void render(GameState state) {
        // 1. Fond de carte personnalisé
        // Utilise l'image statique chargée dans ClientMain
        gc.drawImage(ClientMain.BACKGROUND_IMAGE, 0, 0, canvas.getWidth(), canvas.getHeight());

        // 2. Murs personnalisés
        for (Wall wall : state.walls) {
            // Dessine l'image de texture des murs sur la zone définie par le mur
            gc.drawImage(ClientMain.WALL_IMAGE, wall.x, wall.y, wall.width, wall.height);
        }

        // 3. Dessiner les Tanks
        for (Tank t : state.tanks) {
            if (!t.isAlive) continue;
            // Appel simple de drawTank (l'image est sélectionnée à l'intérieur)
            drawTank(t);

            // Barre de vie (ne tourne pas)
            gc.setFill(Color.RED);
            gc.fillRect(t.x - 15, t.y - 25, 30, 5);
            gc.setFill(Color.LIME); // Vert plus visible
            gc.fillRect(t.x - 15, t.y - 25, 30 * (t.health / 100.0), 5);
        }

        // 4. Dessiner les Balles
        for (Bullet b : state.bullets) {
            // Dessine l'image de la balle
            gc.drawImage(ClientMain.BULLET_IMAGE,
                    b.x - GameConstants.BULLET_SIZE / 2,
                    b.y - GameConstants.BULLET_SIZE / 2,
                    GameConstants.BULLET_SIZE,
                    GameConstants.BULLET_SIZE);
        }

        // 5. Dessiner les Explosions (Nouveau)
        double explosionSize = GameConstants.TANK_SIZE * 1.5;
        for (Explosion exp : state.explosions) {
            // Dessiner l'image d'explosion centrée sur la position
            // L'image de l'explosion disparaîtra automatiquement car le serveur retire l'Explosion de la liste.
            gc.drawImage(
                    ClientMain.EXPLOSION_IMAGE,
                    exp.x - explosionSize / 2,
                    exp.y - explosionSize / 2,
                    explosionSize,
                    explosionSize
            );
        }


        // 6. Draw HUD / Chat
        gc.setFill(Color.BLACK);
        gc.setFont(new Font("Arial", 12));
        int y = 20;
        for(String msg : state.chatMessages) {
            gc.fillText(msg, 10, y);
            y += 15;
        }
    }
    public void showNotification(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null); // Pas de header, juste le contenu
        alert.setContentText(message);
        alert.showAndWait(); // Affiche la fenêtre et attend la confirmation de l'utilisateur
    }
    // Mise à jour pour utiliser les images et gérer les 4 types de tanks
    private void drawTank(Tank t) {
        gc.save();

        gc.translate(t.x, t.y);
        gc.rotate(t.angle);

        // --- CORRECTION DE LA SÉLECTION D'IMAGE ---
        Image tankImage;

        // On utilise un switch sur le String, c'est plus propre et moins sujet aux erreurs
        switch (t.color) {
            case "GREEN":
                tankImage = ClientMain.TANK_IMAGE_GREEN;
                break;
            case "BLUE":
                tankImage = ClientMain.TANK_IMAGE_BLUE;
                break;
            case "DARK":
                tankImage = ClientMain.TANK_IMAGE_DARK;
                break;
            case "SAND":
            default:
                tankImage = ClientMain.TANK_IMAGE_SAND;
                break;
        }

        // Sécurité : Si l'image est null (mauvais chargement), on met un carré rouge pour ne pas planter
        if (tankImage != null) {
            double halfSize = GameConstants.TANK_SIZE / 2.0;
            gc.drawImage(tankImage, -halfSize, -halfSize, GameConstants.TANK_SIZE, GameConstants.TANK_SIZE);
        } else {
            gc.setFill(Color.RED);
            gc.fillRect(-15, -15, 30, 30);
        }

        gc.restore();
    }
}