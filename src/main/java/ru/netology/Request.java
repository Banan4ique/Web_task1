package ru.netology;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public record Request(String method, String path, Map<String, String> headers, String body, List<NameValuePair> queryParams) {
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
}