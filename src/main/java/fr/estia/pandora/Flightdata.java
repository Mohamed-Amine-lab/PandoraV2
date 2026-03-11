package fr.estia.pandora;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stocke l'ensemble des données d'un fichier d'enregistrement de vol (.frd).
 * Contient la section métadonnées et tous les enregistrements de données
 * avec indexation par nom de colonne.
 * Toutes les valeurs sont stockées en unités métriques après conversion si nécessaire.
 *
 * @version 2.0.0
 */
public class Flightdata {

    /** Section métadonnées de l'enregistrement de vol. */
    private final Flightmetadata metadata;

    /** Noms des colonnes de l'en-tête CSV de la section données. */
    private final String[] columnNames;

    /** Table de correspondance : nom de colonne vers son index. */
    private final Map<String, Integer> columnIndex;

    /** Tous les enregistrements de données, chaque ligne est un tableau de valeurs double. */
    private final List<double[]> records;

    /** Chemin du fichier source de cet enregistrement de vol. */
    private final String filePath;

    /**
     * Construit une instance FlightData.
     *
     * @param metadata    section métadonnées parsée
     * @param columnNames noms des colonnes de l'en-tête CSV
     * @param filePath    chemin du fichier source
     */
    public Flightdata(Flightmetadata metadata, String[] columnNames, String filePath) {
        this.metadata = metadata;
        this.columnNames = columnNames;
        this.filePath = filePath;
        this.records = new ArrayList<>();
        this.columnIndex = new HashMap<>();
        for (int i = 0; i < columnNames.length; i++) {
            this.columnIndex.put(columnNames[i], i);
        }
    }

    /**
     * Ajoute un enregistrement de données (une ligne de valeurs capteur).
     *
     * @param record tableau de valeurs double correspondant à l'ordre des colonnes
     */
    public void addRecord(double[] record) {
        records.add(record);
    }

    /**
     * Retourne les métadonnées de ce vol.
     *
     * @return métadonnées du vol
     */
    public Flightmetadata getmetadata() {
        return metadata;
    }

    /**
     * Retourne les noms des colonnes de l'en-tête.
     *
     * @return tableau des noms de colonnes
     */
    public String[] getColumnNames() {
        return columnNames;
    }

    /**
     * Retourne les noms des colonnes triés par ordre alphabétique.
     *
     * @return liste triée des noms de colonnes
     */
    public List<String> getSortedColumnNames() {
        List<String> sorted = new ArrayList<>(Arrays.asList(columnNames));
        Collections.sort(sorted);
        return sorted;
    }

    /**
     * Retourne tous les enregistrements de données.
     *
     * @return liste non modifiable des enregistrements
     */
    public List<double[]> getRecords() {
        return Collections.unmodifiableList(records);
    }

    /**
     * Retourne le nombre d'enregistrements de données.
     *
     * @return nombre d'enregistrements
     */
    public int getRecordCount() {
        return records.size();
    }

    /**
     * Retourne le chemin du fichier source.
     *
     * @return chaîne du chemin de fichier
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Retourne le nom du fichier source sans le chemin du répertoire.
     *
     * @return chaîne du nom de fichier
     */
    public String getFileName() {
        int lastSep = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));
        return lastSep >= 0 ? filePath.substring(lastSep + 1) : filePath;
    }

    /**
     * Vérifie si une colonne existe dans les données.
     *
     * @param name nom de la colonne
     * @return true si la colonne est présente
     */
    public boolean hasColumn(String name) {
        return columnIndex.containsKey(name);
    }

    /**
     * Retourne l'index d'une colonne par son nom.
     *
     * @param name nom de la colonne
     * @return index de la colonne, ou -1 si non trouvée
     */
    public int getColumnIndex(String name) {
        Integer idx = columnIndex.get(name);
        return idx != null ? idx : -1;
    }

    /**
     * Extrait toutes les valeurs d'une colonne spécifique à travers tous les enregistrements.
     * Méthode utilitaire pour faciliter les calculs.
     *
     * @param name nom de la colonne
     * @return tableau des valeurs de cette colonne
     */
    public double[] getColumnValues(String name) {
        int idx = getColumnIndex(name);
        if (idx < 0) {
            return new double[0];
        }
        double[] values = new double[records.size()];
        for (int i = 0; i < records.size(); i++) {
            values[i] = records.get(i)[idx];
        }
        return values;
    }

    /**
     * Retourne les valeurs d'horodatage de tous les enregistrements.
     *
     * @return tableau des horodatages
     */
    public double[] getTimestamps() {
        return getColumnValues("timestamp");
    }
}