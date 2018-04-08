package student_player;

import boardgame.Move;
import coordinates.Coord;
import coordinates.Coordinates;
import tablut.TablutBoardState;
import tablut.TablutMove;
import tablut.TablutPlayer;

import java.util.*;

/**
 * A player file submitted by a student.
 */
public class StudentPlayer extends TablutPlayer {

    private Random rand = new Random(1848);
    private int opponent;
    private short MAX_DEPTH = 3;
    private HashMap<HashPair, HashEntry> cache;

    /**
     * You must modify this constructor to return your student number. This is
     * important, because this is what the code that runs the competition uses to
     * associate you with your agent. The constructor should do nothing else.
     */
    public StudentPlayer() {
        super("260684228");

        if (player_id == TablutBoardState.SWEDE) {
            this.opponent = TablutBoardState.MUSCOVITE;
        } else {
            this.opponent = TablutBoardState.SWEDE;
        }

        this.cache = new HashMap<>();
    }

    /**
     * This is the primary method that you need to implement. The ``boardState``
     * object contains the current state of the game, which your agent must use to
     * make decisions.
     */
    public Move chooseMove(TablutBoardState boardState) {
        TablutMove myMove = this.rootNegamax(boardState, (short) this.MAX_DEPTH, Short.MIN_VALUE, Short.MAX_VALUE, (short) 1);

        return myMove;
    }

    // deprecated
    private int alphaBeta(TablutBoardState board, int depth, int alpha, int beta, boolean maximizingPlayer) {
        if (depth == 0) {
            short moveScore = this.evaluateMove(board);
            return moveScore;
        }

        List<TablutMove> options = board.getAllLegalMoves();

        if (maximizingPlayer) {
            for (TablutMove move : options) {
                TablutBoardState cloneBS = (TablutBoardState) board.clone();
                cloneBS.processMove(move);

                alpha = Math.max(alpha, alphaBeta(cloneBS, depth - 1, alpha, beta, false));

                if (beta <= alpha) {
                    break;
                }
            }

            return alpha;
        } else {
            for (TablutMove move : options) {
                TablutBoardState cloneBS = (TablutBoardState) board.clone();
                cloneBS.processMove(move);

                beta = Math.max(alpha, alphaBeta(cloneBS, depth - 1, alpha, beta, true));

                if (beta <= alpha) {
                    break;
                }
            }

            return beta;
        }
    }

    private TablutMove rootNegamax(TablutBoardState board, short depth, short alpha, short beta, short color) {
        List<TablutMove> options = board.getAllLegalMoves();

        short value;
        short bestValue = Short.MIN_VALUE;
        TablutMove bestMove = options.get(rand.nextInt(options.size()));

        Coord kingPos = board.getKingPosition();
        int minDistance = Coordinates.distanceToClosestCorner(kingPos);

        for (TablutMove move : options) {

            TablutBoardState cloneBS = (TablutBoardState) board.clone();
            cloneBS.processMove(move);

            if (cloneBS.getWinner() == player_id) {
                return move;
            }

            int moveDistance = Coordinates.distanceToClosestCorner(move.getEndPosition());

            // perform a quick, greedy, filthy, capitalist move if we can
            // similar to how the greedy player does it,
            // only we check if it is safe (i.e. no resulting capture)
            if (this.player_id == TablutBoardState.SWEDE) {
                if (moveDistance < minDistance) {
                    if (this.isMoveSafe(cloneBS)) {
                        return move;
                    }
                }
            }

            value = (short) -this.negamax(cloneBS, (short) (depth - 1), (short) -beta, (short) -alpha, (short) -color);

            if (value > bestValue) {
                bestValue = value;
                bestMove = move;
            }

            if (bestValue > alpha) {
                alpha = bestValue;
            }

            if (bestValue >= beta) {
                break;
            }
        }

        return bestMove;
    }

    private short negamax(TablutBoardState board, short depth, short alpha, short beta, short color) {
        short alphaOrig = alpha;

        // check in the cache
        HashEntry cacheEntry = cache.get(this.generateHash(board));

        if (cacheEntry != null && cacheEntry.getDepth() >= depth) {
            if (cacheEntry.getFlag() == 0) {
                return cacheEntry.getValue();
            } else if (cacheEntry.getFlag() == -1) {
                alpha = (short) Math.max(alpha, cacheEntry.getValue());
            } else if (cacheEntry.getValue() == 1) {
                beta = (short) Math.min(beta, cacheEntry.getValue());
            }

            if (alpha >= beta) {
                return cacheEntry.getValue();
            }
        }

        if (depth == 0) {
            return (short) (color * evaluateMove(board));
        }

        List<TablutMove> options = board.getAllLegalMoves();
        short bestValue = Short.MIN_VALUE;
        TablutMove bestMove = options.get(rand.nextInt(options.size()));

        for (TablutMove move : options) {
            TablutBoardState cloneBS = (TablutBoardState) board.clone();
            cloneBS.processMove(move);

            short value = this.negamax(cloneBS, (short) (depth - 1), (short) -beta, (short) -alpha, (short) -color);

            if (value > bestValue) {
                bestValue = value;
                bestMove = move;
            }

            alpha = (short) Math.max(alpha, value);

            if (bestValue >= beta) {
                break;
            }
        }

        // cache the newly found values
        HashEntry newEntry = new HashEntry();
        newEntry.setValue(bestValue);
        newEntry.setMove(bestMove);

        if (bestValue <= alphaOrig) {
            newEntry.setFlag((short) 1);
        } else if (bestValue >= beta) {
            newEntry.setFlag((short) -1);
        } else {
            newEntry.setFlag((short) 0);
        }

        newEntry.setDepth(depth);
        cache.put(generateHash(board), newEntry);

        return bestValue;
    }

    private short evaluateMove(TablutBoardState board) {
        if (board.getWinner() == this.opponent) {
            return Short.MIN_VALUE;
        }

        if (board.getWinner() == this.player_id) {
            return Short.MAX_VALUE;
        }

        return (player_id == TablutBoardState.SWEDE) ? (short) -evaluateSwede(board) : evaluateMuscovite(board);
    }

    private boolean isMoveSafe(TablutBoardState board) {
        int numPieces = board.getNumberPlayerPieces(this.player_id);

        for (TablutMove move : board.getAllLegalMoves()) {
            TablutBoardState cloneBS = (TablutBoardState) board.clone();
            cloneBS.processMove(move);
            int newNumPieces = cloneBS.getNumberPlayerPieces(this.player_id);
            // if we don't loose any pieces, we gucci
            if (numPieces > newNumPieces) {
                return false;
            }
        }

        return true;
    }

    private short evaluateSwede(TablutBoardState board) {
        Coord kingPos = board.getKingPosition();

        int minDistance = Coordinates.distanceToClosestCorner(kingPos);

        short piecesDistance = 0;

        // the closer my players are to my king, the better (more protection)
        for (Coord coord : board.getPlayerPieceCoordinates()) {
            piecesDistance += coord.distance(kingPos);
        }

        // penalize opponent pieces
        for (Coord coord : board.getOpponentPieceCoordinates()) {
            piecesDistance -= coord.distance(kingPos);
        }

        short myPieceCount = (short) board.getPlayerPieceCoordinates().size();

        // the further you are from corner, the more you get penalized
        return (short) (3 * myPieceCount + piecesDistance - minDistance);
    }

    private short evaluateMuscovite(TablutBoardState board) {
        Coord kingPos = board.getKingPosition();

        short piecesDistance = 0;

        // we want a smaller distance from opponets
        for (Coord coord : board.getOpponentPieceCoordinates()) {
            piecesDistance += 1 * coord.distance(kingPos);
        }

        // the closer we are to king, the better (penalize long distances from king)
        for (Coord coord : board.getPlayerPieceCoordinates()) {
            piecesDistance -= 1 * coord.distance(kingPos);
        }

        short opponentPieceCount = (short) board.getNumberPlayerPieces(this.opponent);
        short myPieceCount = (short) board.getPlayerPieceCoordinates().size();

        // clearly, we want to choose the move that puts us in an advantage
        short pieceAdvantage = (short) (myPieceCount - opponentPieceCount);

        return (short) (pieceAdvantage + piecesDistance);
    }


    private HashPair generateHash(TablutBoardState board) {
        return new HashPair(board.getPlayerPieceCoordinates(), board.getOpponentPieceCoordinates());
    }


}