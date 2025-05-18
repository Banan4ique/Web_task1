package ru.netology;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.Buffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    final List<String> validPaths = List.of(
            "/index.html", "/spring.svg", "/spring.png", "/resources.html",
            "/styles.css", "/app.js", "/links.html", "/forms.html",
            "/classic.html", "/events.html", "/events.js");
    private final int port;
    private final ExecutorService threadPool;

    public Server(int port) {
        this.port = port;
        threadPool = Executors.newFixedThreadPool(64);
    }

    public void start() throws IOException {
        try (final var serverSocket = new ServerSocket(port)) {
            System.out.println("Server is running on port: " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                threadPool.submit(() -> handleConnection(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
        }
    }

    private void handleConnection(Socket socket) {
        try (
                socket;
                final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                final var out = new BufferedOutputStream(socket.getOutputStream());
        ) {
            System.out.println("New connection " + socket.getPort());
            // read only request line for simplicity
            // must be in form GET /path HTTP/1.1
            final var requestLine = in.readLine();
            if (requestLine == null) return;

            final var parts = requestLine.split(" ");
            if (parts.length != 3) {
                // just close socket
                return;
            }
            final var path = parts[1];
            if (!validPaths.contains(path)) {
                sendNotFound(out);
                return;
            }

            final var filePath = Path.of(".", "public", path);
            if (!Files.exists(filePath)) {
                sendNotFound(out);
                return;
            }

            if (path.equals("/classic.html")) {
                handleClassicHtml(filePath, out);
            } else {
                handleRegularFile(filePath, out);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleRegularFile(Path filePath, BufferedOutputStream out) throws IOException {
        final var mimeType = Files.probeContentType(filePath);
        final var length = Files.size(filePath);

        out.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        Files.copy(filePath, out);
        out.flush();
    }

    private void handleClassicHtml(Path filePath, BufferedOutputStream out) throws IOException {
        final var mimeType = Files.probeContentType(filePath);
        final var template = Files.readString(filePath);
        final var content = template.replace(
                "{time}",
                LocalDateTime.now().toString()
        ).getBytes();
        out.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + content.length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.write(content);
        out.flush();
    }

    private void sendNotFound(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }
}