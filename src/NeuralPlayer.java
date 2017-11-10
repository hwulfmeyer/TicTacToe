import java.util.ArrayList;
import java.util.List;
import de.ovgu.dke.teaching.ml.tictactoe.api.IBoard;
import de.ovgu.dke.teaching.ml.tictactoe.api.IPlayer;
import de.ovgu.dke.teaching.ml.tictactoe.api.IllegalMoveException;
import de.ovgu.dke.teaching.ml.tictactoe.game.Move;

/**
 * 125(5x5x5) Input neurons; 50 hidden Neurons; 1 Output Neuron L1 -> L2 => 6250
 * weights
 * 
 *
 * @author Hans-Martin Wulfmeyer
 */
public class NeuralPlayer implements IPlayer {

	private static final int INPUT_NEURONS = 125;
	private static final int HIDDEN_NEURONS = 50;
	private static final int HIDDEN_LAYERS = 1;
	private static final float LEARN_RATE = 0.001f;

	private float NeuronWeights[][];
	private float PrevBoardNetOutputs[][];

	public NeuralPlayer() {
		NeuronWeights = new float[1 + HIDDEN_LAYERS][];
		PrevBoardNetOutputs = new float[1 + HIDDEN_LAYERS][];

		/* create jagged array for weights of neurons (also for bias) */
		NeuronWeights[0] = new float[INPUT_NEURONS * HIDDEN_NEURONS + HIDDEN_NEURONS];
		for (int i = 1; i < HIDDEN_LAYERS; i++) {
			NeuronWeights[i] = new float[HIDDEN_NEURONS * HIDDEN_NEURONS + HIDDEN_NEURONS];
		}
		NeuronWeights[HIDDEN_LAYERS] = new float[HIDDEN_NEURONS + 1];

		/* create jagged array for outputs of neurons (needed for backpropagation) */
		for (int i = 0; i < HIDDEN_LAYERS; i++) {
			PrevBoardNetOutputs[i] = new float[HIDDEN_NEURONS+1];
		}
		// only 1 output neuron
		PrevBoardNetOutputs[HIDDEN_LAYERS] = new float[1];
	}

	public String getName() {
		return "NeuralPlayer";
	}

	public int[] makeMove(IBoard board) {
		float curBoardValue = netFeedForward(board);
		// start learning with the current score and prev. score
		if (board.getMoveHistory().size() > 2)
			netBackProp(curBoardValue);
		PrevBoardNetOutputs[HIDDEN_LAYERS][0] = curBoardValue;

		// generate all possible moves from board and choose the one with the best value
		// if there is more than one move with the same value choose randomly
		float bestBoardValue = 0;
		List<int[]> bestMovesList = new ArrayList<int[]>();
		for (int x = 0; x < board.getSize(); x++) {
			for (int y = 0; y < board.getSize(); y++) {
				for (int z = 0; z < board.getSize(); z++) {
					IBoard boardCopy = board.clone();
					// try move out, if move is illegal do nothing and go on
					try {
						boardCopy.makeMove(new Move(this, new int[] { x, y, z }));
						// get value from our learned function how good the 'move'(the board state after
						// the move) is
						float boardCopyValue = netFeedForward(boardCopy);
						if (boardCopyValue > bestBoardValue || bestMovesList.isEmpty()) {
							bestBoardValue = boardCopyValue;
							bestMovesList.clear();
							bestMovesList.add(new int[] { x, y, z });
						} else if (boardCopyValue == bestBoardValue) {
							bestMovesList.add(new int[] { x, y, z });
						}

					} catch (IllegalMoveException e) {
						// illegal move catching
					}
				}
			}
		}
		if (bestMovesList.size() == 1) {
			// could call this random too, I don't do it for performance
			return bestMovesList.get(0);
		} else {
			// random for the case that the bestMovesListe is larger than 1 element
			return bestMovesList.get((int) (Math.random() * bestMovesList.size()));
		}
	}

	// function for neural net calculation of board
	private float netFeedForward(IBoard board) {
		float inputs[] = new float[INPUT_NEURONS + 1];
		inputs[INPUT_NEURONS] = 1; // bias
		//fill input array with values
		for (int x = 0; x < board.getSize(); x++) {
			for (int y = 0; y < board.getSize(); y++) {
				for (int z = 0; z < board.getSize(); z++) {
					if (board.getFieldValue(new int[] { x, y, z }) == this) {
						inputs[x + 5*y + 25*z] = 1;
					} else if (board.getFieldValue(new int[] { x, y, z }) != null) {
						inputs[x + 5*y + 25*z] = -1;
					}
				}
			}
		}
		//calculate outputs of first hidden layer
		for(int i=0;i<HIDDEN_NEURONS;i++) {
			PrevBoardNetOutputs[0][i] = activationFunction(networkFunction(inputs, NeuronWeights[0], i));
		}
		//calculate outputs of all hidden layers with inputs from hidden layers
		for (int k = 1; k < HIDDEN_LAYERS; k++) {
			for(int i=0;i<HIDDEN_NEURONS;i++) {
				PrevBoardNetOutputs[k][i] = activationFunction(networkFunction(PrevBoardNetOutputs[k-1], NeuronWeights[k], i));
			}
		}
		//calculate output from last hidden layer to output neuron
		float netValue = networkFunction(PrevBoardNetOutputs[HIDDEN_LAYERS-1], NeuronWeights[HIDDEN_LAYERS], 0);
		PrevBoardNetOutputs[HIDDEN_LAYERS][0] = activationFunction(netValue);
		
		return PrevBoardNetOutputs[HIDDEN_LAYERS][0];
	}
	
	private float activationFunction(float net) {
		// act = tanh
		return (float) Math.tanh(net);
	}
	
	private float actFuncDerivative(float out) {
		// tanh' = 1 - tanh^2
		return 1-out*out;
	}

	
	private float networkFunction(float[] inputs, float[] weights, int neuronId) {
		float output = 0f;
		for(int i=0;i<inputs.length;i++) {
			output += inputs[i] * weights[neuronId * inputs.length + i];
		}
		return output;
	}

	// function for neural net back propagation to update weights
	private void netBackProp(float curBoardValue) {
		float newNeuronWeights[][] = NeuronWeights;
		// calculate error/weights for output layer
		float error = (PrevBoardNetOutputs[HIDDEN_LAYERS][0] - curBoardValue) * actFuncDerivative(curBoardValue);
		newNeuronWeights[HIDDEN_LAYERS] = updateWeights(PrevBoardNetOutputs[HIDDEN_LAYERS-1], NeuronWeights[HIDDEN_LAYERS], error);
		//calculate error/weights for all hidden layers with outputs into hidden layers
		for (int k = HIDDEN_LAYERS-1; k >= 0; k--) {
			for(int i=0;i<HIDDEN_NEURONS;i++) {
				//TODO
			}
		}
	}
	
	private float[] updateWeights(float[] outputs, float[] weights,  float error) {
		for(int i=0;i<weights.length;i++) {
			weights[i] -= LEARN_RATE * error * outputs[i];
		}
		return weights;
	}
	

	public void onMatchEnds(IBoard board) {
		if (board.getWinner() == null) {
			// draw
		} else if (board.getWinner() == this) {
			// win
		} else {
			// loss
		}

		return;
	}

}
