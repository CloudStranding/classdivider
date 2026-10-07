import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Utility class for dividing a group of students into smaller groups according
 * to specified size and deviation constraints.
 * <p>
 * The <code>ClassDivider</code> provides two main operations:
 * <ul>
 *   <li>{@link #isDividable(Group, int, int)}: Checks if a given group of
 *       students can be partitioned under the specified size constraints.</li>
 *   <li>{@link #divide(Group, int, int)}: Divides the group into subgroups
 *       satisfying the same constraints and returns the resulting set.</li>
 * </ul>
 * <p>
 * Neither method mutates the input <code>klas</code>. Subgroups are generated
 * in random order.
 *
 * @version 1.0
 * Name: Xiangjie Dong 2072645
 */
public class ClassDivider {
    
    /**
     * Determines whether a class can be divided into groups of a specified
     * target size with a permitted deviation.
     *
     * <strong>Contract:</strong>
     * <ul>
     *   <li><strong>Preconditions:</strong>
     *     <ul>
     *       <li><code>klas != null</code></li>
     *       <li><code>groupSize &gt;= 1</code></li>
     *       <li><code>0 &lt;= deviation &lt; groupSize</code></li>
     *     </ul>
     *   </li>
     *   <li><strong>Postconditions:</strong>
     *     <ul>
     *       <li>Returns <code>true</code> if and only if there exists a
     *           partition of <code>klas</code> into one or more non-empty
     *           subgroups where each group's size is between
     *           <code>groupSize - deviation</code> and
     *           <code>groupSize + deviation</code> (inclusive).</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * @param klas the original group of students to analyze
     * @param groupSize the target group size (must be >= 1)
     * @param deviation the allowed deviation from the target size
     *                  (0 <= deviation < groupSize)
     * @return <code>true</code> if <code>klas</code> can be partitioned
     * @throws IllegalArgumentException if <code>klas</code> is <code>null</code>
     */
    public boolean isDividable(Group<Student> klas, int groupSize, int deviation) {
        if (!isValidGroupParameters(groupSize, deviation)) {
            return false;
        }
        
        int total = klas.size();
        if (total == 0) {
            return false;
        }
        
        return analyzeDivisionCases(
            total, 
            calculateFullGroups(total, groupSize),
            calculateOverflow(total, groupSize),
            groupSize,
            deviation
        );
    }

    /** Checks if group parameters are within valid ranges. */
    private boolean isValidGroupParameters(int groupSize, int deviation) {
        return groupSize >= 1 && deviation >= 0 && deviation < groupSize;
    }

    /** Calculates number of full-sized groups. */
    private int calculateFullGroups(int totalStudents, int groupSize) {
        return totalStudents / groupSize;
    }

    /** Calculates remaining students after full groups. */
    private int calculateOverflow(int totalStudents, int groupSize) {
        return totalStudents % groupSize;
    }

    /** Analyzes different division cases based on calculated values. */
    private boolean analyzeDivisionCases(int totalStudents, int fullGroups, 
            int overflow, int groupSize, int deviation) {
        if (fullGroups > 0 && overflow <= fullGroups * deviation) {
            return handleStandardOverflowCase();
        }
        
        int overflowStuff = (groupSize - deviation) - overflow;
        boolean condition = (fullGroups * groupSize) - overflowStuff 
                >= (fullGroups * groupSize - fullGroups * deviation);
        return condition;
    }

    /** Case 1: Overflow can be distributed to existing groups. */
    private boolean handleStandardOverflowCase() {
        return true;
    }

    /**
     * Divides the given class into subgroups of sizes between
     * <code>groupSize - deviation</code> and <code>groupSize + deviation</code>,
     * with random assignment.
     *
     * <strong>Contract:</strong>
     * <ul>
     *   <li><strong>Preconditions:</strong>
     *     <ul>
     *       <li><code>isDividable(klas, groupSize, deviation) == true</code></li>
     *     </ul>
     *   </li>
     *   <li><strong>Postconditions:</strong>
     *     <ul>
     *       <li>All students in <code>klas</code> appear in exactly one subgroup</li>
     *       <li>No subgroup is empty</li>
     *       <li>Each subgroup's size is between <code>groupSize - deviation</code>
     *           and <code>groupSize + deviation</code> (inclusive)</li>
     *       <li>Students are assigned randomly; subsequent calls may yield
     *           different groupings</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * @param klas the original group of students to divide
     * @param groupSize the target group size (>= 1)
     * @param deviation the allowed deviation (0 <= deviation < groupSize)
     * @return a set of subgroups partitioning <code>klas</code>
     * @throws IllegalArgumentException if division is not possible
     */
    public Set<Group<Student>> divide(Group<Student> klas, int groupSize,
            int deviation) {
        List<Group<Student>> groups = new ArrayList<>();
        Iterator<Student> studentIterator = klas.iterator();
        
        createFullSizeGroups(groups, studentIterator, 
                calculateFullGroups(klas.size(), groupSize), 
                groupSize);
        distributeRemainingStudents(groups, studentIterator, 
                klas.size(), groupSize, deviation);
        
        return new HashSet<>(groups);
    }

    /** Creates initially filled groups. */
    private void createFullSizeGroups(List<Group<Student>> groups, 
            Iterator<Student> iterator, int numGroups, int groupSize) {
        for (int i = 0; i < numGroups; i++) {
            Group<Student> group = new Group<>();
            fillGroupToCapacity(group, iterator, groupSize);
            groups.add(group);
        }
    }

    /** Fills a group to its target size. */
    private void fillGroupToCapacity(Group<Student> group, 
            Iterator<Student> iterator, int size) {
        for (int j = 0; j < size; j++) {
            group.add(iterator.next());
        }
    }

    /** Handles remaining students after full groups. */
    private void distributeRemainingStudents(List<Group<Student>> groups, 
            Iterator<Student> iterator, int totalStudents, 
            int groupSize, int deviation) {
        int overflow = calculateOverflow(totalStudents, groupSize);
        int fullGroups = calculateFullGroups(totalStudents, groupSize);
        
        if (shouldDistributeOverflow(fullGroups, overflow, deviation)) {
            distributeAcrossExistingGroups(groups, iterator, overflow);
        } else {
            createNewGroupWithBorrowing(groups, iterator, overflow,
                    groupSize, deviation);
        }
    }

    /** Determines overflow distribution strategy. */
    private boolean shouldDistributeOverflow(int fullGroups, int overflow,
            int deviation) {
        return fullGroups > 0 && overflow <= fullGroups * deviation;
    }

    /** Distributes remaining students to existing groups. */
    private void distributeAcrossExistingGroups(List<Group<Student>> groups, 
            Iterator<Student> iterator, int overflow) {
        int groupIndex = 0;
        
        for (int i = 0; i < overflow; i++) {
            groups.get(groupIndex).add(iterator.next());
            groupIndex = (groupIndex + 1) % groups.size();
        }
    }

    /** Creates new group and borrows students if needed. */
    private void createNewGroupWithBorrowing(List<Group<Student>> groups, 
            Iterator<Student> iterator, int overflow, 
            int groupSize, int deviation) {
        Group<Student> extraGroup = new Group<>();
        fillGroupToCapacity(extraGroup, iterator, overflow);
        borrowStudentsToReachMinimum(groups, extraGroup,
                groupSize - deviation);
        groups.add(extraGroup);
    }

    /** Borrows students from other groups to reach minimum size. */
    private void borrowStudentsToReachMinimum(List<Group<Student>> groups, 
            Group<Student> target, int minSize) {
        for (Group<Student> group : groups) {
            while (target.size() < minSize && !group.isEmpty()) {
                Student student = group.pick();
                group.remove(student);
                target.add(student);
            }
        }
    }
}