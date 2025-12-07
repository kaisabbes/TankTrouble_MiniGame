// src/main/java/server/MapGenerator.java
package server;

import shared.Wall;
import shared.GameConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MapGenerator {

    private static final Random RANDOM = new Random();

    // Méthode principale qui sélectionne une carte aléatoire
    public static List<Wall> generateRandomMap() {
        int mapIndex = RANDOM.nextInt(3); // Choisit entre 0, 1 et 2

        switch (mapIndex) {
            case 0:
                return createMapCentralCross();
            case 1:
                return createMapFourCorners();
            case 2:
                return createMapMaze();
            default:
                return createMapCentralCross(); // Fallback
        }
    }

    // --- Définitions des cartes ---

    // Carte 1: La carte par défaut (Croix centrale)
    private static List<Wall> createMapCentralCross() {
        List<Wall> walls = new ArrayList<>();
        int w = GameConstants.SCREEN_WIDTH;
        int h = GameConstants.SCREEN_HEIGHT;

        // Mur central vertical
        walls.add(new Wall(w / 2 - 10, 50, 20, h - 100));

        // Murs horizontaux aux quarts
        walls.add(new Wall(50, h / 4 - 10, 200, 20));
        walls.add(new Wall(w - 250, h / 4 - 10, 200, 20));

        walls.add(new Wall(50, h * 3 / 4 - 10, 200, 20));
        walls.add(new Wall(w - 250, h * 3 / 4 - 10, 200, 20));

        return walls;
    }

    // Carte 2: Quatre blocs centraux, laissant de l'espace dans les coins
    private static List<Wall> createMapFourCorners() {
        List<Wall> walls = new ArrayList<>();
        int w = GameConstants.SCREEN_WIDTH;
        int h = GameConstants.SCREEN_HEIGHT;
        int size = 100;
        int thick = 15;
        int gap = 50;

        // Bloc supérieur gauche
        walls.add(new Wall(w/2 - size - gap, h/2 - size - gap, size, thick));
        walls.add(new Wall(w/2 - size - gap, h/2 - size - gap, thick, size));

        // Bloc supérieur droit
        walls.add(new Wall(w/2 + gap, h/2 - size - gap, size, thick));
        walls.add(new Wall(w/2 + size + gap - thick, h/2 - size - gap, thick, size));

        // Bloc inférieur gauche
        walls.add(new Wall(w/2 - size - gap, h/2 + gap, size, thick));
        walls.add(new Wall(w/2 - size - gap, h/2 + gap + thick, thick, size));

        // Bloc inférieur droit
        walls.add(new Wall(w/2 + gap, h/2 + gap, size, thick));
        walls.add(new Wall(w/2 + size + gap - thick, h/2 + gap + thick, thick, size));

        return walls;
    }

    // Carte 3: Un simple couloir en spirale/serpent (très basique)
    private static List<Wall> createMapMaze() {
        List<Wall> walls = new ArrayList<>();
        int w = GameConstants.SCREEN_WIDTH;
        int h = GameConstants.SCREEN_HEIGHT;
        int thick = 20;

        // Mur horizontal 1
        walls.add(new Wall(w/4, h/4, w/2, thick));

        // Mur vertical 1
        walls.add(new Wall(w/4, h/4, thick, h/2));

        // Mur horizontal 2
        walls.add(new Wall(w/4, h*3/4 - thick, w/2, thick));

        // Mur vertical 2 (partiel)
        walls.add(new Wall(w*3/4 - thick, h/2, thick, h/4));

        return walls;
    }

    // N'hésitez pas à ajouter plus de cartes ici (createMapXXX())
}