package ru.netology;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.NameValuePair;

public class Server {
    private final Set<String> staticFiles = ConcurrentHashMap.newKeySet();
    private final int port;
    private final ExecutorService threadPool;
    private final Map<String, Map<String, Handler>> handlers = new ConcurrentHashMap<>();

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
            if (threadPool != null) {
                threadPool.shutdown();
            }
        }
    }

    private void handleConnection(Socket socket) {
        try (
                socket;
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            Request request = parseRequest(in);
            if (request == null) return;

            System.out.println("Пришёл запрос: " + request.method() + " " + request.path());
            System.out.println("Параметры запроса:");
            request.getQueryParams().stream().map(x -> x.getName() + "=" + x.getValue())
                    .forEach(System.out::println);
            String subPath = request.path().contains("?") ?
                    request.path().substring(0, request.path().indexOf("?")) :
                    request.path();
            // Проверяем есть ли кастомный обработчик для этого пути и метода
            if (handlers.containsKey(request.method())) {
                if(handlers.get(request.method()).entrySet().stream()
                        .anyMatch(x -> x.getKey().startsWith(subPath))) {
                    System.out.println("Найден обработчик для " + request.method() + " " + request.path());
                    handlers.get(request.method()).get(request.path()).handle(request, out);
                    return;
                }
            }

            // Если нет кастомного обработчика, проверяем статические файлы
            if (staticFiles.contains(subPath)) {
                handleStaticFile(request.path(), out);
                return;
            }

            System.out.println("Не найден обработчик для " + request.method() + " " + request.path());
            sendNotFound(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleStaticFile(String path, BufferedOutputStream out) throws IOException {
        Path filePath = Path.of(".", "public", path);
        if (!Files.exists(filePath)) {
            sendNotFound(out);
            return;
        }

        if (path.equals("/classic.html")) {
            handleClassicHtml(filePath, out);
        } else {
            handleRegularFile(filePath, out);
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

    public void addStaticFiles(List<String> paths) {
        staticFiles.addAll(paths);
    }

    public void addHandler(String method, String path, Handler handler) {
        handlers.computeIfAbsent(method, k -> new ConcurrentHashMap<>()).put(path, handler);
        System.out.println("Добавлен обработчик: " + method + " " + path);
    }

    private Request parseRequest(BufferedReader in) throws IOException {
        String requestLine = in.readLine();
        if (requestLine == null) return null;

        String[] parts = requestLine.split(" ");
        //if (parts.length != 3) return null;

        String method = parts[0].toUpperCase();
        String path = parts[1];

        Map<String, String> headers = new HashMap<>();
        String line;
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            String[] headerParts = line.split(":", 2);
            if (headerParts.length == 2) {
                headers.put(headerParts[0].trim(), headerParts[1].trim());
            }
        }

        StringBuilder body = new StringBuilder();
        if (headers.containsKey("Content-Length")) {
            int contentLength = Integer.parseInt(headers.get("Content-Length"));
            char[] buffer = new char[contentLength];
            in.read(buffer, 0, contentLength);
            body.append(buffer);
        }
        return new Request(method, path, headers, body.toString());
    }
}