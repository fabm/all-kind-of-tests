package pt.fabm.processes.p0001;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.fabm.schedulers.ProcessState;

enum State implements ProcessState {
    S001(0),
    S002(1),
    S003(2),
    S004(3);

    static Logger logger = LoggerFactory.getLogger(State.class);
    int id;

    State(int id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return this.name();
    }

    @Override
    public int getStateId() {
        return id;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
