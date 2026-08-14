# Helm-Setup mit Maven-Filtering

Dieses Projekt nutzt einen ungewöhnlichen Helm-Build: Die Chart-Quelle liegt in
`helm-charts/` und wird per Maven-Filtering (`@...@`-Platzhalter) nach `target/`
kopiert, bevor das `helm-maven-plugin` dort die eigentlichen Helm-Schritte
ausführt. Dadurch kann Maven Werte (Version, Repo, ArtifactId) direkt in
Chart.yaml, values.yaml und die Templates hinein auflösen.

## Build-Pipeline (Maven → `target/`)

Alle Helm-Artefakte entstehen ausschließlich unter `target/`; die Quelle
`helm-charts/` wird nie direkt von Helm verarbeitet.

1. **`initialize`** — `git-commit-id-maven-plugin` setzt `git.commit.id.abbrev`
   (pom.xml:396).
2. **`initialize`** — `maven-antrun-plugin` schreibt `target/helm.properties`
   mit `helm.chart.version=<project.version>`, danach Regex-Rewrite
   (pom.xml:423-435): `0.0.11-SNAPSHOT` → `0.0.11-snapshot.<gitsha>`
   (semver-konform, kollisionsfrei pro Commit).
3. **`initialize`** — `properties-maven-plugin` lädt die Datei wieder ein,
   damit `helm.chart.version` als Maven-Property überall verfügbar ist
   (pom.xml:459).
4. **`process-resources`** — `maven-resources-plugin` `copy-resources` mit
   `filtering=true` kopiert `helm-charts/` → `target/helm-charts/`
   (pom.xml:510) mit zusätzlichem Filter `target/helm.properties`. Aufgelöst
   werden:
   - `@helm.chart.version@` → Chart.yaml `version`/`appVersion`
   - `@docker.repo@` → values.yaml `image.registry`
   - `@project.artifactId@` → Chart-Name, Container-Name/-Image im Template
5. **`process-resources`** — `merge-yaml-plugin` merged `dependencies-values.yaml`
   in `values.yaml` (pom.xml:537).
6. **`test`** — antrun löscht `target/helm-charts/dependencies-values.yaml`
   wieder (pom.xml:440), damit Helm sie nicht als Datei mitnimmt.
7. **`helm-maven-plugin`** (io.kokuwa, 6.17.0, `useLocalHelmBinary=true`)
   arbeitet **nur auf `target/helm-charts/`** (pom.xml:724):
   - `test`: `lint` + `template` → `target/helm-templated`
   - `install`: `dry-run` + `package` → tgz in `target/helm/repo`
   - `deploy`: `registry-login` + `push` →
     `repo.repsy.io/user08694146/helm-dboeckli`

Das „Spezielle": Maven-Filtering läuft **in die Helm-Templates hinein**
(`@...@`-Platzhalter im `deployment.yaml`, kombiniert mit Helm-`{{ }}`-Syntax),
und `chartVersion` wird doppelt gesetzt — einmal per Filtering in Chart.yaml,
einmal als Plugin-Config `chartVersion=${helm.chart.version}`.

Hinweis: Der Helm-`package`-Goal räumt alte tgz in `target/helm/repo` nicht auf.
Ohne `mvn clean` zwischen Builds bleiben daher mehrere Pakete liegen; nur die
jeweils neueste wird per `helm-push` deployed.

## Versionierung Docker / Helm

Zentral ist **ein** Tag: `helm.chart.version` (Docker-Haupt-Tag =
Helm-Chart-Version = `appVersion`).

- **Snapshot-Build:** `0.0.11-SNAPSHOT` → Chart `0.0.11-snapshot.<gitsha>`.
  Das Docker-Image wird `local/<app>:0.0.11-snapshot.<gitsha>` gebaut
  (pom.xml:660) und **zusätzlich** mit dem Kanal-Tag `docker.image.tag`
  (default `development`) getaggt. Im Deployment-Image-Ref
  (deployment.yaml:20) steht `{{ .Chart.AppVersion }}` → identisch zum
  Docker-Tag.
- **Profile (pom.xml:881-938):**
  - `ci-cd` (auto bei `GITHUB_ACTIONS`): `skip.docker.publish=false`,
    `docker.repo=domboeckli`.
  - `master-branch` / `main-branch` (auto nach `GITHUB_REF`):
    `docker.image.tag=snapshot`, `helm.snapshot.suffix=snapshot` → Chart heißt
    dann schlicht `0.0.11-snapshot`.
  - `release` (`-DperformRelease=true`): Version ohne `-SNAPSHOT` → Regex greift
    nicht, Chart = echte Version `0.0.11`, Docker zusätzlich `latest`.

**Beziehung:** `helm.chart.version` = eindeutiger, immutabler Tag pro Build
(Version oder `snapshot.<sha>`); `docker.image.tag` = floating Kanal-Tag
(`development`/`snapshot`/`latest`). Deploy-Targets: ghcr.io + Docker Hub
(jeweils `:${helm.chart.version}` + `:${docker.image.tag}`, pom.xml:667-720).

Damit ist der Docker-Tag garantiert identisch mit dem Chart-Versions-Referenz
im Helm-Image-Ref — das ist der Kern der Abstimmung zwischen beiden
Versionierungswegen.
