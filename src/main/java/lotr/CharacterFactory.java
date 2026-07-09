package lotr;

import java.lang.reflect.Modifier;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import org.reflections.Reflections;

public class CharacterFactory {
    private final List<Class<? extends Character>> characterClasses;

    public CharacterFactory() {
        Reflections reflections = new Reflections("lotr");
        characterClasses = new ArrayList<>();
        for (Class<? extends Character> cls : reflections.getSubTypesOf(Character.class)) {
            if (!Modifier.isAbstract(cls.getModifiers())) {
                characterClasses.add(cls);
            }
        }
    }

    public Character createCharacter() {
        Random rand = new Random();
        int randType = rand.nextInt(characterClasses.size());
        Class<? extends Character> characterClass = characterClasses.get(randType);

        try {
            return characterClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create character", e);
        }
    }
}
