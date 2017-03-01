import java.util.ArrayList;

public class EASquaredJNAH implements Bot
{
	private final double RHO = .5;
	private final double GAMMA = .75;
	private final double TOL = .1;
	private final int INITIAL_STICK_COUNT = 5;
	private final int STICK_POSITION = 5;

	private int stickCount = INITIAL_STICK_COUNT;
	private ArrayList<Integer> player1Moves;
	private ArrayList<Integer> player2Moves;
	private ArrayList<Integer> myMoves;
	/** 
	 * @param player1LastMove the action that was selected by Player 1 on the
	 *                        last round.
	 * @param player2LastMove the action that was selected by Player 2 on the
	 *                        last round.
	 * 
	 * @return the next action to play.
	 */
	public int getNextMove(int player1LastMove, int player2LastMove) 
	{
		int move = 0;
		if(player1Moves == null)
		{
			player1Moves = new ArrayList<Integer>();
			player2Moves = new ArrayList<Integer>();
			myMoves = new ArrayList<Integer>();
		}
		player1Moves.add(player1LastMove);
		player2Moves.add(player2LastMove);
		double p1Stick = stickIndex(player1Moves);
		double p1Follow = followIndex(true);
		double p1FollowP2 = followIndex(player2Moves);
		double p1FollowMe = followIndex(myMoves);
		
		double p2Stick = stickIndex(player2Moves);
		double p2Follow = followIndex(true);
		double p2FollowP1 = followIndex(player1Moves);
		double p2FollowMe = followIndex(myMoves);
		//Stick and signal to other players to follow to begin with
		if(stickCount>0)
		{
			move = initialStick();
		}
		

		
		  // If stickCounter is set to less than -1, this is the signal to employ stick and carrot strategy
//		  else if (stickCount<= -1)
//		  {
//			  move = carrotAndStick();
//		  }
		
		//If another player is sticking
		else if((p1Stick >= p1Follow+TOL) && (p1Stick>=p2Follow+TOL) && (p1Stick>=p2Stick+TOL))
		{
			move = opposite(player1LastMove);
		}
		else if((p2Stick >= p2Follow+TOL) && (p2Stick>=p1Follow+TOL) && (p2Stick>=p1Stick+TOL))
		{
			move = opposite(player2LastMove);
		}
		else if((p1Stick > p1Follow+TOL) && (p2Stick > p2Follow+TOL))
		{
			
		}
		myMoves.add(move);
		return move;
	}
	
	private int lastGain()
	{
		int gain = 0;
		
		
		return gain;
	}
	private int initialStick()
	{
		stickCount--;
		myMoves.add(STICK_POSITION);
		return STICK_POSITION;
	}
	
	private int opposite(int position)
	{
		return ((position+6)%12) + 1;
	}
	
	private double stickIndex(ArrayList<Integer> player)
	{
		double stickIndex = 0;
		double sumGamma = 0;
		double distance;
		double gammaRatio;
		int t = 0;
		for (int i = 0; i < player.size(); i++) 
			sumGamma += Math.pow(GAMMA, i);

		for(int i = player.size()-1; i >0 ; i--) {
			distance = Math.min(Math.abs(player.get(i) - player.get(i-1)),
					Math.abs(player.get(i-1)-player.get(i))); 

			if(distance > 6)
			{
				distance = 12 - distance;
			}
			if(t==0) 
			{
				distance = Math.pow(distance,RHO);
			}
			gammaRatio = Math.pow(GAMMA, t) / sumGamma;
			stickIndex += gammaRatio * distance;
			t++;
		}
		stickIndex = - stickIndex;
		return stickIndex;
	}

	private double followIndex(ArrayList<Integer> otherPlayer)
	{

		double followIndex = 0;
		double sumGamma = 0;
		double distance;
		double gammaRatio;
		int t = 0;
		for (int i = 0; i < otherPlayer.size(); i++) 
			sumGamma += Math.pow(GAMMA, i);

		for(int i = otherPlayer.size()-1; i >0 ; i--) {
			distance = Math.abs((myMoves.get(i) - otherPlayer.get(i-1)+6)%12); 
			if(distance > 6)
			{
				distance = 12 - distance;
			}
			if(t==0) 
			{
				distance = Math.pow(distance,RHO);
			}
			gammaRatio = Math.pow(GAMMA, t) / sumGamma;
			followIndex += gammaRatio * distance;
			t++;
		}
		followIndex = - followIndex;
		return followIndex;
	}

	private double followIndex(boolean player1)
	{

		ArrayList<Integer> currentPlayer;
		ArrayList<Integer> otherPlayer;
		if(player1)
		{
			currentPlayer = player1Moves;
			otherPlayer = player2Moves;
		}
		else
		{
			currentPlayer = player2Moves;
			otherPlayer = player1Moves;
		}
		double followIndex = 0;
		double sumGamma = 0;
		double distance;
		double gammaRatio;
		double dif1;
		double dif2;
		int t = 0;
		for (int i = 0; i < currentPlayer.size(); i++) 
			sumGamma += Math.pow(GAMMA, i);

		for(int i = currentPlayer.size()-1; i >0 ; i--) {
			dif1 = Math.abs((currentPlayer.get(i) -  myMoves.get(i-1) + 6) % 12);
			if(dif1 > 6)
			{
				dif1 = 12 - dif1;
			}
			dif2 = Math.abs((currentPlayer.get(i) - otherPlayer.get(i-1) + 6) % 12);
			if(dif2 > 6)
			{
				dif2 = 12 - dif2;
			}
			distance = Math.min(dif1, dif2);
			if(t==0) 
			{
				distance = Math.pow(distance, RHO);            
			}

			distance = Math.min(dif1, dif2); 
			if(t==0) 
			{
				distance = Math.pow(distance,RHO);
			}
			gammaRatio = Math.pow(GAMMA, t) / sumGamma;
			followIndex += gammaRatio * distance;
			t++;
		}
		followIndex = - followIndex;
		return followIndex;
	}

}


