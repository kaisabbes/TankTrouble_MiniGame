package client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import rmi.IGameClient;
import shared.GameConstants;
import rmi.IGameServer;
import ui.GameUI;
import javafx.scene.image.Image;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Optional;

public class ClientMain extends Application {
    private static final String RESOURCE_PATH = "/images/";

    // Déclaration des assets
    public static Image TANK_IMAGE_GREEN;
    public static Image TANK_IMAGE_BLUE;
    public static Image TANK_IMAGE_DARK;
    public static Image TANK_IMAGE_SAND;
    public static Image BULLET_IMAGE;
    public static Image WALL_IMAGE;
    public static Image BACKGROUND_IMAGE;
    public static Image EXPLOSION_IMAGE;

    @Override
    public void start(Stage primaryStage) {
        try {
            // ============================================================
            // 1. CONFIGURATION RÉSEAU (Pour jouer avec un ami)
            // ============================================================
            TextInputDialog dialog = new TextInputDialog("localhost");
            dialog.setTitle("Connexion");
            dialog.setHeaderText("Configuration Réseau");
            dialog.setContentText("Entrez l'IP du serveur (laissez localhost si vous êtes l'hôte):");

            Optional<String> result = dialog.showAndWait();
            String serverIP = result.orElse("localhost");

            // Connexion au registre RMI distant ou local
            Registry registry = LocateRegistry.getRegistry(serverIP, GameConstants.SERVER_PORT);
            IGameServer server = (IGameServer) registry.lookup(GameConstants.SERVER_NAME);

            // ============================================================
            // 2. CONFIGURATION UI
            // ============================================================
            Canvas canvas = new Canvas(GameConstants.SCREEN_WIDTH, GameConstants.SCREEN_HEIGHT);
            StackPane root = new StackPane(canvas);
            Scene scene = new Scene(root);

            // Chargement des images
            try {
                TANK_IMAGE_GREEN = new Image(getClass().getResourceAsStream(RESOURCE_PATH + "tank_green.png"));
                TANK_IMAGE_BLUE = new Image(getClass().getResourceAsStream(RESOURCE_PATH + "tank_blue.png"));
                TANK_IMAGE_DARK = new Image(getClass().getResourceAsStream(RESOURCE_PATH + "tank_dark.png"));
                TANK_IMAGE_SAND = new Image(getClass().getResourceAsStream(RESOURCE_PATH + "tank_sand.png"));
                BULLET_IMAGE = new Image(getClass().getResourceAsStream(RESOURCE_PATH + "bulletDark1.png"));
                WALL_IMAGE = new Image(getClass().getResourceAsStream(RESOURCE_PATH + "background1.png"));
                BACKGROUND_IMAGE = new Image(getClass().getResourceAsStream(RESOURCE_PATH + "background.png"));
                EXPLOSION_IMAGE = new Image(getClass().getResourceAsStream(RESOURCE_PATH + "explosion3.png"));
            } catch (Exception e) {
                System.err.println("Erreur chargement images : " + e.getMessage());
                throw new RuntimeException("Images manquantes dans src/main/resources/images/");
            }

            // Instanciation de l'UI avec le Stage (Correct !)
            GameUI gameUI = new GameUI(primaryStage, canvas);

            // ============================================================
            // 3. ENREGISTREMENT CLIENT
            // ============================================================
            // Utilisation de GameClient (ou GameClientImpl selon le nom de votre fichier)
            // Si vous avez nommé votre classe GameClientImpl, changez GameClient ci-dessous
            GameClientImpl clientCallback = new GameClientImpl(gameUI);
            int myId = server.joinGame(clientCallback, "Player");

            // Vérification si la partie est pleine
            if (myId == 0) {
                System.out.println("Connexion refusée : Partie pleine.");
                gameUI.showNotification("Erreur", "La partie est pleine (4 joueurs max).");
                primaryStage.close();
                return;
            }

            // Contrôleur
            GameController controller = new GameController(scene, server, myId);

            // Chat
            scene.setOnKeyTyped(e -> {
                if(e.getCharacter().equals("t")) {
                    TextInputDialog chatDialog = new TextInputDialog();
                    chatDialog.setTitle("Chat");
                    chatDialog.setHeaderText("Message :");
                    chatDialog.showAndWait().ifPresent(controller::sendChat);
                }
            });

            primaryStage.setTitle("Tank Trouble - Joueur " + myId);
            primaryStage.setScene(scene);
            primaryStage.setOnCloseRequest(e -> System.exit(0));
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur de connexion. Vérifiez l'IP et le Serveur.");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}