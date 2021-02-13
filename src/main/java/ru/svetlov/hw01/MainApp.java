package ru.svetlov.hw01;

import ru.svetlov.hw01.competitor.*;
import ru.svetlov.hw01.obstacle.*;

public class MainApp {
    public static void main(String[] args) {
        Team teamOne = new Team("DreamTeam", initTeamMembers());
        Course course = new Course(initObstacles());
        teamOne.printMembersInfo();
        course.compete(teamOne);
        teamOne.printResults();
    }

    private static Competitor[] initTeamMembers() {
        return new Competitor[]{
                new Human("Вася", 150, 5),
                new Human("Петя", 130, 3),
                new Cat("Барсик", 160, 10),
                new Cat("Мурзик", 120, 4),
                new Robot("Вилли#1", 150),
                new Robot("Вилли#2", 100)
        };
    }

    private static Obstacle[] initObstacles() {
        return new Obstacle[]{
                new Track(110),
                new Wall(2),
                new Track(140),
                new Wall(4),
                new Track(150)
        };
    }
}
