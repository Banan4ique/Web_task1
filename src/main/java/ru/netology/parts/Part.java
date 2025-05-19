package ru.netology.parts;

import java.io.IOException;
import java.io.InputStream;

public interface Part {
    String getName();
    String getValue() throws IOException;
    String getContentType();
    String getFileName();
    InputStream getInputStream() throws IOException;
    boolean isFile();
    long getSize();
}
