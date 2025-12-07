// src/main/java/shared/Wall.java
package shared;
import java.io.Serializable;

public class Wall implements Serializable {
    private static final long serialVersionUID = 1L;
    public double x, y, width, height;

    public Wall(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    // Fonction d'aide simple pour la détection de collision (AABB)
    public boolean intersects(double cx, double cy, double radius) {
        // Trouvez le point le plus proche sur le mur par rapport au centre du cercle (tank/bullet)
        double closestX = Math.max(x, Math.min(cx, x + width));
        double closestY = Math.max(y, Math.min(cy, y + height));

        // Calculez la distance entre le point le plus proche et le centre du cercle
        double distanceX = cx - closestX;
        double distanceY = cy - closestY;

        // Si la distance est inférieure au rayon, il y a collision
        return (distanceX * distanceX + distanceY * distanceY) < (radius * radius);
    }
}