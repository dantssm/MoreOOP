# PR #2 Review Report

This report reviews the changes proposed in PR #2 for `dantssm/MoreOOP`.

## src/main/java/lotr/Character.java
- **Bug fix:** Constrained `setPower(int power)` to not allow setting a negative power value using `Math.max(power, 0)`.
- **Refactor:** Made the class `Character` and its `kick(Character c)` method `abstract`. Changed `hp` and `power` fields from `public` to `protected` to support subclasses while maintaining encapsulation. Simplified `setHp` using `Math.max(hp, 0)` and updated `toString()` to dynamically resolve the subclass name via `getClass().getSimpleName()`.

## src/main/java/lotr/CharacterFactory.java
- **Bug fix:** Added `!Modifier.isAbstract(cls.getModifiers())` inside the constructor's reflection loop to filter out abstract classes (like `Character` and `RandomFighter`), preventing `InstantiationException` when creating random characters.
- **Edge case:** If no concrete classes extending `Character` are found in the package `lotr`, `characterClasses.size()` will be `0`, causing `rand.nextInt(0)` in `createCharacter()` to throw an `IllegalArgumentException`.

## src/main/java/lotr/Elf.java
- **Refactor:** Removed the redundant `toString()` method since the base `Character.toString()` now dynamically handles subclass names.

## src/main/java/lotr/GameManager.java
- **Bug fix:** Fixed a potential infinite loop in `fight()` by introducing a `MAX_TURNS = 100` limit (and printing a draw message if reached) which prevents fights between characters with 0 power (such as Hobbits) from running indefinitely.
- **Style:** Simplified the `main` method to instantiate and run the fight inline.

## src/main/java/lotr/Hobbit.java
- **Refactor:** Removed the redundant `toString()` method since the base `Character.toString()` now dynamically handles subclass names.

## src/main/java/lotr/King.java
- **Refactor:** Modified `King` to inherit from `RandomFighter` instead of `Character` directly, removing duplicated implementation of `randomValue`, `kick`, `toString`, and the `random` field.

## src/main/java/lotr/Knight.java
- **Refactor:** Modified `Knight` to inherit from `RandomFighter` instead of `Character` directly, removing duplicated implementation of `randomValue`, `kick`, `toString`, and the `random` field.

## src/main/java/lotr/RandomFighter.java
- **Refactor:** Extracted the common random combat logic and utility helper methods of `King` and `Knight` into a new abstract class `RandomFighter` that extends `Character` (adhering to the DRY principle).

## src/test/java/CharacterFactoryTest.java
- **Refactor:** Added unit test coverage to verify that `CharacterFactory.createCharacter()` successfully instantiates concrete character types and returns a valid `toString()` output.

## src/test/java/GameManagerTest.java
- **Refactor:** Added unit test coverage to ensure that fights between 0-power characters (such as Hobbits) terminate within a timeout limit of 2 seconds (safeguarding against future infinite loop regressions).