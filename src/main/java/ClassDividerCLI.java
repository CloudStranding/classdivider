import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

/**
 * ClassDivider–Divide a class of students into groups.
 *
 * @version 0.1
 * @author Xiangjie Dong 2072645
 */
@Command(
    name = "classdivider",
    mixinStandardHelpOptions = true,
    version = "classdivider 0.7",
    description = "Divide a class of students into groups."
)
public class ClassDividerCLI implements Runnable {

    /**
     * Size of the groups to create.
     */
    @CommandLine.Option(
        names = {"-g", "--group-size"},
        description = "target group size.",
        required = true
    )
    private int groupSize;

    /**
     * Number of students that a group can deviate from the target group size.
     * <p>
     * For example, if the group size is 10 and the deviation is 2, all groups
     * in the group set will have sizes between 8 and 12. Defaults to 1.
     */
    @CommandLine.Option(
        names = {"-d", "--deviation"},
        description = "Permitted deviation from target group size. Defaults to ${DEFAULT-VALUE}.",
        defaultValue = "1"
    )
    private int deviation;

    @Parameters(
        index = "0",
        description = "Path to CSV file with student data."
    )
    private Path studentsFile;

    @Spec
    private CommandSpec commandSpec;

    @Override
    public void run() {
        Group<Student> klas = loadStudents();
        validateParameters();
        checkDivisionPossibility(klas);
        printGroups(klas);
    }

    private Group<Student> loadStudents() {
        try {
            return StudentsFile.fromCSV(studentsFile);
        } catch (IOException e) {
            throw new ParameterException(
                commandSpec.commandLine(),
                String.format("Unable to open or read students file '%s': %s.",
                    studentsFile, e.getMessage())
            );
        }
    }

    private void validateParameters() {
        if (groupSize < 1) {
            throw new ParameterException(
                commandSpec.commandLine(),
                "Group size must be a positive integer."
            );
        }
        if (deviation < 0 || deviation >= groupSize) {
            throw new ParameterException(
                commandSpec.commandLine(),
                "Deviation must be >=0 and less than group size."
            );
        }
    }

    private void checkDivisionPossibility(Group<Student> klas) {
        ClassDivider divider = new ClassDivider();
        if (!divider.isDividable(klas, groupSize, deviation)) {
            throw new ParameterException(
                commandSpec.commandLine(),
                String.format("Unable to divide a class of %d into groups of %d+/-%d students.",
                    klas.size(), groupSize, deviation)
            );
        }
    }

    private void printGroups(Group<Student> klas) {
        Set<Group<Student>> groupSet = new ClassDivider().divide(klas, groupSize, deviation);
        Map<String, Boolean> uniqueFirstName = createFirstNameMap(klas);
        
        int groupNr = 1;
        for (Group<Student> group : groupSet) {
            printGroup(group, uniqueFirstName, groupNr++);
        }
    }

    private Map<String, Boolean> createFirstNameMap(Group<Student> klas) {
        Map<String, Boolean> nameMap = new HashMap<>();
        klas.forEach(s -> nameMap.merge(s.firstName(), true, (a, b) -> false));
        return nameMap;
    }

    private void printGroup(Group<Student> group, Map<String, Boolean> uniqueNames, int number) {
        System.out.printf("Group %d:%n", number);
        group.forEach(student -> 
            System.out.println("- " + formatStudentName(student, uniqueNames))
        );
        System.out.println();
    }

    private String formatStudentName(Student student, Map<String, Boolean> uniqueNames) {
        String name = student.firstName();
        if (!uniqueNames.get(student.firstName())) {
            name += " " + student.sortName().charAt(0);
        }
        return name;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new ClassDividerCLI()).execute(args);
        System.exit(exitCode);
    }
}