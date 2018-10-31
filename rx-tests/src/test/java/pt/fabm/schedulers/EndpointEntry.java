package pt.fabm.schedulers;

import io.reactivex.Single;
import io.vertx.reactivex.core.MultiMap;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.core.http.HttpServerRequest;

public class EndpointEntry {

    Buffer body;
    MultiMap headers;
    MultiMap params;

    public static Single<EndpointEntry> create(HttpServerRequest request) {
        return Single.create(s -> {
            request.bodyHandler(body -> {
                EndpointEntry endpointEntry = new EndpointEntry();
                endpointEntry.params = request.params();
                endpointEntry.body = body;
                endpointEntry.headers = request.headers();
            });
        });
    }

    public MultiMap getHeaders() {
        return headers;
    }

    public MultiMap getParams() {
        return params;
    }

    public Buffer getBody() {
        return body;
    }
}
