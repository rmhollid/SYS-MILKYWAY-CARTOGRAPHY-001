# SYS-MILKYWAY-CARTOGRAPHY-001

STSF cartographic navigation system for the Milky Way Galaxy.

Current public proof of concept: **Android POC v0.1.1**.

## Mission

The system is organized around four navigation questions:

1. **Where you are**
2. **Where you were**
3. **Where you want to go**
4. **How to get there**

The Android proof of concept uses phone GNSS and orientation sensors for local measured state and presents one persistent 3D cartographic view across four screens: **NOW**, **HISTORY**, **TARGET**, and **ROUTE**.

## Current POC capabilities

- Android GNSS location ingestion when permission and hardware are available
- rotation-vector attitude readout when available
- local navigation-state history
- selection of visible objects or historical states as targets
- local return-to-history distance/bearing and 3D route vector
- interactive pseudo-3D cartographic visualizer
- five display scales: `LOCAL`, `LOCAL_SYSTEM`, `INTER_SYSTEM_LOCAL_GROUP`, `TRANSAXIAL`, and `MILKY_WAY`
- explicit separation between measured phone state and non-authoritative demonstration geometry
- offline runtime; no network permission, telemetry, advertising, or remote account dependency

## Important limitation

The celestial, local-system, transaxial, and Milky-Way reference points bundled with v0.1.1 are **DEMO geometry**. They are not production ephemerides, astrometric authority, or flight-navigation data. The current POC validates the interaction, state, history, targeting, route, and scale model only.

This software is **not production navigation software**.

## Android profile

- Application ID: `org.stsf.milkyway`
- Version: `0.1.1`
- Version code: `101`
- Java release: `17`
- Minimum Android SDK: `26`
- Target Android SDK: `37`
- Build profile: `STD-ANDROID-APK-BUILD-001 v6.2.11`
- No Gradle, Maven, or network build dependency is required by the STSF build profile used for this POC.

## Repository structure

```text
app/        Android source
std/        system standard
prg/        program description
artifacts/  bound design/validation artifacts
releases/   packaged POC artifacts and checksums
```

The canonical human-readable package access file is also retained as `read me.txt`.

## Build signing

The signing key is intentionally **not** stored in this repository. `BUILD_SPEC.json` references the development signing location used during the original v0.1.1 build. A local builder must provide its own compatible keystore and the required password environment variables.

Never commit `.keystore`, `.jks`, signing passwords, private keys, or other secrets.

## Integrity

Release artifacts include SHA-256 sidecars. Verify the digest before installation or archival use.

## Status

`REVIEW / POC`

Author: **Robert Michael Holliday**
