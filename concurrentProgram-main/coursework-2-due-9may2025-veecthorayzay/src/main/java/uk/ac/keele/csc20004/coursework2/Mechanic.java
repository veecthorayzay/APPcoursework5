package uk.ac.keele.csc20004.coursework2;

import uk.ac.keele.csc20004.Bench;
import uk.ac.keele.csc20004.RepairLine;
import uk.ac.keele.csc20004.robots.Robot;

/**
 * Mechanic is a Runnable task responsible for repairing a single robot from the repair line.
 * Once repaired, the robot is moved to the bench for potential reinsertion into the arena.
 * 
 * This class simulates a repair operation in a concurrent environment, using shared resources.
 * It is designed to be executed by multiple threads.
 * 
 * @author 23045944
 */
public class Mechanic implements Runnable {
    private final RepairLine repairLine;
    private final Bench bench;
    private final MyConcurrentArena arena;

    /**
     * Constructs a Mechanic instance with access to the repair line, bench, and arena.
     * 
     * @param repairLine the repair line containing damaged robots
     * @param bench the bench to hold repaired robots
     * @param arena the arena used for calling the repair logic
     */
    public Mechanic(RepairLine repairLine, Bench bench, MyConcurrentArena arena) {
        this.repairLine = repairLine;
        this.bench = bench;
        this.arena = arena;
    }

    /**
     * Executes the mechanic task in a separate thread.
     * Retrieves a robot from the repair line, repairs it via the arena's repair method,
     * and places it on the bench for reuse.
     */
    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        System.out.println("Mechanic thread started: " + threadName);

        Robot r = repairLine.getNextRobot();
        if (r != null) {
            System.out.println("[" + threadName + "] Repairing robot: " + r);

            arena.repair(r);

            System.out.println("[" + threadName + "] Finished repairing robot: " + r);

            bench.addRobot(r);
        } else {
            System.out.println("[" + threadName + "] No robot to repair.");
        }
    }
}
