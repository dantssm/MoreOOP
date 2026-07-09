import lotr.Character;
import lotr.CharacterFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class CharacterFactoryTest {
    @Test
    void createCharacterReturnsValidInstance() {
        Character c = new CharacterFactory().createCharacter();
        assertNotNull(c);
        assertNotNull(c.toString());
    }
}
