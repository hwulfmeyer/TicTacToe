import de.ovgu.dke.teaching.ml.tictactoe.api.IBoard;
import de.ovgu.dke.teaching.ml.tictactoe.api.IPlayer;
import de.ovgu.dke.teaching.ml.tictactoe.api.IllegalMoveException;
import de.ovgu.dke.teaching.ml.tictactoe.game.Move;

/**
 * Some comments ...
 * 
 * @author Name of your team members
 */

public class JoshuaSARSA implements IPlayer {

	public String getName() {
		// TODO Auto-generated method stub
		return "the name of your player";
	}

	public int[] makeMove(IBoard board) {
		// TODO Auto-generated method stub

		// create a clone of the board that can be modified
		IBoard copy = board.clone();

		// do a move using the cloned board
		try {
			copy.makeMove(new Move(this, new int[] { 0, 0, 0 }));
		} catch (IllegalMoveException e) {
			// move was not allowed
		}

		// return your final decision for your next move
		return new int[] { 1, 2, 3 };
	}

	public void onMatchEnds(IBoard board) {
		return;
	}

}