package uk.ac.keele.csc20004.coursework2;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import uk.ac.keele.csc20004.Bench;
import uk.ac.keele.csc20004.robots.Robot;

public class MyBench implements Bench {
    private final BlockingQueue<Robot> queue;

    public MyBench() {
        this.queue = new LinkedBlockingQueue<>();
    }

    @Override
    public void addRobot(Robot robot) {
        if (robot != null) {
            queue.offer(robot);
        }
    }

    @Override
    public Robot getNextRobot() {
        return queue.poll();
    }

    @Override
    public void printOut() {
        for (Robot robot : queue) {
            System.out.println(robot);
        }
    }

    @Override
    public int size() {
        return queue.size();
    }
}
