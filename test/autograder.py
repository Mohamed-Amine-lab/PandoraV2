import json
import subprocess
import sys
import getopt
import sys


def run_command(command):
    process = subprocess.Popen(
        command, stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=True)
    output, error = process.communicate()
    return output.decode().strip()


def filter_tests(tests, implemented_features):
    filtered_tests = []
    for test in tests:
        if test['feature'] in implemented_features:
            filtered_tests.append(test)
    return filtered_tests


def run_feature_tests(tests, path_to_pandora):
    for test in tests:                
        command = f"java -Duser.country=US -Duser.language=en -jar {path_to_pandora} -o {test['feature']} {test['file']}"
        output = run_command(command)
        test['actual_result'] = output
        if output == test['result']:
            test['score'] = 1
        else:
            test['score'] = 0


def run_full_tests(tests, path_to_pandora):
    grouped_tests = {}
    for test in tests:
        if test['file'] not in grouped_tests:
            grouped_tests[test['file']] = []
        grouped_tests[test['file']].append(test)

    for file, tests in grouped_tests.items():
        command = f"java -Duser.country=US -Duser.language=en -jar {path_to_pandora} {file}"
        output = run_command(command)
        output_lines = output.split('\n')
        for test in tests:
            feature = test['feature']
            expected_result = test['result']
            for line in output_lines:
                if line.startswith(feature):
                    actual_result = line.split(':')[1].strip()
                    test['actual_result'] = actual_result
                    if actual_result == expected_result:
                        test['score'] = 1
                    else:
                        test['score'] = 0


def group_tests_by_milestone(tests):
    grouped_tests = {}
    for test in tests:
        milestone = test['milestone']
        if milestone not in grouped_tests:
            grouped_tests[milestone] = []
        grouped_tests[milestone].append(test)
    return grouped_tests


def sort_tests_by_milestone(tests):

    return sorted(tests, key=lambda x: x['milestone'])


def generate_markdown_table(tests):
    markdown = "| id | mode |feature | file | expected result | actual result | score |\n"
    markdown += "|----|------|---------|------|----------------|---------------|-------|\n"
    for test in tests:
        markdown += f"| {test['id']} | {test['mode']} | {test['feature']} | {test['file']} | {test['result']} | {test['actual_result']} | {test.get('score', '')} |\n"
    return markdown


def sum_scores(tests):
    total_score = 0
    for test in tests:
        if 'score' in test:
            total_score += test['score']
    return total_score


def main():

    test_suite_file = ''
    manifest_file = ''
    path_to_pandora = ''

    try:
        opts, args = getopt.getopt(sys.argv[1:], "t:m:")
    except getopt.GetoptError:
        print(
            "Usage: python autograder.py -t <path_test_suit> -m <path_manifest> <pathToPandora>")
        sys.exit(1)

    for opt, arg in opts:
        if opt == '-t':
            test_suite_file = arg
        elif opt == '-m':
            manifest_file = arg

    if len(args) != 1:
        print(
            "Usage: python autograder.py -t <path_test_suit> -m <path_manifest> <pathToPandora>")
        sys.exit(1)

    path_to_pandora = args[0]

    command = f"java -jar {path_to_pandora} --version"
    output = run_command(command)
    print(output)

    # Read test suite and manifest
    with open(test_suite_file, 'r') as f:
        test_suite = json.load(f)
    with open(manifest_file, 'r') as f:
        manifest = json.load(f)

    # Extract data from manifest
    implemented_features = [feature
                            for feature in manifest['features']]

    # Filter tests by implemented features
    filtered_tests = filter_tests(test_suite, implemented_features)

    # Run feature tests
    feature_tests = [
        test for test in filtered_tests if test['mode'] == 'feature']
    run_feature_tests(feature_tests, path_to_pandora)

    # Run full tests
    full_tests = [test for test in filtered_tests if test['mode'] == 'full']
    run_full_tests(full_tests, path_to_pandora)

    # Group tests by milestone and sort by milestone
    sorted_tests = sort_tests_by_milestone(filtered_tests)
    grouped_tests = group_tests_by_milestone(sorted_tests)

    # Generate markdown tables for each milestone
    markdown_tables = []
    for milestone, tests in grouped_tests.items():
        markdown_table = generate_markdown_table(tests)
        markdown_tables.append(markdown_table)

    # Sum scores per milestone and total score
    milestone_scores = {milestone: sum_scores(
        tests) for milestone, tests in grouped_tests.items()}
    total_score = sum_scores(filtered_tests)

    # Print markdown tables and scores
    for markdown_table in markdown_tables:
        print(markdown_table)
        print()
    print("Milestone Scores:")
    print(milestone_scores)
    print("Total Score:", total_score)


if __name__ == '__main__':
    main()
