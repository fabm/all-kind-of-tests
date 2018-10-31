package pt.fabm.schedulers;

import com.datastax.driver.core.Session;
import pt.fabm.dao.UserDao;
import pt.fabm.errors.handling.ValidationManager;

import java.util.concurrent.Executor;
import java.util.function.Supplier;

public class ApplicationContext {
    private static ApplicationContext INSTANCE = new ApplicationContext();

    public static ApplicationContext getInstance() {
        return INSTANCE;
    }

    private Supplier<Session> sessionSupplier;
    private Supplier<UserDao> userDaoSupplier;
    private Supplier<ValidationManager> validationManagerSupplier;
    private Supplier<Executor> executorSupplier;

    public Session getSession() {
        return sessionSupplier.get();
    }
    public UserDao getUserDao() {
        return userDaoSupplier.get();
    }

    public ValidationManager getValidationManager() {
        return validationManagerSupplier.get();
    }

    void setValidationManagerSupplier(Supplier<ValidationManager> validationManagerSupplier) {
        this.validationManagerSupplier = validationManagerSupplier;
    }

    void setSessionSupplier(Supplier<Session> sessionSupplier) {
        this.sessionSupplier = sessionSupplier;
    }

    void setUserDaoSupplier(Supplier<UserDao> userDaoSupplier) {
        this.userDaoSupplier = userDaoSupplier;
    }


    void setExecutorSupplier(Supplier<Executor> executorSupplier) {
        this.executorSupplier = executorSupplier;
    }

    public Executor getDBExecutor() {
        return executorSupplier.get();
    }
}
