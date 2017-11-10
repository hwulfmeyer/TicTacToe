import de.ovgu.dke.teaching.ml.tictactoe.api.IBoard;

/**
 * @author Hans-Martin Wulfmeyer, Dimtrii Zyrianov
 */
public class JoshuaFix extends JoshuaWOPR {

	// initialize the variables we need
	public JoshuaFix() {
		//Weights = new float[]{ 47.063522f, 107.30812f, 92.928856f, 91.10037f, 48.320328f, 27.0f, -83.94215f, -86.08795f, -58.909874f, -66.64504f, -71.17698f };
		Weights = new float[]{ 0, 1,5,10,30,60,-1,-5,-10,-60,-100};
	}
	
	@Override
	public String getName() {
		return "JoshuaFix";
	}
	
	@Override
	public void updateWeights(float boardValue, float trainValue, int[] boardFeatures) {
		//do nothing
	}
	
	@Override
	public void onMatchEnds(IBoard board) {
		return;
	}
	
}
