package fr.estia.pandora;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Stocke la section métadonnées d'un fichier d'enregistrement de vol (.frd).
 * Les champs de métadonnées sont des paires clé-valeur décrivant l'avion et le vol,
 * comme l'identifiant du vol, l'origine, la date, les aéroports et les coefficients aérodynamiques.
 *
 * @version 2.0.0
 */
public class Flightmetadata {

    /** Paires clé-valeur brutes en préservant l'ordre d'insertion. */
    private final Map<String, String> fields;

    /**
     * Construit une instance FlightMetadata vide.
     */
    public Flightmetadata() {
        this.fields = new LinkedHashMap<>();
    }

    /**
     * Ajoute un champ de métadonnées.
     *
     * @param key   le nom du champ (ex : "flight id", "origin")
     * @param value la valeur du champ
     */
    public void put(String key, String value) {
        fields.put(key, value);
    }

    /**
     * Récupère la valeur associée à un champ de métadonnées.
     *
     * @param key le nom du champ
     * @return la valeur du champ, ou null si absent
     */
    public String get(String key) {
        return fields.get(key);
    }

    /**
     * Vérifie si un champ de métadonnées existe.
     *
     * @param key le nom du champ
     * @return true si le champ est présent
     */
    public boolean contains(String key) {
        return fields.containsKey(key);
    }

    /**
     * Retourne tous les champs de métadonnées.
     *
     * @return map de toutes les paires clé-valeur
     */
    public Map<String, String> getAll() {
        return fields;
    }

    /**
     * Retourne l'identifiant du vol.
     *
     * @return la chaîne identifiant le vol
     */
    public String getFlightId() {
        return fields.get("flight id");
    }

    /**
     * Retourne le code du vol (nom du modèle de l'avion).
     *
     * @return la chaîne du code de vol
     */
    public String getFlightCode() {
        return fields.get("flight code");
    }

    /**
     * Retourne l'origine du jet (RU ou US).
     *
     * @return la chaîne d'origine
     */
    public String getOrigin() {
        return fields.get("origin");
    }

    /**
     * Vérifie si ce jet utilise des unités américaines.
     *
     * @return true si l'origine est US
     */
    public boolean isUS() {
        return Constants.ORIGIN_USA.equals(fields.get("origin"));
    }

    /**
     * Vérifie si ce jet utilise des unités russes (métriques).
     *
     * @return true si l'origine est RU
     */
    public boolean isRU() {
        return Constants.ORIGIN_RUSSIA.equals(fields.get("origin"));
    }

    /**
     * Retourne le nom de l'aéroport de départ.
     *
     * @return l'aéroport de départ
     */
    public String getFrom() {
        return fields.get("from");
    }

    /**
     * Retourne le nom de l'aéroport d'arrivée.
     *
     * @return l'aéroport d'arrivée
     */
    public String getTo() {
        return fields.get("to");
    }

    /**
     * Retourne le nombre de moteurs.
     *
     * @return nombre de moteurs, ou 0 si non spécifié
     */
    public int getMotorCount() {
        String val = fields.get("motor(s)");
        return val != null ? Integer.parseInt(val) : 0;
    }

    /**
     * Retourne le coefficient de portance si présent.
     *
     * @return coefficient de portance, ou Double.NaN si non spécifié
     */
    public double getLiftCoef() {
        String val = fields.get("lift coef");
        return val != null ? Double.parseDouble(val) : Double.NaN;
    }

    /**
     * Retourne le coefficient de traînée si présent.
     *
     * @return coefficient de traînée, ou Double.NaN si non spécifié
     */
    public double getDragCoef() {
        String val = fields.get("drag coef");
        return val != null ? Double.parseDouble(val) : Double.NaN;
    }

    /**
     * Retourne la date du vol.
     *
     * @return la chaîne de date au format aaaa-mm-jj
     */
    public String getDate() {
        return fields.get("date");
    }

    /**
     * Retourne l'identifiant du jet composé de l'id du vol et du code de vol.
     *
     * @return identifiant du jet (ex : "201_MiG-23MLD")
     */
    public String getJetId() {
        return getFlightId() + "_" + getFlightCode();
    }
}