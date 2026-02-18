import java.awt.*;

public class Pipe {
    private int x;
    private int gapY;
    private static final int WIDTH = 60;
    private static final int GAP_SIZE = 150;
    private boolean scored;
    private int screenHeight;

    public Pipe(int x, int gapY, int screenHeight) {
        this.x = x;
        this.gapY = gapY;
        this.screenHeight = screenHeight;
        this.scored = false;
    }

    public void update(int speed) {
        x -= speed;
    }

    public void draw(Graphics2D g2d) {
        // Top pipe
        drawPipeSection(g2d, x, 0, WIDTH, gapY);
        // Top pipe cap
        g2d.setColor(new Color(50, 160, 50));
        g2d.fillRect(x - 4, gapY - 25, WIDTH + 8, 25);
        g2d.setColor(new Color(30, 120, 30));
        g2d.drawRect(x - 4, gapY - 25, WIDTH + 8, 25);

        // Bottom pipe
        int bottomY = gapY + GAP_SIZE;
        drawPipeSection(g2d, x, bottomY, WIDTH, screenHeight - bottomY);
        // Bottom pipe cap
        g2d.setColor(new Color(50, 160, 50));
        g2d.fillRect(x - 4, bottomY, WIDTH + 8, 25);
        g2d.setColor(new Color(30, 120, 30));
        g2d.drawRect(x - 4, bottomY, WIDTH + 8, 25);
    }

    private void drawPipeSection(Graphics2D g2d, int px, int py, int w, int h) {
        // Main pipe body
        g2d.setColor(new Color(70, 190, 70));
        g2d.fillRect(px, py, w, h);

        // Highlight
        g2d.setColor(new Color(100, 220, 100));
        g2d.fillRect(px + 5, py, 10, h);

        // Shadow
        g2d.setColor(new Color(40, 140, 40));
        g2d.fillRect(px + w - 8, py, 8, h);

        // Outline
        g2d.setColor(new Color(30, 120, 30));
        g2d.drawRect(px, py, w, h);
    }

    public Rectangle getTopBounds() {
        return new Rectangle(x, 0, WIDTH, gapY);
    }

    public Rectangle getBottomBounds() {
        int bottomY = gapY + GAP_SIZE;
        return new Rectangle(x, bottomY, WIDTH, screenHeight - bottomY);
    }

    public int getX() { return x; }
    public int getWidth() { return WIDTH; }
    public boolean isScored() { return scored; }
    public void setScored(boolean scored) { this.scored = scored; }
    public boolean isOffScreen() { return x + WIDTH < 0; }
}
