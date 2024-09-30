package lotr;
public class Elf extends Character {
    public Elf() {
        super(10, 10);
    }

    @Override
    public void kick(Character c) {
        if (this.power > c.power) {
            c.setHp(0);
        } else {
            c.setHp(c.hp - 1);
        }
    }

    @Override
    public String toString() {
        return "Elf{hp=" + hp + ", power=" + power + "}";
    }
}
