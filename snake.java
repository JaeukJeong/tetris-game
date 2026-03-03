import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class SnakeGame extends JPanel implements ActionListener {
    // Game settings
    private static final int TILE_SIZE = 25;
    private static final int GRID_WIDTH = 20;
    private static final int GRID_HEIGHT = 20;
    private static final int DELAY = 100; // Game speed (milliseconds)

    private final ArrayList<Point> snake;
    private Point food;
    private char direction; // 'U' = Up, 'D' = Down, 'L' = Left, 'R' = Right
    private boolean gameOver;
    private int score;
    private Timer timer;
    private Random random;

    public SnakeGame() {
        setPreferredSize(new Dimension(GRID_WIDTH * TILE_SIZE, GRID_HEIGHT * TILE_SIZE));
        setBackground(Color.BLACK);
        setFocusable(true);

        snake = new ArrayList<>();
        random = new Random();

        // Keyboard controls
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (gameOver && e.getKeyCode() == KeyEvent.VK_ENTER) {
                    startGame();
                    return;
                }

                // Prevent the snake from reversing into itself
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_W:
                        if (direction != 'D') direction = 'U';
                        break;
                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_S:
                        if (direction != 'U') direction = 'D';
                        break;
                    case KeyEvent.VK_LEFT:
                    case KeyEvent.VK_A:
                        if (direction != 'R') direction = 'L';
                        break;
                    case KeyEvent.VK_RIGHT:
                    case KeyEvent.VK_D:
                        if (direction != 'L') direction = 'R';
                        break;
                }
            }
        });

        startGame();
    }

    private void startGame() {
        snake.clear();
        // Initial snake position (starts with 3 parts)
        snake.add(new Point(5, 5));
        snake.add(new Point(4, 5));
        snake.add(new Point(3, 5));
        
        direction = 'R';
        score = 0;
        gameOver = false;
        
        spawnFood();

        if (timer != null) {
            timer.stop();
        }
        timer = new Timer(DELAY, this);
        timer.start();
        repaint();
    }

    private void spawnFood() {
        int x, y;
        boolean onSnake;
        // Make sure food doesn't spawn inside the snake's body
        do {
            x = random.nextInt(GRID_WIDTH);
            y = random.nextInt(GRID_HEIGHT);
            onSnake = false;
            for (Point p : snake) {
                if (p.x == x && p.y == y) {
                    onSnake = true;
                    break;
                }
            }
        } while (onSnake);

        food = new Point(x, y);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            move();
            checkCollisions();
        }
        repaint();
    }

    private void move() {
        Point head = snake.get(0);
        Point newHead = new Point(head.x, head.y);

        switch (direction) {
            case 'U': newHead.y--; break;
            case 'D': newHead.y++; break;
            case 'L': newHead.x--; break;
            case 'R': newHead.x++; break;
        }

        // Add new head to the front
        snake.add(0, newHead);

        // Check if food was eaten
        if (newHead.x == food.x && newHead.y == food.y) {
            score += 10;
            spawnFood();
        } else {
            // Remove the tail if no food eaten
            snake.remove(snake.size() - 1);
        }
    }

    private void checkCollisions() {
        Point head = snake.get(0);

        // Check wall collisions
        if (head.x < 0 || head.x >= GRID_WIDTH || head.y < 0 || head.y >= GRID_HEIGHT) {
            gameOver = true;
        }

        // Check self collisions (skip the head itself)
        for (int i = 1; i < snake.size(); i++) {
            if (head.x == snake.get(i).x && head.y == snake.get(i).y) {
                gameOver = true;
                break;
            }
        }

        if (gameOver) {
            timer.stop();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (gameOver) {
            drawGameOver(g);
        } else {
            drawGame(g);
        }
    }

    private void drawGame(Graphics g) {
        // Draw food
        g.setColor(Color.RED);
        g.fillOval(food.x * TILE_SIZE, food.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);

        // Draw snake
        for (int i = 0; i < snake.size(); i++) {
            Point p = snake.get(i);
            if (i == 0) {
                g.setColor(new Color(50, 205, 50)); // Bright green for head
            } else {
                g.setColor(new Color(34, 139, 34)); // Darker green for body
            }
            g.fillRect(p.x * TILE_SIZE, p.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            
            // Draw a subtle border around snake segments for clarity
            g.setColor(Color.BLACK);
            g.drawRect(p.x * TILE_SIZE, p.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }

        // Draw Score
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.drawString("Score: " + score, 10, 20);
    }

    private void drawGameOver(Graphics g) {
        String msg1 = "Game Over";
        String msg2 = "Score: " + score;
        String msg3 = "Press ENTER to Restart";
        
        FontMetrics metrics = getFontMetrics(g.getFont());
        
        g.setColor(Color.WHITE);
        
        g.setFont(new Font("SansSerif", Font.BOLD, 30));
        metrics = getFontMetrics(g.getFont());
        g.drawString(msg1, (getWidth() - metrics.stringWidth(msg1)) / 2, getHeight() / 2 - 20);
        
        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        metrics = getFontMetrics(g.getFont());
        g.drawString(msg2, (getWidth() - metrics.stringWidth(msg2)) / 2, getHeight() / 2 + 10);
        
        g.setFont(new Font("SansSerif", Font.PLAIN, 16));
        metrics = getFontMetrics(g.getFont());
        g.drawString(msg3, (getWidth() - metrics.stringWidth(msg3)) / 2, getHeight() / 2 + 50);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Lite Snake Game");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            
            SnakeGame game = new SnakeGame();
            frame.add(game);
            frame.pack();
            
            frame.setLocationRelativeTo(null); // Center the window
            frame.setVisible(true);
        });
    }
}
