package dmu.project.levelgen.exceptions;

/**
 * Created by Dom on 11/03/2017.
 * Exception to throw if level generation constraints are invalid.
 */

public class LevelConstraintsException extends Exception {

    public LevelConstraintsException() {
        super();
    }

    public LevelConstraintsException(String s) {
        super(s);
    }

    public LevelConstraintsException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public LevelConstraintsException(Throwable throwable) {
        super(throwable);
    }
}
