# Pull Request Review Report - PR #2 on dantssm/MoreOOP

This review evaluates the changes in PR #2, assessing correctness, design patterns, safety, and potential improvements.

## src/main/java/lotr/Character.java
- **Refactor**: Changed the class definition to `abstract` and updated the `kick` method signature to be `abstract` as well, enforcing that subclasses must implement their own kick logic. Encapsulated `power` and `hp` fields by changing their access modifier from `public` to `protected`.
- **Bug fix**: Enforced non-negative limits for `hp` and `power` in the setters using `Math.max(x, 0)` instead of direct assignments or custom conditional branching.
- **Style**: Simplified the `setHp` method structure and updated the `toString()` implementation to dynamically print the class name using `getClass().getSimpleName()` instead of a hardcoded `"Character"` string.
- **Suggestion**: Since the fields `power` and `hp` are now `protected`, subclasses can still bypass the setters (`setHp`/`setPower`) and modify the fields directly, which could lead to negative values. Consider making them `private` and forcing subclass interactions through the validated setters/getters.

## src/main/java/lotr/CharacterFactory.java
- **Bug fix**: Filtered out abstract classes from reflections to prevent `InstantiationException` when randomly instantiating subclasses of `Character` (especially since `Character` and `RandomFighter` are now abstract).
- **Edge case**: If no concrete classes of `Character` are found in the package, `characterClasses.size()` will be `0`, causing `rand.nextInt(0)` to throw an `IllegalArgumentException`.
- **Suggestion**: Add a check to ensure `characterClasses` is not empty before picking a random character, and handle the empty state gracefully. Also, instantiate `Random` as a single class member or static field rather than recreating it on every invocation of `createCharacter()` to improve efficiency.

## src/main/java/lotr/Elf.java
- **Refactor**: Removed the redundant `toString()` method since `Character` now dynamically formats the string with the correct class name.
- **Bug**: In the `kick(Character c)` method, `c.setHp(c.hp - 1)` accesses the `hp` field of `c` directly (`c.hp`) rather than calling its getter `c.getHp()`. Direct field access bypasses encapsulation and can lead to bugs if getter logic changes.
- **Suggestion**: Use the getter `c.getHp()` in the kick logic: `c.setHp(c.getHp() - 1)`.

## src/main/java/lotr/GameManager.java
- **Bug fix**: Implemented a `MAX_TURNS` counter and turn limit check in the `fight` loop to prevent infinite loops when two zero-power or low-power characters fight.
- **Refactor**: Simplified the `main` method execution by instantiating and calling `fight` anonymously (`new GameManager().fight(c1, c2)`) instead of using a local variable.
- **Suggestion**: `MAX_TURNS` is currently a private hardcoded constant. Consider making the turn limit configurable via a constructor parameter or setter to allow easier testing and flexibility.

## src/main/java/lotr/Hobbit.java
- **Refactor**: Removed the redundant `toString()` override as the base class now provides it.
- **Suggestion**: Clean up the formatting and extra blank lines around the closing bracket of the class to match project code style standards.

## src/main/java/lotr/King.java
- **Refactor**: Refactored `King` to extend the new `RandomFighter` base class, inheriting the randomized kick logic, constructor parameter generation, and the common string representation, thereby eliminating significant code duplication.
- **Suggestion**: No further concerns. The refactoring is clean and correct.

## src/main/java/lotr/Knight.java
- **Refactor**: Refactored `Knight` to inherit from `RandomFighter`, removing duplicated fields, `randomValue` helper method, `kick` method, and `toString` representation.
- **Suggestion**: Define the range values (2 and 12) as constants (`MIN_POWER`, `MAX_POWER`, etc.) rather than passing magic numbers directly to `super()`.

## src/main/java/lotr/RandomFighter.java
- **Refactor**: Created a new abstract class `RandomFighter` that consolidates shared randomized fight characteristics for classes like `King` and `Knight`.
- **Bug**: In `kick(Character c)`, if `this.power` is somehow negative, `random.nextInt(this.power + 1)` will throw an `IllegalArgumentException`. Although `Character` limits power to >= 0, it is safer to prevent negative bounds explicitly.
- **Suggestion**: Safe-guard the randomized damage generation: `int damage = this.power > 0 ? random.nextInt(this.power + 1) : 0;` to avoid potential runtime exceptions.

## src/test/java/CharacterFactoryTest.java
- **Bug fix**: Added basic JUnit tests for the `CharacterFactory` class to verify character instantiation.
- **Suggestion**: The test class is in the default package, while the production code is in the `lotr` package. It is better to place the test class inside a package structure that mirrors the main code (i.e. `package lotr;` in `src/test/java/lotr/CharacterFactoryTest.java`) to maintain consistency and allow package-private access if needed.

## src/test/java/GameManagerTest.java
- **Bug fix**: Added a test asserting that a fight between two `Hobbit` characters (0 power) terminates within 2 seconds, which validates the turn-limiting bug fix in `GameManager`.
- **Suggestion**: Move the test class from the default package into the `lotr` package to match standard Java conventions.