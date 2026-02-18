import java.awt.*;
import java.awt.geom.*;

public class Bird {
    private double x, y;
    private double velocity;
    private static final double GRAVITY = 0.5;
    private static final double JUMP_FORCE = -8.5;
    private static final int SIZE = 30;
    private double rotation;

    public Bird(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.velocity = 0;
        this.rotation = 0;
    }

    public void update() {
        velocity += GRAVITY;
        y += velocity;

        // Rotation based on velocity
        rotation = Math.min(Math.max(velocity * 3, -30), 70);
    }

    public void jump() {
        velocity = JUMP_FORCE;
    }

    public void draw(Graphics2D g2d) {
        AffineTransform old = g2d.getTransform();
        g2d.translate(x + SIZE / 2.0, y + SIZE / 2.0);
        g2d.rotate(Math.toRadians(rotation));

        // Body
        g2d.setColor(new Color(255, 200, 0));
        g2d.fillOval(-SIZE / 2, -SIZE / 2, SIZE, SIZE);

        // Body outline
        g2d.setColor(new Color(200, 150, 0));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(-SIZE / 2, -SIZE / 2, SIZE, SIZE);

        // Wing
        g2d.setColor(new Color(255, 230, 100));
        g2d.fillOval(-SIZE / 2 - 2, -4, 16, 12);

        // Eye (white)
        g2d.setColor(Color.WHITE);
        g2d.fillOval(2, -10, 12, 12);

        // Eye (pupil)
        g2d.setColor(Color.BLACK);
        g2d.fillOval(7, -8, 6, 6);

        // Beak
        g2d.setColor(new Color(255, 100, 0));
        int[] beakX = {SIZE / 2 - 4, SIZE / 2 + 10, SIZE / 2 - 4};
        int[] beakY = {-3, 2, 7};
        g2d.fillPolygon(beakX, beakY, 3);

        g2d.setTransform(old);
    }

    public Rectangle getBounds() {
        return new Rectangle((int) x + 4, (int) y + 4, SIZE - 8, SIZE - 8);
    }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    public double getX() { return x; }
    public int getSize() { return SIZE; }
    public void resetVelocity() { velocity = 0; }
}
