package fr.estia.pandora;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Effectue tous les calculs de données de vol pour les fonctionnalités de vol unique.
 * Toutes les valeurs d'entrée sont attendues en unités métriques
 * (la conversion se fait au moment du parsing).
 * Les résultats sont retournés en double bruts ; le formatage est géré par l'appelant.
 *
 * @version 2.0.0
 */
public class Calculator {

    /** Formateur pour l'affichage au format HH:mm:ss. */
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());

    /**
     * Calcule la moyenne d'un tableau de valeurs.
     *
     * @param values tableau de valeurs double
     * @return la moyenne arithmétique
     */
    public static double average(double[] values) {
        if (values.length == 0) {
            return 0.0;
        }
        double sum = 0.0;
        for (double v : values) {
            sum += v;
        }
        return sum / values.length;
    }

    /**
     * Trouve la valeur maximale dans un tableau.
     *
     * @param values tableau de valeurs double
     * @return la valeur maximale
     */
    public static double max(double[] values) {
        double maxVal = Double.NEGATIVE_INFINITY;
        for (double v : values) {
            if (v > maxVal) {
                maxVal = v;
            }
        }
        return maxVal;
    }

    /**
     * Trouve la valeur minimale dans un tableau.
     *
     * @param values tableau de valeurs double
     * @return la valeur minimale
     */
    public static double min(double[] values) {
        double minVal = Double.POSITIVE_INFINITY;
        for (double v : values) {
            if (v < minVal) {
                minVal = v;
            }
        }
        return minVal;
    }

    /**
     * Calcule la puissance totale des moteurs pour chaque enregistrement
     * en additionnant toutes les colonnes moteur.
     *
     * @param flight les données de vol
     * @return tableau des puissances totales par enregistrement
     */
    public static double[] computeTotalEnginePower(Flightdata flight) {
        String[] cols = flight.getColumnNames();
        List<double[]> records = flight.getRecords();
        int n = records.size();
        double[] totalPower = new double[n];

        for (int c = 0; c < cols.length; c++) {
            if (cols[c].startsWith("engine_")) {
                for (int i = 0; i < n; i++) {
                    totalPower[i] += records.get(i)[c];
                }
            }
        }
        return totalPower;
    }

    /**
     * Calcule la durée du vol en secondes à partir des horodatages.
     *
     * @param timestamps tableau des horodatages
     * @return durée en secondes
     */
    public static double flightDurationSeconds(double[] timestamps) {
        if (timestamps.length < 2) {
            return 0.0;
        }
        return timestamps[timestamps.length - 1] - timestamps[0];
    }

    /**
     * Formate une durée en secondes au format HH:mm:ss.
     *
     * @param seconds durée en secondes
     * @return chaîne formatée
     */
    public static String formatDuration(double seconds) {
        long totalSec = Math.round(seconds);
        long hours = totalSec / 3600;
        long minutes = (totalSec % 3600) / 60;
        long secs = totalSec % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    /**
     * Formate un horodatage Unix au format HH:mm:ss dans le fuseau horaire système.
     *
     * @param timestamp horodatage Unix en secondes
     * @return chaîne de temps formatée
     */
    public static String formatTimestamp(double timestamp) {
        Instant instant = Instant.ofEpochSecond((long) timestamp,
                (long) ((timestamp % 1) * 1_000_000_000));
        return TIME_FORMATTER.format(instant);
    }

    /**
     * Calcule la distance de Haversine entre deux coordonnées GPS.
     *
     * @param lat1 latitude du point 1 en degrés
     * @param lon1 longitude du point 1 en degrés
     * @param lat2 latitude du point 2 en degrés
     * @param lon2 longitude du point 2 en degrés
     * @return distance en mètres
     */
    public static double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return Constants.EARTH_RADIUS_KM * 1000.0 * c;
    }

    /**
     * Calcule la distance totale du vol en additionnant les distances de Haversine
     * entre les coordonnées GPS consécutives.
     *
     * @param flight les données de vol
     * @return distance totale en mètres
     */
    public static double totalFlightDistance(Flightdata flight) {
        double[] lats = flight.getColumnValues("latitude");
        double[] lons = flight.getColumnValues("longitude");
        double totalDist = 0.0;
        for (int i = 1; i < lats.length; i++) {
            totalDist += haversineDistance(lats[i - 1], lons[i - 1], lats[i], lons[i]);
        }
        return totalDist;
    }

    /**
     * Calcule la vitesse entre les points GPS consécutifs.
     * La vitesse du premier point est répétée depuis le deuxième (spécification du projet).
     *
     * @param flight les données de vol
     * @return tableau des vitesses calculées en m/s
     */
    public static double[] computeGpsSpeed(Flightdata flight) {
        double[] lats = flight.getColumnValues("latitude");
        double[] lons = flight.getColumnValues("longitude");
        double[] timestamps = flight.getTimestamps();
        int n = lats.length;
        double[] speeds = new double[n];

        for (int i = 1; i < n; i++) {
            double dist = haversineDistance(lats[i - 1], lons[i - 1], lats[i], lons[i]);
            double dt = timestamps[i] - timestamps[i - 1];
            speeds[i] = dt > 0 ? dist / dt : 0.0;
        }
        // Répéter la première valeur depuis la deuxième (spécification)
        if (n > 1) {
            speeds[0] = speeds[1];
        }
        return speeds;
    }

    /**
     * Calcule l'accélération entre les valeurs de vitesse consécutives.
     * Les deux premiers points sont répétés depuis le troisième (spécification du projet).
     *
     * @param speeds     tableau des valeurs de vitesse
     * @param timestamps tableau des horodatages
     * @return tableau des valeurs d'accélération en m/s²
     */
    public static double[] computeAcceleration(double[] speeds, double[] timestamps) {
        int n = speeds.length;
        double[] accel = new double[n];

        for (int i = 1; i < n; i++) {
            double dt = timestamps[i] - timestamps[i - 1];
            accel[i] = dt > 0 ? (speeds[i] - speeds[i - 1]) / dt : 0.0;
        }
        // Répéter la première valeur (spécification)
        if (n > 1) {
            accel[0] = accel[1];
        }
        return accel;
    }

    /**
     * Calcule la vitesse du vent comme la différence entre la vitesse air et la vitesse au sol.
     * Vitesse du vent = |vitesse_air - vitesse_sol| moyennée sur le vol.
     *
     * @param flight les données de vol
     * @return vitesse moyenne du vent en m/s
     */
    public static double averageWindSpeed(Flightdata flight) {
        double[] airSpeeds = flight.getColumnValues("air_speed");
        double[] groundSpeeds = computeGpsSpeed(flight);
        double sum = 0.0;
        int n = Math.min(airSpeeds.length, groundSpeeds.length);
        for (int i = 0; i < n; i++) {
            sum += Math.abs(airSpeeds[i] - groundSpeeds[i]);
        }
        return n > 0 ? sum / n : 0.0;
    }

    /**
     * Convertit une vitesse en m/s en nombre de Mach.
     *
     * @param speedMs vitesse en m/s
     * @return nombre de Mach
     */
    public static double speedToMach(double speedMs) {
        double speedKmh = speedMs * 3.6;
        return speedKmh / Constants.MACH_TO_KMH;
    }

    /**
     * Calcule l'heure de début du vol sous forme de chaîne formatée.
     *
     * @param flight les données de vol
     * @return heure de début au format HH:mm:ss
     */
    public static String startTime(Flightdata flight) {
        double[] timestamps = flight.getTimestamps();
        if (timestamps.length == 0) {
            return "00:00:00";
        }
        return formatTimestamp(timestamps[0]);
    }

    /**
     * Calcule le temps nécessaire pour atteindre 80% de l'altitude maximale du vol.
     *
     * <p>Format de sortie : "Xmin / Ym" (X minutes, Y mètres)</p>
     *
     * @param flight les données de vol
     * @return chaîne formatée "Xmin / Ym"
     */
    public static String reachAlt(Flightdata flight) {
        double[] altitudes = flight.getColumnValues("altitude");
        double[] timestamps = flight.getTimestamps();

        if (altitudes.length == 0) {
            return "0min / 0m";
        }

        double maxAlt = max(altitudes);
        double target = maxAlt * 0.80;

        for (int i = 0; i < altitudes.length; i++) {
            if (altitudes[i] >= target) {
                double elapsedSeconds = timestamps[i] - timestamps[0];
                long minutes = Math.round(elapsedSeconds / 60.0);
                return minutes + "min / " + Math.round(altitudes[i]) + "m";
            }
        }

        // Si jamais atteint (ne devrait pas arriver)
        return Math.round((timestamps[timestamps.length - 1] - timestamps[0]) / 60.0)
                + "min / " + Math.round(maxAlt) + "m";
    }

    /**
     * Calcule le temps nécessaire pour parcourir 80% de la distance totale du vol.
     *
     * <p>Format de sortie : "Xmin / Y.YYkm"</p>
     *
     * @param flight les données de vol
     * @return chaîne formatée "Xmin / Y.YYkm"
     */
    public static String reachDist(Flightdata flight) {
        double[] lats = flight.getColumnValues("latitude");
        double[] lons = flight.getColumnValues("longitude");
        double[] timestamps = flight.getTimestamps();

        if (lats.length < 2) {
            return "0min / 0.00km";
        }

        double totalDist = totalFlightDistance(flight);
        double target = totalDist * 0.80;

        double cumDist = 0.0;
        for (int i = 1; i < lats.length; i++) {
            cumDist += haversineDistance(lats[i - 1], lons[i - 1], lats[i], lons[i]);
            if (cumDist >= target) {
                double elapsedSeconds = timestamps[i] - timestamps[0];
                long minutes = Math.round(elapsedSeconds / 60.0);
                return minutes + "min / " + String.format("%.2f", cumDist / 1000.0) + "km";
            }
        }

        long totalMinutes = Math.round(
                (timestamps[timestamps.length - 1] - timestamps[0]) / 60.0);
        return totalMinutes + "min / " + String.format("%.2f", totalDist / 1000.0) + "km";
    }

    /**
     * Calcule l'altitude et la vitesse du vent maximale sur une fenêtre glissante de 5 minutes.
     *
     * <p>Format de sortie : "Xm : Y.YYm/s"</p>
     *
     * @param flight les données de vol
     * @return chaîne formatée "Xm : Y.YYm/s"
     */
    public static String fastWindAlt(Flightdata flight) {
        double[] altitudes = flight.getColumnValues("altitude");
        double[] airSpeeds = flight.getColumnValues("air_speed");
        double[] timestamps = flight.getTimestamps();
        double[] groundSpeeds = computeGpsSpeed(flight);

        if (altitudes.length == 0) {
            return "0m : 0.00m/s";
        }

        double bestWindSpeed = Double.NEGATIVE_INFINITY;
        double bestAltitude = 0.0;

        // Fenêtre glissante de 5 minutes
        for (int i = 0; i < timestamps.length; i++) {
            double windowEnd = timestamps[i] + Constants.ROLLING_WINDOW_SECONDS;
            double sumWind = 0.0;
            int count = 0;

            for (int j = i; j < timestamps.length && timestamps[j] <= windowEnd; j++) {
                sumWind += Math.abs(airSpeeds[j] - groundSpeeds[j]);
                count++;
            }

            if (count > 0) {
                double avgWind = sumWind / count;
                if (avgWind > bestWindSpeed) {
                    bestWindSpeed = avgWind;
                    bestAltitude = altitudes[i];
                }
            }
        }

        return Math.round(bestAltitude) + "m : "
                + String.format(Constants.DECIMAL_FORMAT, bestWindSpeed) + "m/s";
    }

    /**
     * Calcule l'altitude et la vitesse maximale du jet sur une fenêtre glissante de 5 minutes.
     *
     * <p>Format de sortie : "Xm : Y.YYm/s"</p>
     *
     * @param flight les données de vol
     * @return chaîne formatée "Xm : Y.YYm/s"
     */
    public static String fastJetAlt(Flightdata flight) {
        double[] altitudes = flight.getColumnValues("altitude");
        double[] airSpeeds = flight.getColumnValues("air_speed");
        double[] timestamps = flight.getTimestamps();

        if (altitudes.length == 0) {
            return "0m : 0.00m/s";
        }

        double bestSpeed = Double.NEGATIVE_INFINITY;
        double bestAltitude = 0.0;

        // Fenêtre glissante de 5 minutes
        for (int i = 0; i < timestamps.length; i++) {
            double windowEnd = timestamps[i] + Constants.ROLLING_WINDOW_SECONDS;
            double sumSpeed = 0.0;
            int count = 0;

            for (int j = i; j < timestamps.length && timestamps[j] <= windowEnd; j++) {
                sumSpeed += airSpeeds[j];
                count++;
            }

            if (count > 0) {
                double avgSpeed = sumSpeed / count;
                if (avgSpeed > bestSpeed) {
                    bestSpeed = avgSpeed;
                    bestAltitude = altitudes[i];
                }
            }
        }

        return Math.round(bestAltitude) + "m : "
                + String.format(Constants.DECIMAL_FORMAT, bestSpeed) + "m/s";
    }

    /**
     * Calcule le bruit moyen du capteur de température par rapport à la référence de 25°C.
     *
     * <p>Le bruit est la valeur absolue de l'écart entre chaque mesure et 25°C.</p>
     *
     * @param flight les données de vol
     * @return bruit moyen du capteur formaté avec 2 décimales
     */
    public static String noiseTemp(Flightdata flight) {
        double[] temps = flight.getColumnValues("temperature_in");

        if (temps.length == 0) {
            return String.format(Constants.DECIMAL_FORMAT, 0.0);
        }

        double sumNoise = 0.0;
        for (double t : temps) {
            sumNoise += Math.abs(t - Constants.REFERENCE_TEMP_CELSIUS);
        }

        return String.format(Constants.DECIMAL_FORMAT, sumNoise / temps.length);
    }

    /**
     * Détecte si le pilote a subi une crise de stress pendant le vol.
     *
     * <p>Une crise de stress est détectée si la fréquence cardiaque dépasse
     * la moyenne + 2 écarts-types à un moment quelconque du vol.</p>
     *
     * @param flight les données de vol
     * @return "y" si une crise est détectée, "n" sinon
     */
    public static String stressedPilot(Flightdata flight) {
        double[] heartRates = flight.getColumnValues("heart_rate");

        if (heartRates.length == 0) {
            return "n";
        }

        double avg = average(heartRates);
        double sumSq = 0.0;
        for (double hr : heartRates) {
            sumSq += (hr - avg) * (hr - avg);
        }
        double stdDev = Math.sqrt(sumSq / heartRates.length);
        double threshold = avg + 2 * stdDev;

        for (double hr : heartRates) {
            if (hr > threshold) {
                return "y";
            }
        }
        return "n";
    }
}