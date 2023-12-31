import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import javax.swing.*;

public class Server {
    private ServerSocket serverSocket;
    private SnakeGame snakeGame;
    private Socket currsocket;
    private static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();

    public Server(int port, SnakeGame snakeGame) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.snakeGame = snakeGame;
    }


    public void broadcastMessage(String message) {
        for (ClientHandler clientHandler : ClientHandler.clientHandlers) {
            try {
                if (!clientHandler.socket.equals(currsocket)) {
                    clientHandler.bufferedWriter.write("server: " + message);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                clientHandler.closeEverything(clientHandler.socket, clientHandler.bufferedReader, clientHandler.bufferedWriter);
            }
        }
    }


    public void startServer() {
        new Thread(() -> {
            try {
                while (!serverSocket.isClosed()) {
                    Socket socket = serverSocket.accept();
                    this.currsocket = socket;
                    System.out.println("New Client");
                    ClientHandler clientHandler = new ClientHandler(socket);
                    clientHandlers.add(clientHandler);
                    Thread thread = new Thread(clientHandler);
                    thread.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void closeServerSocket() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                int port = 4567;
                int boardWidth = 800;
                int boardHeight = 600;
                SnakeGame snakeGame = new SnakeGame(boardWidth, boardHeight);
                Server server = new Server(port, snakeGame);
                server.startServer();

                CountDownLatch latch = new CountDownLatch(1);
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    server.closeServerSocket();
                    latch.countDown();
                }));
                latch.await();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
}
