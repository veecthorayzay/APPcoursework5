package uk.ac.keele.csc20004.coursework2;
import uk.ac.keele.csc20004.RepairBay;

public class MyRepairBay extends RepairBay {

    
    /**
     * Creates a new RepairBay with the given name (e.g., "Frame").
     *
     * @param name the name of the repair bay
     */
    public MyRepairBay(String name) {
        super(name);
    }

    /**
     * Simulates the repair of a robot, which requires a certain amount of time
     * (this will be proportional to the initial energy of the part)
     *
     * @param energy the energy level to refill (time required to repair the robot)
     */
    public void operate(double energy) {
        try {
            long repairTime = Math.round(energy * SimulationParameters.REPAIR_DELAY);
            Thread.sleep(repairTime);
        } catch (InterruptedException e) {
            System.err.format("IOException: %s%n", e);
        }
    }

    @Override
    public String toString() {
        return String.format("%15s", super.toString() + " bay");
    }
    
}
