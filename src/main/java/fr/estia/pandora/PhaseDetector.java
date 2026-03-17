package fr.estia.pandora;

/**
 * Détecte et représente les phases de vol dans un enregistrement de vol.
 *
 * <p>Les trois phases principales sont :</p>
 * <ul>
 *   <li><b>Décollage (takeOff)</b> : depuis le début jusqu'au moment où l'altitude
 *       dépasse 10% de l'altitude maximale du vol.</li>
 *   <li><b>Croisière (cruise)</b> : entre la fin du décollage et le début de l'atterrissage,
 *       c'est la phase centrale où l'avion vole à altitude stable.</li>
 *   <li><b>Atterrissage (landing)</b> : depuis le moment où l'altitude repasse sous
 *       10% de l'altitude maximale jusqu'à la fin du vol.</li>
 * </ul>
 *
 * <p>Chaque phase est décrite par son horodatage de début et de fin.</p>
 *
 * @version 2.0.0
 */
public class PhaseDetector {

    /**
     * Représente une phase de vol avec ses bornes temporelles.
     */
    public static class FlightPhase {

        /** Horodatage Unix de début de la phase (secondes). */
        private final double startTimestamp;

        /** Horodatage Unix de fin de la phase (secondes). */
        private final double endTimestamp;

        /** Indice du premier enregistrement de la phase. */
        private final int startIndex;

        /** Indice du dernier enregistrement de la phase. */
        private final int endIndex;

        /**
         * Construit une phase de vol.
         *
         * @param startTimestamp horodatage de début
         * @param endTimestamp   horodatage de fin
         * @param startIndex     indice de début dans les enregistrements
         * @param endIndex       indice de fin dans les enregistrements
         */
        public FlightPhase(double startTimestamp, double endTimestamp,
                           int startIndex, int endIndex) {
            this.startTimestamp = startTimestamp;
            this.endTimestamp = endTimestamp;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }

        /**
         * Retourne l'horodatage de début de la phase.
         *
         * @return horodatage de début en secondes Unix
         */
        public double getStartTimestamp() {
            return startTimestamp;
        }

        /**
         * Retourne l'horodatage de fin de la phase.
         *
         * @return horodatage de fin en secondes Unix
         */
        public double getEndTimestamp() {
            return endTimestamp;
        }

        /**
         * Retourne l'indice de début dans les enregistrements.
         *
         * @return indice de début
         */
        public int getStartIndex() {
            return startIndex;
        }

        /**
         * Retourne l'indice de fin dans les enregistrements.
         *
         * @return indice de fin
         */
        public int getEndIndex() {
            return endIndex;
        }

        /**
         * Formate la phase au format "début=HH:mm:ss / fin=HH:mm:ss".
         *
         * @return chaîne formatée
         */
        public String format() {
            return "start=" + Calculator.formatTimestamp(startTimestamp)
                    + " / end=" + Calculator.formatTimestamp(endTimestamp);
        }
    }

    /**
     * Seuil en pourcentage de l'altitude maximale pour délimiter les phases.
     * En dessous de ce seuil → décollage ou atterrissage.
     * Au-dessus → croisière.
     */
    private static final double ALTITUDE_THRESHOLD_PERCENT = 0.10;

    /**
     * Détecte la phase de décollage dans les données de vol.
     *
     * <p>Le décollage commence au premier enregistrement et se termine
     * au premier enregistrement où l'altitude dépasse 10% de l'altitude maximale.</p>
     *
     * @param flight les données de vol
     * @return la phase de décollage, ou null si non détectée
     */
    public static FlightPhase detectTakeOff(Flightdata flight) {
        double[] altitudes = flight.getColumnValues("altitude");
        double[] timestamps = flight.getTimestamps();

        if (altitudes.length == 0) {
            return null;
        }

        double maxAlt = Calculator.max(altitudes);
        double threshold = maxAlt * ALTITUDE_THRESHOLD_PERCENT;

        // Le décollage commence au début du vol
        int startIdx = 0;

        // Il se termine quand l'altitude dépasse le seuil
        for (int i = 0; i < altitudes.length; i++) {
            if (altitudes[i] >= threshold) {
                return new FlightPhase(timestamps[startIdx], timestamps[i], startIdx, i);
            }
        }

        // Si le seuil n'est jamais atteint, pas de décollage détecté
        return null;
    }

    /**
     * Détecte la phase de croisière dans les données de vol.
     *
     * <p>La croisière commence à la fin du décollage (altitude > 10% du max)
     * et se termine au début de l'atterrissage (altitude repasse sous 10% du max).</p>
     *
     * @param flight les données de vol
     * @return la phase de croisière, ou null si non détectée
     */
    public static FlightPhase detectCruise(Flightdata flight) {
        double[] altitudes = flight.getColumnValues("altitude");
        double[] timestamps = flight.getTimestamps();

        if (altitudes.length == 0) {
            return null;
        }

        double maxAlt = Calculator.max(altitudes);
        double threshold = maxAlt * ALTITUDE_THRESHOLD_PERCENT;

        int cruiseStart = -1;
        int cruiseEnd = -1;

        // Trouver le début de la croisière (première fois au-dessus du seuil)
        for (int i = 0; i < altitudes.length; i++) {
            if (altitudes[i] >= threshold) {
                cruiseStart = i;
                break;
            }
        }

        if (cruiseStart < 0) {
            return null;
        }

        // Trouver la fin de la croisière (dernière fois au-dessus du seuil)
        for (int i = altitudes.length - 1; i >= cruiseStart; i--) {
            if (altitudes[i] >= threshold) {
                cruiseEnd = i;
                break;
            }
        }

        if (cruiseEnd <= cruiseStart) {
            return null;
        }

        return new FlightPhase(timestamps[cruiseStart], timestamps[cruiseEnd],
                cruiseStart, cruiseEnd);
    }

    /**
     * Détecte la phase d'atterrissage dans les données de vol.
     *
     * <p>L'atterrissage commence au dernier enregistrement où l'altitude repasse
     * sous 10% de l'altitude maximale et se termine à la fin du vol.</p>
     *
     * @param flight les données de vol
     * @return la phase d'atterrissage, ou null si non détectée
     */
    public static FlightPhase detectLanding(Flightdata flight) {
        double[] altitudes = flight.getColumnValues("altitude");
        double[] timestamps = flight.getTimestamps();

        if (altitudes.length == 0) {
            return null;
        }

        double maxAlt = Calculator.max(altitudes);
        double threshold = maxAlt * ALTITUDE_THRESHOLD_PERCENT;

        int landingStart = -1;

        // L'atterrissage commence quand l'altitude repasse définitivement sous le seuil
        // (depuis la fin du vol, trouver le dernier point où on est encore au-dessus)
        for (int i = altitudes.length - 1; i >= 0; i--) {
            if (altitudes[i] >= threshold) {
                landingStart = i;
                break;
            }
        }

        if (landingStart < 0 || landingStart >= altitudes.length - 1) {
            return null;
        }

        int endIdx = altitudes.length - 1;
        return new FlightPhase(timestamps[landingStart], timestamps[endIdx],
                landingStart, endIdx);
    }

    /**
     * Détecte la phase avec la concentration en oxygène la plus haute (> 50%).
     *
     * <p>Parcourt les trois phases (décollage, croisière, atterrissage) et retourne
     * le nom de celle dont la moyenne en O₂ dépasse 50%. Si aucune ne dépasse ce seuil,
     * retourne {@code Constants.OXYGEN_PHASE_NONE}.</p>
     *
     * @param flight les données de vol
     * @return nom de la phase avec O₂ > 50%, ou "none" si aucune
     */
    public static String detectOxygenPhase(Flightdata flight) {
        double[] oxygen = flight.getColumnValues("oxygen_mask");

        if (oxygen.length == 0) {
            return Constants.OXYGEN_PHASE_NONE;
        }

        FlightPhase takeOff = detectTakeOff(flight);
        FlightPhase cruise = detectCruise(flight);
        FlightPhase landing = detectLanding(flight);

        String bestPhase = Constants.OXYGEN_PHASE_NONE;
        double bestOxygen = Constants.OXYGEN_PHASE_THRESHOLD;

        // Vérifier chaque phase
        if (takeOff != null) {
            double avg = averageInRange(oxygen, takeOff.getStartIndex(), takeOff.getEndIndex());
            if (avg > bestOxygen) {
                bestOxygen = avg;
                bestPhase = "takeOff";
            }
        }

        if (cruise != null) {
            double avg = averageInRange(oxygen, cruise.getStartIndex(), cruise.getEndIndex());
            if (avg > bestOxygen) {
                bestOxygen = avg;
                bestPhase = "cruise";
            }
        }

        if (landing != null) {
            double avg = averageInRange(oxygen, landing.getStartIndex(), landing.getEndIndex());
            if (avg > bestOxygen) {
                bestPhase = "landing";
            }
        }

        return bestPhase;
    }

    /**
     * Détecte la phase avec la puissance moteur moyenne la plus haute.
     *
     * @param flight les données de vol
     * @return nom de la phase la plus puissante, ou "not detected" si aucune
     */
    public static String detectMostPowerPhase(Flightdata flight) {
        double[] totalPower = Calculator.computeTotalEnginePower(flight);

        FlightPhase takeOff = detectTakeOff(flight);
        FlightPhase cruise = detectCruise(flight);
        FlightPhase landing = detectLanding(flight);

        return findPhaseWithMax(totalPower, takeOff, cruise, landing);
    }

    /**
     * Détecte la phase avec la fréquence cardiaque moyenne la plus haute (pilote le plus stressé).
     *
     * @param flight les données de vol
     * @return nom de la phase la plus stressante, ou "not detected" si aucune
     */
    public static String detectMostStressPhase(Flightdata flight) {
        double[] heartRate = flight.getColumnValues("heart_rate");

        FlightPhase takeOff = detectTakeOff(flight);
        FlightPhase cruise = detectCruise(flight);
        FlightPhase landing = detectLanding(flight);

        return findPhaseWithMax(heartRate, takeOff, cruise, landing);
    }

    /**
     * Détecte la phase avec l'accélération horizontale moyenne la plus haute.
     *
     * @param flight les données de vol
     * @return nom de la phase la plus accélérée, ou "not detected" si aucune
     */
    public static String detectMostAccelPhase(Flightdata flight) {
        double[] speeds = flight.getColumnValues("air_speed");
        double[] timestamps = flight.getTimestamps();
        double[] accel = Calculator.computeAcceleration(speeds, timestamps);

        // Valeur absolue pour trouver la phase la plus "agitée"
        double[] absAccel = new double[accel.length];
        for (int i = 0; i < accel.length; i++) {
            absAccel[i] = Math.abs(accel[i]);
        }

        FlightPhase takeOff = detectTakeOff(flight);
        FlightPhase cruise = detectCruise(flight);
        FlightPhase landing = detectLanding(flight);

        return findPhaseWithMax(absAccel, takeOff, cruise, landing);
    }

    /**
     * Trouve la phase dont la moyenne des valeurs est la plus haute.
     *
     * @param values  tableau de valeurs
     * @param takeOff phase de décollage (peut être null)
     * @param cruise  phase de croisière (peut être null)
     * @param landing phase d'atterrissage (peut être null)
     * @return nom de la phase gagnante, ou "not detected"
     */
    private static String findPhaseWithMax(double[] values,
                                           FlightPhase takeOff,
                                           FlightPhase cruise,
                                           FlightPhase landing) {
        String bestPhase = Constants.PHASE_NOT_DETECTED;
        double bestAvg = Double.NEGATIVE_INFINITY;

        if (takeOff != null && values.length > 0) {
            double avg = averageInRange(values, takeOff.getStartIndex(), takeOff.getEndIndex());
            if (avg > bestAvg) {
                bestAvg = avg;
                bestPhase = "takeOff";
            }
        }

        if (cruise != null && values.length > 0) {
            double avg = averageInRange(values, cruise.getStartIndex(), cruise.getEndIndex());
            if (avg > bestAvg) {
                bestAvg = avg;
                bestPhase = "cruise";
            }
        }

        if (landing != null && values.length > 0) {
            double avg = averageInRange(values, landing.getStartIndex(), landing.getEndIndex());
            if (avg > bestAvg) {
                bestPhase = "landing";
            }
        }

        return bestPhase;
    }

    /**
     * Calcule la moyenne d'un tableau de valeurs sur un intervalle d'indices.
     *
     * @param values    tableau de valeurs
     * @param fromIndex indice de début (inclus)
     * @param toIndex   indice de fin (inclus)
     * @return la moyenne sur l'intervalle, ou 0.0 si l'intervalle est vide
     */
    public static double averageInRange(double[] values, int fromIndex, int toIndex) {
        if (fromIndex > toIndex || fromIndex < 0 || toIndex >= values.length) {
            return 0.0;
        }
        double sum = 0.0;
        for (int i = fromIndex; i <= toIndex; i++) {
            sum += values[i];
        }
        return sum / (toIndex - fromIndex + 1);
    }

    /**
     * Calcule le ratio distance réelle / distance point-à-point.
     *
     * <p>La distance réelle est la somme des segments GPS (Haversine).
     * La distance point-à-point est la distance directe entre le départ et l'arrivée.</p>
     *
     * @param flight les données de vol
     * @return ratio formaté avec 2 décimales
     */
    public static String ratioDistance(Flightdata flight) {
        double[] lats = flight.getColumnValues("latitude");
        double[] lons = flight.getColumnValues("longitude");

        if (lats.length < 2) {
            return String.format(Constants.DECIMAL_FORMAT, 1.0);
        }

        double realDistance = Calculator.totalFlightDistance(flight);
        double directDistance = Calculator.haversineDistance(
                lats[0], lons[0], lats[lats.length - 1], lons[lons.length - 1]);

        if (directDistance == 0.0) {
            return String.format(Constants.DECIMAL_FORMAT, 1.0);
        }

        return String.format(Constants.DECIMAL_FORMAT, realDistance / directDistance);
    }
}