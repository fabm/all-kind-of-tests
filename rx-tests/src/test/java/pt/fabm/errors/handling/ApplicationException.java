package pt.fabm.errors.handling;

public class ApplicationException extends RuntimeException{

    private int code;
    private Object[] params;

    public ApplicationException(int code, Object...params) {
        this.code = code;
        this.params = params;
    }

    public int getCode() {
        return code;
    }

    public Object[] getParams() {
        return params;
    }
}
