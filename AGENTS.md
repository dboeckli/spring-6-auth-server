# AGENTS.md

Single-module Maven app: Spring Boot 4 / Spring Authorization Server (Spring 6 course project).
Auth server issuer runs on port 9000. Everything is in-memory — users, registered clients, and
the RSA JWK (`SecurityConfig`) — no DB is required (H2/jdbc deps are unused). Package root:
`ch.springframeworkguru.spring6authserver`.

## Build & verify

- Requires **Java 25** (enforcer fails otherwise). Do not change `java.version`.
- Formatting gates run in the `validate` phase and fail the build: spring-javaformat +
  Spotless (pom.xml, `*.md`, `*.json`, `src/main/resources/application*.yaml`, `*.sh`).
  Fix with `mvn spotless:apply spring-javaformat:apply`, then re-run.
- Unit tests (`*Test`, MockMvc): `mvn test`
- Integration tests (`*IT`, real HTTP via failsafe): `mvn verify`
- Fast local verification (skips Spring Boot app start/stop and docker build):
  `mvn verify -Dskip.start.stop.springboot=true -Dskip.docker.build=true`
- `mvn install` additionally builds a docker image (`local/...`) and packages/renders the helm
  chart — requires a local `helm` binary on the PATH (lint+template run in the
  `test` phase, dry-run+package in `install`).
- `mvn deploy` publishes docker images (ghcr.io, Docker Hub) and the helm chart to repsy — needs
  CI credentials, never run locally. Helm registry login/push run via `exec-maven-plugin`
  (credentials from `HELM_REPSY_USER`/`HELM_REPSY_TOKEN`), not via kokuwaio.

## Test conventions

- `*Test` = `@SpringBootTest` + MockMvc; `*IT` = `@SpringBootTest(RANDOM_PORT)` doing real HTTP
  (`RestTemplate`).
- `TestClassOrderer` (wired via `src/test/resources/junit-platform.properties`) forces `*IT`
  classes to run after `*Test` classes.

## Architecture notes

- `config/SecurityConfig` holds the entire auth server: filter chains, in-memory user
  (`user`/`password`), registered client (`messaging-client`/`secret`; client_credentials +
  authorization_code + refresh_token), generated RSA keypair, and CORS.
- CORS allowed origins live in `application.yaml` under
  `security.oauth2.authorization-server.cors.allowed-origins`, not in code.
- Actuator is fully exposed, incl. `configprops show-values: ALWAYS` — dev-only setting.
- Swagger UI: `/swagger-ui/index.html` (springdoc; `config/OpenApiConfiguration`).
- Manual HTTP testing: `restRequest/*.http` files (IntelliJ HTTP client; base URL via the
  `application-port` env var in `http-client.env.json`).

## Release / versioning

- Default branch is `master`. CI runs on `main`, `master`, `feature/**`, `dependabot/**`,
  `release/**`.
- Releases use maven-release-plugin with the `release` profile (`-DperformRelease=true`).
- Helm chart version derives from the Maven version; snapshots get a `snapshot.<gitsha>` suffix
  (see `helm.snapshot.suffix`). The pom is `sortPom`-formatted — keep the `<relativePath />`
  space-before-close comment intact so sortPom and the release plugin don't fight.

## Deploy

- kubectl: `kubectl apply -f target/k8s/` → default namespace (manifests from `k8s/` are
  Maven-filtered into `target/k8s/`).
- Helm: chart in `helm-charts/` is filtered into `target/helm-charts/`, where
  `dependencies-values.yaml` is merged into `values.yaml` during the build. Install into
  namespace `spring-6-auth-server` (different from the kubectl path). Image refs in values use
  Maven filtering (`@docker.repo@`).

## Dependencies

- Dependency updates are managed by both `.github/dependabot.yml` and `.github/renovate.json`;
  strategy in `.github/renovate-strategy.md` (pin image versions, PRs only, no auto-merge,
  feature branches). When editing these files, follow the current schema (check Context7/docs).
