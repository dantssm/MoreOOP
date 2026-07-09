# Repository Analysis Report: MoreOOP

## Summary
MoreOOP is a Java-based combat simulation project inspired by Lord of the Rings characters. It is designed as a laboratory assignment for a university object-oriented programming course to demonstrate inheritance, polymorphism, and dynamic class loading using the Reflections library.

## Technologies
- Java 11
- Maven
- JUnit Jupiter 5.8.1
- org.reflections 0.10.2
- GitHub Actions (CI)

## Strengths
- **Good use of OOP principles**: Proper inheritance structure with `Character` base class and polymorphically overridden `kick` methods.
- **Dynamic character instantiation**: `CharacterFactory` uses the Reflections library to dynamically scan the classpath for character classes, making adding new characters seamless.
- **Unit Testing**: Contains automated unit tests for classes like `Elf` and `Hobbit` using JUnit 5.
- **CI Integration**: GitHub Actions workflow (`ci.yaml`) ensures all pushed code is built and tested automatically.

## Issues
- **Infinite Loop Hazard**: In `GameManager.fight`, if two `Hobbit` characters fight, they will loop indefinitely crying without dealing any damage.
- **Encapsulation Violation**: `power` and `hp` fields in `Character` are public, exposing class internals and bypassing getters/setters.
- **Non-abstract Base Class**: `Character` is not abstract and contains an empty `kick` implementation, allowing generic instantiations.
- **Reflections Overhead**: Classpath scanning at runtime is heavy and introduces a third-party dependency for a small, static set of character classes.
- **Insufficient Input Validation**: `setPower` does not validate or reject negative values.

## Recommendations
- Make the `Character` class `abstract` and define `kick(Character c)` as an `abstract` method.
- Encapsulate fields `power` and `hp` by making them `private` or `protected` and accessing them strictly via getters/setters.
- Introduce a turn-counter limit or draw condition in `GameManager.fight` to prevent infinite loops.
- Avoid runtime Reflections scanning by using static character registration or a simple factory mapping to remove the third-party dependency.
- Expand unit test coverage to include `King`, `Knight`, `CharacterFactory`, and `GameManager`.
