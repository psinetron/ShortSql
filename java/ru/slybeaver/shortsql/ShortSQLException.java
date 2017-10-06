package ru.slybeaver.shortsql;

public class ShortSQLException extends Exception {

    public ShortSQLException(String message, String originalMessage) {
        super(message + "\n" + originalMessage);
    }

    public ShortSQLException(String message) {
        super(message );
    }

}
