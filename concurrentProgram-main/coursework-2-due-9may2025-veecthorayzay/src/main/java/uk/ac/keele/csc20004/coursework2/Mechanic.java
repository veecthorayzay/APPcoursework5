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
        System.out.println("Mechanic thread started: " + Thread.currentThread().getName());
        Robot r = repairLine.getNextRobot();
        if (r != null) {
            arena.repair(r);
            bench.addRobot(r);
        }
    }
}
