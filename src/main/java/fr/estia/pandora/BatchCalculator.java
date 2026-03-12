package fr.estia.pandora;

import java.util.List;

/**
 * Effectue les calculs comparatifs sur plusieurs vols (mode batch).
 *
 * <p>Toutes les méthodes prennent une liste de {@link Flightdata} et retournent
 * soit une valeur formatée, soit le nom du jet correspondant au critère demandé.</p>
 *
 * <p>Le nom du jet retourné est l'identifiant du jet (ex : "201_MiG-23MLD")
 * composé de l'id du vol et du code de vol.</p>
 *
 * @version 2.0.0
 */
public class BatchCalculator {

    /**
     * Calcule la durée cumulée de tous les vols en secondes, formatée en HH:mm:ss.
     *
     * @param flights liste des vols
     * @return durée totale formatée
     */
    public static String cumulDuration(List<Flightdata> flights) {
        double total = 0.0;
        for (Flightdata f : flights) {
            double[] ts = f.getTimestamps();
            total += Calculator.flightDurationSeconds(ts);
        }
        return Calculator.formatDuration(total);
    }

    /**
     * Calcule la distance cumulée de tous les vols en mètres.
     *
     * @param flights liste des vols
     * @return distance totale formatée avec 2 décimales
     */
    public static String cumulDistance(List<Flightdata> flights) {
        double total = 0.0;
        for (Flightdata f : flights) {
            total += Calculator.totalFlightDistance(f);
        }
        return String.format(Constants.DECIMAL_FORMAT, total);
    }

    /**
     * Retourne l'aéroport de départ le plus utilisé parmi tous les vols.
     *
     * @param flights liste des vols
     * @return nom de l'aéroport de départ le plus fréquent
     */
    public static String airportTakeOff(List<Flightdata> flights) {
        return mostFrequentMetadata(flights, "from");
    }

    /**
     * Retourne l'aéroport d'arrivée le plus utilisé parmi tous les vols.
     *
     * @param flights liste des vols
     * @return nom de l'aéroport d'arrivée le plus fréquent
     */
    public static String airportLanding(List<Flightdata> flights) {
        return mostFrequentMetadata(flights, "to");
    }

    /**
     * Retourne le jet avec le coefficient de traînée le plus élevé.
     *
     * @param flights liste des vols
     * @return identifiant du jet
     */
    public static String highestDrag(List<Flightdata> flights) {
        return jetWithMaxMetadata(flights, "drag coef");
    }

    /**
     * Retourne le jet avec le coefficient de traînée le plus faible.
     *
     * @param flights liste des vols
     * @return identifiant du jet
     */
    public static String smallestDrag(List<Flightdata> flights) {
        return jetWithMinMetadata(flights, "drag coef");
    }

    /**
     * Retourne le jet avec le coefficient de portance le plus élevé.
     *
     * @param flights liste des vols
     * @return identifiant du jet
     */
    public static String highestLift(List<Flightdata> flights) {
        return jetWithMaxMetadata(flights, "lift coef");
    }

    /**
     * Retourne le jet avec le coefficient de portance le plus faible.
     *
     * @param flights liste des vols
     * @return identifiant du jet
     */
    public static String smallestLift(List<Flightdata> flights) {
        return jetWithMinMetadata(flights, "lift coef");
    }

    /**
     * Retourne le jet avec la vitesse air moyenne la plus élevée.
     *
     * @param flights liste des vols
     * @return identifiant du jet
     */
    public static String highestSpeed(List<Flightdata> flights) {
        Flightdata best = null;
        double bestVal = Double.NEGATIVE_INFINITY;
        for (Flightdata f : flights) {
            double val = Calculator.average(f.getColumnValues("air_speed"));
            if (val > bestVal) {
                bestVal = val;
                best = f;
            }
        }
        return best != null ? best.getmetadata().getJetId() : "";
    }

    /**
     * Retourne le jet avec la vitesse air moyenne la plus faible.
     *
     * @param flights liste des vols
     * @return identifiant du jet
     */
    public static String slowestSpeed(List<Flightdata> flights) {
        Flightdata best = null;
        double bestVal = Double.POSITIVE_INFINITY;
        for (Flightdata f : flights) {
            double val = Calculator.average(f.getColumnValues("air_speed"));
            if (val < bestVal) {
                bestVal = val;
                best = f;
            }
        }
        return best != null ? best.getmetadata().getJetId() : "";
    }

    /**
     * Retourne le jet qui a volé à l'altitude maximale la plus haute.
     *
     * @param flights liste des vols
     * @return identifiant du jet
     */
    public static String highestAltitude(List<Flightdata> flights) {
        Flightdata best = null;
        double bestVal = Double.NEGATIVE_INFINITY;
        for (Flightdata f : flights) {
            double val = Calculator.max(f.getColumnValues("altitude"));
            if (val > bestVal) {
                bestVal = val;
                best = f;
            }
        }
        return best != null ? best.getmetadata().getJetId() : "";
    }

    /**
     * Retourne le jet qui a volé le plus longtemps.
     *
     * @param flights liste des vols
     * @return identifiant du jet
     */
    public static String longestDuration(List<Flightdata> flights) {
        Flightdata best = null;
        double bestVal = Double.NEGATIVE_INFINITY;
        for (Flightdata f : flights) {
            double val = Calculator.flightDurationSeconds(f.getTimestamps());
            if (val > bestVal) {
                bestVal = val;
                best = f;
            }
        }
        return best != null ? best.getmetadata().getJetId() : "";
    }

    /**
     * Retourne le jet qui a atterri en premier (horodatage de fin le plus tôt).
     *
     * @param flights liste des vols
     * @return identifiant du jet
     */
    public static String firstLanding(List<Flightdata> flights) {
        Flightdata best = null;
        double bestVal = Double.POSITIVE_INFINITY;
        for (Flightdata f : flights) {
            double[] ts = f.getTimestamps();
            if (ts.length > 0) {
                double end = ts[ts.length - 1];
                if (end < bestVal) {
                    bestVal = end;
                    best = f;
                }
            }
        }
        return best != null ? best.getmetadata().getJetId() : "";
    }

    /**
     * Retourne le jet qui a atterri en dernier (horodatage de fin le plus tardif).
     *
     * @param flights liste des vols
     * @return identifiant du jet
     */
    public static String lastLanding(List<Flightdata> flights) {
        Flightdata best = null;
        double bestVal = Double.NEGATIVE_INFINITY;
        for (Flightdata f : flights) {
            double[] ts = f.getTimestamps();
            if (ts.length > 0) {
                double end = ts[ts.length - 1];
                if (end > bestVal) {
                    bestVal = end;
                    best = f;
                }
            }
        }
        return best != null ? best.getmetadata().getJetId() : "";
    }

    /**
     * Retourne le jet avec la puissance moteur moyenne la plus haute.
     *
     * @param flights liste des vols
     * @return identifiant du jet
     */
    public static String highestPower(List<Flightdata> flights) {
        Flightdata best = null;
        double bestVal = Double.NEGATIVE_INFINITY;
        for (Flightdata f : flights) {
            double val = Calculator.average(Calculator.computeTotalEnginePower(f));
            if (val > bestVal) {
                bestVal = val;
                best = f;
            }
        }
        return best != null ? best.getmetadata().getJetId() : "";
    }

    /**
     * Retourne le jet avec la concentration en oxygène moyenne la plus haute.
     *
     * @param flights liste des vols
     * @return identifiant du jet
     */
    public static String highestOxygen(List<Flightdata> flights) {
        Flightdata best = null;
        double bestVal = Double.NEGATIVE_INFINITY;
        for (Flightdata f : flights) {
            double val = Calculator.average(f.getColumnValues("oxygen_mask"));
            if (val > bestVal) {
                bestVal = val;
                best = f;
            }
        }
        return best != null ? best.getmetadata().getJetId() : "";
    }

    /**
     * Retourne le jet dont le pilote a la fréquence cardiaque moyenne la plus haute.
     *
     * @param flights liste des vols
     * @return identifiant du jet
     */
    public static String highestHeartBeat(List<Flightdata> flights) {
        Flightdata best = null;
        double bestVal = Double.NEGATIVE_INFINITY;
        for (Flightdata f : flights) {
            double val = Calculator.average(f.getColumnValues("heart_rate"));
            if (val > bestVal) {
                bestVal = val;
                best = f;
            }
        }
        return best != null ? best.getmetadata().getJetId() : "";
    }

    /**
     * Retourne le jet dont le pilote a la fréquence cardiaque moyenne la plus basse.
     *
     * @param flights liste des vols
     * @return identifiant du jet
     */
    public static String lowestHeartBeat(List<Flightdata> flights) {
        Flightdata best = null;
        double bestVal = Double.POSITIVE_INFINITY;
        for (Flightdata f : flights) {
            double val = Calculator.average(f.getColumnValues("heart_rate"));
            if (val < bestVal) {
                bestVal = val;
                best = f;
            }
        }
        return best != null ? best.getmetadata().getJetId() : "";
    }

    /**
     * Retourne les paires de jets dont les trajectoires se croisent à moins de 50 km.
     *
     * <p>Compare chaque paire de vols et vérifie si, à un moment donné, les deux avions
     * étaient à moins de {@link Constants#CLOSE_FLIGHT_THRESHOLD_KM} km l'un de l'autre.</p>
     *
     * @param flights liste des vols
     * @return chaîne de résultat avec les paires proches
     */
    public static String closeFlight(List<Flightdata> flights) {
        return closeFlightFiltered(flights, null);
    }

    /**
     * Retourne les paires de jets proches ayant la même origine (US/RU).
     *
     * @param flights liste des vols
     * @return chaîne de résultat avec les paires proches de même origine
     */
    public static String closeFlightSameOrigin(List<Flightdata> flights) {
        return closeFlightFiltered(flights, Boolean.TRUE);
    }

    /**
     * Retourne les paires de jets proches ayant des origines différentes (US/RU).
     *
     * @param flights liste des vols
     * @return chaîne de résultat avec les paires proches d'origines différentes
     */
    public static String closeFlightDiffOrigin(List<Flightdata> flights) {
        return closeFlightFiltered(flights, Boolean.FALSE);
    }

    /**
     * Filtre les paires de vols proches selon leur origine.
     *
     * @param flights    liste des vols
     * @param sameOrigin true = même origine, false = origines différentes, null = toutes les paires
     * @return chaîne de résultat formatée
     */
    private static String closeFlightFiltered(List<Flightdata> flights, Boolean sameOrigin) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < flights.size(); i++) {
            for (int j = i + 1; j < flights.size(); j++) {
                Flightdata f1 = flights.get(i);
                Flightdata f2 = flights.get(j);

                // Filtrage par origine si demandé
                if (sameOrigin != null) {
                    boolean sameSrc = f1.getmetadata().getOrigin()
                            .equals(f2.getmetadata().getOrigin());
                    if (sameSrc != sameOrigin) {
                        continue;
                    }
                }

                if (areFlightsClose(f1, f2)) {
                    if (result.length() > 0) {
                        result.append("\n");
                    }
                    result.append(f1.getmetadata().getJetId())
                            .append(" / ")
                            .append(f2.getmetadata().getJetId());
                }
            }
        }

        return result.length() > 0 ? result.toString() : "none";
    }

    /**
     * Vérifie si deux vols se sont approchés à moins de 50 km l'un de l'autre.
     *
     * <p>Compare les positions GPS des deux vols aux mêmes horodatages (interpolation simple :
     * on compare les points dans l'ordre disponible).</p>
     *
     * @param f1 premier vol
     * @param f2 deuxième vol
     * @return true si les deux vols se sont croisés à moins de 50 km
     */
    private static boolean areFlightsClose(Flightdata f1, Flightdata f2) {
        double[] lats1 = f1.getColumnValues("latitude");
        double[] lons1 = f1.getColumnValues("longitude");
        double[] lats2 = f2.getColumnValues("latitude");
        double[] lons2 = f2.getColumnValues("longitude");

        int minLen = Math.min(Math.min(lats1.length, lats2.length),
                Math.min(lons1.length, lons2.length));

        for (int i = 0; i < minLen; i++) {
            double distKm = Calculator.haversineDistance(
                    lats1[i], lons1[i], lats2[i], lons2[i]) / 1000.0;
            if (distKm < Constants.CLOSE_FLIGHT_THRESHOLD_KM) {
                return true;
            }
        }
        return false;
    }

    /**
     * Trouve la valeur de métadonnée la plus fréquente parmi tous les vols.
     *
     * @param flights liste des vols
     * @param key     clé de métadonnée
     * @return la valeur la plus fréquente
     */
    private static String mostFrequentMetadata(List<Flightdata> flights, String key) {
        java.util.Map<String, Integer> counts = new java.util.HashMap<>();
        for (Flightdata f : flights) {
            String val = f.getmetadata().get(key);
            if (val != null) {
                counts.merge(val, 1, Integer::sum);
            }
        }
        return counts.entrySet().stream()
                .max(java.util.Map.Entry.comparingByValue())
                .map(java.util.Map.Entry::getKey)
                .orElse("");
    }

    /**
     * Trouve le jet avec la valeur de métadonnée numérique la plus élevée.
     *
     * @param flights liste des vols
     * @param key     clé de métadonnée numérique
     * @return identifiant du jet
     */
    private static String jetWithMaxMetadata(List<Flightdata> flights, String key) {
        Flightdata best = null;
        double bestVal = Double.NEGATIVE_INFINITY;
        for (Flightdata f : flights) {
            String val = f.getmetadata().get(key);
            if (val != null) {
                try {
                    double d = Double.parseDouble(val);
                    if (d > bestVal) {
                        bestVal = d;
                        best = f;
                    }
                } catch (NumberFormatException ignored) {
                    // Ignorer les valeurs non numériques
                }
            }
        }
        return best != null ? best.getmetadata().getJetId() : "";
    }

    /**
     * Trouve le jet avec la valeur de métadonnée numérique la plus faible.
     *
     * @param flights liste des vols
     * @param key     clé de métadonnée numérique
     * @return identifiant du jet
     */
    private static String jetWithMinMetadata(List<Flightdata> flights, String key) {
        Flightdata best = null;
        double bestVal = Double.POSITIVE_INFINITY;
        for (Flightdata f : flights) {
            String val = f.getmetadata().get(key);
            if (val != null) {
                try {
                    double d = Double.parseDouble(val);
                    if (d < bestVal) {
                        bestVal = d;
                        best = f;
                    }
                } catch (NumberFormatException ignored) {
                    // Ignorer les valeurs non numériques
                }
            }
        }
        return best != null ? best.getmetadata().getJetId() : "";
    }
}