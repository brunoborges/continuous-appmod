---
name: 'modernize-java'
description: 'Plan and execute incremental Java, Spring Boot, and related dependency upgrades to user-specified target versions. Use when asked to modernize or upgrade a Java application, including from a general-purpose agent.'
---

Use this skill in the current agent session to upgrade to user-specified target versions by (1) generating an incremental plan and (2) executing it per the rules below. Loading the skill does not change the active agent or model and does not require a specialist handoff.

## Entry Requirements

- Read `.github/copilot-instructions.md` and follow its **Modernization audit evidence** requirements after every modernization MCP call, before and after edits, and for validation and the final report or PR description.
- Discover the configured App Modernization MCP tools and their actual schemas before calling them. The `#appmod-*` names below identify MCP tools, not shell commands; use the names and arguments exposed by the current host.
- Record the active agent, loaded skill, and required tool availability. If the server or a required tool is unavailable, report the limitation explicitly and stop the affected workflow; do not fabricate responses or claim an MCP-guided upgrade.
- Do not call `appmod-generate-upgrade-plan` or `appmod-redirect-to-upgrade-agent`. These request a specialist handoff rather than generating a plan. Generate the plan here using the workflow below and the available MCP tools; do not delegate just to use this skill.
- Preserve explicit user targets and constraints. Do not upgrade unrelated frameworks or dependencies unless required for compatibility. If a vulnerability fix would violate a pinned target, report the conflict and obtain approval before changing that target.

## Rules

### Upgrade Success Criteria (ALL must be met)

- **Goal**: All user-specified target versions met.
- **Compilation**: Both main source code AND test code compile successfully = `mvn clean test-compile` (or equivalent) succeeds. This includes compiling production code and all test classes.
- **Test**: **100% test pass rate** = `mvn clean test` succeeds. Minimum acceptable: test pass rate ≥ baseline (pre-upgrade pass rate). Every test failure MUST be fixed unless proven to be a pre-existing flaky test (documented with evidence from baseline run). **Skip if user set "Run tests before and after the upgrade: false" in plan.md Options.**

### Anti-Excuse Rules (MANDATORY)

- **NO premature termination**: Token limits, time, or complexity are NEVER valid reasons to skip fixing test failures.
- **NO "close enough"**: 95% is NOT 100%. Every failing test requires a fix attempt.
- **NO deferred fixes**: "Fix post-merge" or "TODO later" are NOT acceptable. Fix NOW.
- **NO categorical dismissals**: ALL tests must pass regardless of category.
- **Genuine limitations ONLY**: Valid only if multiple fix approaches were attempted, root cause identified, and fix is technically impossible.

### Review Code Changes (MANDATORY for each step)

After completing changes in each step, review code changes per the **Review Code Changes** section below BEFORE verification. Key areas:

- **Sufficiency**: all required upgrade changes are present
- **Necessity**: no CRITICAL unnecessary changes — Unnecessary changes that do not affect behavior may be retained; however, it is essential to ensure that the functional behavior remains consistent and security controls are preserved.

### Upgrade Strategy

- **Incremental upgrades**: Stepwise dependency upgrades; use intermediates to avoid large jumps breaking builds.
- **Minimal changes**: Only upgrade dependencies essential for compatibility with target versions.
- **Risk-first**: Handle EOL/challenging deps early in isolated steps.
- **Necessary/Meaningful steps only**: Each step MUST change code/config. NO steps for pure analysis/validation. Merge small related changes. **Test**: "Does this step modify project files?"
- **Automation tools**: Use automation tools like OpenRewrite etc. for efficiency; always verify output.
- **Successor preference**: Compatible successor > Adapter pattern > Code rewrite.
- **Build tool compatibility**: Check Maven/Gradle version compatibility with target JDK. Upgrade build tool (including wrapper) if needed. Gradle 8.5+ for Java 21, Gradle 9.1+ for Java 25. Maven 3.9+ recommended.
- **Kotlin version compatibility (MANDATORY)**: When upgrading JDK, also upgrade Kotlin if used. Minimum: **Kotlin 1.9.20+** for Java 21, **Kotlin 2.3.0+** for Java 25. Update `<kotlin.version>` property (controls stdlib, test, and plugins).
- **Temporary errors OK**: Steps may pass with known errors if resolved later or pre-existing.
- **CVE version pin protection**: Before removing/downgrading any explicit `<version>` override, verify it's not a CVE-driven pin (check for nearby CVE comments or if pinned version is newer than BOM-managed). When in doubt, keep the override.

### Execution Guidelines

- **Wrapper preference**: Use Maven Wrapper (`mvnw`/`mvnw.cmd`) or Gradle Wrapper (`gradlew`/`gradlew.bat`) when present in the project root, unless user explicitly specifies otherwise. This ensures consistent build tool versions across environments.
- **Version control via tool**: 🛑 NEVER use direct `git` commands — ONLY use `#appmod-version-control` for ALL version control operations. **ALWAYS pass `sessionId: <SESSION_ID>`**. When `GIT_AVAILABLE=false`, skip ALL version control operations and use `N/A` for branch/commit placeholders.
- **Version control timing**: `#appmod-version-control` requires `SESSION_ID` which is only available after Phase 1 (Precheck) succeeds. Do NOT use `#appmod-version-control` during Precheck. Git availability detection is deferred to Phase 2 Initialize.
- **Template compliance**: For `plan.md`, follow the **Plan Format Specification** below and write the complete file in a single file-creation operation using the host's available editing tool (for example, `apply_patch` or `create_file`). For `summary.md`, read `summary.template.md` (in the session directory) as a spec, then write `summary.md` as a new file using the available editing tool. If the template is unavailable, report that limitation and use the plan, observed results, and repository audit requirements; do not invent template contents.
- **Uninterrupted run**: Complete each phase fully without pausing for user input, except for the mandatory user confirmation after plan generation (Phase 3).
- **User input**: Prefer the ask tool (`#askQuestions`, `#ask_user`, or `#ask_questions`) when available to collect user input (e.g., choices, confirmations). Fall back to plain-text prompts only when none is available.

### Event Reporting (MANDATORY)

Call `#appmod-report-event` immediately at each key milestone. **NO skipping. NO batching. This is non-negotiable.**

- **When**: Report at every milestone defined in the Workflow phases — do not wait until the end of a phase.
- **Details**: Pass `details` for `precheckCompleted` (on both success and failure — see Phase 1), `environmentSetup`, `upgradeStepStarted`, and `upgradeStepCompleted`.
- **Status values**: `"succeeded"` | `"failed"` (must include `message`) | `"skipped"` (must include `message`).
- **Reporting vs. evidence**: Avoid narrating routine telemetry in progress messages. Still record every MCP call, including event-reporting calls, in the repository's required sanitized audit evidence. A telemetry acknowledgment is not application analysis or proof of an upgrade.

### Efficiency

- **Targeted reads**: Use `grep` over full file reads; read sections, not entire files.
- **Quiet commands**: Use `-q`, `--quiet` for build/test when appropriate.
- **Single write for plan.md**: Generate the complete `plan.md` in one file-creation operation after gathering all information. Do NOT make multiple edits.

### Session ID Consistency (CRITICAL)

- `SESSION_ID` is generated in Phase 1 (Precheck) on success. Use this **exact** ID for ALL subsequent tool calls — never fabricate or change it.

### Branch Handling

- Use the current task branch, whether this skill is loaded by a general-purpose agent or another agent. Record it via `#appmod-version-control` after Precheck succeeds.
- Do not switch branches, create a replacement branch, or stash existing changes merely because the skill was loaded. Preserve and integrate user changes; ask if they conflict with the upgrade.
- Do not assume a coordinator prepared the workspace. If the user explicitly requests another branch, obtain any necessary approval and use the MCP version-control tool. Never fabricate a branch or commit ID.

### Intermediate Version Strategy

Use intermediates **when direct upgrade risks breaking builds**. A good intermediate is a stable LTS release that bridges compatibility between current and target versions.

**Example**: Spring Boot 2.7.x bridges `1.x → 3.x` (supports Java 8-21, uses javax.servlet with migration path to jakarta).

### Version Knowledge

LLM training data may be outdated. **Never reject a target version solely based on training data.**

- Known Java LTS: 11, 17, 21, 25. Spring Boot stable: 2.7.x, 3.5.x, 4.0.x
- For unrecognized versions: use `fetch` tool to verify before rejecting.

## Plan Format Specification

Generate the **complete `plan.md`** in a single file-creation operation to `.github/modernize/java-upgrade/<SESSION_ID>/plan.md`. Structure:

### Plan Header

    # Upgrade Plan: <PROJECT_NAME> (<SESSION_ID>)

    - **Generated**: <actual date and time>
    - **HEAD Branch**: <branch from git status, or "N/A" if GIT_AVAILABLE=false>
    - **HEAD Commit ID**: <HEAD commit, or "N/A" if GIT_AVAILABLE=false>

### Section: Available Tools

List ONLY JDKs and build tools required/used. Use `#appmod-list-jdks` and `#appmod-list-mavens` to check availability. Mark missing as `**<TO_BE_INSTALLED>**`. Exception: base JDK not found → note as "not available (baseline will be skipped)", do NOT mark as TO_BE_INSTALLED. Mark build tools needing upgrade as `**<TO_BE_UPGRADED>**`.

**Build tool compatibility**:
- Maven 3.9+: recommended (3.8.x EOL). Gradle 8.5+: Java 21. Gradle 9.1+: Java 25.
- Kotlin 1.9.20+: Java 21. Kotlin 2.3.0+: Java 25. Update `<kotlin.version>` property.

### Section: Guidelines

User-specified constraints in bullet points. Always include:

    > Note: You can add any specific guidelines or constraints for the upgrade process here if needed, bullet points are preferred.

### Section: Options

    ## Options

    - Working branch: <current task branch, or N/A if GIT_AVAILABLE=false>
    - Run tests before and after the upgrade: true

### Section: Upgrade Goals

List ONLY user-requested target versions.

### Section: Technology Stack

Table of core dependencies and compatibility. Columns: Technology/Dependency | Current | Min Compatible Version | Why Incompatible. Flag EOL deps with "⚠️ EOL".

### Section: Derived Upgrades

Required upgrades inferred from user targets. Common derivations:
- Spring Boot 3.x → Java 17+, Jakarta EE 9+, Hibernate 6.x, Spring Framework 6.x
- Spring Boot 4.x → Java 17+, Jakarta EE 10+, Spring Framework 7.x
- Java 21 → Gradle 8.5+, Kotlin 1.9.20+ (if used)
- Java 25 → Gradle 9.1+, Kotlin 2.3.0+ (if used)
- Build tool upgrade → update wrapper version

### Section: Impact Analysis

**Core of the plan.** Complete file-level specification of every change. Subsections:

- **Dependency Changes**: Table — File | Dependency | Current | Action (`upgrade`/`replace`/`remove`/`add`) | Target | Reason
- **Source Code Changes**: Table — File | Location | Current | Required Change | Reason
- **Configuration Changes**: Table — File | Property/Setting | Current | Required Change | Reason (omit if none)
- **CI/CD Changes**: Table — File | Location | Current | Required Change
- **Risks & Warnings**: Non-trivial rewrites, runtime-only issues, CVE-pinned versions. Include mitigation for each.

### Section: Upgrade Steps

Format: `Step N: <Title>` with Rationale, Changes to Make (reference Impact Analysis), Verification (command, JDK, expected result).

**Step design rules**: Every step must compile. Reference Impact Analysis, don't duplicate. Fewer, coarser steps.

**Mandatory steps:**
- **Step 1**: Setup Environment — Install required JDKs/build tools marked `<TO_BE_INSTALLED>`.
- **Step 2**: Setup Baseline — Run baseline with current JDK if available; skip if not.
- **Steps 3-N**: Upgrade steps — apply Impact Analysis changes. Verify with `mvn clean test-compile -q`.
- **CVE Validation & Fix**: Scan with `#appmod-validate-cves-for-java`, fix all CVEs, re-scan.
- **Final step**: Final Validation — Verify all goals, resolve TODOs, full test suite, iterative fix until 100% pass. Skip tests if disabled in Options.

## Workflow

### Phase 1: Precheck

| Category            | Scenario                        | Action (use the ask tool (`#askQuestions`, `#ask_user`, or `#ask_questions`) when available and appropriate)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
|---------------------|---------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Unsupported Project | Not a Java project              | Do NOT call `#appmod-report-event`. Stop this workflow and inform the user that the modernize-java skill only supports Java projects. The current agent can still handle other tasks without this skill. |
| Unsupported Project | Not a Maven/Gradle project      | Check for alternative build systems: look for `build.xml` (Ant), `BUILD`/`BUILD.bazel` (Bazel), or other build files. If detected, call `#appmod-report-event` with details, then inform the user: "Detected [Ant/Bazel/other] build system. Maven and Gradle are fully supported; [Ant/Bazel/other] support is experimental and results may vary." Attempt to continue with best-effort analysis. If no recognizable build system is found, call `#appmod-report-event`, then STOP with error listing supported build systems (Maven, Gradle).                                                           |
| Invalid Goal        | Missing target version          | Do NOT call `#appmod-report-event` yet. Instead, analyze project dependencies (read `pom.xml`/`build.gradle` to detect current Java version, Spring Boot version, and other key deps), derive feasible upgrade options (e.g., Java 17, Java 21, Java 25, Spring Boot 3.2, Spring Boot 3.5, Spring Boot 4.0), and use the ask tool (`#askQuestions`, `#ask_user`, or `#ask_questions`) to present those options as selectable choices for the user to pick the desired target(s). Only report `precheckCompleted` (succeeded or failed) after the user has selected a target or the interaction concludes. |
| Invalid Goal        | Incompatible target combination | Call `#appmod-report-event`, then STOP and explain incompatibility                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |

**On failure**: → `#appmod-report-event(event: "precheckCompleted", phase: "precheck", status: "failed", details: {category: "<category>", scenario: "<scenario>"}, message: "<what failed and why>")` — **Call this FIRST** before stopping or asking users. Pass the failed category (e.g., "Unsupported Project", "Invalid Goal") and scenario from the table above. **IMPORTANT**: `details.category` and `details.scenario` are **REQUIRED** when status is "failed" — the tool will reject the call without them. **Exception**: For the "Missing target version" scenario, do NOT report failure immediately — interact with the user first (see table above) and only report `precheckCompleted` (succeeded or failed) after the user has selected a target or the interaction concludes.

**On success**: → `#appmod-report-event(event: "precheckCompleted", phase: "precheck", status: "succeeded", details: {components: [{name: "<component>", baseVersion: "<current version>", targetVersion: "<target version>"}, ...]})` — **This generates a new `SESSION_ID`. Use this `SESSION_ID` for all subsequent tool calls.**

Pass ALL detected upgrade components in a single call. Valid component names: `jdk`, `spring-boot`, `spring-framework`, `jakarta-ee`, `quarkus`, `micronaut`, `azure-sdk`, `unknown` (use `unknown` for any framework not in this list).

Examples:
- JDK-only upgrade: `details: {components: [{name: "jdk", baseVersion: "17", targetVersion: "21"}]}`
- JDK + Spring Boot: `details: {components: [{name: "jdk", baseVersion: "17", targetVersion: "21"}, {name: "spring-boot", baseVersion: "2.7.18", targetVersion: "3.5.0"}]}`

**IMPORTANT**: `baseVersion` and `targetVersion` must be **numeric or semver only** (e.g., "17", "21", "2.7.18", "3.5.0"). Do NOT include prefixes like "Java" or "Spring Boot".

### Phase 2: Generate Upgrade Plan

#### 1. Initialize & Analyze

1. Call tool `#appmod-report-event(sessionId, event: "planGenerationStarted", phase: "plan", status: "succeeded")` — **FIRST action, before any file or version control operations**
2. **Detect version control availability**: Use `#appmod-version-control(sessionId: <SESSION_ID>, workspacePath, action: "checkStatus")` to detect if git is available. If the response indicates version control is unavailable, set `GIT_AVAILABLE=false`. **Do not ask the user. Do not report failure.**
3. Record the current task branch and existing changes from the status response. Follow **Branch Handling**; do not stash changes or create a branch by default.
4. **Project environment**: Extract user-specified guidelines. Detect all available JDKs/build tools via `#appmod-list-jdks(sessionId)`, `#appmod-list-mavens(sessionId)`. Detect wrapper presence and read wrapper properties if present. Check build tool version compatibility with target JDK — flag incompatible versions.
5. **Technology stack analysis**: Identify core tech stack across **ALL modules** — direct deps, upgrade-critical transitive deps, build tools, and build plugins (`maven-compiler-plugin`, `maven-surefire-plugin`, `maven-war-plugin`, etc.). Flag EOL dependencies. Determine compatibility against upgrade goals.
6. **Compatibility scan**: Perform a comprehensive scan for all upgrade-blocking patterns.

    **What to find:**

    | Dimension                    | What to look for                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
    |------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
    | JDK source compatibility     | Reflection into java.base internals (`setAccessible` on JDK classes), internal package imports (`sun.misc.*`, `sun.reflect.*`, `jdk.internal.*`), removed/deprecated APIs for the target JDK version, JDK-removed modules needing explicit deps (JAXB, javax.activation, etc.)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
    | Framework breaking changes   | For each framework major version jump: fetch the official migration guide, extract all breaking change patterns (removed/renamed classes, changed package namespaces, removed config properties, deprecated-then-removed APIs, changed defaults), then search source code and config files for every pattern found                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
    | CI/CD configuration          | All CI/CD files (Dockerfile, workflows, pipelines, Jenkinsfile, etc.) with hardcoded JDK/Java version references that need updating                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
    | Build plugin compatibility   | Build plugins incompatible with the target JDK version                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
    | Dependency version conflicts | Explicit version pins/overrides in dependency management that conflict with the target framework's BOM                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
    | Test infrastructure          | Test framework compatibility with target JDK/framework (JUnit 4→5 migration, Mockito version compatibility, Spring Test API changes, test utility class removals/renames)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
    | Configuration properties     | Renamed, removed, or restructured config properties in application.properties/application.yml (e.g., Spring Boot 2→3 renames like `spring.redis.*` → `spring.data.redis.*`, removed properties, changed defaults)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
    | Runtime behavior changes     | JDK runtime behavior differences that compile but fail at runtime: serialization format changes, default locale/charset changes, `HashMap`/`HashSet` iteration order assumptions, `SecurityManager` removal (JDK 17+), strong encapsulation of internal APIs                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
    | Resource/metadata files      | Framework metadata files that changed format or location (e.g., Spring Boot 3: `META-INF/spring.factories` → `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`), `META-INF/services` entries, `persistence.xml`, `web.xml`, and other descriptor files                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
    | Post-upgrade consistency     | Artifacts that become stale or inconsistent after dependency/namespace changes: (1) **Descriptor namespace alignment** — XML descriptors (`web.xml`, `persistence.xml`, etc.) whose namespace/version must match the migrated platform (e.g., `java.sun.com/xml/ns/javaee` → `jakarta.ee/xml/ns/jakartaee` + schema version bump when javax→jakarta migration is performed); (2) **Orphaned configuration** — config files, directories, and property files belonging to removed/replaced dependencies (e.g., Dandelion config dirs after Dandelion removal, EhCache XML after switching to Caffeine); (3) **Documentation drift** — README, CONTRIBUTING, and other project docs referencing old Java/framework versions, removed dependencies, or outdated build/run/deploy instructions |

    **Output requirement**: For each finding, record: file, line/location, current state, what needs to change, and why. Every finding must appear in the plan's Impact Analysis section. No known findings may be deferred to execution. Document known unknowns (e.g., transitive dependency conflicts only discoverable after version changes, runtime-only reflection issues) in Risks & Warnings with mitigation strategies.

#### 2. Design & Review

1. For incompatible deps in the Technology Stack, prefer: Replacement > Adaptation > Rewrite
2. Determine intermediate versions needed (see **Intermediate Version Strategy**)
3. Finalize Available Tools based on the planned step sequence; determine which JDK versions are required and at which steps; mark missing ones as `<TO_BE_INSTALLED>`, mark build tools needing upgrade as `<TO_BE_UPGRADED>` (including wrapper version if applicable). **Exception — base (current) JDK**: If the project's current JDK version is not found via `#appmod-list-jdks`, do **not** mark it as `<TO_BE_INSTALLED>`. The base JDK is only needed for the optional baseline step. Instead, note it as "not available (baseline will be skipped)".
4. Design upgrade steps — group related changes so each step leaves the project in a compilable state. No step should expect compilation failure. Reference Impact Analysis items rather than repeating details.
5. **Self-verify completeness**: Every finding from the Compatibility Scan must appear in the Impact Analysis section. Every item in Impact Analysis must be addressed by an Upgrade Step. If gaps are found, go back and fill them.
6. **Write complete `plan.md`** to `.github/modernize/java-upgrade/<SESSION_ID>/plan.md` using the available editing tool — follow the **Plan Format Specification** above. Include all sections (Available Tools, Guidelines, Options, Upgrade Goals, Technology Stack, Derived Upgrades, Impact Analysis, Upgrade Steps) in a single write. If `GIT_AVAILABLE=false`, use "N/A" for branch/commit and include a notice about version control.
7. Verify all placeholders are filled, check for missing coverage/infeasibility/limitations. If issues found, rewrite the file.
8. Call tool `#appmod-report-event(sessionId, event: "planReviewed", phase: "plan", status: "succeeded")`

### Phase 3: Confirm Plan with User (MANDATORY)

1. Call tool `#appmod-confirm-upgrade-plan(sessionId)` — awaits user confirmation

### Phase 4: Execute Upgrade Plan

#### 1. Initialize

1. Read `.github/modernize/java-upgrade/<SESSION_ID>/plan.md` for "Options"
2. **Branch setup**:
   - Use `#appmod-version-control(sessionId: <SESSION_ID>, workspacePath, action: "checkStatus")` to confirm the current branch matches the approved plan. Follow **Branch Handling**; do not switch, stash, or create a branch by default. If it no longer matches, stop and clarify before editing.
   - If version control is unavailable (`GIT_AVAILABLE=false`), log warning in `plan.md` that changes are not version-controlled.
3. Call tool `#appmod-report-event(sessionId, event: "planExecutionStarted", phase: "execute", status: "succeeded")`

#### 2. Execute:

For each step:

1. Read `.github/modernize/java-upgrade/<SESSION_ID>/plan.md` for step details and guidelines
2. Mark step as in-progress
3. Make changes as planned (use OpenRewrite if helpful, verify results)
    - Add TODOs for any deferred work, e.g., temporary workarounds
4. **Review Code Changes** (per **Review Code Changes** section above): Verify sufficiency (all required changes present) and necessity (no unnecessary changes, functional behavior preserved, security controls maintained).
    - Add missing changes and revert unnecessary changes. Document any unavoidable behavior changes with justification.
5. Verify with specified command/JDK
    - **Steps 1-N (Setup/Upgrade)**: Compilation must pass (including both main and test code, fix immediately if not). Test failures acceptable - document count.
    - **Final Validation Step**: Achieve **Upgrade Success Criteria** - iterative test & fix loop until 100% pass (or ≥ baseline). NO deferring. **Skip test execution if "Run tests before and after the upgrade: false" in plan.md Options — only verify compilation in that case.**
    - After each build (`mvn clean test-compile` or equivalent): `#appmod-report-event(sessionId, event: "buildCompleted", phase: "execute", status: "succeeded"|"failed")`
    - After each test run (`mvn clean test` or equivalent): `#appmod-report-event(sessionId, event: "testCompleted", phase: "execute", status: "succeeded"|"failed")`
6. Commit using `#appmod-version-control(sessionId: <SESSION_ID>, workspacePath, action: "commitChanges")` (if version control available):
    - commitMessage format — First line: `Step <x>: <title> - Compile: <result>` or `Step <x>: <title> - Compile: <result>, Tests: <pass>/<total> passed` (if tests run)
    - Body: Changes summary + concise known issues/limitations (≤5 lines)
    - **Security note**: If any security-related changes were made, include "Security: <change description and justification>"
7. Mark step as ✅ or ❗
8. Report event at end of each step:
    - **Step 1 (Setup Environment)**: `#appmod-report-event(sessionId, event: "environmentSetup", phase: "execute", status: "succeeded"|"failed"|"skipped", details: {jdkPath: "<JDK path>", buildToolPath: "<build tool executable path>"})` — **details are REQUIRED** for this event. The `jdkPath` and `buildToolPath` must be valid paths that exist on this machine. Use `"."` for `buildToolPath` if a wrapper (mvnw/gradlew) is used.
    - **Step 2 (Setup Baseline)**: `#appmod-report-event(sessionId, event: "baselineSetup", phase: "execute", status: "succeeded"|"failed"|"skipped")` — use `"skipped"` with a `message` when the base JDK is not available
    - **Before each upgrade step (Steps 3-N)**: `#appmod-report-event(sessionId, event: "upgradeStepStarted", phase: "execute", status: "succeeded", details: {stepNumber: <N>, stepTitle: "<title>"})`
    - **After each upgrade step (Steps 3-N)**: `#appmod-report-event(sessionId, event: "upgradeStepCompleted", phase: "execute", status: "succeeded"|"failed", details: {stepNumber: <N>, stepTitle: "<title>", commitId: "<commitId from #appmod-version-control response, or 'N/A' if version control unavailable>"})`
    - **Final step (Final Validation)**: `#appmod-report-event(sessionId, event: "upgradeValidationCompleted", phase: "execute", status: "succeeded"|"failed", details: {stepNumber: <N>, stepTitle: "<title>", commitId: "<commit_id from #appmod-version-control response if version control available, otherwise 'N/A'>"})`

#### 3. Complete

1. Validate all steps in `plan.md` are completed
2. Validate all **Upgrade Success Criteria** are met, or otherwise go back to Final Validation step to fix
3. Call tool `#appmod-report-event(sessionId, event: "planExecutionCompleted", phase: "execute", status: "succeeded")`

### Phase 5: CVE Validation & Fix

Execute the "CVE Validation & Fix" step from the plan.

1. **Scan CVEs**: Extract direct deps (`mvn dependency:list -DexcludeTransitive=true`), call `#appmod-validate-cves-for-java(sessionId, dependencies, projectPath)`
2. **Fix all reported CVEs** (if any found in step 1):
   - Upgrading to a newer patch version within the same minor line is acceptable (e.g., 3.5.0 → 3.5.14) to resolve CVEs.
   - For each CVE with an available patched version, upgrade the dependency:
     - BOM-managed dependencies → update the BOM version (e.g., `spring-boot-dependencies`)
     - Direct dependencies → update the `<version>` tag in `pom.xml` / `build.gradle`
     - Property-referenced versions (e.g., `${spring.version}`) → update the property in `<properties>`
   - After applying all fixes, verify build compiles: `mvn clean test-compile` (or equivalent)
   - If build fails, analyze errors and apply minimal fixes
   - Re-scan: call `#appmod-validate-cves-for-java(sessionId, dependencies, projectPath)` to verify resolution
   - Record results: note which CVEs were fixed and which remain (no patch available)

### Phase 6: Summarize & Cleanup

1. **Collect test coverage**: Run `mvn clean verify -Djacoco.skip=false` or equivalent; record metrics
2. Generate `summary.md`:
    - **Read spec**: Read `summary.template.md` (in the session directory) — it contains the format specification with rules and samples.
    - **Write**: Collect observed build results, CVE scan results, and available coverage metrics. Write `summary.md` as a new file using the available editing tool per **Template compliance**. Do not collect personal information or invent missing metrics.
    - **Self-check**: Scan the written `summary.md` for HTML comments, `<placeholder>` tokens, empty bullets, unfilled table cells, bare headings without content, duplicate section headings. Fix any issues found.
3. Clean up only temporary files created by this upgrade; remove unfilled template comments from generated plan/summary files, not unrelated Markdown files.
4. → `#appmod-report-event(sessionId, event: "summaryGenerated", phase: "summarize", status: "succeeded", message: "<1-2 sentence summary>")`