package student_player;

import boardgame.Move;
import coordinates.Coord;
import coordinates.Coordinates;
import tablut.TablutBoardState;
import tablut.TablutMove;
import tablut.TablutPlayer;

import java.util.List;
import java.util.Random;

/**
 * A player file submitted by a student.
 */
public class StudentPlayer extends TablutPlayer {

    private Random rand = new Random(1848);

    /**
     * You must modify this constructor to return your student number. This is
     * important, because this is what the code that runs the competition uses to
     * associate you with your agent. The constructor should do nothing else.
     */
    public StudentPlayer() {
        super("260684228");
    }

    /**
     * This is the primary method that you need to implement. The ``boardState``
     * object contains the current state of the game, which your agent must use to
     * make decisions.
     */
    public Move chooseMove(TablutBoardState boardState) {
        // You probably will make separate functions in MyTools.
        // For example, maybe you'll need to load some pre-processed best opening
        // strategies...
        MyTools.getSomething();

        List<TablutMove> options = boardState.getAllLegalMoves();

        // Is random the best you can do?
        TablutMove myMove = options.get(rand.nextInt(options.size()));

        TablutBoardState randomState = (TablutBoardState) boardState.clone();
        randomState.processMove(myMove);
        int opponent = randomState.getOpponent();
        int min = randomState.getNumberPlayerPieces(opponent);
        int startingState = min;

        // we set current state as root
        for (int i = 0; i < options.size(); i++) {
            TablutBoardState cloneBS = (TablutBoardState) boardState.clone();

            TablutMove currentMove = options.get(i);

            cloneBS.processMove(currentMove);

            int result = this.alphaBeta(cloneBS, 3, Integer.MIN_VALUE, Integer.MAX_VALUE, true);

            if (result <= min) {
                myMove = currentMove;
                min = result;
            }
        }

        System.out.println("RESULT = " + min);
        System.out.println(player_id);
        
        if (player_id == TablutBoardState.SWEDE && startingState == min) {
            Coord kingPos = boardState.getKingPosition();

            // Don't do a move if it wouldn't get us closer than our current position.
            int minDistance = Coordinates.distanceToClosestCorner(kingPos);

            // Iterate over moves from a specific position, the king's position!
            for (TablutMove move : boardState.getLegalMovesForPosition(kingPos)) {
                /*
                 * Here it is not necessary to actually process the move on a copied boardState.
                 * Note that it is more efficient NOT to copy the boardState. Consider this
                 * during implementation...
                 */
                int moveDistance = Coordinates.distanceToClosestCorner(move.getEndPosition());
                if (moveDistance < minDistance) {
                    minDistance = moveDistance;
                    myMove = move;
                }
            }
        }
        
        
        // Return your move to be processed by the server.
        return myMove;
    }

    private int alphaBeta(TablutBoardState board, int depth, int alpha, int beta, boolean maximizingPlayer) {
        // TODO: add an "isWinner" clause or "isTerminal"
        if (depth == 0) {
            int opponent = board.getOpponent();
            int result = board.getNumberPlayerPieces(opponent);
            return result;
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

    private boolean checkTerminal(TablutBoardState board) {
        return board.getWinner() == player_id || board.getWinner() == board.getOpponent();
    }

}