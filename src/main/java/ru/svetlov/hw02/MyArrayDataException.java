package ru.svetlov.hw02;

public class MyArrayDataException extends RuntimeException {
    private final int invalidRowIndex;
    private final int invalidColumnIndex;

    public int getInvalidRowIndex() {
        return invalidRowIndex;
    }

    public int getInvalidColumnIndex() {
        return invalidColumnIndex;
    }

    public MyArrayDataException(String message, int invalidRowIndex, int invalidColumnIndex) {
        super(message);
        this.invalidRowIndex = invalidRowIndex;
        this.invalidColumnIndex = invalidColumnIndex;
    }
}
