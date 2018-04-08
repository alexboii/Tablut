package student_player;

import coordinates.Coord;

import java.util.HashSet;

/**
 * Created by alex on 4/7/18.
 */
public class HashPair {

    public HashSet<Coord> myCoords;
    public HashSet<Coord> opponentCords;


    public HashPair(HashSet<Coord> myCoords, HashSet<Coord> opponentCords) {
        this.myCoords = myCoords;
        this.opponentCords = opponentCords;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HashPair hashPair = (HashPair) o;

        if (myCoords != null ? !myCoords.equals(hashPair.myCoords) : hashPair.myCoords != null) return false;
        return opponentCords != null ? opponentCords.equals(hashPair.opponentCords) : hashPair.opponentCords == null;
    }

    @Override
    public int hashCode() {
        int result = myCoords != null ? myCoords.hashCode() : 0;
        result = 31 * result + (opponentCords != null ? opponentCords.hashCode() : 0);
        return result;
    }
}
