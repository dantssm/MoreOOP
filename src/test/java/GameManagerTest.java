import lotr.GameManager;
import lotr.Hobbit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import java.time.Duration;

class GameManagerTest {
    @Test
    void fightTerminatesEvenWithZeroPowerCharacters() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () ->
            new GameManager().fight(new Hobbit(), new Hobbit()));
    }
}
