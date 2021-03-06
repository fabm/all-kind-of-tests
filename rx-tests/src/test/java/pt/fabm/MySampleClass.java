package pt.fabm;

/**
 * the javadoc comment
 * in the class
 */
public class MySampleClass {
    /**
     * private var
     *
     * javadoc
     */
    private String thePrivateVar;
    private String thePrivateVar1;
    private String thePrivateVar2;
    private String thePrivateVar3;
    private String thePrivateVar4;
    private String thePrivateVar5;


    /**
     * default constructor
     */
    public MySampleClass() {
        thePrivateVar = "thePrivate1";
    }

    /**
     * the method javadoc
     * @return {@link String}
     */
    public String getThePrivateVarChanged() {
        if(thePrivateVar == null){
            return CONST_VAR;
        }

        //empty space after comment
        return thePrivateVar+" "+CONST_VAR;
    }

    /**
     * const var with
     * javadoc multiline
     */
    private static final String CONST_VAR = "this is a const var";

    private static String transform(String parameter){
        return parameter + CONST_VAR;
    }
}
