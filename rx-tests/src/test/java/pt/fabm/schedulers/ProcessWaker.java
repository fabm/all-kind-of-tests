package pt.fabm.schedulers;

public interface ProcessWaker {
    String saveProcess();
    void loadProcess(String name);
}