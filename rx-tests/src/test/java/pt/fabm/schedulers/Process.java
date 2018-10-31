package pt.fabm.schedulers;

import io.reactivex.Single;

public interface Process<Tin, Tout> {

    default String getName() {
        return getClass().getSimpleName();
    }

    ProcessState[] getStates();

    Single<Tout> execute(Tin entry);

}