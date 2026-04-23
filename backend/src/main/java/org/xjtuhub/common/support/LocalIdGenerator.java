package org.xjtuhub.common.support;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class LocalIdGenerator implements IdGenerator {
    private final AtomicInteger sequence = new AtomicInteger();

    @Override
    public long nextId() {
        long millis = System.currentTimeMillis();
        int suffix = sequence.updateAndGet(current -> current >= 999 ? 0 : current + 1);
        return millis * 1000 + suffix;
    }
}
