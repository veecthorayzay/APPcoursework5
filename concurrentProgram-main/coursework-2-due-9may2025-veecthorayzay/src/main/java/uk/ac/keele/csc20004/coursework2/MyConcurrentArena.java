/* **********************
 * CSC-20004 COURSEWORK *
 * Component 2          * 
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
 * This class is meant to represent the concurrent arena for robot battles.
 * It extends the RobotArena class and manages multiple threads running battles,
 * repairs, and robot transfers between the arena, repair line, and bench all at the same time.
 * 
 * <p><b>Concurrency and Resource Management:</b></p>
 * 
 * <p><b>Concurrent Access to Repair Line and Bench:</b><br>
 * The repair line and bench are shared resources that are accessed by multiple threads
 * (e.g. battle managers and mechanics). In order to prevent race conditions, 
 * I implemented both using thread safe BlockingQueues, which can handle internal
 * synchronization automatically. This avoided inconsistent states like losing
 * or duplicating robots.</p>
 * 
 * <p><b>Concurrent Access to Service Bays:</b><br>
 * The service bays represent limited resources used during repairs. Since
 * the repair methods refill energy atomically within a single thread, and
 * these bays are not explicitly shared objects, the risk of conflicts is small.
 * If these were shared, I would require synchronized blocks or locks.</p>
 * 
 * <p><b>Deadlock and Livelock Prevention:</b><br>
 * Deadlock is avoided by making sure that locks are always acquired in a consistent
 * order and by using threadsafe collections to minimize explicit locking.
 * Using non blocking methods like offer() and poll() also prevents threads
 * from waiting indefinitely, thus avoiding any livelock scenarios.</p>
 * 
 * <p><b>Starvation and Mitigation:</b><br>
 * Starvation was very unlikely because the BlockingQueues used (LinkedBlockingQueue)
 * which employ a fair FIFO ordering for threads accessing the repair line and bench.
 * Overall, the design ensured all robots eventually got serviced without starvation.</p>
 * 
 * <p><b> Issues and Improvements:</b><br>
 * The current implementation uses a fixed thread pool of 6 threads. This could 
 * be improved by using a dynamic thread pool that adjusts the number of threads
 * based on the workload. This would allow for better resource utilization
 * and potentially reduce the time taken for repairs and battles.</p><br>
 * <p>I also faced the issue of robots not being added to the repairline or bench properly
 * which was frustrating to debug. I had to ensure that the addRobot() method was
 * synchronized correctly,make sure files were accessing one another correctly and make sure that the robots were not null before adding them.</P><br>
 * <p>Another issue was that I would have multiple battlemanager and mechanic threads starting
 * without ever stopping. I had to ensure that threads were properly managed and shut down after they had completed their tasks.</p>
 * 
 * @author 23045944
 */
public class MyConcurrentArena extends RobotArena {

    final Object robotListLock = new Object();
    private final ExecutorService executorService = Executors.newFixedThreadPool(6);

    /**
     * Constructor for MyConcurrentArena.
     * This constructor initializes the arena with a given student ID,
     * capacity, repair line, and bench, and populates it with robots of various types.
     * 
     * @param studentId  the student ID associated with this arena
     * @param capacity   the maximum number of robots in the arena
     * @param repairLine the repair line for repairing robots
     * @param bench      the bench for storing robots
     */
    public MyConcurrentArena(int studentId, int capacity, RepairLine repairLine, Bench bench) {
        super(studentId, capacity, repairLine, bench);

        RobotType[] types = RobotType.values();
        for (int i = 0; i < capacity; i++) {
            Robot r = createRobot(i, types[i % types.length]);
            robots.add(r);
        }
    }

    /**
     * Creates a robot of the specified type with the given ID.
     * 
     * @param id   the identifier for the robot
     * @param type the type of robot to create
     * @return the created Robot instance
     */
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

    /**
     * Repairs the given robot by refilling any energy bay that is empty.
     * 
     * @param r the robot to be repaired
     */
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

    /**
     * Simulates a round of the robot arena, including random battles,
     * repairs, and moving robots from the bench back into the arena.
     */
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

    /**
     * Processes the result of a robot's battle.
     * Depending on its total energy, it is either returned to the arena or sent for repair.
     * 
     * @param r the robot to process
     */
    public void processResult(Robot r) {
        double totalEnergy = r.getFrameEnergy() + r.getMotorEnergy() + r.getSensorsEnergy() + r.getActuatorsEnergy();
        if (totalEnergy > 50.0) {
            addRobot(r);
        } else {
            repairLine.addRobot(r);
        }
    }

    /**
     * Enum representing the types of robots that can be created.
     */
    private enum RobotType {
        HUMANOID, WHEELED, CYCLOBOT
    }

    /**
     * Returns the robot with the highest total energy in the arena.
     * 
     * @return the winning robot, or null if none found
     */
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

    /**
     * Adds a robot to the arena.
     * 
     * @param r the robot to add
     */
    @Override
    public void addRobot(Robot r) {
        synchronized (robotListLock) {
            robots.add(r);
        }
    }

    /**
     * Gets the current number of robots in the arena.
     * 
     * @return the number of robots
     */
    @Override
    public int getNumRobots() {
        synchronized (robotListLock) {
            return robots.size();
        }
    }

    /**
     * Prints statistics about the arena, including all robots,
     * the repair line, and the bench.
     */
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

    /**
     * Retrieves two robots from the arena for battle.
     * 
     * @return a CombatPair containing two robots, or null if not enough robots
     */
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

    /**
     * Shuts down the executor service gracefully. Forces shutdown if not completed in time.
     */
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

    /**
     * Main method to run the simulation.
     * 
     * @param args command line arguments
     * @throws InterruptedException if thread sleep is interrupted
     */
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
