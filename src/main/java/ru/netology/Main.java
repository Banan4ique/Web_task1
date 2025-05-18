package ru.netology;

import java.io.IOException;

public class Main {
  public static void main(String[] args) throws IOException {
    var server = new Server(9999);
    server.start();
  }
}