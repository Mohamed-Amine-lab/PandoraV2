package fr.estia.pandora;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Parse les fichiers d'enregistrement de vol (.frd) en objets FlightData.
 * Gère les formats russe (métrique) et américain (impérial),
 * en convertissant toutes les valeurs US en unités métriques pendant le parsing
 * pour garantir la cohérence des calculs en aval.
 *
 * <p>Structure du fichier : section métadonnées, ligne vide, section données CSV.</p>
 *
 * @version 2.0.0
 */
public class Flightrecordparser {

    /**
     * Parse un seul fichier .frd en un objet FlightData.
     * Les fichiers US sont automatiquement convertis en unités métriques.
     *
     * @param filePath chemin vers le fichier .frd
     * @return FlightData parsé
     * @throws IOException si le fichier ne peut pas être lu
     */
    public static Flightdata parse(String filePath) throws IOException {
        Flightmetadata metadata = new Flightmetadata();
        String[] columnNames = null;
        List<double[]> rawRecords = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            // Lecture de la section métadonnées (jusqu'à la ligne vide)
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    break;
                }
                int colonIndex = line.indexOf(':');
                if (colonIndex > 0) {
                    String key = line.substring(0, colonIndex).trim();
                    String value = line.substring(colonIndex + 1).trim();
                    metadata.put(key, value);
                }
            }

            // Lecture de l'en-tête CSV
            line = reader.readLine();
            if (line != null) {
                columnNames = line.trim().split(",");
            }

            // Lecture des enregistrements de données
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] parts = line.split(",");
                double[] values = new double[parts.length];
                for (int i = 0; i < parts.length; i++) {
                    values[i] = Double.parseDouble(parts[i].trim());
                }
                rawRecords.add(values);
            }
        }

        if (columnNames == null) {
            throw new IOException("Fichier d'enregistrement invalide : en-tête de données absent dans " + filePath);
        }

        Flightdata flightData = new Flightdata(metadata, columnNames, filePath);

        // Conversion des unités US en métrique si nécessaire, puis ajout des enregistrements
        boolean isUS = metadata.isUS();
        int altIdx = indexOf(columnNames, "altitude");
        int airSpeedIdx = indexOf(columnNames, "air_speed");
        int tempIdx = indexOf(columnNames, "temperature_in");
        int humidIdx = indexOf(columnNames, "humidity_in");
        int pressIdx = indexOf(columnNames, "pressure_in");
        int oxyIdx = indexOf(columnNames, "oxygen_mask");

        // Recherche de toutes les colonnes moteur
        List<Integer> engineIndices = new ArrayList<>();
        for (int i = 0; i < columnNames.length; i++) {
            if (columnNames[i].startsWith("engine_")) {
                engineIndices.add(i);
            }
        }

        for (double[] record : rawRecords) {
            if (isUS) {
                convertUSToMetric(record, altIdx, airSpeedIdx, tempIdx,
                        humidIdx, pressIdx, oxyIdx, engineIndices);
            }
            flightData.addRecord(record);
        }

        return flightData;
    }

    /**
     * Parse tous les fichiers .frd d'un répertoire.
     *
     * @param dirPath chemin vers le répertoire
     * @return liste d'objets FlightData parsés
     * @throws IOException si un fichier ne peut pas être lu
     */
    public static List<Flightdata> parseDirectory(String dirPath) throws IOException {
        List<Flightdata> flights = new ArrayList<>();
        File dir = new File(dirPath);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".frd"));
        if (files != null) {
            for (File file : files) {
                flights.add(parse(file.getAbsolutePath()));
            }
        }
        return flights;
    }

    /**
     * Parse plusieurs chemins sources (fichiers ou répertoires).
     *
     * @param sources tableau de chemins de fichiers ou répertoires
     * @return liste d'objets FlightData parsés
     * @throws IOException si un fichier ne peut pas être lu
     */
    public static List<Flightdata> parseSources(String[] sources) throws IOException {
        List<Flightdata> flights = new ArrayList<>();
        for (String source : sources) {
            File f = new File(source);
            if (f.isDirectory()) {
                flights.addAll(parseDirectory(source));
            } else if (f.isFile() && source.endsWith(".frd")) {
                flights.add(parse(source));
            }
        }
        return flights;
    }

    /**
     * Convertit un enregistrement US en unités métriques sur place.
     * - altitude : pieds vers mètres
     * - vitesse air : mph vers m/s
     * - moteur : chevaux vers watts
     * - température : Kelvin vers Celsius
     * - humidité : ratio (0-1) vers pourcentage (0-100)
     * - pression : psi vers Pascal
     * - oxygène : ratio (0-1) vers pourcentage (0-100)
     *
     * @param record         enregistrement de données à convertir
     * @param altIdx         index de la colonne altitude
     * @param airSpeedIdx    index de la colonne vitesse air
     * @param tempIdx        index de la colonne température
     * @param humidIdx       index de la colonne humidité
     * @param pressIdx       index de la colonne pression
     * @param oxyIdx         index de la colonne masque oxygène
     * @param engineIndices  indices des colonnes moteur
     */
    private static void convertUSToMetric(double[] record, int altIdx, int airSpeedIdx,
            int tempIdx, int humidIdx, int pressIdx, int oxyIdx,
            List<Integer> engineIndices) {
        if (altIdx >= 0) {
            record[altIdx] = record[altIdx] * Constants.FEET_TO_METER;
        }
        if (airSpeedIdx >= 0) {
            record[airSpeedIdx] = record[airSpeedIdx] * Constants.MPH_TO_MS;
        }
        for (int idx : engineIndices) {
            record[idx] = record[idx] * Constants.HP_TO_WATT;
        }
        if (tempIdx >= 0) {
            record[tempIdx] = record[tempIdx] - Constants.KELVIN_OFFSET;
        }
        if (humidIdx >= 0) {
            record[humidIdx] = record[humidIdx] * Constants.RATIO_TO_PERCENT;
        }
        if (pressIdx >= 0) {
            record[pressIdx] = record[pressIdx] * Constants.PSI_TO_PA;
        }
        if (oxyIdx >= 0) {
            record[oxyIdx] = record[oxyIdx] * Constants.RATIO_TO_PERCENT;
        }
    }

    /**
     * Trouve l'index d'un nom de colonne dans le tableau d'en-tête.
     *
     * @param columns tableau des noms de colonnes
     * @param name    nom de la colonne à trouver
     * @return index de la colonne, ou -1 si non trouvée
     */
    private static int indexOf(String[] columns, String name) {
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].equals(name)) {
                return i;
            }
        }
        return -1;
    }
}