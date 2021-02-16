package ru.svetlov.hw02;

public class Hw02App {
    public static void main(String[] args) {
        String[][] array = new String[][]{
                {"1", "2", "3", "4"},
                {"1", "2", "3", "4"},
                {"1", "2", "3", "4"},
                {"1", "2", "3", "4"}
        };
        try {
            int sum = arraySum(array);
            System.out.println("Array sum = " + sum);
        } catch (MyArraySizeException e) {
            System.out.println(e.getMessage());
        } catch (MyArrayDataException e) {
            System.out.println(e.getMessage());
            System.out.printf("in cell [%d][%d]\n", e.getInvalidRowIndex(), e.getInvalidColumnIndex());
        }
    }

    public static int arraySum(String[][] arr) {
        for (String[] strings : arr) {
            if (arr.length != 4 || strings.length != 4) {
                throw new MyArraySizeException("Array size is not [4][4]");
            }
        }
        int sum = 0;
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[i].length; j++) {
                try {
                    sum += Integer.parseInt(arr[i][j]);
                } catch (NumberFormatException e) {
                    throw new MyArrayDataException("Invalid data format", i, j);
                }
            }
        }
        return sum;
    }
}
