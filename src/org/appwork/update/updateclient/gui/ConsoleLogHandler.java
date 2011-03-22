package org.appwork.update.updateclient.gui;

import java.io.PrintStream;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

public class ConsoleLogHandler extends StreamHandler {

    public ConsoleLogHandler(final PrintStream printStream) {

        this.setOutputStream(printStream);

    }

    /**
     * Override <tt>StreamHandler.close</tt> to do a flush but not to close the
     * output stream. That is, we do <b>not</b> close <tt>System.err</tt>.
     */
    @Override
    public void close() {
        this.flush();
    }

    /**
     * Publish a <tt>LogRecord</tt>.
     * <p>
     * The logging request was made initially to a <tt>Logger</tt> object, which
     * initialized the <tt>LogRecord</tt> and forwarded it here.
     * <p>
     * 
     * @param record
     *            description of the log event. A null record is silently
     *            ignored and is not published
     */
    @Override
    public void publish(final LogRecord record) {
        super.publish(record);
        this.flush();
    }
}
