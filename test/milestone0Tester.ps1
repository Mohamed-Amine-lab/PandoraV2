# This script is an example on how to test the milestone0 of the pandora project
# There are 2 features to test: Version and Help
# The script will run the pandora.jar with the specified command and compare the output with the expected value
# If the output is the same as the expected value the test will pass, otherwise it will fail
# The expected value is hardcoded in the $tests variable

# You should add more tests to cover all the possible cases

# to run the tests execute the following command in the terminal
# powershell -ExecutionPolicy Bypass -File test/milestone0Tester.ps1


$expectedVersion = "pandora@1.0.0"
$helpMessage = "java -jar pandora.jar [OPTIONS] ...source

...source - path to flightRecord files or folder containing flightRecord files

OPTIONS o:m:bhvd
-b, --batch,            Batch Mode - process all files in the source folder one by one
-d, --debug,            Debug - print additional debug information on Unhandled 
-h, --help,             Help - print this help message
-m arg, --metadata arg  Metadata - Print the value of the specified metadata
-o arg, --output arg,   output - Print only the specified feature at the end
-p, --parameters        Parameters - List in alphabetical order the parameters presents in the source
-v, --version,          Version - print the version of the application 

Implemented Features
"

$tests = @(
    @{
        FeatureName = "Version"
        Command = "--version"
        ExpectedValue = $expectedVersion
    },
    @{
        FeatureName = "Version"
        Command = "-v"
        ExpectedValue = $expectedVersion
    },
    @{
        FeatureName = "Version"
        Command = ""
        ExpectedValue = ""
    },
    @{
        FeatureName = "Help"
        Command = "--help"
        ExpectedValue = $helpMessage
    }, 
    @{
        FeatureName = "Help"
        Command = "-h"
        ExpectedValue = $helpMessage
    }
)

foreach ($test in $tests) {
    #utf8 encoding is required to handle the special characters
    $actualValue = java -jar ./target/pandora.jar $test.Command
    # remove the new line character from the output
    $actualValue = $actualValue -replace "`r`n", ""


    if ($actualValue -eq $test.ExpectedValue -or ($test.ExpectedValue -eq "" -and ($actualValue -eq "" -or $null -eq $actualValue))) {
        Write-Host "$($test.FeatureName) $($test.Command) test passed" -ForegroundColor Green
    } else {
        Write-Host "$($test.FeatureName) $($test.Command) test failed expected : '$($test.ExpectedValue)' got '$($actualValue)'" -ForegroundColor Red        
    }
}
