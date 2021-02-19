package ru.svetlov.hw03;

import java.util.*;

public class Hw03App {
    public static void main(String[] args) {
        // задание 1
        List<String> list = initList();
        printUniqueWords(list);
        printFrequencyArray(list);

        // задание 2
        PhoneBook book = new PhoneBook();
        book.add("Jack", "+71234567890"); // однофамильцы
        book.add("Jack", "+13565551426"); // однофамильцы
        book.add("John", "+13565551424");
        book.add("Mary", "+14567156465");
        book.add("Mary", "+14567156465"); // дубль номера [не допускается]

        printPhoneBookEntry("Jim", book);
        printPhoneBookEntry("Jack", book);
        printPhoneBookEntry("Mary", book);

        System.out.println(book);

    }

    private static void printPhoneBookEntry(String name, PhoneBook book) {
        System.out.println(name);
        for (var numbers : book.get(name)) {
            System.out.println(numbers);
        }
    }

    //Посчитать сколько раз встречается каждое слово
    private static void printFrequencyArray(List<String> list) {
        Map<String, Integer> map = new HashMap<>(list.size());
        for (String s : list)
            map.put(s, map.containsKey(s) ? map.get(s) + 1 : 1); // такой код читаем или лучше в if..else развернуть?
        for (String s : map.keySet())
            System.out.println(s + " : " + map.get(s));
    }

    //Найти и вывести список уникальных слов, из которых состоит массив
    private static void printUniqueWords(List<String> list) {
        System.out.println("Список слов:");
        System.out.println(list);
        System.out.printf("Длина исходного списка = %d\n", list.size());

        System.out.println("Уникальные слова:");
        Set<String> set = new HashSet<>(list.size());
        set.addAll(list);
        System.out.println(set);
        System.out.printf("Длина списка = %d\n", set.size());
    }

    private static List<String> initList() {
        return Arrays.asList(
                "яблоко", "груша", "ананас", "киви", "апельсин", "смородина", "малина", "клубника",
                "помело", "личи", "яблоко", "апельсин", "ананас", "виноград", "кумкват", "апельсин",
                "яблоко", "фенхель", "авокадо", "айва", "персик", "киви", "лайм", "лимон", "мандарин"
        );
    }
}

/*
1. Создать массив с набором слов (10-20 слов, должны встречаться повторяющиеся).
  Найти и вывести список уникальных слов, из которых состоит массив (дубликаты не считаем).
  Посчитать сколько раз встречается каждое слово.
2. Написать простой класс ТелефонныйСправочник, который хранит в себе список фамилий и телефонных номеров.
  В этот телефонный справочник с помощью метода add() можно добавлять записи.
  С помощью метода get() искать номер телефона по фамилии.
  Следует учесть, что под одной фамилией может быть несколько телефонов (в случае однофамильцев),
  тогда при запросе такой фамилии должны выводиться все телефоны.
 */