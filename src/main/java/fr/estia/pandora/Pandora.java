
package fr.estia.pandora;

/**
 * pandora is a tool to analyze flight record data to provide summary and
 * high-level information based on low-level sensor data (e.g., fighter jet
 * position).
 * 
 * <h1>Synopsis:</h1>
 * 
 * <pre>
 * pandora java -jar pandora.jar [OPTIONS] ...source
 * </pre>
 * 
 * <h2>Parameters</h2>
 * 
 * <pre>
 * ...source - path to flightRecord files or folder containing flightRecord files 
 * 
 * OPTIONS o:m:hvd
 * -d, --debug,            Debug      - print additional debug information 
 * -h, --help,             Help       - print this help message
 * -m arg, --metadata arg  Metadata   - Print the value of the specified metadata
 * -o arg, --output arg,   Output     - Print only the specified feature at the end
 * -p, --parameters        Parameters - List in alphabetical order the parameters presents in the source
 * -v, --version,          Version    - print the version of the application
 * </pre>
 * 
 * @author Dimitri Masson
 * @author William Delamare
 * 
 */
public class Pandora {
	/**
	 * *
	 * 
	 * @param arguments Arguments passed to the program (-o feature, --version ...)
	 * 
	 */
	public static void main(String[] arguments) {
		System.out.println("pandora@1.0.1");
	}
}
