package game.nikhitha.connectfour;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.widget.ImageView;

public class Panel {
    private Game myGame;    // the game to display
    private int slotRadius;  // size of the individual slots (radius)
    private int slotSpacing; // space between slots
    View current;
    ImageView ll;
    int[] dimensions;
    Bitmap bg;

    /**
     * Creates a new Panel with a given game.
     *
     * @param game the game to display.
     */
    public Panel(Game game, View current, int[] dimensions) {
        this.myGame = game;
        this.slotRadius = 71;
        this.slotSpacing = slotRadius + 5;
        this.ll = (ImageView) current.findViewById(R.id.gameField);
        this.current = current;
        this.dimensions = dimensions;
    }

    /**
     * Paints the current status of the game.
     */
    public void paint() {
        Paint paint = new Paint();

        paint.setColor(Color.parseColor("#CD5C5C"));
        bg = Bitmap.createBitmap(dimensions[0], dimensions[1], Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bg);

        // formula to spread out slots to whole canvas
        slotRadius = (int) ((float) (dimensions[0]) / 7 - (float) dimensions[0] / 7 * 0.07) / 2;
        slotSpacing = slotRadius + (int) ((float) dimensions[0] / 7 * 0.07);

        for (int i = 0; i < myGame.getColumnCount(); i++) {
            for (int j = 0; j < myGame.getRowCount(); j++) {
                Column column = myGame.getColumn(i);
                Slot currentSlot = column.getSlot(j);

                if (!currentSlot.getIsFilled()) {
                    paint.setColor(Color.WHITE);
                } else {
                    if (currentSlot.getIsRed()) {
                        paint.setColor(Color.RED);
                    } else {
                        paint.setColor(Color.YELLOW);
                    }
                }
                int x = ((i + 1) * slotSpacing) + (i * slotRadius);
                int y = ((j + 1) * slotSpacing) + (j * slotRadius);
                y += dimensions[1] - dimensions[0] + 2 * slotRadius;

                drawSlot(canvas, x, y, slotRadius, paint);

                // highlight if it is in winning line or if it was filled last
                if (currentSlot.getIsHighlighted() || currentSlot.getLastFilled()) {
                    if (currentSlot.getIsRed()) {
                        paint.setColor(Color.RED);
                    } else {
                        paint.setColor(Color.YELLOW);
                    }
                    drawHighlight(canvas, x, y, slotRadius, paint);
                }
            }
        }
        ll.setImageDrawable(new BitmapDrawable(current.getResources(), bg));
    }

    /**
     * Draw a single slot.
     *
     * @param canvas the canvas to draw on
     * @param x      the center x-coordinate where to draw the slot.
     * @param y      the center y-coordinate where to draw the slot.
     * @param color  the color for the slot.
     */
    public void drawSlot(Canvas canvas, int x, int y, int slotRadius, Paint color) {
        canvas.drawCircle(x, y, slotRadius, color);
    }

    /**
     * Highlight a slot.
     * the graphics object with which to paint.
     *
     * @param canvas the canvas to draw on
     * @param x      the top-left x-coordinate where to draw the highlight.
     * @param y      the top-left y-coordinate where to draw the highlight.
     * @param paint  color to use
     */
    public void drawHighlight(Canvas canvas, int x, int y, int slotRadius, Paint paint) {
        Paint old = new Paint();
        old.setColor(paint.getColor());

        paint.setColor(Color.GREEN);
        canvas.drawCircle(x, y, slotRadius, paint);
        canvas.drawCircle(x, y, slotRadius - 10, old);
    }

}

