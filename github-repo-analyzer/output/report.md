## Summary
A Lord of the Rings themed Object-Oriented Programming (OOP) combat simulation in Java. It features character types such as Hobbits and Elves, utilizing Java Reflection to dynamically load characters in a combat game manager.

## Technologies
- Java 11
- Maven
- JUnit Jupiter
- Reflections Library

## Strengths
- Clean usage of inheritance and polymorphism with a base Character class and specific subclasses.
- Dynamic character discovery and loading using the Reflections library, making it easy to add new character types.
- Includes basic unit tests for validating character properties and kick logic.

## Issues
- Fields `power` and `hp` in `Character` class are public, exposing internals and violating encapsulation.
- The fight simulation in `GameManager` can run into an infinite loop if two Hobbits fight, as they only cry and do not deal damage.
- The main `Demo.java` entrypoint is a simple stub printing 'Hello, world!' and does not execute the combat game.

## Recommendations
- Make `power` and `hp` private or protected in the `Character` class to improve encapsulation.
- Introduce a maximum turn limit or a stalemate detection check in `GameManager.fight` to prevent infinite loops.
- Update `Demo.java` to run the combat simulation or remove the redundant entrypoint.
