/*
 * MIT License
 *
 * Copyright (c) 2016. Dmytro Karataiev
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package game.nikhitha.connectfour.players;

import java.util.Random;

import game.nikhitha.connectfour.Column;
import game.nikhitha.connectfour.Game;
import game.nikhitha.connectfour.Slot;

public class BrilliantAgent extends Agent {
    Random r = new Random();

    public BrilliantAgent(Game var1, boolean var2) {
        super(var1, var2);
    }

    public void move() {
        boolean var1 = false;
        int var2 = this.canWin(this.iAmRed);
        int var3 = this.canWin(!this.iAmRed);
        int var4;
        if(var2 >= 0) {
            var4 = var2;
        } else if(var3 >= 0) {
            var4 = var3;
        } else {
            var4 = this.randomMove();
        }

        this.moveOnColumn(var4);
    }

    public int randomMove() {
        int var1;
        for(var1 = this.r.nextInt(this.myGame.getColumnCount()); this.getTopEmptySlot(this.myGame.getColumn(var1)) == null; var1 = this.r.nextInt(this.myGame.getColumnCount())) {
            ;
        }

        return var1;
    }

//    public void moveOnColumn(int var1) {
//        Slot var2 = this.getTopEmptySlot(this.myGame.getColumn(var1));
//        if(var2 != null) {
//            if(this.iAmRed) {
//                var2.addRed();
//            } else {
//                var2.addYellow();
//            }
//        }
//
//    }

    public Slot getTopEmptySlot(Column var1) {
        int var2 = -1;

        for(int var3 = 0; var3 < var1.getRowCount(); ++var3) {
            if(!var1.getSlot(var3).getIsFilled()) {
                var2 = var3;
            }
        }

        if(var2 < 0) {
            return null;
        } else {
            return var1.getSlot(var2);
        }
    }

    public int canWin(boolean var1) {
        for(int var2 = 0; var2 < this.myGame.getColumnCount(); ++var2) {
            int var3 = this.getTopEmptyIndex(this.myGame.getColumn(var2));
            if(var3 > -1) {
                if(var3 < this.myGame.getRowCount() - 3 && this.myGame.getColumn(var2).getSlot(var3 + 1).getIsRed() == var1 && this.myGame.getColumn(var2).getSlot(var3 + 2).getIsRed() == var1 && this.myGame.getColumn(var2).getSlot(var3 + 3).getIsRed() == var1) {
                    return var2;
                }

                if(var2 < this.myGame.getColumnCount() - 3 && this.checkIfEqual(var1, this.myGame.getColumn(var2 + 1).getSlot(var3), this.myGame.getColumn(var2 + 2).getSlot(var3), this.myGame.getColumn(var2 + 3).getSlot(var3))) {
                    return var2;
                }

                if(var2 < this.myGame.getColumnCount() - 2 && var2 > 0 && this.checkIfEqual(var1, this.myGame.getColumn(var2 - 1).getSlot(var3), this.myGame.getColumn(var2 + 1).getSlot(var3), this.myGame.getColumn(var2 + 2).getSlot(var3))) {
                    return var2;
                }

                if(var2 < this.myGame.getColumnCount() - 1 && var2 > 1 && this.checkIfEqual(var1, this.myGame.getColumn(var2 - 1).getSlot(var3), this.myGame.getColumn(var2 + 1).getSlot(var3), this.myGame.getColumn(var2 - 2).getSlot(var3))) {
                    return var2;
                }

                if(var2 > 2 && this.checkIfEqual(var1, this.myGame.getColumn(var2 - 1).getSlot(var3), this.myGame.getColumn(var2 - 3).getSlot(var3), this.myGame.getColumn(var2 - 2).getSlot(var3))) {
                    return var2;
                }

                if(var2 < this.myGame.getColumnCount() - 3 && var3 < this.myGame.getRowCount() - 3 && this.checkIfEqual(var1, this.myGame.getColumn(var2 + 1).getSlot(var3 + 1), this.myGame.getColumn(var2 + 3).getSlot(var3 + 3), this.myGame.getColumn(var2 + 2).getSlot(var3 + 2))) {
                    return var2;
                }

                if(var2 < this.myGame.getColumnCount() - 2 && var2 > 0 && var3 < this.myGame.getRowCount() - 2 && var3 > 0 && this.checkIfEqual(var1, this.myGame.getColumn(var2 + 1).getSlot(var3 + 1), this.myGame.getColumn(var2 - 1).getSlot(var3 - 1), this.myGame.getColumn(var2 + 2).getSlot(var3 + 2))) {
                    return var2;
                }

                if(var2 < this.myGame.getColumnCount() - 1 && var2 > 1 && var3 < this.myGame.getRowCount() - 1 && var3 > 1 && this.checkIfEqual(var1, this.myGame.getColumn(var2 + 1).getSlot(var3 + 1), this.myGame.getColumn(var2 - 1).getSlot(var3 - 1), this.myGame.getColumn(var2 - 2).getSlot(var3 - 2))) {
                    return var2;
                }

                if(var2 < this.myGame.getColumnCount() && var2 > 2 && var3 < this.myGame.getRowCount() && var3 > 2 && this.checkIfEqual(var1, this.myGame.getColumn(var2 - 1).getSlot(var3 - 1), this.myGame.getColumn(var2 - 2).getSlot(var3 - 2), this.myGame.getColumn(var2 - 3).getSlot(var3 - 3))) {
                    return var2;
                }

                if(var2 > 2 && var2 < this.myGame.getColumnCount() && var3 < this.myGame.getRowCount() - 3 && var3 >= 0 && this.checkIfEqual(var1, this.myGame.getColumn(var2 - 1).getSlot(var3 + 1), this.myGame.getColumn(var2 - 2).getSlot(var3 + 2), this.myGame.getColumn(var2 - 3).getSlot(var3 + 3))) {
                    return var2;
                }

                if(var2 > 1 && var2 < this.myGame.getColumnCount() - 1 && var3 < this.myGame.getRowCount() - 2 && var3 > 0 && this.checkIfEqual(var1, this.myGame.getColumn(var2 - 1).getSlot(var3 + 1), this.myGame.getColumn(var2 - 2).getSlot(var3 + 2), this.myGame.getColumn(var2 + 1).getSlot(var3 - 1))) {
                    return var2;
                }

                if(var2 > 0 && var2 < this.myGame.getColumnCount() - 2 && var3 < this.myGame.getRowCount() - 1 && var3 > 1 && this.checkIfEqual(var1, this.myGame.getColumn(var2 - 1).getSlot(var3 + 1), this.myGame.getColumn(var2 + 2).getSlot(var3 - 2), this.myGame.getColumn(var2 + 1).getSlot(var3 - 1))) {
                    return var2;
                }

                if(var2 >= 0 && var2 < this.myGame.getColumnCount() - 3 && var3 < this.myGame.getRowCount() && var3 > 2 && this.checkIfEqual(var1, this.myGame.getColumn(var2 + 3).getSlot(var3 - 3), this.myGame.getColumn(var2 + 2).getSlot(var3 - 2), this.myGame.getColumn(var2 + 1).getSlot(var3 - 1))) {
                    return var2;
                }
            }
        }

        return -1;
    }

    public boolean checkIfEqual(boolean var1, Slot var2, Slot var3, Slot var4) {
        return var2.getIsFilled() && var3.getIsFilled() && var4.getIsFilled() && var2.getIsRed() == var1 && var3.getIsRed() == var1 && var4.getIsRed() == var1;
    }

    public int getTopEmptyIndex(Column var1) {
        int var2 = -1;

        for(int var3 = 0; var3 < var1.getRowCount(); ++var3) {
            if(!var1.getSlot(var3).getIsFilled()) {
                var2 = var3;
            }
        }

        return var2;
    }

}