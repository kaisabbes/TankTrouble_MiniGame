// src/main/java/shared/GameState.java
package shared;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;
    public List<Tank> tanks = new ArrayList<>();
    public List<Bullet> bullets = new ArrayList<>();
    public List<String> chatMessages = new ArrayList<>();
    public List<Wall> walls = new ArrayList<>();
    public List<Explosion> explosions = new ArrayList<>();
}