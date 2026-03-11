package fr.estia.pandora;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Pandora est un outil d'analyse des données d'enregistrement de vol qui fournit
 * des résumés et des informations de haut niveau basées sur les données de capteurs
 * bas niveau (ex : position d'un avion de chasse).
 *
 * <h1>Synopsis :</h1>
 * <pre>
 * pandora java -jar pandora.jar [OPTIONS] ...source
 * </pre>
 *
 * <h2>Paramètres</h2>
 * <pre>
 * ...source - chemin vers les fichiers d'enregistrement de vol ou un dossier les contenant
 *
 * OPTIONS o:m:hvdpnMIu:b
 * -d, --debug            Débogage    - afficher des informations de débogage supplémentaires
 * -h, --help             Aide        - afficher ce message d'aide
 * -m arg, --metadata arg Métadonnées - afficher la valeur du champ de métadonnées spécifié
 * -o arg, --output arg   Sortie      - afficher uniquement la fonctionnalité spécifiée
 * -p, --parameters       Paramètres  - lister les paramètres par ordre alphabétique
 * -n, --number           Nombre      - afficher le nombre d'enregistrements
 * -v, --version          Version     - afficher la version de l'application
 * -u arg, --unit arg     Unité       - choisir le système d'unités (metric|imperial)
 * -M, --metric           Métrique    - raccourci pour --unit metric
 * -I, --imperial         Impérial    - raccourci pour --unit imperial
 * -b, --batch            Batch       - traiter tous les fichiers d'un dossier
 * </pre>
 *
 * @author Dimitri Masson
 * @author William Delamare
 * @version 2.0.0
 */
public class Pandora {

    /**
     * Point d'entrée principal de l'application Pandora.
     * Parse les options de la ligne de commande et délègue au gestionnaire approprié.
     *
     * @param arguments arguments de la ligne de commande
     */
    public static void main(String[] arguments) {
        // Définition des options longues
        LongOpt[] longOpts = new LongOpt[] {
            new LongOpt("version", LongOpt.NO_ARGUMENT, null, 'v'),
            new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h'),
            new LongOpt("debug", LongOpt.NO_ARGUMENT, null, 'd'),
            new LongOpt("metadata", LongOpt.REQUIRED_ARGUMENT, null, 'm'),
            new LongOpt("output", LongOpt.REQUIRED_ARGUMENT, null, 'o'),
            new LongOpt("parameters", LongOpt.NO_ARGUMENT, null, 'p'),
            new LongOpt("number", LongOpt.NO_ARGUMENT, null, 'n'),
            new LongOpt("unit", LongOpt.REQUIRED_ARGUMENT, null, 'u'),
            new LongOpt("metric", LongOpt.NO_ARGUMENT, null, 'M'),
            new LongOpt("imperial", LongOpt.NO_ARGUMENT, null, 'I'),
            new LongOpt("batch", LongOpt.NO_ARGUMENT, null, 'b'),
        };

        Getopt g = new Getopt(Constants.APP_NAME, arguments, "vhdm:o:pnu:MIb", longOpts);

        boolean showVersion = false;
        boolean showHelp = false;
        boolean showParameters = false;
        boolean showNumber = false;
        boolean debug = false;
        boolean batch  = false;
        String metadataField = null;
        String outputFeature = null;
        String unitSystem = "metric";

        int c;
        while ((c = g.getopt()) != -1) {
            switch (c) {
                case 'v':
                    showVersion = true;
                    break;
                case 'h':
                    showHelp = true;
                    break;
                case 'd':
                    debug = true;
                    break;
                case 'm':
                    metadataField = g.getOptarg();
                    break;
                case 'o':
                    outputFeature = g.getOptarg();
                    break;
                case 'p':
                    showParameters = true;
                    break;
                case 'n':
                    showNumber = true;
                    break;
                case 'u':
                    unitSystem = g.getOptarg();
                    break;
                case 'M':
                    unitSystem = "metric";
                    break;
                case 'I':
                    unitSystem = "imperial";
                    break;
                case 'b':
                    batch = true;
                    break;
                default:
                    break;
            }
        }

        // Gestion de l'option version
        if (showVersion) {
            System.out.println(Constants.APP_NAME + "@" + Constants.VERSION);
            return;
        }

        // Gestion de l'option aide
        if (showHelp) {
            printHelp();
            return;
        }

        // Récupération des arguments restants comme chemins sources
        List<String> sources = new ArrayList<>();
        for (int i = g.getOptind(); i < arguments.length; i++) {
            sources.add(arguments[i]);
        }

        // Si aucune source et pas de version/aide, afficher le message par défaut
        if (sources.isEmpty()) {
            System.out.println(Constants.APP_NAME + "@" + Constants.VERSION);
            return;
        }

        try {
            // Parsing de tous les fichiers sources
            List<Flightdata> flights = Flightrecordparser.parseSources(
                    sources.toArray(new String[0]));

            if (flights.isEmpty()) {
                System.err.println("Aucun fichier d'enregistrement de vol trouvé.");
                return;
            }

            // Gestion de l'option métadonnées
            if (metadataField != null) {
                for (Flightdata flight : flights) {
                    String value = flight.getmetadata().get(metadataField);
                    if (value != null) {
                        System.out.println(value);
                    }
                }
                return;
            }

            // Gestion de l'option paramètres
            if (showParameters) {
                for (Flightdata flight : flights) {
                    List<String> sorted = flight.getSortedColumnNames();
                    for (String param : sorted) {
                        System.out.println(param);
                    }
                }
                return;
            }

            // Gestion de l'option nombre
            if (showNumber) {
                for (Flightdata flight : flights) {
                    System.out.println(flight.getRecordCount());
                }
                return;
            }

            // Gestion de la fonctionnalité de sortie
            if (outputFeature != null) {
                handleOutputFeature(outputFeature, flights, unitSystem, debug);
                return;
            }

        } catch (IOException e) {
            System.err.println("Erreur de lecture du fichier : " + e.getMessage());
        }
    }

    /**
     * Délègue la fonctionnalité de sortie demandée au calcul approprié.
     *
     * @param feature    le nom de la fonctionnalité (ex : "avgAlt", "maxAirSpeed")
     * @param flights    liste des données de vol parsées
     * @param unitSystem le système d'unités pour la sortie ("metric" ou "imperial")
     * @param debug      si le mode débogage est activé
     */
    private static void handleOutputFeature(String feature, List<Flightdata> flights,
            String unitSystem, boolean debug) {
        for (Flightdata flight : flights) {
            String result = computeFeature(feature, flight, unitSystem);
            if (result != null) {
                System.out.println(result);
            }
        }
    }

    /**
     * Calcule une fonctionnalité pour un vol et retourne le résultat formaté.
     *
     * @param feature    le nom de la fonctionnalité
     * @param flight     les données de vol
     * @param unitSystem le système d'unités pour la sortie
     * @return chaîne du résultat formaté, ou null si la fonctionnalité est inconnue
     */
    private static String computeFeature(String feature, Flightdata flight, String unitSystem) {
        boolean imperial = "imperial".equals(unitSystem);

        switch (feature) {
            case "start_time":
                return Calculator.startTime(flight);

            case "filenames":
                return flight.getFileName();

            case "avgAlt": {
                double val = Calculator.average(flight.getColumnValues("altitude"));
                if (imperial) {
                    val = val * Constants.METER_TO_FEET;
                }
                return String.format(Constants.DECIMAL_FORMAT, val);
            }
            case "maxAlt": {
                double val = Calculator.max(flight.getColumnValues("altitude"));
                if (imperial) {
                    val = val * Constants.METER_TO_FEET;
                }
                return String.format(Constants.DECIMAL_FORMAT, val);
            }
            case "avgAirSpeed": {
                double val = Calculator.average(flight.getColumnValues("air_speed"));
                if (imperial) {
                    val = val / Constants.MPH_TO_MS;
                }
                return String.format(Constants.DECIMAL_FORMAT, val);
            }
            case "maxAirSpeed": {
                double val = Calculator.max(flight.getColumnValues("air_speed"));
                if (imperial) {
                    val = val / Constants.MPH_TO_MS;
                }
                return String.format(Constants.DECIMAL_FORMAT, val);
            }
            case "avgEnginePower": {
                double[] power = Calculator.computeTotalEnginePower(flight);
                double val = Calculator.average(power);
                if (imperial) {
                    val = val * Constants.WATT_TO_HP;
                }
                return String.format(Constants.DECIMAL_FORMAT, val);
            }
            case "maxEnginePower": {
                double[] power = Calculator.computeTotalEnginePower(flight);
                double val = Calculator.max(power);
                if (imperial) {
                    val = val * Constants.WATT_TO_HP;
                }
                return String.format(Constants.DECIMAL_FORMAT, val);
            }
            case "avgTemp": {
                double val = Calculator.average(flight.getColumnValues("temperature_in"));
                if (imperial) {
                    val = val + Constants.KELVIN_OFFSET;
                }
                return String.format(Constants.DECIMAL_FORMAT, val);
            }
            case "minTemp": {
                double val = Calculator.min(flight.getColumnValues("temperature_in"));
                if (imperial) {
                    val = val + Constants.KELVIN_OFFSET;
                }
                return String.format(Constants.DECIMAL_FORMAT, val);
            }
            case "maxTemp": {
                double val = Calculator.max(flight.getColumnValues("temperature_in"));
                if (imperial) {
                    val = val + Constants.KELVIN_OFFSET;
                }
                return String.format(Constants.DECIMAL_FORMAT, val);
            }
            case "avgPressure": {
                double val = Calculator.average(flight.getColumnValues("pressure_in"));
                if (imperial) {
                    val = val * Constants.PA_TO_PSI;
                }
                return String.format(Constants.DECIMAL_FORMAT, val);
            }
            case "maxPressure": {
                double val = Calculator.max(flight.getColumnValues("pressure_in"));
                if (imperial) {
                    val = val * Constants.PA_TO_PSI;
                }
                return String.format(Constants.DECIMAL_FORMAT, val);
            }
            case "minPressure": {
                double val = Calculator.min(flight.getColumnValues("pressure_in"));
                if (imperial) {
                    val = val * Constants.PA_TO_PSI;
                }
                return String.format(Constants.DECIMAL_FORMAT, val);
            }
            case "avgHumidity":
                return String.format(Constants.DECIMAL_FORMAT,
                        Calculator.average(flight.getColumnValues("humidity_in")));
            case "maxHumidity":
                return String.format(Constants.DECIMAL_FORMAT,
                        Calculator.max(flight.getColumnValues("humidity_in")));
            case "minHumidity":
                return String.format(Constants.DECIMAL_FORMAT,
                        Calculator.min(flight.getColumnValues("humidity_in")));
            case "avgHeartRate":
                return String.format(Constants.DECIMAL_FORMAT,
                        Calculator.average(flight.getColumnValues("heart_rate")));
            case "maxHeartRate":
                return String.format(Constants.DECIMAL_FORMAT,
                        Calculator.max(flight.getColumnValues("heart_rate")));
            case "minHeartRate":
                return String.format(Constants.DECIMAL_FORMAT,
                        Calculator.min(flight.getColumnValues("heart_rate")));
            case "avgOxygen":
                return String.format(Constants.DECIMAL_FORMAT,
                        Calculator.average(flight.getColumnValues("oxygen_mask")));
            case "maxOxygen":
                return String.format(Constants.DECIMAL_FORMAT,
                        Calculator.max(flight.getColumnValues("oxygen_mask")));
            case "minOxygen":
                return String.format(Constants.DECIMAL_FORMAT,
                        Calculator.min(flight.getColumnValues("oxygen_mask")));
            case "flightDuration": {
                double[] ts = flight.getTimestamps();
                double dur = Calculator.flightDurationSeconds(ts);
                return Calculator.formatDuration(dur);
            }
            case "flightDistance": {
                double dist = Calculator.totalFlightDistance(flight);
                if (imperial) {
                    dist = dist * Constants.METER_TO_FEET;
                }
                return String.format(Constants.DECIMAL_FORMAT, dist);
            }
            case "avgAcceleration": {
                double[] speeds = flight.getColumnValues("air_speed");
                double[] ts = flight.getTimestamps();
                double[] accel = Calculator.computeAcceleration(speeds, ts);
                return String.format(Constants.DECIMAL_FORMAT, Calculator.average(accel));
            }
            case "maxAcceleration": {
                double[] speeds = flight.getColumnValues("air_speed");
                double[] ts = flight.getTimestamps();
                double[] accel = Calculator.computeAcceleration(speeds, ts);
                return String.format(Constants.DECIMAL_FORMAT, Calculator.max(accel));
            }
            case "windSpeed":
                return String.format(Constants.DECIMAL_FORMAT,
                        Calculator.averageWindSpeed(flight));
            case "avgMachSpeed": {
                double avgSpeed = Calculator.average(flight.getColumnValues("air_speed"));
                return String.format(Constants.DECIMAL_FORMAT, Calculator.speedToMach(avgSpeed));
            }
            case "maxMachSpeed": {
                double maxSpeed = Calculator.max(flight.getColumnValues("air_speed"));
                return String.format(Constants.DECIMAL_FORMAT, Calculator.speedToMach(maxSpeed));
            }
            case "maxAccelG": {
                double[] speeds = flight.getColumnValues("air_speed");
                double[] ts = flight.getTimestamps();
                double[] accel = Calculator.computeAcceleration(speeds, ts);
                double maxAccel = Calculator.max(accel);
                return String.format(Constants.DECIMAL_FORMAT, maxAccel / Constants.GRAVITY);
            }
            default:
                return null;
        }
    }

    /**
     * Affiche le message d'aide décrivant toutes les options disponibles.
     */
    private static void printHelp() {
        System.out.println("Utilisation : pandora [OPTIONS] ...source");
        System.out.println();
        System.out.println("Options :");
        System.out.println("  -v, --version          Afficher la version");
        System.out.println("  -h, --help             Afficher ce message d'aide");
        System.out.println("  -d, --debug            Afficher les informations de débogage");
        System.out.println("  -m arg, --metadata arg Afficher la valeur d'un champ de métadonnées");
        System.out.println("  -o arg, --output arg   Afficher la fonctionnalité spécifiée");
        System.out.println("  -p, --parameters       Lister les paramètres par ordre alphabétique");
        System.out.println("  -n, --number           Afficher le nombre d'enregistrements");
        System.out.println("  -u arg, --unit arg     Choisir le système d'unités (metric|imperial)");
        System.out.println("  -M, --metric           Utiliser les unités métriques");
        System.out.println("  -I, --imperial         Utiliser les unités impériales");
        System.out.println("  -b, --batch            Traiter tous les fichiers d'un dossier");
    }
}