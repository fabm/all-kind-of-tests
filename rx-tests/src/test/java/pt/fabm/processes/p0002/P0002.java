package pt.fabm.processes.p0002;

import io.reactivex.Single;
import io.reactivex.functions.Function;
import io.vertx.core.json.JsonObject;
import pt.fabm.errors.handling.ValidationManager;
import pt.fabm.model.User;
import pt.fabm.schedulers.ApplicationContext;
import pt.fabm.schedulers.EndpointEntry;
import pt.fabm.schedulers.Process;
import pt.fabm.schedulers.ProcessState;

public class P0002 implements Process<EndpointEntry, JsonObject> {


    @Override
    public ProcessState[] getStates() {
        return State.values();
    }

    private String checkEntry(EndpointEntry entry) {
        ValidationManager vm = ApplicationContext.getInstance().getValidationManager();
        final String id = entry.getParams().get("id");
        vm.throwIfEmpty(id, "id");
        return id;
    }

    private JsonObject toJson(User user) {
        return new JsonObject()
                .put("id", user.getId())
                .put("name", user.getName());
    }

    @Override
    public Single<JsonObject> execute(EndpointEntry entry) {
        final Function<String, Single<User>> getUser = ApplicationContext.getInstance().getUserDao()::getUser;
        return Single.just(entry)
                .map(State.S001.call(this::checkEntry))
                .map(State.S002.call(getUser)).flatMap(e -> e)
                .map(State.S003.call(this::toJson));
    }

}