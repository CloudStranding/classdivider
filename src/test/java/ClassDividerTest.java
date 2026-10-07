import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Set;
import java.util.HashSet;

class ClassDividerTest {
    private ClassDivider divider;
    private Group<Student> klas;

    @BeforeEach
    void setUp() {
        divider = new ClassDivider();
    }

    private Group<Student> createKlas(int size) {
        Group<Student> group = new Group<>();
        for (int i = 1; i <= size; i++) {
            group.add(new Student("Student" + i, "Doe", "ID" + i));
        }
        return group;
    }

    // Helper assertion method
    private void assertValidGroups(Set<Group<Student>> groups, int targetSize, int deviation) {
        int total = 0;
        int min = targetSize - deviation;
        int max = targetSize + deviation;
        Set<Student> allStudents = new HashSet<>();

        for (Group<Student> group : groups) {
            int size = group.size();
            assertTrue(size >= min && size <= max,
                "Group size " + size + " outside range [" + min + "-" + max + "]");
            
            for (Student s : group) {
                assertTrue(allStudents.add(s), "Duplicate student found: " + s);
            }
            
            total += size;
        }
        
        assertEquals(klas.size(), total, "Student count mismatch");
    }

    // Test cases for isDividable()

    @Test
    void testPerfectFitWithMaxDeviation() {
        klas = createKlas(17);
        assertTrue(divider.isDividable(klas, 5, 2));
    }

    @Test
    void testExactDivisionNoRemainder() {
        klas = createKlas(15);
        assertTrue(divider.isDividable(klas, 5, 0));
    }

    @Test
    void testMultipleGroupsWithMaxDeviation() {
        klas = createKlas(24);
        assertTrue(divider.isDividable(klas, 7, 3));
    }

    @Test
    void testSingleOversizedGroup() {
        klas = createKlas(8);
        assertTrue(divider.isDividable(klas, 5, 3));
    }

    @Test
    void testImpossibleDistribution() {
        klas = createKlas(7);
        assertFalse(divider.isDividable(klas, 5, 1));
    }

    @Test
    void testNeedExtraGroupCase() {
        klas = createKlas(19);
        assertTrue(divider.isDividable(klas, 5, 2));
    }

    @Test
    void testMinimumGroupSizeCase() {
        klas = createKlas(3);
        assertTrue(divider.isDividable(klas, 1, 0));
    }

    @Test
    void testLargeDeviationSmallGroups() {
        klas = createKlas(20);
        assertTrue(divider.isDividable(klas, 3, 2));
    }

    @Test
    void testExactMaxOverflowDistribution() {
        klas = createKlas(14); // 4 groups of 3 (+1 deviation)
        assertTrue(divider.isDividable(klas, 3, 1));
    }

    @Test
    void testBorderlinePossibleCase() {
        klas = createKlas(11);
        assertTrue(divider.isDividable(klas, 4, 2));
    }
    
    // Test cases for divide()
    @Test
    void testDivideExactGroups() {
        klas = createKlas(12);
        Set<Group<Student>> groups = divider.divide(klas, 4, 0);
        assertEquals(3, groups.size());
        assertValidGroups(groups, 4, 0);
    }

    @Test
    void testDivideWith1ExtraStudent() {
        klas = createKlas(16);
        Set<Group<Student>> groups = divider.divide(klas, 5, 1);
        assertValidGroups(groups, 5, 1);
    }

    @Test
    void testDivideWithMultipleOverflow() {
        klas = createKlas(23);
        Set<Group<Student>> groups = divider.divide(klas, 5, 2);
        assertValidGroups(groups, 5, 2);
    }

    @Test
    void testDivideMinimumSizeGroups() {
        klas = createKlas(5);
        Set<Group<Student>> groups = divider.divide(klas, 2, 1);
        assertValidGroups(groups, 2, 1);
    }

    @Test
    void testDivideMaximumSizeGroups() {
        klas = createKlas(18);
        Set<Group<Student>> groups = divider.divide(klas, 5, 2);
        assertValidGroups(groups, 5, 2);
    }

    @Test
    void testDivideLargeClass() {
        klas = createKlas(100);
        Set<Group<Student>> groups = divider.divide(klas, 7, 3);
        assertValidGroups(groups, 7, 3);
    }

    @Test
    void testDivideWithExactDeviation() {
        klas = createKlas(14);
        Set<Group<Student>> groups = divider.divide(klas, 4, 2);
        assertValidGroups(groups, 4, 2);
    }

    @Test
    void testDivideSingleGroupCase() {
        klas = createKlas(8);
        Set<Group<Student>> groups = divider.divide(klas, 5, 3);
        assertEquals(1, groups.size());
        assertValidGroups(groups, 5, 3);
    }

    @Test
    void testDivideComplexDistribution() {
        klas = createKlas(29);
        Set<Group<Student>> groups = divider.divide(klas, 6, 2);
        assertValidGroups(groups, 6, 2);
    }

    @Test
    void testDivideAllStudentsAccountedFor() {
        klas = createKlas(37);
        Set<Group<Student>> groups = divider.divide(klas, 8, 3);
        assertValidGroups(groups, 8, 3);
        assertEquals(37, groups.stream().mapToInt(Group::size).sum());
    }
}
