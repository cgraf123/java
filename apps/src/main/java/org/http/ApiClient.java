package org.http;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ApiClient {

    private static final String GEOJSON_KEY = "geojson";

    private final URI host;
    private final Logger logger;

    public ApiClient(@NotNull URI uri, boolean useLogger) {
        host = uri;
        logger = useLogger ? Logger.getLogger(this.getClass().getName()) : null;
        if (logger != null) {
            logger.setLevel(Level.INFO);
        }
    }

    public UUID add(@NotNull Path path) throws IOException {
        return add(path, null);
    }

    public UUID add(@NotNull Path path, @Nullable UUID uuid) throws IOException {
        HttpPost post = new HttpPost(uuid == null ? host : host.resolve(uuid.toString()));
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addBinaryBody(GEOJSON_KEY, path.toFile(), ContentType.APPLICATION_JSON, path.getFileName().toString());
        HttpEntity multipart = builder.build();
        post.setEntity(multipart);
        return UUID.fromString(request(post).getContentString());
    }

    public UUID delete(@NotNull UUID uuid) throws IOException {
        HttpDelete delete = new HttpDelete(host.resolve(uuid.toString()));
        return UUID.fromString(request(delete).getContentString());
    }

    public String get_uuid(@NotNull UUID uuid) throws IOException {
        HttpGet get = new HttpGet(host.resolve(uuid.toString()));
        return request(get).getContentString();
    }

    public String get_uuids() throws IOException {
        HttpGet get = new HttpGet(host);
        return request(get).getContentString();
    }

    private Response request(@NotNull HttpRequestBase request) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            try (CloseableHttpResponse httpResponse = client.execute(request)) {
                Response response = new Response(httpResponse);
                if (logger != null) {
                    logger.info("request URI: " + request.getURI().toString());
                    logger.info("request method: " + request.getMethod());
                    logger.info("request headers: " + Arrays.toString(request.getAllHeaders()));
                    logger.info("response headers: " + Arrays.toString(response.headers));
                    logger.info("response status: " + response.statusLine.getStatusCode() + ": "
                            + response.statusLine.getReasonPhrase());
                }
                return response;
            }
        }
    }
}
