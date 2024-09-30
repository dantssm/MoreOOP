package lotr;

public class GameManager {
    public void fight(Character c1, Character c2) {
        System.out.println(c1 + " vs. " + c2);

        while (c1.isAlive() && c2.isAlive()) {
            System.out.println(c1.getClass().getSimpleName() + " attacks " + c2.getClass().getSimpleName());
            c1.kick(c2);
            System.out.println(c2.getClass().getSimpleName() + " has " + c2.getHp() + " HP left.");

            if (!c2.isAlive()) {
                System.out.println(c2.getClass().getSimpleName() + " has been defeated.");
                break;
            }

            System.out.println(c2.getClass().getSimpleName() + " attacks " + c1.getClass().getSimpleName());
            c2.kick(c1);
            System.out.println(c1.getClass().getSimpleName() + " now has " + c1.getHp() + " HP left.");

            if (!c1.isAlive()) {
                System.out.println(c1.getClass().getSimpleName() + " has been defeated.");
            }
        }

        System.out.println("The fight is over!");
    }
    public static void main(String[] args) {
        CharacterFactory factory = new CharacterFactory();
        Character c1 = factory.createCharacter();
        Character c2 = factory.createCharacter();

        GameManager game = new GameManager();
        game.fight(c1, c2);
    }
}

