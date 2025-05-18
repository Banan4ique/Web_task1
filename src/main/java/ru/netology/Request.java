package ru.netology;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.util.List;
import java.util.Map;

public record Request(String method, String path, Map<String, String> headers, String body) {
    public List<NameValuePair> getQueryParam (String name) {
        return URLEncodedUtils.parse(URI.create(path), "UTF-8")
                .stream().filter(x -> x.getName().equals(name))
                .toList();
    }

    public List<NameValuePair> getQueryParams() {
        return URLEncodedUtils.parse(URI.create(path), "UTF-8");
    }
}