package uk.ac.keele.csc20004.coursework2;


import uk.ac.keele.csc20004.CombatPair;

public class BattleManager implements Runnable {
    private final MyConcurrentArena arena;

    public BattleManager(MyConcurrentArena arena) {
        this.arena = arena;
    }

    @Override
    public void run() {
        System.out.println("BattleManager thread started: " + Thread.currentThread().getName());
        CombatPair pair = arena.getOpponents();
        if (pair == null) return;

        pair.fight();

        arena.processResult(pair.getRobot1());
        arena.processResult(pair.getRobot2());
    }
}