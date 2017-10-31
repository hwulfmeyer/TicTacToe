import de.ovgu.dke.teaching.ml.tictactoe.api.IBoard;
import de.ovgu.dke.teaching.ml.tictactoe.api.IPlayer;
import de.ovgu.dke.teaching.ml.tictactoe.api.IllegalMoveException;
import de.ovgu.dke.teaching.ml.tictactoe.game.Move;

/**
 * @author Hans-Martin Wulfmeyer, Dimtrii Zyrianov
 */
public class JoshuaWOPR implements IPlayer {

	private static final float LEARN_RATE = 0.01f;
	private static final int NUM_FEATURES = 11;
	private float[] Weights;
	private float PrevBoardValue;
	private int[] PrevBoardFeatures;

	// init the variables we need
	public JoshuaWOPR() {
		PrevBoardFeatures = new int[NUM_FEATURES];
		PrevBoardValue = 0f;
		Weights = new float[NUM_FEATURES];
		// init weights as 1 beside weight_0
		for (int i = 1; i < NUM_FEATURES; i++) {
			/*
			 * if(i>5) Weights[i] = -1f; else Weights[i] = 1f;
			 */
			Weights[i] = 1f;
		}
		// Weights = new float[] {0,10,25,50,75,100,-10,-25,-50,-75,-100};
	}

	public String getName() {
		return "Joshua/WOPR";
	}

	// returns the move to make in this current play
	public int[] makeMove(IBoard board) {
		float curBoardValue = getBoardValue(board);
		// start learning with the current score and prev. score
		if (board.getMoveHistory().size() > 2)
			updateWeights(PrevBoardValue, curBoardValue, PrevBoardFeatures);
		PrevBoardValue = curBoardValue;
		PrevBoardFeatures = getBoardFeatures(board);

		/*
		 * System.out.print("F: "); for (int i = 0; i < PrevBoardFeatures.length; i++) {
		 * System.out.print("F"+i+"="+PrevBoardFeatures[i] + " | "); }
		 * System.out.println();
		 */
		/*
		 * System.out.print("WEIGHTS: "); for (int i = 0; i < Weights.length; i++) {
		 * System.out.print(Weights[i] + " | "); } System.out.println();
		 */

		// generate all possible moves from board and choose the one with the best value
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
					if (isLegalMove) {
						// get value from our learned function how good the 'move'(the board state after
						// the move) is
						float boardCopyValue = getBoardValue(boardCopy);
						// we could ask here if the move gives us a win and do it but we want to train
						// it instead; board.isFinalState()
						if (bestMove == null || boardCopyValue >= bestBoardValue) {
							bestBoardValue = boardCopyValue;
							bestMove = new int[] { x, y, z };
						}
					}
				}
			}
		}
		return bestMove;
	}

	// calculate the value for 'board' from learned function
	public float getBoardValue(IBoard board) {
		int[] features = getBoardFeatures(board);
		float value = 0.0f;
		for (int i = 0; i < NUM_FEATURES; i++) {
			value += Weights[i] * features[i];
		}
		return value > 100 ? 100 : value < -100 ? -100 : value;
	}

	// calculate the values for our board features
	private int[] getBoardFeatures(IBoard board) {
		int[] boardFeatures = new int[NUM_FEATURES];
		boardFeatures[0] = 1;
		/*
		 * x0 = 1 X = my player, O = enemy player x1 = rows/columns/aisles/diagonals
		 * containing at least 1 X and 0 O x2 = rows/columns/aisles/diagonals containing
		 * at least 2 X and 0 O x3 = rows/columns/aisles/diagonals containing at least 3
		 * X and 0 O x4 = rows/columns/aisles/diagonals containing at least 4 X and 0 O
		 * x5 = rows/columns/aisles/diagonals containing at least 5 X and 0 O
		 * 
		 * x6 = rows/columns/aisles/diagonals containing at least 1 O and 0 X x7 =
		 * rows/columns/aisles/diagonals containing at least 2 O and 0 X x8 =
		 * rows/columns/aisles/diagonals containing at least 3 O and 0 X x9 =
		 * rows/columns/aisles/diagonals containing at least 4 O and 0 X x10 =
		 * rows/columns/aisles/diagonals containing at least 5 O and 0 X
		 */

		// check rows/columns/aisles
		for (int z = 0; z < board.getSize(); z++) {
			for (int x = 0; x < board.getSize(); x++) {

				int columnCounterEnemy = 0;
				int columnCounterMine = 0;
				int rowCounterEnemy = 0;
				int rowCounterMine = 0;
				int aisleCounterEnemy = 0;
				int aisleCounterMine = 0;
				for (int y = 0; y < board.getSize(); y++) {
					// start counting X and 0 in the current column (x=width, y=height, z=depth)
					if (board.getFieldValue(new int[] { x, y, z }) == this) {
						columnCounterMine++;
					} else if (board.getFieldValue(new int[] { x, y, z }) != null) {
						columnCounterEnemy++;
					}

					// interchange x & y and we go through current row (y=width, x=height, z=depth)
					if (board.getFieldValue(new int[] { y, x, z }) == this) {
						rowCounterMine++;
					} else if (board.getFieldValue(new int[] { y, x, z }) != null) {
						rowCounterEnemy++;
					}

					// interchange ... and we go through current aisle (z=width, x=height, y=depth)
					if (board.getFieldValue(new int[] { z, x, y }) == this) {
						aisleCounterMine++;
					} else if (board.getFieldValue(new int[] { z, x, y }) != null) {
						aisleCounterEnemy++;
					}
				}
				boardFeatures = addToFeatures(boardFeatures, columnCounterMine, columnCounterEnemy);
				boardFeatures = addToFeatures(boardFeatures, rowCounterMine, rowCounterEnemy);
				boardFeatures = addToFeatures(boardFeatures, aisleCounterMine, aisleCounterEnemy);
			}
		}

		// check for diagonals in layers
		for (int z = 0; z < board.getSize(); z++) {
			int diagCounterEnemy = 0;
			int diagCounterMine = 0;
			// forward diagonals /
			for (int x = 0, y = 0; x < board.getSize() && y < board.getSize(); x++, y++) {
				if (board.getFieldValue(new int[] { x, y, z }) == this) {
					diagCounterMine++;
				} else if (board.getFieldValue(new int[] { x, y, z }) != null) {
					diagCounterEnemy++;
				}
			}
			boardFeatures = addToFeatures(boardFeatures, diagCounterMine, diagCounterEnemy);
			diagCounterEnemy = 0;
			diagCounterMine = 0;
			// backward diagonals \
			for (int x = board.getSize() - 1, y = 0; x >= 0 && y < board.getSize(); x--, y++) {
				if (board.getFieldValue(new int[] { x, y, z }) == this) {
					diagCounterMine++;
				} else if (board.getFieldValue(new int[] { x, y, z }) != null) {
					diagCounterEnemy++;
				}
			}
			boardFeatures = addToFeatures(boardFeatures, diagCounterMine, diagCounterEnemy);
		}

		// diagonal from lower left corner in L0 to upper right corner in L4
		int cornerdiagCounterEnemy = 0;
		int cornerdiagCounterMine = 0;
		for (int x = 0, y = 0, z = 0; x < board.getSize() && y < board.getSize()
				&& z < board.getSize(); x++, y++, z++) {
			if (board.getFieldValue(new int[] { x, y, z }) == this) {
				cornerdiagCounterMine++;
			} else if (board.getFieldValue(new int[] { x, y, z }) != null) {
				cornerdiagCounterEnemy++;
			}
		}
		boardFeatures = addToFeatures(boardFeatures, cornerdiagCounterMine, cornerdiagCounterEnemy);
		
		// diagonal from lower right corner in L0 to upper left corner in L4
		cornerdiagCounterEnemy = 0;
		cornerdiagCounterMine = 0;
		for (int x = board.getSize() - 1, y = 0, z = 0; x > 0 && y < board.getSize()
				&& z < board.getSize(); x--, y++, z++) {
			if (board.getFieldValue(new int[] { x, y, z }) == this) {
				cornerdiagCounterMine++;
			} else if (board.getFieldValue(new int[] { x, y, z }) != null) {
				cornerdiagCounterEnemy++;
			}
		}
		boardFeatures = addToFeatures(boardFeatures, cornerdiagCounterMine, cornerdiagCounterEnemy);
		
		// diagonal from upper left corner in L0 to lower right corner in L4
		cornerdiagCounterEnemy = 0;
		cornerdiagCounterMine = 0;
		for (int x = 0, y = board.getSize() - 1, z = 0; x < board.getSize() && y > 0
				&& z < board.getSize(); x++, y--, z++) {
			if (board.getFieldValue(new int[] { x, y, z }) == this) {
				cornerdiagCounterMine++;
			} else if (board.getFieldValue(new int[] { x, y, z }) != null) {
				cornerdiagCounterEnemy++;
			}
		}
		boardFeatures = addToFeatures(boardFeatures, cornerdiagCounterMine, cornerdiagCounterEnemy);
		
		// diagonal from upper right corner in L0 to lower left corner in L4
		cornerdiagCounterEnemy = 0;
		cornerdiagCounterMine = 0;
		for (int x = board.getSize() - 1, y = board.getSize() - 1, z = 0; x > 0 && y > 0
				&& z < board.getSize(); x--, y--, z++) {
			if (board.getFieldValue(new int[] { x, y, z }) == this) {
				cornerdiagCounterMine++;
			} else if (board.getFieldValue(new int[] { x, y, z }) != null) {
				cornerdiagCounterEnemy++;
			}
		}
		boardFeatures = addToFeatures(boardFeatures, cornerdiagCounterMine, cornerdiagCounterEnemy);
		
		//check for diagonals spanning through layers
		for (int x = 0; x < board.getSize() - 1; x++) {
			int diagCounterEnemy = 0;
			int diagCounterMine = 0;
			
			// diagonals from lower row in L0 to upper row in L4 
			for (int y = 0, z = 0; y < board.getSize() - 1 && z < board.getSize() - 1; y++, z++) {
				if (board.getFieldValue(new int[] { x, y, z }) == this) {
					diagCounterMine++;
				} else if (board.getFieldValue(new int[] { x, y, z }) != null) {
					diagCounterEnemy++;
				}
			}
			boardFeatures = addToFeatures(boardFeatures, diagCounterMine, diagCounterEnemy);		
			diagCounterEnemy = 0;
			diagCounterMine = 0;
			
			// diagonals from upper row in L0 to lower row in L4 
			for (int y = board.getSize() - 1, z = 0; y > 0 && z < board.getSize() - 1; y--, z++) {
				if (board.getFieldValue(new int[] { x, y, z }) == this) {
					diagCounterMine++;
				} else if (board.getFieldValue(new int[] { x, y, z }) != null) {
					diagCounterEnemy++;
				}
			}
			boardFeatures = addToFeatures(boardFeatures, diagCounterMine, diagCounterEnemy);
		}
		
		//check for diagonals spanning through layers
		for (int y = 0; y < board.getSize() - 1; y++) {
			int diagCounterEnemy = 0;
			int diagCounterMine = 0;
			
			//diagonals from most left column in L0 to most right column in L4
			for (int x = 0, z = 0; x < board.getSize() - 1 && z < board.getSize() - 1; x++, z++) {
				if (board.getFieldValue(new int[] { x, y, z }) == this) {
					diagCounterMine++;
				} else if (board.getFieldValue(new int[] { x, y, z }) != null) {
					diagCounterEnemy++;
				}
			}
			boardFeatures = addToFeatures(boardFeatures, diagCounterMine, diagCounterEnemy);		
			diagCounterEnemy = 0;
			diagCounterMine = 0;
			
			//diagonals from most right column in L0 to most left column in L4
			for (int x = board.getSize() - 1, z = 0; x > 0 && z < board.getSize() - 1; x--, z++) {
				if (board.getFieldValue(new int[] { x, y, z }) == this) {
					diagCounterMine++;
				} else if (board.getFieldValue(new int[] { x, y, z }) != null) {
					diagCounterEnemy++;
				}
			}
			boardFeatures = addToFeatures(boardFeatures, diagCounterMine, diagCounterEnemy);
		}

		return boardFeatures;
	}

	// function to get the features out of the counters in getBoardFeatures()
	private int[] addToFeatures(int[] features, int counterMine, int counterEnemy) {
		/*
		 * counters always count the number of X & O in the row/aisle/... How does this
		 * work? e.g. if I count 2 X and zero O I add a 1 to feature x1 & x2 This is
		 * because taking away something from x1 (when adding a X into the board) skews
		 * the board score
		 */
		// features for me
		if (counterEnemy == 0 && counterMine != 0) {
			while (counterMine > 0) {
				features[counterMine]++;
				counterMine--;
			}
		}
		// features for enemy
		else if (counterMine == 0 && counterEnemy != 0) {
			while (counterEnemy > 0) {
				features[counterEnemy + 5]++;
				counterEnemy--;
			}
		}

		return features;
	}

	// train our learned function, during the game trainValue is the prev value and
	// boardValue the current
	public void updateWeights(float boardValue, float trainValue, int[] boardFeatures) {
		float error = trainValue - boardValue;
		for (int i = 0; i < NUM_FEATURES; i++) {
			Weights[i] += LEARN_RATE * boardFeatures[i] * error;
		}
	}

	public void onMatchEnds(IBoard board) {
		if (board.getWinner() == null) {
			// draw
			updateWeights(getBoardValue(board), 0, getBoardFeatures(board));
		} else if (board.getWinner() == this) {
			// win
			updateWeights(getBoardValue(board), 100, getBoardFeatures(board));
		} else {
			// loss
			updateWeights(getBoardValue(board), -100, getBoardFeatures(board));
		}
		for (int i = 0; i < Weights.length; i++) {
			System.out.print("W" + i + "=" + Weights[i] + " | ");
		}
		System.out.println();
		/*
		 * PrevBoardFeatures = getBoardFeatures(board); for (int i = 0; i <
		 * PrevBoardFeatures.length; i++) {
		 * System.out.print("F"+i+"="+PrevBoardFeatures[i] + " | "); }
		 * System.out.println();
		 */
		return;
	}

}
