package org.http;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class Response {
    public Header[] headers;
    public StatusLine statusLine;
    public byte[] content;

    Response(@NotNull HttpResponse httpResponse) throws IOException {
        HttpEntity httpEntity = httpResponse.getEntity();
        headers = httpResponse.getAllHeaders();
        statusLine = httpResponse.getStatusLine();
        content = httpEntity.getContent().readAllBytes();
    }
    public String getContentString() throws UnsupportedEncodingException {
        return new String(content, StandardCharsets.UTF_8);
    }
}
