package ru.svetlov.hw01;


import ru.svetlov.hw01.competitor.Competitor;
import ru.svetlov.hw01.obstacle.Obstacle;

public class Course {
    private final Obstacle[] obstacles;

    public Course(Obstacle[] obstacles) {
        this.obstacles = obstacles;
    }
    public void compete(Team team){
        System.out.printf("Выступает команда %s\n", team.getName());
        for (Competitor c : team.getMembers()) {
            boolean isFinished = false;
            for (Obstacle o : obstacles) {
                isFinished = o.canPass(c);
                if (!isFinished) break;
            }
            team.setResult(c, isFinished);
        }
    }
}
