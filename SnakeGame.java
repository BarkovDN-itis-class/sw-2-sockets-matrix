import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class SnakeGame extends JPanel implements ActionListener, KeyListener {
    private class Tile {
        int x;
        int y;

        Tile(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    int boardWidth;
    int boardHeight;
    int tileSize = 25;

    //snake
    Tile snakeHead;
    ArrayList<Tile> snakeBody;

    //food
    Tile food;
    Random random;

    //game logic
    int velocityX;
    int velocityY;
    Timer gameLoop;

    boolean gameOver = false;

    // start button for the game
    JButton startButton;
    private boolean isServer;
    private Server server;
    private Client client;

    // for making chat
    ChatPanel chatPanel;

    SnakeGame(int boardWidth, int boardHeight) {
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        setPreferredSize(new Dimension(this.boardWidth, this.boardHeight));
        setBackground(Color.black);
        addKeyListener(this);
        setFocusable(true);

        snakeHead = new Tile(5, 5);
        snakeBody = new ArrayList<>();

        food = new Tile(10, 10);
        random = new Random();
        placeFood();

        velocityX = 1;
        velocityY = 0;

        // Initialize ChatPanel
        chatPanel = new ChatPanel("DefaultUsername");
        chatPanel.setBounds(boardWidth / 2 - 100, boardHeight - 100, 200, 100);
        add(chatPanel);
        // game timer
        gameLoop = new Timer(100, this);

        // the starting button for the game
        startButton = new JButton("Start");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                move();
                repaint();
                if (gameOver) {
                    gameLoop.stop();
                    startButton.setEnabled(true);
                }
                try {
                    startGame();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        add(startButton);
        setLayout(null);
        // the location of the button
        startButton.setBounds(boardWidth / 2 - 50, boardHeight / 2 - 25, 100, 50);
    }
    private void startGame() throws IOException {
        snakeHead = new Tile(5, 5);
        snakeBody.clear();
        placeFood();
        velocityX = 1;
        velocityY = 0;
        gameOver = false;

        stopGame();
        // make the button start after the game over
        startButton.setEnabled(true);
        startButton.setVisible(true);

        // requestFocus();
        int choice = JOptionPane.showOptionDialog(
                null, // or provide a valid parent component
                "Choose your role:",
                "Role Selection",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new Object[]{"Server", "Client"},
                "Server"
        );
        isServer = (choice == JOptionPane.YES_OPTION);

        if (isServer) {
            server = new Server(1024, this);
            server.startServer();
        } else {
            String username = JOptionPane.showInputDialog("Enter your name:");
            chatPanel.setUsername(username);
            client = new Client("localhost", 4567, username, this);
            client.listenForMessage();
        }
        gameLoop.start();

        requestFocus();
    }

    private void stopGame() {
        if (server != null) {
            server.closeServerSocket();
            server = null;
        }
    }
    public void sendMessage() {
        String message = chatPanel.getChatInput();
        if (!message.isEmpty()) {
            chatPanel.appendMessage("you", message);
            if (isServer) {
                server.broadcastMessage(message);
            } else {
                client.sendMessage(message);
            }
        }
    }
    public void handleReceivedMessage(String message) {
        chatPanel.appendMessage("Opponent", message);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
        chatPanel.draw(g);
    }
    public void draw(Graphics g) {
        // Grid Lines
        if (!gameLoop.isRunning() || gameOver) {
            startButton.setVisible(true);
        } else {
            startButton.setVisible(false);
        }

        for (int i = 0; i < boardWidth / tileSize; i++) {
            g.drawLine(i * tileSize, 0, i * tileSize, boardHeight);
            g.drawLine(0, i * tileSize, boardWidth, i * tileSize);
        }

        // Snake Body
        for (int i = 0; i < snakeBody.size(); i++) {
            Tile snakePart = snakeBody.get(i);
            g.setColor(Color.blue);
            g.fill3DRect(snakePart.x * tileSize, snakePart.y * tileSize, tileSize, tileSize, true);
        }

        // Food
        g.setColor(Color.white);
        g.fill3DRect(food.x * tileSize, food.y * tileSize, tileSize, tileSize, true);

        // Snake Head
        g.setColor(Color.blue);
        g.fill3DRect(snakeHead.x * tileSize, snakeHead.y * tileSize, tileSize, tileSize, true);

        // Score
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        if (gameOver) {
            g.setColor(Color.red);
            g.drawString("Game Over: " + String.valueOf(snakeBody.size()), tileSize - 16, tileSize);
        } else {
            g.drawString("Score: " + String.valueOf(snakeBody.size()), tileSize - 16, tileSize);
        }
//for the chat
        chatPanel.draw(g);
    }
    public void placeFood() {
        if (boardWidth <= 0 || boardHeight <= 0 || tileSize >= boardWidth || tileSize >= boardHeight) {
            throw new IllegalArgumentException("Invalid board dimensions or tileSize");
        }

        food.x = random.nextInt(boardWidth / tileSize);
        food.y = random.nextInt(boardHeight / tileSize);
    }



    public void move() {
        // eat food
        if (collision(snakeHead, food)) {
            snakeBody.add(new Tile(food.x, food.y));
            placeFood();
        }

        // move snake body
        for (int i = snakeBody.size() - 1; i >= 0; i--) {
            Tile snakePart = snakeBody.get(i);
            if (i == 0) {
                snakePart.x = snakeHead.x;
                snakePart.y = snakeHead.y;
            } else {
                Tile prevSnakePart = snakeBody.get(i - 1);
                snakePart.x = prevSnakePart.x;
                snakePart.y = prevSnakePart.y;
            }
        }

        // move snake head
        snakeHead.x += velocityX;
        snakeHead.y += velocityY;

        // game over conditions
        for (int i = 0; i < snakeBody.size(); i++) {
            Tile snakePart = snakeBody.get(i);

            // collide with snake head
            if (collision(snakeHead, snakePart)) {
                gameOver = true;
            }
        }

        if (snakeHead.x * tileSize < 0 || snakeHead.x * tileSize > boardWidth || snakeHead.y * tileSize < 0
                || snakeHead.y * tileSize > boardHeight) {
            gameOver = true;
        }
    }

    public boolean collision(Tile tile1, Tile tile2) {
        return tile1.x == tile2.x && tile1.y == tile2.y;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            gameLoop.stop();
            startButton.setEnabled(true);
            stopGame();

        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP && velocityY != 1) {
            velocityX = 0;
            velocityY = -1;
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN && velocityY != -1) {
            velocityX = 0;
            velocityY = 1;
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT && velocityX != 1) {
            velocityX = -1;
            velocityY = 0;
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT && velocityX != -1) {
            velocityX = 1;
            velocityY = 0;
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            sendMessage();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}
