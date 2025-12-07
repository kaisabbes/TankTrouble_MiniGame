package shared;

import java.io.Serializable;

public class Explosion implements Serializable {
    private static final long serialVersionUID = 1L;

    // Position de l'explosion
    public double x;
    public double y;

    // Gestion du temps pour savoir quand l'explosion disparaît
    public long startTime;
    public static final long DURATION = 500; // Durée en millisecondes (0.5 secondes)

    public Explosion(double x, double y) {
        this.x = x;
        this.y = y;
        this.startTime = System.currentTimeMillis();
    }

    // Méthode utilitaire pour vérifier si l'explosion est terminée
    public boolean isFinished() {
        return System.currentTimeMillis() - startTime >= DURATION;
    }
}