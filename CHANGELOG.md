# Changelog

All notable changes to this project will be documented in this file.
the format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.0.0] - 2026-03-17

### Added
- Parser complet des fichiers .frd (format RU et US)
- Conversion automatique des unités US vers métriques
- Options CLI complètes : -v, -h, -m, -p, -n, -u, -M, -I, -b, -d
- Calculs simples : avgAlt, maxAlt, avgAirSpeed, maxAirSpeed
- Calculs température, pression, humidité, fréquence cardiaque, oxygène
- Calculs moteur : avgEnginePower, maxEnginePower
- Durée et distance : flightDuration, flightDistance
- Calculs avancés : avgAcceleration, maxAccelG, windSpeed, avgMachSpeed
- Features avancées : reachAlt, reachDist, fastWindAlt, fastJetAlt, noiseTemp, stressedPilot
- Détection phases de vol : takeOff, cruise, landing, ratioDistance
- Phases exigeantes : oxygenPhase, mostPowerPhase, mostStressPhase, mostAccelPhase
- Mode batch multi-vols : highestSpeed, highestAltitude, longestDuration, closeFlight
- testSuite.json avec 23 tests
- Correction locale décimale (Locale.US)

## [1.0.1] - 2024-03-06

### Added
- Add a test goal to maven to automatically run the test in testSuites.json
- Add evaluation PR

### Changed
- Encrypt the autograder
- Update the java-version in autoevaluation

### Fixed
- Fix issues in the autoevaluation workflow

## [1.0.0] - 2024-03-05

### Added
- Add a skeleton of the pandora main class
- Add build config (maven, eclipse, vscode)
- Add a License file
- Add a readme file
- Add some default test flight recording and empty testSuite.json
- add github action for issue management and autoevaluation