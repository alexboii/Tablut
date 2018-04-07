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
    private short MAX_TURN = 3;
    private final short CLOSE_ESTIMATOR = 5;
    private final short KING_CLOSE_CORNER_ESTIMATOR = 5;

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
    }

    /**
     * This is the primary method that you need to implement. The ``boardState``
     * object contains the current state of the game, which your agent must use to
     * make decisions.
     */
    public Move chooseMove(TablutBoardState boardState) {
        TablutMove myMove = this.rootNegamax(boardState, (short) this.MAX_TURN, Short.MIN_VALUE, Short.MAX_VALUE, (short) 1);

        // Return your move to be processed by the server.
        return myMove;
    }

    private int alphaBeta(TablutBoardState board, int depth, int alpha, int beta, boolean maximizingPlayer) {
        // TODO: add an "isWinner" clause or "isTerminal"
        if (depth == 0 || this.checkTerminal(board)) {
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

        for (TablutMove move : options) {
            TablutBoardState cloneBS = (TablutBoardState) board.clone();
            cloneBS.processMove(move);

            value = (short) -this.negamax(cloneBS, (short) (depth - 1), (short) -beta, (short) -alpha, (short) -color);

            if (value > bestValue) {
                bestValue = value;
                bestMove = move;
            }

            if (bestValue > alpha) {
                alpha = bestValue;
            }

            // prunning
            if (bestValue >= beta) {
                break;
            }
        }

        return bestMove;
    }

    private short negamax(TablutBoardState board, short depth, short alpha, short beta, short color) {
        if (depth == 0) {
            return (short) (color * evaluateMove(board));
        }

        List<TablutMove> options = board.getAllLegalMoves();
        short bestValue = Short.MIN_VALUE;
        for (TablutMove move : options) {
            TablutBoardState cloneBS = (TablutBoardState) board.clone();
            cloneBS.processMove(move);

            short value = this.negamax(cloneBS, (short) (depth - 1), (short) -beta, (short) -alpha, (short) -color);

            bestValue = (short) Math.max(value, bestValue);
            alpha = (short) Math.max(alpha, value);

            if (bestValue >= beta) {
                break;
            }
        }

        return bestValue;
    }


    private boolean checkTerminal(TablutBoardState board) {
        return board.getWinner() == player_id || board.getWinner() == this.opponent;
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
            if (numPieces - newNumPieces != 0) {
                return false;
            }
        }
        return true;
    }

    private short evaluateSwede(TablutBoardState board) {
        Coord kingPos = board.getKingPosition();

        int minDistance = Coordinates.distanceToClosestCorner(kingPos);

        short numberOfMoves = 0;
        for (TablutMove move : board.getLegalMovesForPosition(kingPos)) {
            int moveDistance = Coordinates.distanceToClosestCorner(move.getEndPosition());
            if (moveDistance < minDistance ) {
                TablutBoardState cloneBS = (TablutBoardState) board.clone();
                cloneBS.processMove(move);
                if(isMoveSafe(cloneBS)) {
                    minDistance = moveDistance;
                }
            }

            numberOfMoves++;
        }

        short score = 0;
        for (Coord coord : board.getPlayerPieceCoordinates()) {
            score -= coord.distance(kingPos);
        }

        for (Coord coord : board.getOpponentPieceCoordinates()) {
            score += coord.distance(kingPos);
        }

        short opponentPieceCount = (short) board.getNumberPlayerPieces(this.opponent);
        short myPieceCount = (short) board.getPlayerPieceCoordinates().size();
        short pieceAdvantage = (short) (myPieceCount - opponentPieceCount);

        // the further you are from corner, the more you get penalized
        return (short)(pieceAdvantage + score + numberOfMoves - minDistance);
    }

    private short evaluateMuscovite(TablutBoardState board) {
        Coord kingPos = board.getKingPosition();

        short score = 0;
        for (Coord coord : board.getPlayerPieceCoordinates()) {
            score -= 1 * coord.distance(kingPos);
        }
        for (Coord coord : board.getOpponentPieceCoordinates()) {
            score += 1 * coord.distance(kingPos);
        }

        short opponentPieceCount = (short) board.getNumberPlayerPieces(this.opponent);
        short myPieceCount = (short) board.getPlayerPieceCoordinates().size();
        short pieceAdvantage = (short) (myPieceCount - opponentPieceCount);

        return (short) (pieceAdvantage + score);
    }

}