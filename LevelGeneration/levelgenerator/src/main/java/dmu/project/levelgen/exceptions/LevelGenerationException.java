package dmu.project.levelgen.exceptions;

/**
 * Created by Dom on 11/03/2017.
 * Exception to throw if level generation encounters a problem.
 */

public class LevelGenerationException extends Exception {
    public LevelGenerationException() {
    }

    public LevelGenerationException(String s) {
        super(s);
    }

    public LevelGenerationException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public LevelGenerationException(Throwable throwable) {
        super(throwable);
    }
}
