import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer {
    private static final int PORT = 5000;

    public static void main(String[] args) {
        System.out.println("Chat server starting on port " + PORT + "...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening.");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (
            Socket socket = clientSocket;
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            out.println("Bot: Connected to server. Type 'quit' to exit.");

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Received: " + inputLine);

                if (inputLine.equalsIgnoreCase("quit")) {
                    out.println("Bot: Goodbye!");
                    break;
                }

                String response = generateReply(inputLine);
                out.println(response);
            }

            System.out.println("Client disconnected.");
        } catch (IOException e) {
            System.out.println("Client handling error: " + e.getMessage());
        }
    }

    private static String generateReply(String message) {
        message = message.toLowerCase().trim();

        if (message.contains("hello") || message.contains("hi")) {
            return "Bot: Hello! How can I help you?";
        } else if (message.contains("name")) {
            return "Bot: I am a simple Java socket chatbot.";
        } else if (message.contains("how are you")) {
            return "Bot: I'm running smoothly over sockets.";
        } else {
            return "Bot: You said '" + message + "'";
        }
    }
}