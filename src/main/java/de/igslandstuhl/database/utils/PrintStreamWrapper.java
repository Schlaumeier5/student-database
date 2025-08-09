package de.igslandstuhl.database.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Locale;
import java.util.function.Consumer;

public class PrintStreamWrapper extends PrintStream {
    private final PrintStream origin;
    private final Consumer<PrintStream> beforePrintCallback;
    
    public PrintStreamWrapper(PrintStream replace, PrintStream origin, Consumer<PrintStream> beforePrintCallback, Consumer<PrintStream> afterPrintCallback) {
        super(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                replace.write(b);
                // Prompt nur bei Zeilenende setzen
                if (b == '\n') {
                    afterPrintCallback.accept(origin);
                }
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                replace.write(b, off, len);
                if (len > 0 && b[off + len - 1] == '\n') {
                    afterPrintCallback.accept(origin);
                }
            }

            @Override
            public void flush() throws IOException {
                replace.flush();
            }

            @Override
            public void close() throws IOException {
                replace.close();
            }
        }, true); // autoFlush = true);
        this.origin = origin;
        this.beforePrintCallback = beforePrintCallback;
    }

    @Override
    public PrintStream append(CharSequence csq) {
        beforePrintCallback.accept(origin);
        super.append(csq);
        return this;
    }

    @Override
    public PrintStream append(char c) {
        beforePrintCallback.accept(origin);
        super.append(c);
        return this;
    }

    @Override
    public PrintStream append(CharSequence csq, int start, int end) {
        beforePrintCallback.accept(origin);
        super.append(csq, start, end);
        return this;
    }

    @Override
    public void print(boolean b) {
        beforePrintCallback.accept(origin);
        super.print(b);
    }

    @Override
    public void print(char c) {
        beforePrintCallback.accept(origin);
        super.print(c);
    }

    @Override
    public void print(int i) {
        beforePrintCallback.accept(origin);
        super.print(i);
    }

    @Override
    public void print(long l) {
        beforePrintCallback.accept(origin);
        super.print(l);
    }

    @Override
    public void print(float f) {
        beforePrintCallback.accept(origin);
        super.print(f);
    }

    @Override
    public void print(double d) {
        beforePrintCallback.accept(origin);
        super.print(d);
    }

    @Override
    public void print(char[] s) {
        beforePrintCallback.accept(origin);
        super.print(s);
    }

    @Override
    public void print(String s) {
        beforePrintCallback.accept(origin);
        super.print(s);
    }

    @Override
    public void print(Object obj) {
        beforePrintCallback.accept(origin);
        super.print(obj);
    }

    @Override
    public PrintStream printf(String format, Object... args) {
        beforePrintCallback.accept(origin);
        super.printf(format, args);
        return this;
    }

    @Override
    public PrintStream printf(Locale l, String format, Object... args) {
        beforePrintCallback.accept(origin);
        super.printf(l, format, args);
        return this;
    }

    @Override
    public void println() {
        beforePrintCallback.accept(origin);
        super.println();
    }

    @Override
    public void println(boolean x) {
        beforePrintCallback.accept(origin);
        super.println(x);
    }

    @Override
    public void println(char x) {
        beforePrintCallback.accept(origin);
        super.println(x);
    }

    @Override
    public void println(int x) {
        beforePrintCallback.accept(origin);
        super.println(x);
    }

    @Override
    public void println(long x) {
        beforePrintCallback.accept(origin);
        super.println(x);
    }

    @Override
    public void println(float x) {
        beforePrintCallback.accept(origin);
        super.println(x);
    }

    @Override
    public void println(double x) {
        beforePrintCallback.accept(origin);
        super.println(x);
    }

    @Override
    public void println(char[] x) {
        beforePrintCallback.accept(origin);
        super.println(x);
    }

    @Override
    public void println(String x) {
        beforePrintCallback.accept(origin);
        super.println(x);
    }

    @Override
    public void println(Object x) {
        beforePrintCallback.accept(origin);
        super.println(x);
    }

}
