package lotr;

import java.util.Random;

public abstract class RandomFighter extends Character {
    private static final Random random = new Random();

    protected RandomFighter(int power, int hp) {
        super(power, hp);
    }

    protected static int randomValue(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }

    @Override
    public void kick(Character c) {
        int damage = random.nextInt(this.power + 1);
        c.setHp(c.getHp() - damage);
    }
}
