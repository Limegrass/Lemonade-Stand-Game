import java.util.List;
import java.util.LinkedList;

public class EASquaredJNAH implements Bot
{
	private final int CAPACITY = 20;
	private final double RHO = .5;
	private final double GAMMA = .75;
	private final double TOL = .1;
	private final int INITIAL_STICK_COUNT = 5;

	private int lastMove = 5;
	private int stickCount = INITIAL_STICK_COUNT;
	private LinkedList<Integer> player1Moves;
	private LinkedList<Integer> player2Moves;
	private LinkedList<Integer> myMoves;
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
		int move = lastMove;
		if(player1Moves == null)
		{
			System.out.println(player1LastMove);
			System.out.println(player2LastMove);
			player1Moves = new LinkedList<Integer>();
			player2Moves = new LinkedList<Integer>();
			myMoves = new LinkedList<Integer>();
			myMoves.add(12); //Seems the tournament gives each other bot an inital value of 12 for the first move.
		}
		player1Moves.add(player1LastMove);
		player2Moves.add(player2LastMove);
		if(player1Moves.size()>CAPACITY){
			player1Moves.poll();
			player2Moves.poll();
			System.out.println(player1Moves.size());
			System.out.println(myMoves.size());
		}
		double p1Stick = stickIndex(player1Moves);
		double p1Follow = followIndex(true);
		double p1FollowP2 = followIndex(player2Moves);
		double p1FollowMe = followIndex(myMoves);

		double p2Stick = stickIndex(player2Moves);
		double p2Follow = followIndex(true);
		double p2FollowP1 = followIndex(player1Moves);
		double p2FollowMe = followIndex(myMoves);

		int lastIndex = myMoves.size() - 1;
		//Stick and signal to other players to follow to begin with
		if(stickCount>0)
		{
			move = stickLast();
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
			if(lastGain()>7)
			{
				//	move=lastMove;
			}
			else{
				//Follow stickier
				if(p1Stick>p2Stick)
				{
					move = opposite(player1LastMove);
				}
				else
				{
					move = opposite(player2LastMove);
					stickCount = 1;
				}
			}
		}
		else if( (p1Follow >= p1Stick + TOL) && (p1Follow >= p2Stick + TOL) && (p1Follow >= p2Follow + TOL))
		{

			//If it is already following me, stick
			if(p1FollowMe >= p1FollowP2)
			{
				//	move = lastMove;
			}
			else
			{
				//It's following the other player, so stick on the other player
				//to force a move
				move = player2LastMove;	
			}

		}
		else if( (p2Follow >= p2Stick + TOL) && (p2Follow >= p1Stick + TOL) && (p2Follow >= p1Follow + TOL))
		{

			//If it is already following me, stick
			if(p2FollowMe >= p2FollowP1)
			{
				//	move = lastMove;
			}
			else
			{
				//It's following the other player, so stick on the other player
				//to force a move
				move = player1LastMove;	
			}
		}
		else if( (p1Follow > p1Stick + TOL) && (p2Follow > p2Stick + TOL) )
		{
			if( (p1FollowP2 > p1FollowMe + TOL) && (p2FollowP1 > p2FollowMe+ TOL) )
			{

				boolean p1FollowsHarder = true;
				if(p2Follow>p1Follow)
				{
					p1FollowsHarder = false;
				}

				if(p1FollowsHarder)
				{
					move = opposite(player1LastMove);
				}
				else
				{
					move = opposite(player2LastMove);
				}
			}

		}
		else if( (player1LastMove%6==player2LastMove%6) && (player1LastMove!=player2LastMove) )
		{


		}

		myMoves.add(move);
		if(myMoves.size()>CAPACITY){
			myMoves.poll();
			System.out.println(myMoves.size());
		}
		lastMove = move;
		if(myMoves.size()==10)
			System.out.println(myMoves.size() + " " + player1Moves.size() + " " + player2Moves.size());

		return move;
	}

	private int lastGain()
	{
		int gain = 0;
		int lastIndex = myMoves.size() - 1;
		gain += Math.abs(myMoves.get(lastIndex) - player1Moves.get(lastIndex));
		gain += Math.abs(myMoves.get(lastIndex) - player2Moves.get(lastIndex));


		return gain;
	}
	private int stickLast()
	{
		stickCount--;
		return lastMove;
	}

	private int opposite(int position)
	{
		return ((position+6)%12) + 1;
	}

	private double stickIndex(List<Integer> player)
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

	private double followIndex(List<Integer> otherPlayer)
	{

		double followIndex = 0;
		double sumGamma = 0;
		double distance;
		double gammaRatio;
		int t = 0;
		for (int i = 0; i < otherPlayer.size(); i++) 
			sumGamma += Math.pow(GAMMA, i);

		for(int i = otherPlayer.size()-1; i>0 ; i--) {
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

		List<Integer> currentPlayer;
		List<Integer> otherPlayer;
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

	private int carrotStick(double p1Stick, double p2Stick)
	{
		int carrot = -1;
		boolean p1Follows = true;
		if(p1Sticks > p2Stick)
		{
			p1Follows = false;
		}
	}

}



