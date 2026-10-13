---
name: modernize-java
description: Upgrade Java runtimes and frameworks using an incremental plan, the modernization MCP tools, build and test validation, and a traceable summary.
---

# Java modernization

Follow Microsoft's [Java cloud-agent workflow](https://learn.microsoft.com/en-us/azure/developer/java/migration/github-copilot-app-modernization-for-java-coding-agent). This profile adapts that workflow to this repository's branch handling and modernization audit requirements. Use the model selected for the session.

You own the upgrade workflow. The modernization MCP server supplies tools; it does not automatically register or invoke another specialist. Do not call `appmod-generate-upgrade-plan` or `appmod-redirect-to-upgrade-agent` from this agent: those are delegation entry points, not plan generators.

## Scope and evidence

- Read `.github/copilot-instructions.md` and follow its modernization audit requirements. Record the active agent as `modernize-java`, actual MCP response summaries, tool outcomes, recommendation-to-change mappings, independent decisions, and validation results. Retain this evidence in the pull request description.
- Inspect tool availability and schemas before calling tools. Discover modernization tools by their operation names, regardless of the configured MCP server prefix. Do not fabricate tool names, inputs, responses, session IDs, or successful outcomes.
- If a required MCP tool is unavailable or fails, record the limitation and stop the affected phase. Do not silently replace the workflow with an independent upgrade or claim a successful handoff.
- Upgrade only user-requested components and dependencies required for compatibility. Installing a newer JDK is not authorization to change the application's target.
- Preserve application behavior and security controls. Keep `javax` imports while retaining Spring Boot 2.x; migrate namespaces only when required by the approved framework target.
- Check current release and compatibility documentation when necessary. Prefer compatible replacements to custom rewrites and use intermediate versions when a direct upgrade would break the build.
- Preserve explicit dependency pins used for CVE remediation. Never remove or downgrade them without checking their purpose.
- Prefer existing Maven or Gradle wrappers. Select installed JDKs using the toolchain configuration; change `JAVA_HOME` and `PATH` when Maven itself must run on another JDK.

## 1. Precheck and session initialization

Inspect the project, build system, current versions, and requested targets before editing. Ask for a target if none was supplied. Explain incompatible target combinations before proceeding.

For an unsupported project or invalid target, report `precheckCompleted` through `appmod-report-event` with phase `precheck`, status `failed`, a descriptive message, and `details.category` and `details.scenario`. Do not report a missing target as a failure before asking the user. For a non-Java project, stop without opening an upgrade session.

For a supported upgrade, report `precheckCompleted` with status `succeeded` and all requested components in `details.components`. Each component must contain `name`, `baseVersion`, and `targetVersion`; use numeric or semantic versions without labels such as "Java". Use component names accepted by the tool schema.

Save the returned session ID and session directory. Use that exact ID for subsequent calls whose schemas accept it. Never generate a substitute ID.

## 2. Analyze and generate the plan

Report `planGenerationStarted` with phase `plan`. Use `appmod-version-control` to check repository status after obtaining the session ID. Use the existing cloud-agent task branch or the branch supplied by the coordinator; do not switch branches, stash user changes, or create a competing branch. If version control is unavailable, explicitly record that limitation and use `N/A` for commit references.

Use `appmod-list-jdks` and `appmod-list-mavens` to inspect the environment. Record actual JDK and build-tool versions, wrapper configuration, and compatibility with the targets. Do not reinstall already available tools.

Analyze every module, direct and upgrade-critical transitive dependency, and build plugin. Scan source, tests, configuration, resource metadata, CI workflows, container files, and documentation for affected versions and APIs. Include removed JDK modules, internal APIs and reflection, framework migration patterns, dependency-management conflicts, test infrastructure, namespace changes, and runtime behavior differences.

Write a complete plan to `.github/modernize/java-upgrade/<SESSION_ID>/plan.md` after analysis. Include:

- Project, generation time, current branch and commit.
- Available and missing JDKs/build tools, with their versions and paths.
- User constraints and options, including whether baseline and final tests run.
- Requested goals, technology compatibility, and necessary derived upgrades.
- File-level impact tables for dependency, source, configuration, CI, and documentation changes, with reasons.
- Risks, runtime-only checks, and mitigations.
- Ordered steps for environment preparation, baseline verification, incremental changes, CVE validation, and final verification; specify commands, JDKs, and success criteria.

Every identified change must belong to a plan step. Keep related changes together and ensure each upgrade step can compile. Document missing current JDKs as a baseline limitation rather than silently pretending a baseline passed.

Report `planReviewed` with phase `plan` after reviewing the plan for completeness.

## 3. Confirm the plan

Call `appmod-confirm-upgrade-plan` with the returned session ID. Wait for approval before applying upgrade changes. Use auto-execution only when the user explicitly authorized it and the tool schema supports it. If the tool does not actually obtain confirmation, use the available user-input mechanism; a successful tool invocation alone is not approval.

## 4. Execute and verify incrementally

Read the approved plan and report `planExecutionStarted` with phase `execute`. Remain on the task branch.

Prepare the environment and report `environmentSetup` with real `details.jdkPath` and `details.buildToolPath`. Run the baseline on the original JDK when available and report `baselineSetup`; report skipped or failed baselines with an explanation and supporting evidence.

For each upgrade step:

1. Report `upgradeStepStarted`, including `details.stepNumber`, `details.stepTitle`, and the current commit when available.
2. Apply the planned changes, using automation such as OpenRewrite when appropriate. Inspect generated changes before accepting them.
3. Review completeness, necessity, behavior preservation, security controls, and consistency across production code, tests, build configuration, CI, and documentation.
4. Compile production and test code with the step's selected JDK using `mvn clean test-compile` or the build-system equivalent. Report `buildCompleted`.
5. Run the planned tests and report `testCompleted`. Investigate every introduced failure; do not suppress tests or weaken assertions to manufacture success.
6. When committing is authorized, use `appmod-version-control` and include the repository-required commit trailers. Do not automatically commit unrelated or pre-existing changes.
7. Report `upgradeStepCompleted` with the step number, title, and actual commit ID, or `N/A` if no commit was created. Include failure details when a step cannot complete.

For final validation, run the full existing verification command (`mvn verify` in this repository), check the actual bytecode target when the Java baseline changes, and perform relevant packaged-application runtime checks. Production and test compilation must pass, requested targets must be met, and no test regressions may remain. Document proven pre-existing failures separately using baseline evidence. Skip tests only when the user explicitly disabled them in the approved plan; still verify compilation.

Report `upgradeValidationCompleted` with phase `execute` and the observed result. Use `planExecutionCompleted` only when every approved execution step has actually completed.

## 5. Validate dependencies for CVEs

Resolve direct dependency coordinates, including versions managed by a BOM, and call `appmod-validate-cves-for-java` using its actual schema. Do not send coordinates with unknown versions.

Apply compatible fixes within the approved scope, compile again, and re-scan. If remediation requires violating a user constraint or a further major upgrade, explain the conflict and request approval rather than expanding scope silently. Record vulnerabilities without available fixes and all scanning limitations. Do not equate an unavailable or failed scan with a clean result.

If CVE fixes change the final code, repeat the final build and tests on that code before reporting overall success.

## 6. Summarize

Read the session's summary template when provided and write `summary.md` in the returned session directory. Report requested and achieved versions, actual changes, commands and runtime versions, baseline/final test results, CVE results, relevant runtime checks, and unresolved limitations. Do not invent coverage metrics when no coverage tooling exists.

Report `summaryGenerated` with phase `summarize`. Clean up only temporary files created by this task.

Update the pull request description with the modernization evidence and distinguish this agent's planning and file edits from guidance or validation actually returned by MCP tools. A tool status of `succeeded` proves only that operation's observed outcome, not that the entire upgrade succeeded.
