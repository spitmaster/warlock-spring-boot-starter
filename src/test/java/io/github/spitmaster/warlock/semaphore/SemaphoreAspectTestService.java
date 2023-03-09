package io.github.spitmaster.warlock.semaphore;

import io.github.spitmaster.warlock.annotation.Wsemaphore;
import io.github.spitmaster.warlock.enums.Scope;
import org.springframework.stereotype.Service;

@Service
public class SemaphoreAspectTestService {

    private long counter = 0;

    @Wsemaphore(name = "mys2", permits = 1, scope = Scope.DISTRIBUTED)
    public void testWsemaphore(int id) {
        for (int i = 0; i < id * 1000; i++) {
            counter++;
        }
    }

    public long getCounter() {
        return counter;
    }

}
