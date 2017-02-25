import java.util.ArrayList;

/**
 * Plays the opposite of a different player each successive round.
 * Works well with someone who only chooses one spot.
 * @author James Ni
 *
 */

public class OppositeBot implements Bot 
{
	boolean odd = false;
	ArrayList<Integer> player1Moves;
	ArrayList<Integer> player2Moves;
	ArrayList<Integer> myMoves;
	public int getNextMove(int player1LastMove, int player2LastMove) 
	{
		if(player1Moves.isEmpty())
		{
			player1Moves.add(13);
			player2Moves.add(13);
		}
		player1Moves.add(player1LastMove);
		player2Moves.add(player2LastMove);
		
		odd = !odd;
		int move;
		if(odd)
		{
			move = (player1LastMove+6)%12 + 1;
		}
		else
		{
			move = (player2LastMove+6)%12 + 1;
		}
		myMoves.add(move);
		return move;
	}
}


