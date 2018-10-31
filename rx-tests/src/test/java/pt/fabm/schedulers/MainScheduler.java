package pt.fabm.schedulers;

import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;

import java.util.concurrent.TimeUnit;

public class MainScheduler extends Scheduler{
    @Override
    public Worker createWorker() {
        return null;
    }
}
