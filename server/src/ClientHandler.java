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
    private String privateIp;
    private String publicIp;

    public ClientHandler(Socket socket) {
        this.socket = socket;
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

            privateIp = in.readLine();
            if (privateIp == null || privateIp.trim().isEmpty()) {
                privateIp = "unknown-private";
            }

            publicIp = in.readLine();
            if (publicIp == null || publicIp.trim().isEmpty()) {
                publicIp = "unknown-public";
            }

            String joinMessage = clientName + " (private: " + privateIp + ", public: " + publicIp + ") has joined the chat.";
            System.out.println(joinMessage);
            ChatServer.broadcast(joinMessage);

            String message;
            while ((message = in.readLine()) != null) {
                if (message.equalsIgnoreCase("quit")) {
                    break;
                }

                String formattedMessage = clientName + " (private: " + privateIp + ", public: " + publicIp + "): " + message;
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
            String leavePrivateIp = (privateIp == null) ? "unknown-private" : privateIp;
            String leavePublicIp = (publicIp == null) ? "unknown-public" : publicIp;

            String leaveMessage = leaveName + " (private: " + leavePrivateIp + ", public: " + leavePublicIp + ") has left the chat.";
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