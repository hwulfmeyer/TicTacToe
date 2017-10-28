import de.ovgu.dke.teaching.ml.tictactoe.api.IBoard;
import de.ovgu.dke.teaching.ml.tictactoe.api.IPlayer;
import de.ovgu.dke.teaching.ml.tictactoe.api.IllegalMoveException;
import de.ovgu.dke.teaching.ml.tictactoe.game.Move;

/**
 * 125(5x5x5) Input neurons; 62 hidden Neurons; 1 Output Neuron
 * L1 -> L2 => 7750 weights
 * 
 *
 * @author Hans-Martin Wulfmeyer
 */
public class NeuralPlayer implements IPlayer {


	public NeuralPlayer() {
		
	}
	
	public String getName() {
		// TODO Auto-generated method stub
		return "NeuralPlayer";
	}

	public int[] makeMove(IBoard board) {

		//generate all possible moves from board and choose the one with the best value
		float bestBoardValue = 0;
		int[] bestMove = null;
		for (int x = 0; x < board.getSize(); x++) {
			for (int y = 0; y < board.getSize(); y++) {
				for (int z = 0; z < board.getSize(); z++) {
					IBoard boardCopy = board.clone();
					// try move out, if move is illegal do nothing and go on
					boolean isLegalMove = true;
					try {
						boardCopy.makeMove(new Move(this, new int[] { x, y, z }));
					} catch (IllegalMoveException e) {
						isLegalMove = false;
					}
					if(isLegalMove) {
						//get value from our learned function how good the 'move'(the board state after the move) is
						float boardCopyValue = getBoardValue(boardCopy);
						// we could ask here if the move gives us a win and do it but we want to train it instead; board.isFinalState()
						if(bestMove == null || boardCopyValue >= bestBoardValue) {
							bestBoardValue = boardCopyValue;
							bestMove = new int[] { x, y, z };
						}
					}
				}
			}
		}
		return bestMove;
	}

	// function for neural net calculation of board
	public float getBoardValue(IBoard board) {
		
		return 0;
	}
	
	public void onMatchEnds(IBoard board) {
		if(board.getWinner() == null) {
			//draw
		}
		else if(board.getWinner() == this) {
			//win
		}
		else {
			//loss
		}
		
		return;
	}

}
