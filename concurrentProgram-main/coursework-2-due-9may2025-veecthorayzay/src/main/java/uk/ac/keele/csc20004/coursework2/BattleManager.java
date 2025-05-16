package uk.ac.keele.csc20004.coursework2;

import uk.ac.keele.csc20004.CombatPair;

/**
 * BattleManager is a Runnable task responsible for conducting a single robot battle.
 * It retrieves a pair of robots from the arena, initiates a fight between them,
 * and processes the results based on their remaining energy.
 * 
 * This class is intended to be executed concurrently by multiple threads.
 * 
 * @author 23045944
 */
public class BattleManager implements Runnable {
    private final MyConcurrentArena arena;

    /**
     * Constructs a BattleManager instance for the given arena.
     * 
     * @param arena the arena from which to retrieve robots and process battle results
     */
    public BattleManager(MyConcurrentArena arena) {
        this.arena = arena;
    }

    /**
     * Executes the battle task in a separate thread.
     * Retrieves two opponents from the arena, makes them fight,
     * and then processes the results to either return them to the arena
     * or send them to the repair line.
     */
    @Override
    public void run() {
        System.out.println("BattleManager thread started: " + Thread.currentThread().getName());
        CombatPair pair = arena.getOpponents();
        if (pair == null) return;

        System.out.println("Combat started between " + pair);
        pair.fight();

        arena.processResult(pair.getRobot1());
        arena.processResult(pair.getRobot2());
    }
}
