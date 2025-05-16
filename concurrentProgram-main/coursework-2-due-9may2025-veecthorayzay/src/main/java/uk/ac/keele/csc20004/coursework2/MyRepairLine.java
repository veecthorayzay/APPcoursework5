package uk.ac.keele.csc20004.coursework2;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import uk.ac.keele.csc20004.RepairLine;
import uk.ac.keele.csc20004.robots.Robot;

public class MyRepairLine implements RepairLine {
    private final BlockingQueue<Robot> queue;


    public MyRepairLine(int capacity) {
        this.queue = new LinkedBlockingQueue<>(capacity);
    }

    @Override
    public void addRobot(Robot robot){
        if (robot != null) {
            queue.offer(robot);
        }
    }

    @Override
    public Robot getNextRobot() {
        return queue.poll();
    }

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public void printOut() {
        for (Robot robot : queue) {
            System.out.println(robot);
        }
    }
}
