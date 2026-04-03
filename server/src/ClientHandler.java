import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String clientName;
    private String clientIp;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.clientIp = socket.getInetAddress().getHostAddress();
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println("Enter your name:");
            clientName = in.readLine();

            if (clientName == null || clientName.trim().isEmpty()) {
                clientName = "Anonymous";
            }

            String joinMessage = clientName + " (" + clientIp + ") has joined the chat.";
            System.out.println(joinMessage);
            ChatServer.broadcast(joinMessage);

            String message;
            while ((message = in.readLine()) != null) {
                if (message.equalsIgnoreCase("quit")) {
                    break;
                }

                String formattedMessage = clientName + " (" + clientIp + "): " + message;
                System.out.println(formattedMessage);
                ChatServer.broadcast(formattedMessage);
            }
        } catch (IOException e) {
            System.out.println("Connection error with " + clientName + ": " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    private void disconnect() {
        try {
            ChatServer.removeClient(this);

            String leaveName = (clientName == null) ? "A user" : clientName;
            String leaveMessage = leaveName + " (" + clientIp + ") has left the chat.";
            System.out.println(leaveMessage);
            ChatServer.broadcast(leaveMessage);

            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.out.println("Error closing socket: " + e.getMessage());
        }
    }
}