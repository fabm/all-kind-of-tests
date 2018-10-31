package pt.fabm;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;

@AutoFactory
public class TestSample {
    private String xpto;

    public TestSample(@Provided String xpto) {
        this.xpto = xpto;
    }
}
