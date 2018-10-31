package pt.fabm.dao;

import com.datastax.driver.core.*;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import pt.fabm.errors.handling.ApplicationException;
import pt.fabm.model.User;
import pt.fabm.schedulers.ApplicationContext;

public class UserDao {
    private final PreparedStatement getQuery;
    private final PreparedStatement createQuery;
    private Session session;

    public UserDao() {
        this.session = ApplicationContext.getInstance().getSession();
        createQuery = session.prepare("insert into user (id, name) values (?, ?)");
        getQuery = session.prepare("select * from user where id=?");
    }

    public void createUser(User user) {
        BoundStatement bound = createQuery.bind(user.getId(), user.getName());
        session.execute(bound);
    }

    private static User toUser(Row row) {
        User user = new User();
        user.setName(row.getString("name"));
        user.setId(row.getString("id"));
        return user;
    }

    public Single<User> getUser(String id) {
        BoundStatement bound = getQuery.bind(id);
        ResultSetFuture futureExecution = session.executeAsync(bound);

        return Single.create((SingleEmitter<User> source) -> {
            FutureCallback<ResultSet> callback = new FutureCallback<ResultSet>() {
                @Override
                public void onSuccess(ResultSet result) {
                    if (result.isExhausted()) {
                        source.onError(new ApplicationException(10));
                    } else {
                        source.onSuccess(toUser(result.one()));
                    }
                }

                @Override
                public void onFailure(Throwable error) {
                    source.onError(error);
                }
            };
            Futures.addCallback(futureExecution, callback, ApplicationContext.getInstance().getDBExecutor());
        });
    }
}
