package eu.lightest.horn.error;

public abstract class HornError extends Throwable {

    protected final int line;
    protected final int pos;

    protected HornError ( String msg, int line, int pos ) {
        super(msg);
        this.line = line;
        this.pos = pos;
    }

    public int getLine() {
        return line;
    }

    public int getPos() {
        return pos;
    }
}
