package com.mindex.challenge;

import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Employee;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
public class DataBootstrapTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    public void test() {
        Employee employee = employeeRepository.findByEmployeeId("16a596ae-edd3-4847-99fe-c4518e82c86f");
        assertNotNull(employee);
        assertEquals("John", employee.getFirstName());
        assertEquals("Lennon", employee.getLastName());
        assertEquals("Development Manager", employee.getPosition());
        assertEquals("Engineering", employee.getDepartment());
    }

    private static boolean solution(int[] numbers) {
        if (isStrictlySorted(numbers)) {
            return true;
        }
        for (int i = 0; i < numbers.length; i++) {
            String numStr = String.valueOf(numbers[i]);
            if (numStr.length() > 1) {
                Set<String> permutations = generatePermutations(numStr);
                for (String permutation : permutations) {
                    if (permutation.startsWith("0")) {
                        permutation = leftTrim(permutation);
                    }
                    int intPerm = Integer.parseInt(permutation);
                    if (numbers[i] != intPerm) {
                        int[] newNumbers = Arrays.copyOf(numbers, numbers.length);
                        newNumbers[i] = intPerm;
                        if (isStrictlySorted(newNumbers)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private static boolean isStrictlySorted(int[] array) {
        for (int i = 0; i < array.length - 1; i++) {
            if (array[i] >= array[i + 1]) {
                return false;
            }
        }
        return true;
    }

    private int[] removeElement(int[] source, int index) {
        int[] result = new int[source.length - 1];
        System.arraycopy(source, 0, result, 0, index);
        if (source.length != index) {
            System.arraycopy(source, index + 1, result, index, source.length - index - 1);
        }
        return result;
    }

    private static Set<String> generatePermutations(String value) {
        Set<String> permutationSet = new HashSet<>();
        generatePermutations("", value, permutationSet);
        return permutationSet;
    }

    private static void generatePermutations(String prefix, String value, Set<String> permutationSet) {
        if (value.isEmpty()) {
            permutationSet.add(prefix);
        } else {
            int length = value.length();
            for (int i = 0; i < length; i++) {
                generatePermutations(prefix + value.charAt(i), value.substring(0, i) + value.substring(i + 1, length), permutationSet);
            }
        }
    }

    private static String swap(String a, int i, int j) {
        char[] charArray = a.toCharArray();
        char temp = charArray[i];
        charArray[i] = charArray[j];
        charArray[j] = temp;
        return String.valueOf(charArray);
    }

    private static String leftTrim(String value) {
        value = value.replaceFirst("^0*", "");
        if (!value.isEmpty()) {
            return value;
        }
        return "0";
    }

    @Test
    public void test1() {

        int[] numbers = {13, 31, 30};

        assertFalse(solution(numbers));

        int k = Integer.parseInt("009");

        System.out.println(k);

    }

    private long solution(String[] queryType, int[][] query) {
        MyMap myMap = new MyMap();
        long getSum = 0;
        for (int i = 0; i < queryType.length; i++) {
            switch (queryType[i]) {
                case "insert":
                    myMap.insert(query[i][0], query[i][1]);
                    break;
                case "addToKey":
                    myMap.addToKey(query[i][0]);
                    break;
                case "addToValue":
                    myMap.addToValue(query[i][0]);
                    break;
                case "get":
                    getSum += myMap.get(query[i][0]);
                    break;
            }
        }
        return getSum;
    }

    private static class MyMap {

        private final Map<Integer, Integer> hashMap = new HashMap<>();

        public void insert(int x, int y) {
            hashMap.put(x, y);
        }

        public Integer get(int x) {
            return hashMap.get(x);
        }

        public void addToKey(int x) {
            Map<Integer, Integer> newMap = new HashMap<>(hashMap.size());
            for (Map.Entry<Integer, Integer> entry : hashMap.entrySet()) {
                newMap.put(entry.getKey() + x, entry.getValue());
            }
            hashMap.clear();
            hashMap.putAll(newMap);
        }

        public void addToValue(int y) {
            for (Map.Entry<Integer, Integer> entry : hashMap.entrySet()) {
                entry.setValue(entry.getValue() + y);
            }
        }

    }

    /**
     * Given a sequence of integers as an array, determine whether it is possible to obtain a strictly increasing sequence by removing no more than one element from the array.
     */
    boolean solution1(int[] sequence) {

        // Stores the count of numbers that
        // are needed to be removed
        int count = 0;

        // Store the index of the element
        // that needs to be removed
        int index = -1;

        // Traverse the range [1, N - 1]
        for (int i = 1; i < sequence.length; i++) {

            // If arr[i-1] is greater than
            // or equal to arr[i]
            if (sequence[i - 1] >= sequence[i]) {

                // Increment the count by 1
                count++;

                // Update index
                index = i;
            }
        }

        // If count is greater than one
        if (count > 1) {
            return false;
        }

        // If no element is removed
        if (count == 0) {
            return true;
        }

        // If only the last or the
        // first element is removed
        if (index == sequence.length - 1 || index == 1) {
            return true;
        }

        // If a[index] is removed
        if (sequence[index - 1] < sequence[index + 1]) {
            return true;
        }

        // If a[index - 1] is removed
        if (sequence[index - 2] < sequence[index]) {
            return true;
        }

        return false;
    }

    /*
    After becoming famous, the CodeBots decided to move into a new building together. Each of the rooms has a different cost, and some of them are free, but there's a rumour that all the free rooms are haunted! Since the CodeBots are quite superstitious, they refuse to stay in any of the free rooms, or any of the rooms below any of the free rooms.

Given matrix, a rectangular matrix of integers, where each value represents the cost of the room, your task is to return the total sum of all rooms that are suitable for the CodeBots (ie: add up all the values that don't appear below a 0).

Example

For

matrix = [[0, 1, 1, 2],
          [0, 5, 0, 0],
          [2, 0, 3, 3]]
the output should be
solution(matrix) = 9.
     */
    private int solution2(int[][] matrix) {
        int roomCost = 0;
        for (int col = 0; col < matrix[0].length; col++) {
            for (int row = 0; row < matrix.length; row++) {
                if (matrix[row][col] > 0) {
                    roomCost += matrix[row][col];
                } else {
                    break;
                }
            }
        }
        return roomCost;
    }

    private int longestBinaryGap(int n) {
        String str = Integer.toBinaryString(n);
        String[] zeros = str.split("1");
        boolean oneIsLast = (str.charAt(str.length() - 1) == '1');
        if (zeros.length < 3 && !oneIsLast) {
            return 0;
        }
        int maxGap = 0;
        int endIndex = oneIsLast ? zeros.length - 1 : zeros.length - 2;
        for (int i = 1; i <= endIndex; i++) {
            int length = zeros[i].length();
            if (length > maxGap) {
                maxGap = length;
            }
        }
        return maxGap;
    }

    @Test
    public void testLongestBinaryGap() {

        assertEquals(2, longestBinaryGap(9));  // 1001

        assertEquals(1, longestBinaryGap(20)); // 10100

        assertEquals(4, longestBinaryGap(529)); // 1000010001

        assertEquals(0, longestBinaryGap(15));  // 1111

        assertEquals(0, longestBinaryGap(32)); // 100000

        assertEquals(5, longestBinaryGap(1041)); // 10000010001
    }

    private int[] cyclicRotation(int[] inputArray, int shift) {
        int[] outputArray = new int[inputArray.length];
        for (int i = 0; i < inputArray.length; i++) {
            int sum = i + shift;
            int index = (sum < inputArray.length) ? sum : sum % inputArray.length;
            outputArray[index] = inputArray[i];
        }
        return outputArray;
    }

    @Test
    public void testCyclicRotation() {

        int[] inputArray = {3, 8, 9, 7, 6};
        int offset = 3;

        assertEquals("[9, 7, 6, 3, 8]", Arrays.toString(cyclicRotation(inputArray, offset)));

        inputArray = new int[]{1, 2, 3, 4};
        offset = 4;

        assertEquals("[1, 2, 3, 4]", Arrays.toString(cyclicRotation(inputArray, offset)));

        inputArray = new int[]{3, 8, 9, 7, 6};
        offset = 102;

        assertEquals("[7, 6, 3, 8, 9]", Arrays.toString(cyclicRotation(inputArray, offset)));

    }

    /*
    A non-empty array A consisting of N integers is given.
    The array contains an odd number of elements, and each element of the array can be paired with another element that has the same value,
    except for one element that is left unpaired.

For example, in array A such that:

  A[0] = 9  A[1] = 3  A[2] = 9
  A[3] = 3  A[4] = 9  A[5] = 7
  A[6] = 9
the elements at indexes 0 and 2 have value 9,
the elements at indexes 1 and 3 have value 3,
the elements at indexes 4 and 6 have value 9,
the element at index 5 has value 7 and is unpaired.
     */
    private int unpairedElement(int[] inputArray) {
        int result = 0;
        for (int value : inputArray) {
            result ^= value;
        }
        return result;
    }

    /*
    A non-empty array A consisting of N integers is given. Array A represents numbers on a tape.

Any integer P, such that 0 < P < N, splits this tape into two non-empty parts: A[0], A[1], ..., A[P − 1] and A[P], A[P + 1], ..., A[N − 1].

The difference between the two parts is the value of: |(A[0] + A[1] + ... + A[P − 1]) − (A[P] + A[P + 1] + ... + A[N − 1])|

In other words, it is the absolute difference between the sum of the first part and the sum of the second part.

For example, consider array A such that:

  A[0] = 3
  A[1] = 1
  A[2] = 2
  A[3] = 4
  A[4] = 3
We can split this tape in four places:

P = 1, difference = |3 − 10| = 7
P = 2, difference = |4 − 9| = 5
P = 3, difference = |6 − 7| = 1
P = 4, difference = |10 − 3| = 7
Write a function:

class Solution { public int solution(int[] A); }

that, given a non-empty array A of N integers, returns the minimal difference that can be achieved.

For example, given:

  A[0] = 3
  A[1] = 1
  A[2] = 2
  A[3] = 4
  A[4] = 3
the function should return 1, as explained above.
     */
    private int tapeEquilibrium(int[] inputArray) {
        Map<String, Integer> diffMap = new HashMap<>(2);
        diffMap.put("left", inputArray[0]);
        int rightSum = 0;
        for (int i = 1; i < inputArray.length; i++) {
            rightSum += inputArray[i];
        }
        diffMap.put("right", rightSum);
        int minDiff = Math.abs(rightSum - inputArray[0]);
        for (int p = 1; p < inputArray.length - 1; p++) {
            int diff = leftRightDiff(p, inputArray, diffMap);
            if (diff < minDiff) {
                minDiff = diff;
            }
        }
        return minDiff;
    }

    private int leftRightDiff(int p, int[] inputArray, Map<String, Integer> diffMap) {
        int leftSum = diffMap.get("left") + inputArray[p];
        int rightSum = diffMap.get("right") - inputArray[p];
        diffMap.put("left", leftSum);
        diffMap.put("right", rightSum);
        return Math.abs(rightSum - leftSum);
    }

    @Test
    public void testTapeEquilibrium() {

        assertEquals(1, tapeEquilibrium(new int[]{3, 1, 2, 4, 3}));

        assertEquals(3, tapeEquilibrium(new int[]{9, 1, 8, 2, 3}));

        assertEquals(8, tapeEquilibrium(new int[]{9, 1}));

        assertEquals(0, tapeEquilibrium(new int[]{9, 9}));

        assertEquals(9, tapeEquilibrium(new int[]{9}));

        assertEquals(2002, tapeEquilibrium(new int[]{-2001, 1}));

    }

    /*
 Given two strings, find the number of common characters between them.

Example

For s1 = "aabcc" and s2 = "adcaa", the output should be
solution(s1, s2) = 3.

Strings have 3 common characters - 2 "a"s and 1 "c".

Input/Output

[execution time limit] 3 seconds (java)

[memory limit] 1 GB

[input] string s1

A string consisting of lowercase English letters.

Guaranteed constraints:
1 ≤ s1.length < 15.

[input] string s2

A string consisting of lowercase English letters.

Guaranteed constraints:
1 ≤ s2.length < 15.

[output] integer
     */
    private int commonCharacterCount(String s1, String s2) {
        int count = 0;
        char[] s1Array = s1.toCharArray();
        char[] s2Array = s2.toCharArray();
        for (int i = 0; i < s1Array.length; i++) {
            for (int j = 0; j < s2Array.length; j++) {
                if (s1Array[i] == s2Array[j]) {
                    count++;
                    s1Array[i] = ' ';
                    s2Array[j] = ' ';
                    break;
                }
            }
        }
        return count;
    }

    // B-tree search

    private static final int MAX_KEYS = 5;

    private static class Node {
        int n;
        int[] key = new int[MAX_KEYS];
        Node[] child = new Node[MAX_KEYS+1];
        boolean leaf;
    }

    private Node bTreeSearch(Node x, int k) {
        int i = 0;
        while (i < x.n && k >= x.key[i]) {
            i++;
        }
        if (i < x.n && k == x.key[i]) {
            return x;
        }
        if (x.leaf) {
            return null;
        }
        return bTreeSearch(x.child[i], k);
    }


}