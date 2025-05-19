package ru.netology.parts;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class FieldPart implements Part {
    private final String name;
    private final String value;

    public FieldPart(String name, String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public String getFileName() {
        return null;
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(value.getBytes());
    }

    @Override
    public boolean isFile() {
        return false;
    }

    @Override
    public long getSize() {
        return value.getBytes().length;
    }
}