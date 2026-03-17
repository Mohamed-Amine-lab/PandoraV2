# Pandora V2

Pandora est un outil en ligne de commande (CLI) développé en Java qui analyse les données de boîtes noires d'avions (fichiers `.frd`). Il fournit des résumés et des analyses de haut niveau basées sur les données de capteurs bas niveau (position, vitesse, altitude, etc.).

## Utilisation

```bash
java -jar target/pandora.jar [OPTIONS] ...source
```

### Options

| Option | Description |
|--------|-------------|
| `-v`, `--version` | Afficher la version de l'application |
| `-h`, `--help` | Afficher le message d'aide |
| `-m arg`, `--metadata arg` | Afficher la valeur d'un champ de métadonnées |
| `-o arg`, `--output arg` | Afficher une feature spécifique |
| `-p`, `--parameters` | Lister les paramètres du fichier de vol |
| `-n`, `--number` | Afficher le nombre d'enregistrements |
| `-u arg`, `--unit arg` | Choisir le système d'unités (`metric` ou `imperial`) |
| `-M`, `--metric` | Utiliser les unités métriques |
| `-I`, `--imperial` | Utiliser les unités impériales |
| `-b`, `--batch` | Traiter un dossier entier de fichiers |
| `-d`, `--debug` | Afficher les informations de débogage |

### Exemples

```bash
# Afficher la version
java -jar target/pandora.jar --version

# Altitude moyenne d'un vol
java -jar target/pandora.jar -o avgAlt test/resources/common/0_201_MiG-23MLD.frd

# Durée du vol
java -jar target/pandora.jar -o flightDuration test/resources/common/0_201_MiG-23MLD.frd

# Phase de décollage
java -jar target/pandora.jar -o takeOff test/resources/common/0_201_MiG-23MLD.frd

# Jet le plus rapide parmi plusieurs vols
java -jar target/pandora.jar -o highestSpeed test/resources/common
```

## Features disponibles

### Calculs simples
`avgAlt`, `maxAlt`, `avgAirSpeed`, `maxAirSpeed`, `avgEnginePower`, `maxEnginePower`, `avgTemp`, `minTemp`, `maxTemp`, `avgPressure`, `minPressure`, `maxPressure`, `avgHumidity`, `minHumidity`, `maxHumidity`, `avgHeartRate`, `minHeartRate`, `maxHeartRate`, `avgOxygen`, `minOxygen`, `maxOxygen`

### Calculs avancés
`flightDuration`, `flightDistance`, `avgAcceleration`, `maxAcceleration`, `maxAccelG`, `windSpeed`, `avgMachSpeed`, `maxMachSpeed`, `reachAlt`, `reachDist`, `fastWindAlt`, `fastJetAlt`, `noiseTemp`, `stressedPilot`

### Phases de vol
`takeOff`, `cruise`, `landing`, `ratioDistance`, `oxygenPhase`, `mostPowerPhase`, `mostStressPhase`, `mostAccelPhase`

### Mode batch (multi-vols)
`highestSpeed`, `slowestSpeed`, `highestAltitude`, `longestDuration`, `firstLanding`, `lastLanding`, `highestPower`, `highestOxygen`, `highestHeartBeat`, `lowestHeartBeat`, `cumulDuration`, `cumulDistance`, `airportTakeOff`, `airportLanding`, `highestDrag`, `smallestDrag`, `highestLift`, `smallestLift`, `closeFlight`, `closeFlightSameOri`, `closeFlightDiffOri`

## Format des fichiers .frd

Les fichiers `.frd` contiennent deux sections :
1. **Métadonnées** — informations sur le vol (id, origine, date, aéroports...)
2. **Données CSV** — enregistrements des capteurs (altitude, vitesse, GPS...)

Les fichiers d'origine `RU` (Russie) utilisent les unités métriques. Les fichiers `US` sont automatiquement convertis en métriques au parsing.

## Compilation

```bash
mvn clean package
```

## Tests

```bash
python test/autograder.py -t test/testSuite.json -m manifest.json target/pandora.jar
```

## Architecture

```
src/main/java/fr/estia/pandora/
├── Pandora.java           # Point d'entrée, gestion CLI
├── Constants.java         # Constantes et conversions
├── Flightmetadata.java    # Métadonnées d'un vol
├── Flightdata.java        # Données d'un vol
├── Flightrecordparser.java # Parser des fichiers .frd
├── Calculator.java        # Calculs sur un seul vol
├── PhaseDetector.java     # Détection des phases de vol
└── BatchCalculator.java   # Calculs multi-vols
```

## Auteur

Mohamed-Amine — ESTIA 2026