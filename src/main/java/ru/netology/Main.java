package ru.netology;

import java.io.IOException;
import java.util.List;

public class Main {
  public static void main(String[] args) throws IOException {
    final var server = new Server(9999);

    server.addStaticFiles(List.of(
            "/index.html", "/spring.svg", "/spring.png", "/resources.html",
            "/styles.css", "/app.js", "/links.html", "/forms.html",
            "/classic.html", "/events.html", "/events.js", "/multi.html"
    ));

    // добавление хендлеров (обработчиков)
    server.addHandler("GET", "/messages", (request, responseStream) -> {
      String response = "HTTP/1.1 200 OK\r\n" +
              "Content-Type: text/plain\r\n" +
              "Connection: close\r\n" +
              "\r\n";
      responseStream.write(response.getBytes());
      responseStream.flush();
    });

    server.addHandler("POST", "/messages", (request, responseStream) -> {
      String response = "HTTP/1.1 200 OK\r\n" +
              "Content-Type: text/plain\r\n" +
              "Content-Length: 13\r\n" +
              "Connection: close\r\n" +
              "\r\n" +
              "POST messages";
      responseStream.write(response.getBytes());
      responseStream.flush();
    });

    server.addHandler("POST", "/upload", (request, responseStream) -> {
      String response = "HTTP/1.1 200 OK\r\n" +
              "Content-Type: multipart/form-data\r\n" +
              "Content-Length: 77\r\n" +
              "Connection: close\r\n" +
              "\r\n" +
              "GET uploaded";
      responseStream.write(response.getBytes());
      responseStream.flush();
    });

    server.start();
  }
}