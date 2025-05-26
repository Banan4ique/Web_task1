package ru.netology;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Request {

    private final String method;
    private final String path;
    private final Map<String, String> headers;
    private final String body;
    private final List<NameValuePair> queryParams;

    public Request(String method, String path, Map<String, String> headers, String body) {
        this.method = method;
        this.path = path;
        this.headers =headers;
        this.body = body;
        this.queryParams = path != null ?
                URLEncodedUtils.parse(URI.create(path), "UTF-8") :
                Collections.emptyList();
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

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
        return queryParams;
    }
}