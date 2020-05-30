package de.timmi6790.statsbotdiscord.datatypes;

import java.util.concurrent.atomic.AtomicLong;

public class BiggestLong {
    private final AtomicLong number;

    public BiggestLong(final int defaultValue) {
        this.number = new AtomicLong(defaultValue);
    }

    public boolean tryNumber(final long number) {
        if (number > this.number.get()) {
            this.number.set(number);
            return true;
        }
        return false;
    }

    public long get() {
        return this.number.get();
    }
}
