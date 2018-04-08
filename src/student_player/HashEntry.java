package student_player;

import tablut.TablutMove;

/**
 * Created by alex on 4/7/18.
 */
public class HashEntry {

    TablutMove move;

    short flag;
    short value;
    short depth;

    public HashEntry() {
        this.move = null;
    }

    public HashEntry(TablutMove move, short type, short value, short depth) {
        this.move = move;
        this.flag = type;
        this.value = value;
        this.depth = depth;
    }

    public TablutMove getMove() {
        return move;
    }

    public void setMove(TablutMove move) {
        this.move = move;
    }

    public short getFlag() {
        return flag;
    }

    public short getValue() {
        return value;
    }

    public short getDepth() {
        return depth;
    }

    public void setFlag(short flag) {
        this.flag = flag;
    }

    public void setValue(short value) {
        this.value = value;
    }

    public void setDepth(short depth) {
        this.depth = depth;
    }

}
