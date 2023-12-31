import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class ChatPanel extends JPanel {
    private JTextArea chatArea;
    private JTextField chatInput ;
    private String username;
    private ArrayList<String> messages;

    public ChatPanel(String defaultUsername){
        this.username = defaultUsername;
        this.messages = new ArrayList<>();

        setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());

        chatInput = new JTextField();
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(e -> sendMessage());

        inputPanel.add(chatInput, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        add(inputPanel, BorderLayout.SOUTH);
    }
    public void appendMessage(String sender, String message){
        chatArea.append(sender+": "+message+"\n");
    }
    private void sendMessage(){
        String message = chatInput.getText().trim();
        if (!message.isEmpty()){
            appendMessage(username, message);
            chatInput.setText("");
        }
    }
    public void setUsername(String username){
        this.username= username;
    }
    public String  getChatInput(){
        return chatInput.getText();
    }

    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g){
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial",Font.PLAIN,12));
        int y= 20;
        for (String message: messages){
            g.drawString(message, 10, y);
            y += 20;
        }
    }


}
