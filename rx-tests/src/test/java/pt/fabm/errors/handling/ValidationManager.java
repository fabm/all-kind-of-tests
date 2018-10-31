package pt.fabm.errors.handling;

import com.google.common.base.Strings;

import java.util.Objects;

public class ValidationManager {
    public void throwIfEmpty(String value, String label){
        if (Strings.isNullOrEmpty(value)){
            throw new ApplicationException(1,label,value);
        }
    }
    public void throwIfEmpty(Object value, String label){
        if (Objects.isNull(value)){
            throw new ApplicationException(1,label);
        }
    }
}
