/* **********************
 * CSC-20004 COURSEWORK *
 * Component 2         * 
 * 2024/25 First sit    *
 * **********************/
package uk.ac.keele.csc20004.robots.parts;

/**
 * A concrete implementation of a RobotPart; more specifically a Frame.
 * The only purpose of the concrete implementation is to define
 * the resistance coefficient for this part, based on the material.
 * The material and the correspondance to resisteance coeefficient
 * are defined in the superclass.
 * 
 * @author marcoortolani
 */
public class Chassis extends Frame {

    /**
     * Constructor for a Chassis, which calls the superclass constructor
     * with the material for this part.
     * The material is defined in the superclass and is used to set the
     * resistance coefficient for this part.
     */
    public Chassis() {
        super(Frame.STEEL);
    }

    /**
     * Returns a string representation of this part.
     * 
     * @return a string representation with the id of this part
     */
    @Override
    public String toString() {
        return getMaterialName() + "-chassis-" + getId();
    }
}
