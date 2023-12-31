import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;
    private SnakeGame snakeGame;


    public Client(String serverAddress,int serverPort, String username, SnakeGame snakeGame){
        try {
            this.socket= new Socket(serverAddress,serverPort);
            this.bufferedWriter= new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader= new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username= username;
            this.snakeGame = snakeGame;
        }catch (IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);

        }
    }
    public void sendMessage(String message){
        try {
            bufferedWriter.write(username +": "+message );
            bufferedWriter.newLine();
            bufferedWriter.flush();

        }catch (IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }
    public void listenForMessage() {
        new Thread(new Runnable() {
            public void run() {
                String msgFromGroupChat;
                while (socket.isConnected()) {
                    try {
                        msgFromGroupChat = bufferedReader.readLine();
                        System.out.println(msgFromGroupChat);
                        handleReceivedMessage(msgFromGroupChat);
                    } catch (IOException e) {
                        closeEverything(socket, bufferedReader, bufferedWriter);
                        break;
                    }
                }
            }
        }).start();
    }

    private void handleReceivedMessage(String message){
        snakeGame.handleReceivedMessage(message);
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            if (bufferedReader != null){
                bufferedReader.close();
            }
            if (bufferedWriter != null){
                bufferedWriter.close();
            }
            if (socket != null){
                socket.close();

            }
        }catch (IOException e){
            e.printStackTrace();
        }

    }
}
