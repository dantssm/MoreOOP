package lotr;
public class Character {
    public int power;
    public int hp;

    public Character(int power, int hp) {
        this.power = power;
        this.hp = hp;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        if (hp >= 0) {this.hp = hp;} 
        else {this.hp = 0;}
    }

    public String toString() {
        return "Character{hp=" + hp + ", power=" + power + "}";
    }

    public void kick(Character c) {}
    
    public boolean isAlive() {
        return hp > 0;
    } 
}
