package lotr;

import java.util.Random;

public class King extends Character {
    private static final Random random = new Random();

    public King() {
        super(randomValue(5, 15), randomValue(5, 15));
    }

    private static int randomValue(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }

    @Override
    public void kick(Character c) {
        int damage = random.nextInt(this.power + 1);
        c.setHp(c.getHp() - damage);
    }

    @Override
    public String toString() {
        return "King{hp=" + hp + ", power=" + power + "}";
    }
}
