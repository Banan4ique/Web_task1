package ru.netology.parts;

import org.apache.commons.fileupload.FileItem;

import java.io.IOException;
import java.io.InputStream;

public class FilePart implements Part {
    private final FileItem fileItem;

    public FilePart(FileItem fileItem) {
        this.fileItem = fileItem;
    }

    @Override
    public String getName() {
        return fileItem.getFieldName();
    }

    @Override
    public String getValue() throws IOException {
        return fileItem.getString();
    }

    @Override
    public String getContentType() {
        return fileItem.getContentType();
    }

    @Override
    public String getFileName() {
        return fileItem.getName();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return fileItem.getInputStream();
    }

    @Override
    public boolean isFile() {
        return true;
    }

    @Override
    public long getSize() {
        return fileItem.getSize();
    }
}
