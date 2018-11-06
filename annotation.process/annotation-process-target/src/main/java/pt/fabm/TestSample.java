package pt.fabm;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;

@AutoFactory
public class TestSample {
    private Session session;

    PreparedStatement ps;

    public TestSample(@Provided Session session) {
        this.session = session;
        session.prepare("aaaaaa");
    }



}
