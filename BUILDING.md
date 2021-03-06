# Build and testing notes


## Building

Produce deliverables with:

    # Default OMERO profile
    ./gradlew clean dist

    # Alternate OMERO profile
    ./gradlew clean dist -Pprofile=omero508-ice34


## Testing

### Structure and tooling overview

#### Prerequisites

- One local OMERO.server per profile mainline
- One PostgreSQL database per profile mainline

#### Tooling

- Flyway: database schema migration
- Unitils/DbUnit: database fixtures loading
- Gradle/TestNG: test configuration generation & tests execution/reporting

#### Resources generation

Integration testing relies Gradle resource generation to produce profile-specific test configurations
at build time (see <https://gradle.org/docs/2.2.1/dsl/org.gradle.api.tasks.SourceSetOutput.html>).

Generated resources are located in `<buildDir>/generated-resources/<sourceSetName>`:

- `omero-config.<projectName>.properties`: minimal OMERO.server configuration (ManagedRepository location
   and database connection settings)
- `unitils-local.<projectName>.properties`: local Unitils/DbUnit database connection settings for fixtures loading
- `integration-tests.sh`: basic command shorthand for integration environment initialization

### Gradle testing

Example "quick testing" steps for `5.1.0-m4` profile:

    export OMERO_PREFIX=~/apps/OMERO.server-5.1.0-m4-ice35-b31
    ./gradlew clean generateTestResources -Pprofile=omero51m4-ice34
    chmod u+x ./build/generated-resources/test/integration-tests.sh
    ./build/generated-resources/test/integration-tests.sh
    unset OMERO_PREFIX

Note: the OMERO configuration generated in `omero-config.<projectName>.properties` is loaded
into the server using an OMERO.grid profile entitled `<projectName>-<buildProfileName>`.

### Eclipse testing

Prerequisites:

  - Integration database initialized (both schema and tests fixtures)
  - Integration OMERO.server configured + started

Both server and database schema versions are expected to match the build profile's baseline version number.

Run via TestNG plugin with either of:

  - JVM argument: `-Dice.config.location=${project_loc:omero-csv-tools}/bin/ice-local.config`
  - environment variable: `ICE_CONFIG=ice-local.config`

Note: this step is unnecessary in a Gradle environment, as the `test` build target sets the `ice.config.location`
system property to the relevant file location within the build directory.

### Test coverage reporting

 The JaCoCo coverage report is generated via the `./gradlew test jacocoTestReport` build targets.
 Reports are produced with every integration tests run from `./build/generated-resources/test/integration-tests.sh`
 into `./build/reports/jacoco/`.


## Publishing

Upload and publish released deliverables with:

    # Default OMERO profile, all artifacts
    ORG_GRADLE_PROJECT_bintray_user=user_name ORG_GRADLE_PROJECT_bintray_key=api_key \
    ./gradlew -Pbintray_org=imagopole -Pbintray_dryRun=false -Pbintray_publish=true \
    clean dist bintrayUpload --info

    # Alternate OMERO profile, split artifacts
    ./gradlew clean dist -Pprofile=omero510-ice35

    ORG_GRADLE_PROJECT_bintray_user=user_name ORG_GRADLE_PROJECT_bintray_key=api_key \
    ./gradlew -Pbintray_org=imagopole -Pbintray_dryRun=false -Pbintray_publish=true \
    bintrayDocsUpload bintrayDistsUpload --info

