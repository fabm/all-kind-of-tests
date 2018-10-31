package pt.fabm.schedulers;

import io.reactivex.Single;
import io.reactivex.functions.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface ProcessState{
    int getStateId();

    String getName();

    Logger getLogger();

    default String getStateName() {
        return getName() + ":" + getStateId();
    }

    /**
     * by default, an id between 1000 and 2000(excluded) means the {@link pt.fabm.schedulers.Process} is persisted
     *
     * @return true if is a state persisted
     */
    default boolean isPersisted() {
        final int stateId = getStateId();
        return (stateId >= 1000 && stateId < 2000);
    }

    /**
     * by default, the start process has the 0 id
     *
     * @return true if is the start process
     */
    default boolean isStart() {
        return getStateId() == 0;
    }

    /**
     * by default, an id between 2000 and 3000(excluded) means the {@link pt.fabm.schedulers.Process} was woke up
     * @return true if is a wokeUp state
     */
    default boolean wasWokeUp(){
        final int stateId = getStateId();
        return stateId >=2000 && stateId <3000;
    }

    /**
     * by default, an id greater than 3000 means that is an ok final state
     * @return true if is a an ok final state
     */
    default boolean isFinal(){
        return getStateId()>3000;
    }

    /**
     * by default, an id less than 0 means tha
     *
     *  is an error final state
     * @return true if is an error final state
     */
    default boolean isError(){
        return getStateId()>3000;
    }

    default <E, R1,R2> Function<E, R2> call(Function<E, R1> fn1,Function<R1,R2>fn2){
        return e -> {
            getLogger().info("state:" + this.getStateName());
            return fn2.apply(fn1.apply(e));
        };
    }
    default <E, R> Function<E, R> call(Function<E, R> fn){
        return e -> {
            getLogger().info("state:" + this.getStateName());
            return fn.apply(e);
        };
    }

    default ProcessState checkState(ProcessState other){
        if (this!=other){
            throw new IllegalStateException("Illegal process state "+ this.getStateName());
        }
        return this;
    }
}
