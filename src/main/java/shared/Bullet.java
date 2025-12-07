// src/main/java/shared/Bullet.java
package shared;
import java.io.Serializable;

public class Bullet implements Serializable {
    private static final long serialVersionUID = 1L;
    public int id;
    public int ownerId;
    public double x, y;
    public double dx, dy; // Vecteur de direction
    public int bounces = 0; // Nombre de rebonds

    public Bullet(int id, int ownerId, double x, double y, double angle) {
        this.id = id;
        this.ownerId = ownerId;
        this.x = x;
        this.y = y;
        // Calcul du vecteur basé sur l'angle
        this.dx = Math.cos(Math.toRadians(angle)) * GameConstants.BULLET_SPEED;
        this.dy = Math.sin(Math.toRadians(angle)) * GameConstants.BULLET_SPEED;
    }
}