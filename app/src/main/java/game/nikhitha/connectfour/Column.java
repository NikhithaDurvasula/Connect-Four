package game.nikhitha.connectfour;

public class Column
{
    private Slot[] slots;
    
    /**
     * Creates a new Column with a given height.
     *
     * @param height the height of the column.
     */
    public Column(int height)
    {
        slots = new Slot[height];
        for (int i = 0; i < height; i++)
        {
            slots[i] = new Slot();
        }
    }

    /**
     * Creates a copy of the given Column.
     *
     * @param column the column to copy.
     */
    public Column(Column column)
    {
        this.slots = new Slot[column.getRowCount()];
        for (int i = 0; i < column.getRowCount(); i++)
        {
            slots[i] = new Slot(column.getSlot(i));
        }
    }

    /**
     * Returns a single Slot from the column.
     *
     * @param i the Slot to retrieve.
     * @return the Slot at that index.
     */
    public Slot getSlot(int i)
    {
        if (i < slots.length && i >= 0)
        {
            return slots[i];
        }
        else
        {
            return null;
        }
    }

    /**
     * Checks if the column is full.
     *
     * @return true if the column is full, false otherwise.
     */
    public boolean getIsFull()
    {
        for (Slot slot : slots)
        {
            if (!slot.getIsFilled())
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the number of rows in the column.
     *
     * @return the number of rows in the column.
     */
    public int getRowCount()
    {
        return slots.length;
    }

    /**
     * Returns true if column is empty
     * 
     * @return boolean true if column is empty, false otherwise
     */
    public boolean isEmpty()
    {
    	boolean empty = true;
    	
    	for (Slot each : slots)
    	{
    		if (each.getIsFilled())
    		{
    			empty = false;
    		}
    	}
    	
    	return empty;
    }

    /**
     * Method to set false for each slot's isLastFilled variable,
     * so it won't be highlighted on draw
     */
    public void clearLastMove() {
        for (Slot slot : slots)
        {
            slot.setLastFilled();
        }
    }
}
