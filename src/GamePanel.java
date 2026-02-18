import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener, KeyListener, MouseListener {
    public static final int WIDTH = 400;
    public static final int HEIGHT = 600;

    private enum GameState { MENU, PLAYING, GAME_OVER }

    private GameState state;
    private Bird bird;
    private ArrayList<Pipe> pipes;
    private Timer gameTimer;
    private Random random;
    private int score;
    private int bestScore;
    private int pipeSpeed;
    private int pipeSpawnTimer;
    private int pipeSpawnInterval;
    private int groundOffset;

    // Visual elements
    private int cloudX1, cloudX2, cloudX3;
    private float menuBirdBob;
    private int flashAlpha;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);

        random = new Random();
        bestScore = 0;

        cloudX1 = 50;
        cloudX2 = 200;
        cloudX3 = 340;

        resetGame();

        gameTimer = new Timer(16, this); // ~60 FPS
        gameTimer.start();
    }

    private void resetGame() {
        bird = new Bird(80, HEIGHT / 2 - 15);
        pipes = new ArrayList<>();
        score = 0;
        pipeSpeed = 3;
        pipeSpawnTimer = 0;
        pipeSpawnInterval = 100;
        groundOffset = 0;
        menuBirdBob = 0;
        flashAlpha = 0;
        state = GameState.MENU;
    }

    private void startGame() {
        bird = new Bird(80, HEIGHT / 2 - 15);
        pipes.clear();
        score = 0;
        pipeSpeed = 3;
        pipeSpawnTimer = 0;
        flashAlpha = 0;
        state = GameState.PLAYING;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        update();
        repaint();
    }

    private void update() {
        // Animate clouds always
        cloudX1 -= 0.5;
        cloudX2 -= 0.3;
        cloudX3 -= 0.4;
        if (cloudX1 < -80) cloudX1 = WIDTH + 20;
        if (cloudX2 < -80) cloudX2 = WIDTH + 40;
        if (cloudX3 < -80) cloudX3 = WIDTH + 10;

        if (state == GameState.MENU) {
            menuBirdBob += 0.05f;
            bird.setY(HEIGHT / 2 - 15 + Math.sin(menuBirdBob) * 15);
            groundOffset = (groundOffset + 2) % 24;
            return;
        }

        if (state == GameState.GAME_OVER) {
            if (flashAlpha > 0) flashAlpha -= 15;
            return;
        }

        // Playing state
        bird.update();
        groundOffset = (groundOffset + pipeSpeed) % 24;

        // Spawn pipes
        pipeSpawnTimer++;
        if (pipeSpawnTimer >= pipeSpawnInterval) {
            int minGapY = 80;
            int maxGapY = HEIGHT - 200;
            int gapY = random.nextInt(maxGapY - minGapY) + minGapY;
            pipes.add(new Pipe(WIDTH, gapY, HEIGHT));
            pipeSpawnTimer = 0;
        }

        // Update pipes
        Iterator<Pipe> it = pipes.iterator();
        while (it.hasNext()) {
            Pipe pipe = it.next();
            pipe.update(pipeSpeed);

            // Score
            if (!pipe.isScored() && pipe.getX() + pipe.getWidth() < bird.getX()) {
                pipe.setScored(true);
                score++;
                // Increase difficulty
                if (score % 5 == 0 && pipeSpeed < 6) {
                    pipeSpeed++;
                }
                if (score % 10 == 0 && pipeSpawnInterval > 70) {
                    pipeSpawnInterval -= 5;
                }
            }

            // Remove off-screen pipes
            if (pipe.isOffScreen()) {
                it.remove();
            }
        }

        // Collision detection
        checkCollisions();
    }

    private void checkCollisions() {
        Rectangle birdBounds = bird.getBounds();

        // Ground and ceiling
        if (bird.getY() + bird.getSize() > HEIGHT - 70 || bird.getY() < 0) {
            gameOver();
            return;
        }

        // Pipes
        for (Pipe pipe : pipes) {
            if (birdBounds.intersects(pipe.getTopBounds()) ||
                birdBounds.intersects(pipe.getBottomBounds())) {
                gameOver();
                return;
            }
        }
    }

    private void gameOver() {
        state = GameState.GAME_OVER;
        flashAlpha = 200;
        if (score > bestScore) {
            bestScore = score;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawBackground(g2d);
        drawClouds(g2d);

        // Draw pipes
        for (Pipe pipe : pipes) {
            pipe.draw(g2d);
        }

        drawGround(g2d);

        // Draw bird
        bird.draw(g2d);

        // Draw UI based on state
        switch (state) {
            case MENU:
                drawMenu(g2d);
                break;
            case PLAYING:
                drawScore(g2d);
                break;
            case GAME_OVER:
                drawScore(g2d);
                drawGameOver(g2d);
                break;
        }

        // Flash effect on death
        if (flashAlpha > 0) {
            g2d.setColor(new Color(255, 255, 255, Math.min(flashAlpha, 255)));
            g2d.fillRect(0, 0, WIDTH, HEIGHT);
        }
    }

    private void drawBackground(Graphics2D g2d) {
        // Sky gradient
        GradientPaint sky = new GradientPaint(0, 0, new Color(80, 180, 255),
                                               0, HEIGHT, new Color(140, 220, 255));
        g2d.setPaint(sky);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);
    }

    private void drawClouds(Graphics2D g2d) {
        g2d.setColor(new Color(255, 255, 255, 180));
        drawCloud(g2d, (int) cloudX1, 60);
        drawCloud(g2d, (int) cloudX2, 120);
        drawCloud(g2d, (int) cloudX3, 40);
    }

    private void drawCloud(Graphics2D g2d, int x, int y) {
        g2d.fillOval(x, y, 60, 30);
        g2d.fillOval(x + 15, y - 12, 40, 30);
        g2d.fillOval(x + 30, y, 50, 28);
    }

    private void drawGround(Graphics2D g2d) {
        int groundY = HEIGHT - 70;

        // Dirt
        g2d.setColor(new Color(200, 160, 80));
        g2d.fillRect(0, groundY + 15, WIDTH, HEIGHT - groundY - 15);

        // Grass
        GradientPaint grass = new GradientPaint(0, groundY, new Color(90, 200, 50),
                                                 0, groundY + 15, new Color(70, 170, 40));
        g2d.setPaint(grass);
        g2d.fillRect(0, groundY, WIDTH, 15);

        // Grass detail (moving lines)
        g2d.setColor(new Color(60, 150, 30));
        for (int i = -groundOffset; i < WIDTH + 24; i += 24) {
            g2d.fillRect(i, groundY, 12, 4);
        }

        // Ground line
        g2d.setColor(new Color(50, 130, 20));
        g2d.fillRect(0, groundY, WIDTH, 2);
    }

    private void drawMenu(Graphics2D g2d) {
        // Title shadow
        g2d.setFont(new Font("Arial", Font.BOLD, 48));
        g2d.setColor(new Color(0, 0, 0, 80));
        String title = "Flappy Bird";
        FontMetrics fm = g2d.getFontMetrics();
        int titleX = (WIDTH - fm.stringWidth(title)) / 2;
        g2d.drawString(title, titleX + 3, 143);

        // Title
        g2d.setColor(new Color(255, 255, 255));
        g2d.drawString(title, titleX, 140);

        // Title outline
        g2d.setColor(new Color(80, 50, 0));
        g2d.setStroke(new BasicStroke(3));
        // Simulated outline by drawing text shifted in all directions
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                if (dx != 0 || dy != 0) {
                    g2d.setColor(new Color(80, 50, 0));
                    g2d.drawString(title, titleX + dx, 140 + dy);
                }
            }
        }
        g2d.setColor(Color.WHITE);
        g2d.drawString(title, titleX, 140);

        // Instructions
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.setColor(new Color(255, 255, 255, (int)(180 + 75 * Math.sin(menuBirdBob * 2))));
        String start = "Pressione ESPACO para jogar";
        fm = g2d.getFontMetrics();
        g2d.drawString(start, (WIDTH - fm.stringWidth(start)) / 2, HEIGHT / 2 + 100);

        // Best score
        if (bestScore > 0) {
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            g2d.setColor(new Color(255, 215, 0));
            String best = "Melhor: " + bestScore;
            fm = g2d.getFontMetrics();
            g2d.drawString(best, (WIDTH - fm.stringWidth(best)) / 2, HEIGHT / 2 + 140);
        }
    }

    private void drawScore(Graphics2D g2d) {
        String s = String.valueOf(score);
        g2d.setFont(new Font("Arial", Font.BOLD, 50));
        FontMetrics fm = g2d.getFontMetrics();
        int x = (WIDTH - fm.stringWidth(s)) / 2;

        // Shadow
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.drawString(s, x + 3, 73);

        // Outline
        g2d.setColor(new Color(80, 50, 0));
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                g2d.drawString(s, x + dx, 70 + dy);
            }
        }

        // Score text
        g2d.setColor(Color.WHITE);
        g2d.drawString(s, x, 70);
    }

    private void drawGameOver(Graphics2D g2d) {
        // Overlay
        g2d.setColor(new Color(0, 0, 0, 120));
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        // Game Over panel
        int panelW = 260;
        int panelH = 200;
        int panelX = (WIDTH - panelW) / 2;
        int panelY = (HEIGHT - panelH) / 2 - 30;

        // Panel background
        g2d.setColor(new Color(220, 200, 150));
        g2d.fillRoundRect(panelX, panelY, panelW, panelH, 15, 15);
        g2d.setColor(new Color(160, 140, 90));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(panelX, panelY, panelW, panelH, 15, 15);

        // Game Over text
        g2d.setFont(new Font("Arial", Font.BOLD, 36));
        g2d.setColor(new Color(200, 50, 50));
        String go = "Game Over";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(go, (WIDTH - fm.stringWidth(go)) / 2, panelY + 50);

        // Score
        g2d.setFont(new Font("Arial", Font.BOLD, 22));
        g2d.setColor(new Color(80, 60, 20));
        String sc = "Pontos: " + score;
        fm = g2d.getFontMetrics();
        g2d.drawString(sc, (WIDTH - fm.stringWidth(sc)) / 2, panelY + 95);

        // Best
        g2d.setColor(new Color(200, 150, 0));
        String best = "Melhor: " + bestScore;
        g2d.drawString(best, (WIDTH - fm.stringWidth(best)) / 2, panelY + 130);

        // Medal
        if (score >= 10) {
            Color medalColor = score >= 30 ? new Color(255, 215, 0) :
                               score >= 20 ? new Color(192, 192, 192) :
                               new Color(205, 127, 50);
            g2d.setColor(medalColor);
            g2d.fillOval(panelX + 20, panelY + 70, 40, 40);
            g2d.setColor(medalColor.darker());
            g2d.drawOval(panelX + 20, panelY + 70, 40, 40);
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            g2d.setColor(Color.WHITE);
            String star = "\u2605";
            fm = g2d.getFontMetrics();
            g2d.drawString(star, panelX + 20 + (40 - fm.stringWidth(star)) / 2,
                          panelY + 70 + 26);
        }

        // Restart prompt
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        g2d.setColor(new Color(255, 255, 255, 200));
        String restart = "ESPACO para reiniciar";
        fm = g2d.getFontMetrics();
        g2d.drawString(restart, (WIDTH - fm.stringWidth(restart)) / 2, panelY + panelH + 40);
    }

    // Input handling
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_UP) {
            handleInput();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        handleInput();
    }

    private void handleInput() {
        switch (state) {
            case MENU:
                startGame();
                bird.jump();
                break;
            case PLAYING:
                bird.jump();
                break;
            case GAME_OVER:
                resetGame();
                break;
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}
