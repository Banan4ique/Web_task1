package ru.netology;

import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import ru.netology.parts.FieldPart;
import ru.netology.parts.FilePart;
import ru.netology.parts.Part;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public record Request(String method, String path, Map<String, String> headers,
                      String body, List<NameValuePair> queryParams, List<NameValuePair> bodyParams) {
    public List<NameValuePair> getQueryParam (String name) {
        if (path != null) {
            return URLEncodedUtils.parse(URI.create(path), "UTF-8")
                    .stream().filter(x -> x.getName().equals(name))
                    .toList();
        } else {
            return Collections.emptyList();
        }
    }

    public List<NameValuePair> getQueryParams() {
        return path != null ? URLEncodedUtils.parse(URI.create(path), "UTF-8") : Collections.emptyList();
    }

    public List<NameValuePair> getPostParam (String name) {
        if (!method.equals("GET") && headers.containsKey("Content-Type") &&
                headers.get("Content-Type").equals("application/x-www-form-urlencoded") && body != null) {
            return URLEncodedUtils.parse(URI.create("?" + body), "UTF-8")
                    .stream().filter(x -> x.getName().equals(name))
                    .toList();
        } else {
            return Collections.emptyList();
        }
    }

    public List<NameValuePair> getPostParams() {
        if (!method.equals("GET") && headers.containsKey("Content-Type") &&
                headers.get("Content-Type").equals("application/x-www-form-urlencoded") && body != null) {
            return URLEncodedUtils.parse(URI.create("?" + body), "UTF-8");
        } else {
            return Collections.emptyList();
        }
    }

    public boolean isMultipart() {
        if (method.equals("GET")) {
            return false;
        }

        String contentType = headers.get("Content-Type");
        return contentType != null && contentType.startsWith("multipart/form-data");
    }

    public List<Part> getParts() {
        if (!isMultipart()) {
            return Collections.emptyList();
        }

        try {
            FileItemFactory factory = new DiskFileItemFactory();
            FileUpload upload = new FileUpload(factory);

            // Используем ByteArrayInputStream для тела запроса
            InputStream inputStream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));

            // Парсим multipart данные напрямую из InputStream
            List<FileItem> items = upload.parseRequest(new RequestContext() {
                @Override
                public String getCharacterEncoding() {
                    return StandardCharsets.UTF_8.name();
                }

                @Override
                public String getContentType() {
                    return headers.get("Content-Type");
                }

                @Override
                public int getContentLength() {
                    return body.length();
                }

                @Override
                public InputStream getInputStream() throws IOException {
                    return inputStream;
                }
            });

            List<Part> parts = new ArrayList<>();
            for (FileItem item : items) {
                parts.add(item.isFormField()
                        ? new FieldPart(item.getFieldName(), item.getString())
                        : new FilePart(item));
            }
            return parts;
        } catch (FileUploadException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<Part> getParts(String name) {
        List<Part> allParts = getParts();
        List<Part> result = new ArrayList<>();
        for (Part part : allParts) {
            if (part.getName().equals(name)) {
                result.add(part);
            }
        }
        return result;
    }
}