package uk.ac.keele.csc20004.coursework2;

import uk.ac.keele.csc20004.Bench;
import uk.ac.keele.csc20004.RepairLine;
import uk.ac.keele.csc20004.robots.Robot;

public class Mechanic implements Runnable {
    private final RepairLine repairLine;
    private final Bench bench;
    private final MyConcurrentArena arena;

    public Mechanic(RepairLine repairLine, Bench bench, MyConcurrentArena arena) {
        this.repairLine = repairLine;
        this.bench = bench;
        this.arena = arena;
    }

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
