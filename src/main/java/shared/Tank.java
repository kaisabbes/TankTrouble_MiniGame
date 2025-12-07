package shared;
import java.io.Serializable;

public class Tank implements Serializable {
    private static final long serialVersionUID = 1L;

    public int id;
    public double x, y;
    public double angle; // angle en degrés
    public boolean up, down, left, right;
    public double health = 100;
    public boolean isAlive = true;
    public String color;
    public long lastShotTime = 1000;

    public Tank(int id, double x, double y, String color) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.angle = 0;
        this.color = color;
    }
}