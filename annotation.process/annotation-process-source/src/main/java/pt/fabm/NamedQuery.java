package pt.fabm;

import java.lang.annotation.*;
import java.util.Map;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
@Repeatable(NamedQueries.class)
public @interface NamedQuery {
    String name();
    String value();
}
