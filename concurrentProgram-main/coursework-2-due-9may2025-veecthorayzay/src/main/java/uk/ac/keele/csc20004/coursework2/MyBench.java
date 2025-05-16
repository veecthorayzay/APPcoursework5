package uk.ac.keele.csc20004.coursework2;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import uk.ac.keele.csc20004.Bench;
import uk.ac.keele.csc20004.robots.Robot;

/**
 * MyBench is an implementation of the Bench interface using a thread-safe queue.
 * It serves as temporary storage for robots that have been repaired and are ready
 * to return to the arena.
 * 
 * This class uses a {@link LinkedBlockingQueue} to handle concurrency safely.
 * 
 * @author 23045944
 */
public class MyBench implements Bench {
    private final BlockingQueue<Robot> queue;

    /**
     * Constructs an empty bench backed by a LinkedBlockingQueue.
     */
    public MyBench() {
        this.queue = new LinkedBlockingQueue<>();
    }

    /**
     * Adds a robot to the bench if it is not null.
     * 
     * @param robot the robot to add to the bench
     */
    @Override
    public void addRobot(Robot robot) {
        if (robot != null) {
            queue.offer(robot);
        }
    }

    /**
     * Retrieves and removes the next robot from the bench,
     * or returns null if the bench is empty.
     * 
     * @return the next available robot, or null if none
     */
    @Override
    public Robot getNextRobot() {
        return queue.poll();
    }

    /**
     * Prints the robots currently on the bench to the standard output.
     */
    @Override
    public void printOut() {
        for (Robot robot : queue) {
            System.out.println(robot);
        }
    }

    /**
     * Returns the number of robots currently on the bench.
     * 
     * @return the number of robots on the bench
     */
    @Override
    public int size() {
        return queue.size();
    }
}
