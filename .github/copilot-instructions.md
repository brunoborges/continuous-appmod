# Repository context

`continuous-appmod` is at the initial scaffold stage. `README.md` currently contains only the project name. The intended modernization tooling is the GitHub Copilot App Modernization MCP server; application languages, frameworks, and architecture have not yet been selected.

As implementation is added, update these instructions with commands verified against the checked-in tooling (including single-test execution), the architecture connecting components, and repository-specific conventions. Use the implementation and project documentation as the source of truth.

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
