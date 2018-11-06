package pt.fabm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface Field {
    enum KeyType{
        NONE,PARTITION_KEY,CLUSTERING_KEY
    }
    String value() ;

    KeyType keyType() default KeyType.NONE;
}
