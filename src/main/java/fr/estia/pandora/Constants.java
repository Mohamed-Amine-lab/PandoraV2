package fr.estia.pandora;

/**
 * Centralise toutes les valeurs constantes utilisées dans l'application Pandora.
 * Assure la cohérence des calculs et des conversions d'unités en correspondant
 * exactement à l'implémentation de référence.
 *
 * @version 2.0.0
 */
public final class Constants {

    /** Version actuelle de l'application. */
    public static final String VERSION = "2.0.0";

    /** Nom de l'application. */
    public static final String APP_NAME = "pandora";

    /** Format de précision numérique pour l'affichage (2 décimales). */
    public static final String DECIMAL_FORMAT = "%.2f";

    /** Conversion : 1 mètre = 3.281 pieds. */
    public static final double METER_TO_FEET = 3.281;

    /** Conversion : 1 pied en mètres. */
    public static final double FEET_TO_METER = 1.0 / METER_TO_FEET;

    /** Rayon de la Terre en km (formule de Haversine). */
    public static final double EARTH_RADIUS_KM = 6371.0;

    /** Conversion : 1 kg = 2.205 livres. */
    public static final double KG_TO_LBS = 2.205;

    /** Conversion : 1 livre en kg. */
    public static final double LBS_TO_KG = 1.0 / KG_TO_LBS;

    /** Conversion : 1 cheval-vapeur = 754.7 Watts. */
    public static final double HP_TO_WATT = 754.7;

    /** Conversion : 1 Watt en chevaux-vapeur. */
    public static final double WATT_TO_HP = 1.0 / HP_TO_WATT;

    /** Décalage Kelvin vers Celsius : °C = K - 273.15. */
    public static final double KELVIN_OFFSET = 273.15;

    /** Température de référence pour le calcul du bruit capteur (°C). */
    public static final double REFERENCE_TEMP_CELSIUS = 25.0;

    /** Conversion : 1 psi = 6894.76 Pascals. */
    public static final double PSI_TO_PA = 6894.76;

    /** Conversion : 1 Pascal en psi. */
    public static final double PA_TO_PSI = 1.0 / PSI_TO_PA;

    /** Conversion : 1 noeud = 1.852 km/h. */
    public static final double KNOT_TO_KMH = 1.852;

    /** Conversion : 1 mph = 1.609 km/h. */
    public static final double MPH_TO_KMH = 1.609;

    /** Vitesse du son : 1 Mach = 1225 km/h. */
    public static final double MACH_TO_KMH = 1225.0;

    /** Conversion : 1 km/h en m/s. */
    public static final double KMH_TO_MS = 1.0 / 3.6;

    /** Conversion : 1 mph en m/s. */
    public static final double MPH_TO_MS = MPH_TO_KMH * KMH_TO_MS;

    /** Accélération gravitationnelle standard en m/s². */
    public static final double GRAVITY = 9.80665;

    /** Seuil subsonique en Mach. */
    public static final double MACH_SUBSONIC = 1.0;

    /** Seuil hypersonique en Mach. */
    public static final double MACH_HYPERSONIC = 5.0;

    /** Seuil de proximité entre vols en km. */
    public static final double CLOSE_FLIGHT_THRESHOLD_KM = 50.0;

    /** Seuil de détection de phase oxygène en pourcentage. */
    public static final double OXYGEN_PHASE_THRESHOLD = 50.0;

    /** Fenêtre de moyenne glissante en secondes (5 minutes). */
    public static final double ROLLING_WINDOW_SECONDS = 300.0;

    /** Facteur de conversion : les fichiers US utilisent le format ratio pour l'humidité et l'oxygène. */
    public static final double RATIO_TO_PERCENT = 100.0;

    /** Identifiant d'origine pour les jets russes. */
    public static final String ORIGIN_RUSSIA = "RU";

    /** Identifiant d'origine pour les jets américains. */
    public static final String ORIGIN_USA = "US";

    /** Message de sortie quand une phase de vol n'est pas détectée. */
    public static final String PHASE_NOT_DETECTED = "not detected";

    /** Message de sortie quand aucune phase n'a un taux d'oxygène supérieur à 50%. */
    public static final String OXYGEN_PHASE_NONE = "none";

    /** Constructeur privé pour empêcher l'instanciation. */
    private Constants() {
        throw new UnsupportedOperationException("La classe Constants ne peut pas être instanciée");
    }
}