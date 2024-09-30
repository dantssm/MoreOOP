package lotr;

import java.util.Random;

public class Knight extends Character {
    private static final Random random = new Random();

    public Knight() {
        super(randomValue(2, 12), randomValue(2, 12));
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
        return "Knight{hp=" + hp + ", power=" + power + "}";
    }
}
