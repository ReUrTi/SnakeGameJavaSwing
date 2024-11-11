import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Random;

public class Main extends JPanel implements ActionListener{
    JFrame frame;
    private final int FRAME_WIDTH = 910;
    private final int FRAME_HEIGHT = 600;
    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private int playerX;
    private int playerY;
    private int spawnTime;
    private int score;
    private boolean gameOver;
    private JPanel menuPanel;
    private JPanel gamePanel;
    private JPanel settingsPanel;
    private JPanel infoPanel;
    private JDialog gameExitDialog;
    private JDialog mainMenuDialog;
    private Direction lastDirection = Direction.RIGHT;
    private Direction direction = Direction.RIGHT;
    private ArrayList<Snake> tail = new ArrayList<>();
    private ArrayList<Fruit> fruits = new ArrayList<>();
    private final Timer timer;
    private int countdown;

    public Main() {
        timer = new Timer(1000, this);
        frame = new JFrame("Snake Game");
        cardLayout = new CardLayout();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(FRAME_WIDTH + 16, FRAME_HEIGHT + 39);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        mainPanel = new JPanel(cardLayout);
        buildSettingsPanel();
        buildMenuPanel();
        buildGameExitDialog();
        buildMainMenuDialog();
        buildInfoPanel();
        buildGamePanel();
        mainPanel.add(menuPanel, "Menu");
        mainPanel.add(infoPanel, "Info");
        mainPanel.add(settingsPanel, "Settings");
        frame.add(mainPanel);
        frame.setVisible(true);
        cardLayout.show(mainPanel, "Menu");
    }

    // Iteration of timer
    @Override
    public void actionPerformed(ActionEvent e) {
        // Countdown iterator
        if(countdown != 0){
            gamePanel.repaint();
            return;
        }
        // Field
        boolean[][] positions = new boolean[16][12];
        // Each segment of the tail moves to the position previously occupied by the segment in front of it.
        int prevX = playerX;
        int prevY = playerY;
        for(Snake snake: tail){
            int tmpX = snake.posX;
            int tmpY = snake.posY;
            snake.posX = prevX;
            snake.posY = prevY;
            positions[snake.posX][snake.posY] = true;
            prevX = tmpX;
            prevY = tmpY;
        }
        // Change the direction of the snake's head when the appropriate key is pressed.
        switch (direction) {
            case UP:
                lastDirection = Direction.UP;
                playerY--;
                break;
            case RIGHT:
                lastDirection = Direction.RIGHT;
                playerX++;
                break;
            case DOWN:
                lastDirection = Direction.DOWN;
                playerY++;
                break;
            case LEFT:
                lastDirection = Direction.LEFT;
                playerX--;
                break;
        }
        // Relocating the head of the snake to the opposite side of the field
        playerX = outOfBorder(playerX, 15);
        playerY = outOfBorder(playerY, 11);
        // Collision check
        if(positions[playerX][playerY]) {
            timer.stop();
            gameOver = true;
            gamePanel.repaint();
        } else {
            // Fruit spawn and head collision check
            int index = 0;
            while (index < fruits.size()) {
                Fruit fruit = fruits.get(index);
                if (playerX == fruit.posX && playerY == fruit.posY) {
                    switch (fruit.color) {
                        case BLACK:
                            score -= 50;
                            break;
                        case RED:
                            score += 15;
                            tail.add(new Snake(tail.getLast().posX, tail.getLast().posY));
                            break;
                        case YELLOW:
                            score += 100;
                            tail.add(new Snake(tail.getLast().posX, tail.getLast().posY));
                            break;
                    }
                    fruits.remove(index);
                } else {
                    fruit.lifeTime--;
                    if (fruit.lifeTime == 0) {
                        fruits.remove(index);
                    } else {
                        index++;
                        positions[fruit.posX][fruit.posY] = true;
                    }
                }
            }
            positions[playerX][playerY] = true;

            spawnTime--;
            if (spawnTime == 0) fruitSpawn(positions);
        }
        gamePanel.repaint();
    }

    private void buildMenuPanel() {
        int buttonsWidth = 100;
        int buttonsHeight = 30;
        int buttonsPosX = ((FRAME_WIDTH + 16) / 2) - (buttonsWidth / 2);
        int buttonsPosY = (FRAME_HEIGHT + 40) / 2;

        menuPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Background
                g.setColor(new Color(222, 201, 175));
                g.fillRect(0, 0, FRAME_WIDTH, FRAME_HEIGHT);
            }
        };
        menuPanel.setLayout(null);
        JButton startButton = new JButton("Start game");
        JButton infoButton = new JButton("Information");
        JButton settingsButton = new JButton("Settings");
        JButton exitButton = new JButton("Exit");

        startButton.setBounds(buttonsPosX, buttonsPosY - (2 * buttonsHeight) - 20, buttonsWidth, buttonsHeight);
        infoButton.setBounds(buttonsPosX, buttonsPosY  - buttonsHeight - 10, buttonsWidth, buttonsHeight);
        settingsButton.setBounds(buttonsPosX, buttonsPosY, buttonsWidth, buttonsHeight);
        exitButton.setBounds(buttonsPosX, buttonsPosY  + buttonsHeight + 10, buttonsWidth, buttonsHeight);

        startButton.addActionListener(e -> {
            setDefaultPositions();
            timer.setDelay(1000);
            cardLayout.show(mainPanel, "Game");
            timer.start();
        });
        infoButton.addActionListener(e -> cardLayout.show(mainPanel, "Info"));
        settingsButton.addActionListener(e -> cardLayout.show(mainPanel, "Settings"));
        exitButton.addActionListener(e -> {
            gameExitDialog.setLocationRelativeTo(frame);
            gameExitDialog.setVisible(true);
        });

        menuPanel.add(startButton);
        menuPanel.add(infoButton);
        menuPanel.add(settingsButton);
        menuPanel.add(exitButton);
    }

    // Potential for settings
    // Refreshing all valuable variables
    private void setDefaultPositions() {
        playerX = 3;
        playerY = 4;
        spawnTime = 5;
        score = 0;
        countdown = 3;
        gameOver = false;
        lastDirection = Direction.RIGHT;
        direction = Direction.RIGHT;
        tail = new ArrayList<>();
        fruits = new ArrayList<>();
        tail.add(new Snake(2, 4));
    }

    private void fruitSpawn(boolean[][] positions){
        Random random = new Random();
        // Random spawn time
        spawnTime = random.nextInt(9) + 3;
        //Random chance for color
        int colorChance = random.nextInt(101);
        FruitColor color;
        if (colorChance < 10) color = FruitColor.YELLOW;
        else if (colorChance > 75) color = FruitColor.BLACK;
        else color = FruitColor.RED;
        // Checking field for potential free slot to spawn fruit in a random spot
        ArrayList<int[]> falseIndices = new ArrayList<>();
        for (int i = 0; i < positions.length; i++) {
            for (int j = 0; j < positions[i].length; j++) {
                if (!positions[i][j]) {
                    falseIndices.add(new int[]{i, j});
                }
            }
        }
        int position = random.nextInt(falseIndices.size());
        fruits.add(new Fruit(falseIndices.get(position)[0], falseIndices.get(position)[1], color));
    }

    private int outOfBorder(int playerC, int width){
        if(playerC > width) playerC = 0;
        else if(playerC < 0) playerC = width;
        return  playerC;
    }

    private enum Direction {
        UP, RIGHT, DOWN, LEFT
    }

    private enum FruitColor {
        RED, BLACK, YELLOW
    }

    public static class Snake {
        int posX;
        int posY;

        Snake(int posX, int posY) {
            this.posX = posX;
            this.posY = posY;
        }
    }

    private static class Fruit {
        int posX;
        int posY;
        int lifeTime;
        FruitColor color;

        Fruit(int posX, int posY, FruitColor color) {
            this.posX = posX;
            this.posY = posY;
            this.lifeTime = 15;
            this.color = color;
        }
    }

    private void buildGamePanel() {
        gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                final int OBJECT_SIZE = 50;

                super.paintComponent(g);

                // Background
                g.setColor(Color.lightGray);
                g.fillRect(0, 0, 800, 600);
                g.setColor(new Color(222, 201, 175));
                g.fillRect(800, 0, 110, 600);

                // Score
                g.setColor(Color.BLACK);
                g.setFont(new Font("Arial", Font.BOLD, 16));
                g.drawString("Score: " + score, 805, 570);

                // GameOver String
                if(gameOver) {
                    g.setColor(Color.BLACK);
                    g.setFont(new Font("Arial", Font.BOLD, 100));
                    g.drawString("GAME OVER", 60, 250);
                    return;
                }

                // Fruits
                for(Fruit fruit: fruits) {
                    if(fruit.color == FruitColor.RED) g.setColor(Color.RED);
                    else if(fruit.color == FruitColor.BLACK) g.setColor(Color.BLACK);
                    else g.setColor(Color.YELLOW);
                    g.fillRect(fruit.posX * OBJECT_SIZE, fruit.posY * OBJECT_SIZE, OBJECT_SIZE, OBJECT_SIZE);
                }

                // Head
                g.setColor(Color.GREEN);
                g.fillRect(playerX * OBJECT_SIZE, playerY * OBJECT_SIZE, OBJECT_SIZE, OBJECT_SIZE);
                g.setColor(Color.BLUE);
                g.fillRect((playerX * OBJECT_SIZE) + 10, (playerY * OBJECT_SIZE) + 10, 10, 10);
                g.fillRect((playerX * OBJECT_SIZE) + 30, (playerY * OBJECT_SIZE) + 10, 10, 10);
                g.setColor(Color.RED);
                g.fillRect((playerX * OBJECT_SIZE) + 20, (playerY * OBJECT_SIZE) + 30, 10, 10);

                // Tail
                g.setColor(Color.GREEN);
                for(Snake snake: tail) g.fillRect(snake.posX * OBJECT_SIZE, snake.posY * OBJECT_SIZE, OBJECT_SIZE, OBJECT_SIZE);
                g.setColor(Color.BLACK);
                g.fillRect((tail.getLast().posX * OBJECT_SIZE) + 20, (tail.getLast().posY * OBJECT_SIZE) + 20, 10, 10);

                // Countdown
                if(countdown > 0) {
                    g.setFont(new Font("Arial", Font.BOLD, 46));
                    g.drawString(String.valueOf(countdown), 815, 370);
                    countdown--;
                    if(countdown == 0){
                        timer.stop();
                        timer.setDelay(250);
                        gamePanel.requestFocusInWindow();
                        timer.start();
                    }
                }
            }
        };
        // Head control
        gamePanel.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_LEFT && lastDirection != Direction.RIGHT) {
                    direction = Direction.LEFT;
                }
                if (e.getKeyCode() == KeyEvent.VK_RIGHT && lastDirection != Direction.LEFT) {
                    direction = Direction.RIGHT;
                }
                if (e.getKeyCode() == KeyEvent.VK_UP && lastDirection != Direction.DOWN) {
                    direction = Direction.UP;
                }
                if (e.getKeyCode() == KeyEvent.VK_DOWN && lastDirection != Direction.UP) {
                    direction = Direction.DOWN;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        gamePanel.setLayout(null);
        JButton restartButton = new JButton("Restart");
        restartButton.addActionListener(e -> {
            setDefaultPositions();
            countdown = 3;
            timer.stop();
            timer.setDelay(1000);
            gamePanel.requestFocusInWindow();
            timer.start();
        });
        restartButton.setBounds(805, 20, 100, 30);
        JButton menuButton = new JButton("Menu");
        menuButton.addActionListener(e -> {
            timer.stop();
            mainMenuDialog.setLocationRelativeTo(frame);
            mainMenuDialog.setVisible(true);
        });
        menuButton.setBounds(805, 60, 100, 30);
        gamePanel.add(restartButton);
        gamePanel.add(menuButton);

        JTextArea textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setBounds(805, 140, 100, 100);
        textArea.setBackground(new Color(222, 201, 175));
        textArea.setFont(new Font("Arial", Font.BOLD, 14));
        textArea.setText("""
                R += 15 + tail
                Y += 100 + tail
                B -= 50
                """);
        gamePanel.add(textArea);

        mainPanel.add(gamePanel, "Game");
        repaint();
    }

    private void buildInfoPanel() {
        infoPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Background
                g.setColor(new Color(222, 201, 175));
                g.fillRect(0, 0, FRAME_WIDTH, FRAME_HEIGHT);
            }
        };
        infoPanel.setLayout(null);
        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "Menu"));
        backButton.setBounds(((FRAME_WIDTH + 16) / 2) - 50, FRAME_HEIGHT - 130, 100, 30);

        infoPanel.add(backButton);
    }

    private void buildSettingsPanel() {
        settingsPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Background
                g.setColor(new Color(222, 201, 175));
                g.fillRect(0, 0, FRAME_WIDTH, FRAME_HEIGHT);
            }
        };
        settingsPanel.setLayout(null);
        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "Menu"));
        backButton.setBounds(((FRAME_WIDTH + 16) / 2) - 50, FRAME_HEIGHT - 130, 100, 30);

        settingsPanel.add(backButton);
    }

    private void buildGameExitDialog() {
        gameExitDialog = new JDialog();
        gameExitDialog.setLayout(null);
        gameExitDialog.setTitle("Exit");
        gameExitDialog.setModal(true);
        gameExitDialog.setSize(300, 150);
        gameExitDialog.setResizable(false);

        JLabel messageLabel = new JLabel("Are you sure you want to quit?");
        messageLabel.setBounds(8, 15,300,30);
        messageLabel.setFont(new Font("Arial", Font.BOLD, 18));

        JButton yesButton = new JButton("Yes");
        JButton noButton = new JButton("I want to stay");
        yesButton.setBounds(10, 60, 130, 30);
        noButton.setBounds(145, 60, 130, 30);

        yesButton.addActionListener(e -> System.exit(0));
        noButton.addActionListener(e -> gameExitDialog.dispose());
        gameExitDialog.add(messageLabel);
        gameExitDialog.add(yesButton);
        gameExitDialog.add(noButton);
    }

    private void buildMainMenuDialog() {
        mainMenuDialog = new JDialog();
        mainMenuDialog.setLayout(null);
        mainMenuDialog.setTitle("Menu");
        mainMenuDialog.setModal(true);
        mainMenuDialog.setSize(300, 150);
        mainMenuDialog.setResizable(false);

        JLabel messageLabel = new JLabel("Are you a quitter?");
        messageLabel.setBounds(60, 15,300,30);
        messageLabel.setFont(new Font("Arial", Font.BOLD, 18));

        JButton yesButton = new JButton("Yes, i am!");
        JButton noButton = new JButton("No, i will fight!");
        yesButton.setBounds(10, 60, 130, 30);
        noButton.setBounds(145, 60, 130, 30);

        yesButton.addActionListener(e -> {
            cardLayout.show(mainPanel, "Menu");
            mainMenuDialog.dispose();
        });
        noButton.addActionListener(e -> {
            mainMenuDialog.dispose();
            gamePanel.requestFocusInWindow();
            if(!gameOver) timer.start();
        });
        mainMenuDialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                gamePanel.requestFocusInWindow();
                if(!gameOver) timer.start();
            }
        });
        mainMenuDialog.add(messageLabel);
        mainMenuDialog.add(yesButton);
        mainMenuDialog.add(noButton);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}