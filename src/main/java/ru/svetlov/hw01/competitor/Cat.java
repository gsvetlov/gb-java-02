package ru.svetlov.hw01.competitor;

public class Cat implements Runner, Jumper{
    private final int runLength;
    private final int jumpHeight;
    private final String name;

    public Cat(String name, int runLength, int jumpHeight) {
        this.name = name;
        this.runLength = runLength;
        this.jumpHeight = jumpHeight;
    }

    @Override
    public int jump(int height) {
        int result = Math.min(height, jumpHeight);
        System.out.printf("Кот %s прыгнул на %d\n", name, result);
        return result;
    }

    @Override
    public int run(int distance) {
        int result = Math.min(distance, runLength);
        System.out.printf("Кот %s пробежал %d\n", name, result);
        return result;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void printInfo() {
        System.out.printf("%s; бег: %d, прыжки: %d\n", name, runLength, jumpHeight);
    }
}
