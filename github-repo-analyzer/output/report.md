## Summary

A lightweight Java application simulating a Lord of the Rings combat game between characters such as Elves, Hobbits, Kings, and Knights. It demonstrates object-oriented programming concepts like polymorphism, inheritance, and the factory pattern dynamically driven by Java reflections.

## Technologies

- Java
- Maven
- JUnit 5
- Reflections Library

## Strengths

- Clean OOP design leveraging inheritance and polymorphism for character creation and combat simulation.
- Decoupled combat controller (`GameManager`) that works with any subclass of `Character`.
- Dynamic character discovery in `CharacterFactory` using the Reflections library, following the Open-Closed Principle.

## Issues

- Public field access in the `Character` base class (`power` and `hp` are public), violating proper encapsulation principles.
- Potential infinite game loop in `GameManager` if two low-power characters (like Hobbits) fight without dealing damage.
- Heavy external dependency (`org.reflections`) imported solely to perform simple class discovery.
- The `kick` method in the base `Character` class has a silent default empty implementation instead of being abstract or throwing an exception.

## Recommendations

- Encapsulate fields in the `Character` class by changing `power` and `hp` to private or protected and enforcing usage of getters/setters.
- Make the `Character` class and its `kick` method abstract to enforce implementation by all concrete character subclasses.
- Add a turn counter limit or timeout mechanism in `GameManager` to prevent infinite fight loops.
- Replace the Reflections library dependency with Java's native `ServiceLoader` or a manual static registry to improve start-up time and reduce dependencies.
- Decouple `GameManager` console prints by using a callback or listener pattern to allow modular UI integration.
