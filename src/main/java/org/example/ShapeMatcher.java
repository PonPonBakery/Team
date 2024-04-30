package org.example;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

enum ShapeType {
    RECTANGLE,
    CIRCLE,
    TRIANGLE
}

public class ShapeMatcher extends JFrame implements KeyListener {
    private static final int WIDTH = 500;
    private static final int HEIGHT = 500;
    private static final int PLAYER_WIDTH = 50;
    private static final int PLAYER_HEIGHT = 50;
    private static final int OBSTACLE_WIDTH = 20;
    private static final int OBSTACLE_HEIGHT = 20;
    private static final int PROJECTILE_WIDTH = 5;
    private static final int PROJECTILE_HEIGHT = 10;
    private static final int PLAYER_SPEED = 25;
    private static final int OBSTACLE_SPEED = 3;
    private static final int PROJECTILE_SPEED = 15;
    private int score = 0;

    private JPanel gamePanel;
    private JLabel scoreLabel;
    private Timer timer;
    private boolean isGameOver;
    private int playerX, playerY;
    private int projectileX, projectileY;
    private boolean isProjectileVisible;
    private boolean isFiring;
    private List<Obstacle> obstacles;
    private Image pointerImage;
    private Image spriteTriangle;
    private Image spriteSheet;
    private Image spriteRectangle;
    private Image spriteCircle;

    private Image scaledPointerImage;
    private int spriteWidth;
    private int spriteHeight;

    public ShapeMatcher() {
        setTitle("Shape and Number Game!");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        obstacles = new ArrayList<>();

        try {
            pointerImage = ImageIO.read(new File("pointer.png"));
            spriteCircle = ImageIO.read(new File("circle.png"));
            spriteTriangle = ImageIO.read(new File("triangle.png"));
            spriteRectangle = ImageIO.read(new File("rectangle.png"));

            scaledPointerImage = pointerImage.getScaledInstance(PLAYER_WIDTH, PLAYER_HEIGHT, Image.SCALE_DEFAULT);
            spriteWidth = OBSTACLE_WIDTH;
            spriteHeight = OBSTACLE_HEIGHT;
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                draw(g);
            }
        };

        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setBounds(10, 10, 100, 20);
        scoreLabel.setForeground(Color.BLUE);
        gamePanel.add(scoreLabel);

        add(gamePanel);
        gamePanel.setFocusable(true);
        gamePanel.addKeyListener(this);

        playerX = WIDTH / 2 - PLAYER_WIDTH / 2;
        playerY = HEIGHT - PLAYER_HEIGHT - 20;
        projectileX = playerX + PLAYER_WIDTH / 2 - PROJECTILE_WIDTH / 2;
        projectileY = playerY;
        isProjectileVisible = false;
        isGameOver = false;
        isFiring = false;

        timer = new Timer(20, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isGameOver) {
                    update();
                    gamePanel.repaint();
                }
            }
        });
        timer.start();
    }

    private void draw(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // Draw player ship
        if (scaledPointerImage != null) {
            g.drawImage(scaledPointerImage, playerX, playerY, null);
        }

        // Draw obstacles
        for (Obstacle obstacle : obstacles) {
            switch (obstacle.shapeType) {
                case RECTANGLE:
                    g.drawImage(spriteRectangle, obstacle.x, obstacle.y, null);
                    break;
                case CIRCLE:
                    g.drawImage(spriteCircle, obstacle.x, obstacle.y, null);
                    break;
                case TRIANGLE:
                    g.drawImage(spriteTriangle, obstacle.x, obstacle.y, null);
                    break;
            }
        }

        // Draw projectile
        if (isProjectileVisible) {
            g.setColor(Color.BLUE);
            g.fillRect(projectileX, projectileY, PROJECTILE_WIDTH, PROJECTILE_HEIGHT);
        }

        // Draw score label
        scoreLabel.setText("Score: " + score);

        // Draw game over message
        if (isGameOver) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("Game Over!", WIDTH / 2 - 80, HEIGHT / 2);
        }
    }

    private void update() {
        if (!isGameOver) {
            // Update obstacle positions
            for (int i = 0; i < obstacles.size(); i++) {
                obstacles.get(i).y += OBSTACLE_SPEED;
                if (obstacles.get(i).y > HEIGHT) {
                    obstacles.remove(i);
                    i--;
                }
            }
            // Generate new obstacles
            if (Math.random() < 0.02) {
                int obstacleX = (int) (Math.random() * (WIDTH - OBSTACLE_WIDTH));
                ShapeType shapeType = getRandomShapeType();
                obstacles.add(new Obstacle(obstacleX, 0, shapeType));
            }

            // Move projectile
            if (isProjectileVisible) {
                projectileY -= PROJECTILE_SPEED;
                if (projectileY < 0) {
                    isProjectileVisible = false;
                }
            }

            // Check collision between projectile and obstacles
            if (isProjectileVisible) {
                Rectangle projectileRect = new Rectangle(projectileX, projectileY, PROJECTILE_WIDTH, PROJECTILE_HEIGHT);
                for (int i = 0; i < obstacles.size(); i++) {
                    Rectangle obstacleRect = new Rectangle(obstacles.get(i).x, obstacles.get(i).y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT);
                    if (projectileRect.intersects(obstacleRect)) {
                        obstacles.remove(i);
                        score += 10;
                        isProjectileVisible = false;
                        break;
                    }
                }
            }
        }
    }

    private ShapeType getRandomShapeType() {
        int randomIndex = new Random().nextInt(3); // 0, 1, or 2
        switch (randomIndex) {
            case 0:
                return ShapeType.RECTANGLE;
            case 1:
                return ShapeType.CIRCLE;
            case 2:
                return ShapeType.TRIANGLE;
            default:
                return ShapeType.RECTANGLE; // Default to rectangle
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_LEFT && playerX > 0) {
            playerX -= PLAYER_SPEED;
        } else if (keyCode == KeyEvent.VK_RIGHT && playerX < WIDTH - PLAYER_WIDTH) {
            playerX += PLAYER_SPEED;
        } else if (keyCode == KeyEvent.VK_SPACE && !isFiring) {
            isFiring = true;
            projectileX = playerX + PLAYER_WIDTH / 2 - PROJECTILE_WIDTH / 2;
            projectileY = playerY;
            isProjectileVisible = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(500);
                        isFiring = false;
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }).start();

        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ShapeMatcher().setVisible(true);
            }
        });
    }
}
