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

        public List<NameValuePair> getPostParam (String name) {
            if (!method.equals("GET") && headers.containsKey("Content-Type") &&
                    headers.get("Content-Type").equals("application/x-www-form-urlencoded")) {
                return URLEncodedUtils.parse(URI.create("?" + body), "UTF-8")
                        .stream().filter(x -> x.getName().equals(name))
                        .toList();
            } else {
                return null;
            }
        }

        public List<NameValuePair> getPostParams() {
            if (!method.equals("GET") && headers.containsKey("Content-Type") &&
                    headers.get("Content-Type").equals("application/x-www-form-urlencoded")) {
                return URLEncodedUtils.parse(URI.create("?" + body), "UTF-8");
            } else {
                return null;
            }
        }
}