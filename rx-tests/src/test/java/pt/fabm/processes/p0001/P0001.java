package pt.fabm.processes.p0001;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.buffer.Buffer;
import pt.fabm.errors.handling.ValidationManager;
import pt.fabm.model.User;
import pt.fabm.schedulers.ApplicationContext;
import pt.fabm.schedulers.EndpointEntry;
import pt.fabm.schedulers.Process;
import pt.fabm.schedulers.ProcessState;

public class P0001 implements Process<EndpointEntry, String> {


    @Override
    public ProcessState[] getStates() {
        return State.values();
    }

    private JsonObject checkEntry(EndpointEntry entry) {
        ValidationManager vm = ApplicationContext.getInstance().getValidationManager();
        final Buffer userBuffer = entry.getBody();
        vm.throwIfEmpty(userBuffer,"user");
        JsonObject user = userBuffer.toJsonObject();
        vm.throwIfEmpty("name",user.getString("name"));
        vm.throwIfEmpty("getId",user.getString("id"));
        return user;
    }

    private User mapUser(JsonObject json) {
        User user = new User();
        user.setId(json.getString("id"));
        user.setName(json.getString("name"));
        return user;
    }

    private String createUser(User user) throws Exception {
        final ApplicationContext ac = ApplicationContext.getInstance();
        ac
                .getUserDao()
                .createUser(user);
        return "ok";
    }

    @Override
    public Single<String> execute(EndpointEntry entry) {
        return Single.just(entry)
                .map(State.S001.call(this::checkEntry))
                .map(State.S002.call(this::mapUser))
                .map(State.S003.call(this::createUser));
    }

}