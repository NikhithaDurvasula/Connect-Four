package game.nikhitha.connectfour;

public class Slot {
    private boolean isFilled;
    private boolean isRed;
    private boolean isHighlighted;
    private boolean isLastFilled;

    /**
     * Creates a new Slot, initially unfilled.
     */
    public Slot() {
        this.isFilled = false;
        this.isRed = false;
    }

    /**
     * Copies the given slot.
     *
     * @param slot the slot to copy.
     */
    public Slot(Slot slot) {
        this.isFilled = slot.getIsFilled();
        this.isRed = slot.getIsRed();
    }

    /**
     * Checks if the slot is currently filled.
     *
     * @return true if filled, false if not.
     */
    public boolean getIsFilled() {
        return isFilled;
    }

    /**
     * If the slot is filled, checks if the token in the slot is red.
     * <p/>
     * If the slot is not filled, this will still return false; so, this should only
     * be checked after checking getIsFilled().
     *
     * @return true if the token in the slot is red, false if it is yellow.
     */
    public boolean getIsRed() {
        return isRed;
    }

    /**
     * If the slot is currently empty, adds a red token to it.
     */
    public void addRed() {
        if (!isFilled) {
            this.isFilled = true;
            this.isRed = true;
        }
    }

    /**
     * If the slot is currently empty, adds a yellow token to it.
     */
    public void addYellow() {
        if (!isFilled) {
            this.isFilled = true;
            this.isRed = false;
        }
    }

    /**
     * Checks if the slot should be highlighted because it is part of a winning move.
     *
     * @return true if the slot is highlighted, false if not.
     */
    public boolean getIsHighlighted() {
        return isHighlighted;
    }

    /**
     * Highlights the slot.
     */
    public void highlight() {
        this.isHighlighted = true;
    }

    /**
     * Clears the slot.
     */
    public void clear() {
        this.isFilled = false;
        this.isRed = false;
        this.isHighlighted = false;
    }

    /**
     * Set the slot as filled last
     */
    public void addLastFilled() {
        this.isLastFilled = true;
    }

    /**
     * Set the slot as "usual" (do not highlight)
     */
    public void setLastFilled() {
        this.isLastFilled = false;
    }

    /**
     * Check if it was the last filled slot and then highlight
     *
     * @return true if it was last filled
     */
    public boolean getLastFilled() {
        return isLastFilled;
    }
}
