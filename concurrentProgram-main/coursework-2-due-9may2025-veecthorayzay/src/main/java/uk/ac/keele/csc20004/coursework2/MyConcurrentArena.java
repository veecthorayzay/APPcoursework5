/* **********************
 * CSC-20004 COURSEWORK *
 * Component 2         * 
 * 2024/25 First sit    *
 * **********************/
package uk.ac.keele.csc20004.coursework2;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import uk.ac.keele.csc20004.Bench;
import uk.ac.keele.csc20004.CombatPair;
import uk.ac.keele.csc20004.RepairLine;
import uk.ac.keele.csc20004.RobotArena;
import uk.ac.keele.csc20004.robots.CycloBot;
import uk.ac.keele.csc20004.robots.HumanoidRobot;
import uk.ac.keele.csc20004.robots.Robot;
import uk.ac.keele.csc20004.robots.WheeledRobot;

/**
 * This is an example of the class you need to implement for the coursework.
 * Pleese feel free to modify this class as you see fit.
 * Please add the appropriate Javadoc to the class and its methods.
 * 
 * @author 23045944
 */
public class MyConcurrentArena extends RobotArena {

    final Object robotListLock = new Object();
    private final ExecutorService executorService = Executors.newFixedThreadPool(6);

    /**
     * This is the constructor for your class.
     * Please add the appropriate Javadoc to the class and its methods.
     * 
     * @param studentId  please document this parameter
     * @param capacity   please document this parameter
     * @param repairLine please document this parameter
     * @param bench      please document this parameter
     */
    public MyConcurrentArena(int studentId, int capacity, RepairLine repairLine, Bench bench) {
        super(studentId, capacity, repairLine, bench);

        RobotType[] types = RobotType.values();
        for (int i = 0; i < capacity; i++) {
            Robot r = createRobot(i, types[i % types.length]);
            robots.add(r);
        }
    }

    private Robot createRobot(int id, RobotType type) {
        switch (type) {
            case HUMANOID:
                return new HumanoidRobot("R-" + id);
            case WHEELED:
                return new WheeledRobot("R-" + id);
            case CYCLOBOT:
                return new CycloBot("R-" + id);
            default:
                throw new IllegalArgumentException("Unknown robot type: " + type);
        }
    }

    @Override
    public void repair(Robot r) {
        if (r.getFrameEnergy() == 0.0)
            r.refillFrameEnergy(frameBay);
        if (r.getMotorEnergy() == 0.0)
            r.refillMotorEnergy(motorBay);
        if (r.getSensorsEnergy() == 0.0)
            r.refillSensorsEnergy(sensorBay);
        if (r.getActuatorsEnergy() == 0.0)
            r.refillActuatorsEnergy(actuatorBay);
    }

    public void simulateRound() {
        if (executorService.isShutdown())
            return;

        System.out.println("Simulating a round...");

        int numFights = (int) (Math.random() * (getNumRobots() / 2));
        for (int i = 0; i < numFights; i++) {
            executorService.submit(new BattleManager(this));
        }

        int numRepairs = (int) (Math.random() * repairLine.size());
        for (int i = 0; i < numRepairs; i++) {
            executorService.submit(new Mechanic(repairLine, bench, this));
        }

        int benchCount = (int) (Math.random() * bench.size());
        for (int i = 0; i < benchCount; i++) {
            executorService.submit(() -> {
                Robot r = bench.getNextRobot();
                if (r != null) {
                    addRobot(r);
                }
            });
        }
    }

    public void processResult(Robot r) {
        double totalEnergy = r.getFrameEnergy() + r.getMotorEnergy() + r.getSensorsEnergy() + r.getActuatorsEnergy();
        if (totalEnergy > 50.0) { // You can tune this threshold
            addRobot(r);
        } else {
            repairLine.addRobot(r);
        }
    }

    private enum RobotType {
        HUMANOID, WHEELED, CYCLOBOT
    }

    @Override
    public Robot getWinner() {
        Robot winner = null;
        double maxEnergy = 0.0;

        synchronized (robotListLock) {
            for (Robot r : robots) {
                double totalEnergy = r.getFrameEnergy() + r.getMotorEnergy() + r.getSensorsEnergy()
                        + r.getActuatorsEnergy();
                if (totalEnergy > maxEnergy) {
                    maxEnergy = totalEnergy;
                    winner = r;
                }
            }
        }

        return winner;
    }

    @Override
    public void addRobot(Robot r) {
        synchronized (robotListLock) {
            robots.add(r);
        }
    }

    @Override
    public int getNumRobots() {
        synchronized (robotListLock) {
            return robots.size();
        }
    }

    @Override
    public void printStats() {
        synchronized (robotListLock) {
            System.out.println("Stats for arena: " + studentId);
            System.out.println("Robots in arena:");
            for (Robot r : robots) {
                System.out.println(r);
            }
        }

        System.out.println("Robots in repair line:");
        repairLine.printOut();

        System.out.println("Robots on bench:");
        bench.printOut();
    }

    public CombatPair getOpponents() {
        synchronized (robotListLock) {
            if (robots.size() < 2) {
                return null;
            }
            Robot r1 = robots.remove(0);
            Robot r2 = robots.remove(0);
            return new CombatPair(r1, r2);
        }
    }

    public void shutdown() {
        System.out.println("Shutting down the thread pool...");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                System.out.println("Forcing shutdown...");
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        MyBench bench = new MyBench();
        MyRepairLine repairLine = new MyRepairLine(SimulationParameters.MAX_REPAIRLINE_SIZE);
        MyConcurrentArena arena = new MyConcurrentArena(23045944, SimulationParameters.NUM_ROBOTS, repairLine, bench);

        System.out.println("Starting the battle simulation...");

        int rounds = 15;
        while (rounds-- > 0) {
            System.out.println("Round " + (15 - rounds));
            arena.simulateRound();
            Thread.sleep(500); // Delay for visibility/debugging
            arena.printStats();
        }

        Robot winner = arena.getWinner();
        System.out.println("The winner of the battle is: " + (winner != null ? winner : "No winner found"));
        arena.shutdown();
        System.exit(0);
    }
}
