package pt.fabm.schedulers;

import io.reactivex.Observable;
import io.reactivex.Single;

public interface WakeAbleProcess<Tin,Tout,C> extends Process<Tin,Tout> {
    Single<Tout> wake(Single<C> entry);
}
