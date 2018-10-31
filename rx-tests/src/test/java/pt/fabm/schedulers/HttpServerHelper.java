package pt.fabm.schedulers;

import io.reactivex.Single;
import io.reactivex.functions.Function;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.core.http.HttpServerRequest;
import io.vertx.reactivex.ext.web.RoutingContext;

public class HttpServerHelper {

    public static Handler<RoutingContext> routeSubscription(Function<EndpointEntry, Single<Buffer>> execution) {
        return routeSubscription(execution,e->e);
    }

    public static <T>Handler<RoutingContext> routeSubscription(Function<EndpointEntry, Single<T>> execution, Function<T,Buffer> bufferTransformer) {
        return rc -> {
            final HttpServerRequest request = rc.request();
            Single<EndpointEntry> endpointEntrySingle = Single.create(source -> {
                request.bodyHandler(body -> {
                    EndpointEntry endpointEntry = new EndpointEntry();
                    endpointEntry.params = request.params();
                    endpointEntry.body = body;
                    endpointEntry.headers = request.headers();
                    try {
                        source.onSuccess(endpointEntry);
                    } catch (Exception e) {
                        source.onError(e);
                    }
                });
            });
            endpointEntrySingle.flatMap(execution).subscribe((T element) -> {
                request.response().end(bufferTransformer.apply(element));
            });
        };
    }
    public static Buffer jsonToBuffer(JsonObject jsonObject) {
        return Buffer.newInstance(jsonObject.toBuffer());
    }

}
