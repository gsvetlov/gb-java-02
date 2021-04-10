package ru.svetlov.junit.example.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.svetlov.junit.example.ExampleApp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class ExampleAppTest {
    private final static int TEST_ITERATIONS = 8;
    private final static int TEST_ARRAY_RANGE = 10;
    private final static Random random = new Random();
    private ExampleApp app;

    @BeforeEach
    public void init() {
        app = new ExampleApp();
    }

    @ParameterizedTest
    @MethodSource("takeLastValidSource")
    public void takeLastAfterFour_Works(int[] source, int[] expected) {
        int[] actual = app.takeLastAfterFour(source);
        Assertions.assertArrayEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("takeLastInvalidSource")
    public void takeLastAfterFour_Throws(int[] source) {
        Assertions.assertThrows(IllegalArgumentException.class, () -> app.takeLastAfterFour(source));
    }

    @ParameterizedTest
    @MethodSource("findOneSourceTrue")
    public void findOneOrFourIsTrue(int[] source) {
        boolean actual = app.findOneOrFour(source);
        Assertions.assertTrue(actual);
    }

    @ParameterizedTest
    @MethodSource("findOneSourceFalse")
    public void findOneOrFourIsFalse(int[] source) {
        boolean actual = app.findOneOrFour(source);
        Assertions.assertFalse(actual);
    }

    private static Stream<Arguments> takeLastValidSource() { //генерируем валидные исходники и ответы
        List<Arguments> out = new ArrayList<>();
        out.add(Arguments.arguments(new int[]{0, 1, 2, 3, 4}, new int[0])); // case = последний элемент в массиве
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            List<Integer> source = null;
            List<Integer> result = null;
            boolean isValid = false;
            while (!isValid) {
                source = new ArrayList<>();
                result = new ArrayList<>();
                while (source.size() < TEST_ARRAY_RANGE + 1) {
                    int value = random.nextInt(10);
                    source.add(value);
                    result.add(value);
                    if (value == 4)
                        result.clear();
                }
                if (result.size() == 0) continue;
                for (int v : source) {
                    if (v == 4) {
                        isValid = true;
                        break;
                    }
                }
            }
            out.add(Arguments.arguments(toInt(source), toInt(result)));
        }
        return out.stream();
    }

    private static Stream<Arguments> takeLastInvalidSource() { //генерируем неверные исходники
        List<Arguments> out = new ArrayList<>();
        out.add(Arguments.arguments(new int[0])); // case = пустой массив
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            List<Integer> source = new ArrayList<>();
            while (source.size() < TEST_ARRAY_RANGE + 1) {
                int value = random.nextInt(10);
                if (value != 4)
                    source.add(value);
            }
            out.add(Arguments.arguments(toInt(source)));
        }
        return out.stream();
    }

    private static int[] toInt(List<Integer> source) { // преобразуем List<Integer> в int[]
        int[] result = new int[source.size()];
        for (int i = 0; i < result.length; i++)
            result[i] = source.get(i);
        return result;
    }

    private static Stream<Arguments> findOneSourceTrue() { // генерируем валидный массив
        List<Arguments> out = new ArrayList<>();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            boolean isValid = false;
            List<Integer> list = null;
            while (!isValid) {
                list = new ArrayList<>();
                for (int j = 0; j < TEST_ARRAY_RANGE; j++) {
                    list.add(random.nextInt(TEST_ARRAY_RANGE));
                }
                if (list.contains(1) || list.contains(4))
                    isValid = true;
            }
            out.add(Arguments.arguments(toInt(list)));
        }
        return out.stream();
    }

    private static Stream<Arguments> findOneSourceFalse() { // генерируем не валидный массив
        List<Arguments> out = new ArrayList<>();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            List<Integer> list = new ArrayList<>();
            while (list.size() < TEST_ARRAY_RANGE) {
                int value = random.nextInt(TEST_ARRAY_RANGE);
                if (value != 1 && value != 4)
                    list.add(value);
            }
            out.add(Arguments.arguments(toInt(list)));
        }
        return out.stream();
    }
}
