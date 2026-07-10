## src/main/java/lotr/Character.java
- **Refactor:** The class `Character` has been changed to `abstract` and fields `power` and `hp` are now `protected`, improving the class hierarchy and encapsulation.
- **Edge case:** The constructor of `Character` directly assigns values to the fields (`this.power = power; this.hp = hp;`) bypassing the validation in the setters `setPower` and `setHp`. If subclasses initialize with negative values (e.g. via `super(-5, -10)`), it leads to invalid internal states.
- **Code Patch:**
  ```java
  public Character(int power, int hp) {
      setPower(power);
      setHp(hp);
  }
  ```

## src/main/java/lotr/CharacterFactory.java
- **Bug fix:** Added a `!Modifier.isAbstract(cls.getModifiers())` check when scanning subclasses. This prevents trying to instantiate the abstract `Character` class itself, avoiding `InstantiationException`.
- **Refactor:** In `createCharacter()`, a new `Random` instance is created on each invocation. Reusing a single class-level static `Random` instance is more efficient.
- **Code Patch:**
  ```java
  private static final Random random = new Random();

  public Character createCharacter() {
      int randType = random.nextInt(characterClasses.size());
      // ...
  }
  ```

## src/main/java/lotr/Elf.java
- **Refactor:** Cleanly removed the redundant `toString()` override as it is now inherited from `Character`.
- **Style:** In `kick(Character c)`, the code accesses the protected field `c.hp` directly. While package-private subclass access is allowed within the `lotr` package, using getters/setters makes the implementation cleaner and more robust against future packaging refactors.
- **Code Patch:**
  ```java
  @Override
  public void kick(Character c) {
      if (c.getHp() < this.hp) {
          c.setHp(0);
      } else {
          c.setHp(c.getHp() - 1);
      }
  }
  ```

## src/main/java/lotr/GameManager.java
- **Bug fix:** Added a turn limit protection (`MAX_TURNS = 100`) to prevent infinite combat loops when two zero-power characters (e.g. Hobbits) fight.
- **Edge case:** Hardcoding `MAX_TURNS` as a static final variable makes the simulation turn limit rigid. It's better to make it configurable via the constructor or the `fight` method parameters.
- **Code Patch:**
  ```java
  private int maxTurns = 100;

  public GameManager() {}

  public GameManager(int maxTurns) {
      this.maxTurns = maxTurns;
  }

  public void fight(Character c1, Character c2) {
      fight(c1, c2, this.maxTurns);
  }

  public void fight(Character c1, Character c2, int maxTurnsLimit) {
      // Use maxTurnsLimit in the combat loop logic
  }
  ```

## src/main/java/lotr/Hobbit.java
- **Refactor:** Removed the redundant `toString()` override.
- **Style:** The method name `toCry()` does not follow Java's naming conventions for actions (methods are usually verbs, e.g. `cry()`).
- **Code Patch:**
  ```java
  public void cry() {
      System.out.println("Hobbit is crying");
  }
  ```

## src/main/java/lotr/King.java
- **Refactor:** Refactored class to extend `RandomFighter`, reducing duplicate random value generation and kick implementation.
- **No further concerns:** The class structure is clean and correctly delegates common logic to `RandomFighter`.

## src/main/java/lotr/Knight.java
- **Refactor:** Refactored class to extend `RandomFighter`, matching the design of `King` and promoting DRY code.
- **No further concerns:** The subclass is clean and simple.

## src/main/java/lotr/RandomFighter.java
- **Refactor:** Added a new `RandomFighter` abstract class, which is a great design choice to encapsulate shared random fighter logic.
- **Edge case:** The `randomValue` method uses `random.nextInt(max - min + 1)`. If callers pass parameters such that `max < min`, this throws an `IllegalArgumentException`. A safety precondition check is recommended.
- **Code Patch:**
  ```java
  protected static int randomValue(int min, int max) {
      if (max < min) {
          throw new IllegalArgumentException("max must be greater than or equal to min");
      }
      return random.nextInt(max - min + 1) + min;
  }
  ```

## src/test/java/CharacterFactoryTest.java
- **Bug fix:** Added unit test coverage for checking character instantiation via `CharacterFactory`.
- **No further concerns:** The test correctly validates non-null character generation.

## src/test/java/GameManagerTest.java
- **Bug fix:** Added a test that verifies that fights between weak characters terminate within a specific duration, validating the turn-limit loop safety.
- **No further concerns:** The test uses `assertTimeoutPreemptively` which is the correct testing method for this scenario.
