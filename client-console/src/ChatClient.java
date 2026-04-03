import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.URL;
import java.util.Enumeration;
import java.util.Scanner;

public class ChatClient {
    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 5000;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Server host [" + DEFAULT_HOST + "]: ");
        String host = scanner.nextLine().trim();
        if (host.isEmpty()) {
            host = DEFAULT_HOST;
        }

        System.out.print("Server port [" + DEFAULT_PORT + "]: ");
        String portInput = scanner.nextLine().trim();
        int port = DEFAULT_PORT;
        if (!portInput.isEmpty()) {
            try {
                port = Integer.parseInt(portInput);
            } catch (NumberFormatException e) {
                System.out.println("Invalid port. Using default: " + DEFAULT_PORT);
                port = DEFAULT_PORT;
            }
        }

        System.out.println("Connecting to chat server at " + host + ":" + port + "...");

        try (
            Socket socket = new Socket(host, port);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String prompt = in.readLine();
            if (prompt == null) {
                System.out.println("Server closed the connection before sending a prompt.");
                return;
            }
            System.out.println(prompt);

            String name = scanner.nextLine().trim();
            if (name.isEmpty()) {
                name = "Anonymous";
            }
            out.println(name);

            String privateIp = getPrivateIpv4Address();
            String publicIp = getPublicIpAddress();

            out.println(privateIp);
            out.println(publicIp);

            Thread readerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String serverMessage;
                        while ((serverMessage = in.readLine()) != null) {
                            System.out.println(serverMessage);
                        }
                    } catch (IOException e) {
                        System.out.println("Disconnected from server.");
                    }
                }
            });

            readerThread.setDaemon(true);
            readerThread.start();

            while (true) {
                String userInput = scanner.nextLine();
                out.println(userInput);

                if (userInput.equalsIgnoreCase("quit")) {
                    break;
                }
            }

        } catch (IOException e) {
            System.out.println("Could not connect: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }

    private static String getPrivateIpv4Address() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();

                if (!networkInterface.isUp() || networkInterface.isLoopback() || networkInterface.isVirtual()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();

                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();

                    if (address instanceof Inet4Address
                            && !address.isLoopbackAddress()
                            && address.isSiteLocalAddress()) {
                        return address.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            // Ignore
        }

        return "unknown-private";
    }

    private static String getPublicIpAddress() {
        BufferedReader reader = null;
        try {
            URL url = new URL("https://api.ipify.org");
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String ip = reader.readLine();
            if (ip != null && !ip.trim().isEmpty()) {
                return ip.trim();
            }
        } catch (IOException e) {
            // Ignore
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }

        return "unknown-public";
    }
}