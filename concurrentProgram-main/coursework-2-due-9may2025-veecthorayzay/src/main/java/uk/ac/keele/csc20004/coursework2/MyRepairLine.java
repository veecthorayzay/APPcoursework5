package uk.ac.keele.csc20004.coursework2;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import uk.ac.keele.csc20004.RepairLine;
import uk.ac.keele.csc20004.robots.Robot;

/**
 * MyRepairLine is an implementation of the RepairLine interface using a thread-safe queue.
 * It manages robots that require repair before they can return to the arena.
 * 
 * The repair line has a fixed capacity and uses a {@link LinkedBlockingQueue} for safe
 * concurrent access by multiple threads.
 * 
 * @author 23045944
 */
public class MyRepairLine implements RepairLine {
    private final BlockingQueue<Robot> queue;

    /**
     * Constructs a MyRepairLine instance with the specified capacity.
     * 
     * @param capacity the maximum number of robots that can be in the repair line
     */
    public MyRepairLine(int capacity) {
        this.queue = new LinkedBlockingQueue<>(capacity);
    }

    /**
     * Adds a robot to the repair line if the robot is not null and there is space available.
     * 
     * @param robot the robot to be added to the repair line
     */
    @Override
    public void addRobot(Robot robot){
        if (robot != null) {
            queue.offer(robot);
        }
    }

    /**
     * Retrieves and removes the next robot in the repair line, or returns null if the line is empty.
     * 
     * @return the next robot to be repaired, or null if the repair line is empty
     */
    @Override
    public Robot getNextRobot() {
        return queue.poll();
    }

    /**
     * Returns the number of robots currently waiting in the repair line.
     * 
     * @return the size of the repair line
     */
    @Override
    public int size() {
        return queue.size();
    }

    /**
     * Prints all robots currently in the repair line to the standard output.
     */
    @Override
    public void printOut() {
        for (Robot robot : queue) {
            System.out.println(robot);
        }
    }
}
