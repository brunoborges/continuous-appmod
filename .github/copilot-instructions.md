# Repository context

`continuous-appmod` contains a sample Java 8 todo application built with Spring Boot 2.7.18, Spring MVC, Thymeleaf, Spring Data JPA, Hibernate, and an in-memory H2 database. This deliberately legacy stack provides a baseline for later modernization.

## Application commands and architecture

- Run locally: `mvn spring-boot:run`, then open `http://localhost:8080`.
- Build and run all tests: `mvn verify`.
- Run a single test class: `mvn -Dtest=TodoServiceTest test`.
- Run the packaged application: `java -jar target/todo-webapp-0.0.1-SNAPSHOT.jar`.

Use Java 8-compatible syntax and APIs and `javax.persistence` / `javax.validation` imports for the current Spring Boot 2.x baseline.

`TodoController` validates `TodoForm` input and delegates to transactional `TodoService` methods. `TodoRepository` persists the `Todo` JPA entity; modifying JPQL queries perform atomic completion toggles and deletion and flush/clear the persistence context. Thymeleaf renders `templates/index.html`, with CSS under `static/css/`.

Hibernate generates the H2 schema with `ddl-auto=create-drop`; tasks disappear on restart. Open EntityManager in View is disabled. Tests use isolated in-memory databases and roll back changes, except the commit test, which explicitly cleans up its task.

Update these instructions as the implementation evolves. Use the implementation and project documentation as the source of truth.

## Modernization tooling

Use the [GitHub Copilot App Modernization MCP server](https://www.npmjs.com/package/@microsoft/github-copilot-app-modernization-mcp-server) for application modernization tasks. It provides tools for analyzing and upgrading Java and .NET applications and migrating them to Azure; this does not establish either language or Azure as the repository's application stack.

Configure the server in this GitHub repository's **Settings > Copilot > MCP servers > MCP configuration**, following [GitHub's repository MCP configuration guide](https://docs.github.com/en/copilot/how-tos/copilot-on-github/customize-copilot/configure-mcp-servers). A repository administrator must save the configuration; adding these instructions alone does not enable the server.

The server requires Node.js LTS with `npm` and `npx` on `PATH` in the agent environment. Add this entry to the repository's MCP configuration, preserving any existing server entries:

```json
{
  "mcpServers": {
    "ghcp-appmod-mcp-server": {
      "type": "local",
      "command": "npx",
      "args": [
        "-y",
        "@microsoft/github-copilot-app-modernization-mcp-server@latest"
      ],
      "tools": ["*"]
    }
  }
}
```

Click **Save MCP configuration**. This repository-level configuration is shared by Copilot cloud agent and Copilot code review. The example enables all server tools; review that scope before saving because the cloud agent can invoke enabled tools autonomously. Confirm that the server's tools are available in the current session before attempting to use them.

The package enables telemetry by default. To opt out, add `"env": {"APPMOD_MCP_COLLECT_TELEMETRY": "false"}` to the server entry.
