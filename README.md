# Spring Boot Todo Webapp

A sample todo web application using **Spring Boot 2.7.18**, **Java 8**,
**Spring MVC**, **Thymeleaf**, and **Spring Data JPA / Hibernate** with an
embedded **H2** database. It renders HTML on the server without a JavaScript
framework.

## Requirements

- JDK 8 (or a newer JDK capable of compiling Java 8 source)
- Maven 3.6.3 or newer

## Run

```sh
mvn spring-boot:run
```

Open <http://localhost:8080>. Add tasks, mark them complete, undo completion,
and delete them. Titles are required and limited to 200 characters.

Tasks are stored in an **in-memory H2 database**, shared by all visitors, and
reset when the application restarts. Spring Boot configures the data source
and connection pool; Hibernate generates the schema from the `Todo` entity
using `spring.jpa.hibernate.ddl-auto=create-drop`. No external database setup
is required. The JDBC URL and JPA settings are configured in
`src/main/resources/application.properties`.

Open EntityManager in View is disabled: database operations run inside service
transactions rather than during template rendering. Schema creation/drop is
appropriate for this ephemeral sample, not for a persistent production database.

## Test and package

```sh
mvn test
mvn package
java -jar target/todo-webapp-0.0.1-SNAPSHOT.jar
```

Tests run against H2 and Hibernate and cover the todo lifecycle, entity
persistence and reloads, committed service transactions, input validation,
missing tasks, escaped HTML, rendered Thymeleaf pages, and static CSS. Tests
roll back database changes, except the commit test, which deletes its task
afterward.

## Structure

- `TodoController`: MVC routes and form validation.
- `TodoService`: transactional todo operations and title validation.
- `TodoRepository`: Spring Data JPA repository with ordered reads and
  parameterized JPQL for atomic completion toggles and deletion. Modifying
  queries flush and clear the persistence context to avoid stale entity state.
- `Todo`: JPA entity with database-generated IDs.
- `TodoForm`: validated form input.
- `src/main/resources/templates/index.html`: Thymeleaf page.
- `src/main/resources/static/css/styles.css`: responsive styling.
- `src/main/resources/application.properties`: in-memory database and Hibernate
  schema-generation configuration.

All state-changing routes use POST and successful changes redirect to the
home page to avoid accidental resubmission on refresh.

This intentionally uses the requested legacy stack. Spring Boot 2.x and Java 8
are not recommended defaults for new production systems. This sample has no
authentication or CSRF protection and is intended for local learning, not
public deployment.