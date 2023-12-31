import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    Socket socket;
    BufferedReader bufferedReader;
    BufferedWriter bufferedWriter;
    private String clientUserName;

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUserName = bufferedReader.readLine();
            clientHandlers.add(this);
            broadcastMessage("server " + clientUserName + " opening");
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void run() {
        String messageFromClient;
        while (socket.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine();
                broadcastMessage(messageFromClient);
            }catch (IOException e ){
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }
    public  void broadcastMessage(String  messageToSend){
        for (ClientHandler clientHandlers : clientHandlers){
            try {
                if (!clientHandlers.clientUserName.equals(clientUserName)){
                    clientHandlers.bufferedWriter.write(clientUserName+": " +messageToSend);
                    clientHandlers.bufferedWriter.newLine();
                    clientHandlers.bufferedWriter.flush();
                }

            }catch (IOException e){
                closeEverything(socket, bufferedReader, bufferedWriter);

            }
        }
    }
    public void removeClientHandler(){
        clientHandlers.remove(this);
        broadcastMessage("server "+clientUserName+" done");
    }
    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();
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