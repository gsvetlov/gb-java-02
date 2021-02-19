package ru.svetlov.hw03;

import java.util.*;

public class PhoneBook {
    private final static Set<String> defaultSet = new HashSet<>(1);

    static {
        defaultSet.add("Записи отсутствуют."); // есть вариант добавить запись без статик инициализатора?
    }

    private final Map<String, Set<String>> book = new TreeMap<>(); // храним записи в алфавитном порядке

    public void add(String name, String phoneNumber) {
        if (book.containsKey(name)) {
            book.get(name).add(phoneNumber);
        } else {
            SortedSet<String> innerSet = new TreeSet<>(); // телефоны храним в сортированном виде
            innerSet.add(phoneNumber);
            book.put(name, innerSet);
        }
    }

    public List<String> get(String name) {
        return List.copyOf(book.getOrDefault(name, defaultSet)); // возвращаем immutableList
    }

    @Override
    public String toString() {
        return book.toString();
    }
}
