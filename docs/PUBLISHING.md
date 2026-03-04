# Publishing to Maven Central (Sonatype OSSRH)

The project is set up to publish to both **GitHub Packages** and **Maven Central** (Sonatype).

## Maven Central (open repository)

To publish to [Central](https://central.sonatype.com/) (so users can resolve `io.github.makbn:jthumbnail` without GitHub token):

### 1. Sonatype account and staging profile

- Create an account at [central.sonatype.com](https://central.sonatype.com/) and get your **staging profile ID**.
- Set environment variables (or use Gradle properties):
  - `OSSRH_USERNAME` – Sonatype username
  - `OSSRH_PASSWORD` – Sonatype password
  - `SONATYPE_PROFILE_ID` – staging profile ID (e.g. from Central → Staging Profiles)

### 2. Signing (required for Central)

Artifacts must be signed with GPG. Use one of:

**Option A – In-memory key (CI / script)**  
Set before running Gradle:

- `SIGNING_KEY_ID` – key id (e.g. last 8 chars of key fingerprint)
- `SIGNING_KEY` – base64-encoded **private** key (armored), e.g.  
  `gpg --armor --export-secret-keys KEY_ID | base64`
- `SIGNING_PASSWORD` – passphrase for the key

**Option B – Key ring file**  
Configure the `signing` block in `build.gradle` to use `secretKeyRingFile` and `password` instead of in-memory keys.

### 3. Publish

With the above set:

```bash
./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository
```

- `publishToSonatype` – builds, signs, and uploads to the Sonatype staging repository.
- `closeAndReleaseSonatypeStagingRepository` – closes the staging repo and releases to Central (after Central sync, the artifact is available without GitHub token).

If you only want to upload without releasing:

```bash
./gradlew publishToSonatype
```

Then close and release from the [Central Staging UI](https://central.sonatype.com/).

### 4. After release

Users can depend on the library without any custom repository:

```xml
<dependency>
  <groupId>io.github.makbn</groupId>
  <artifactId>jthumbnail</artifactId>
  <version>2.3.0</version>
</dependency>
```

## GitHub Packages

Publishing to GitHub Packages uses:

- `GITHUB_ACTOR` – GitHub username
- `GITHUB_TOKEN` – token with `write:packages` (and read if using GitHub Actions)

Then:

```bash
./gradlew publish
```

This publishes to the GitHub Maven repository configured in `build.gradle`. Users need to add that repository and (optionally) authentication to resolve the artifact.
