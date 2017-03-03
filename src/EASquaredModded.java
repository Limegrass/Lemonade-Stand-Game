import java.util.List;
import java.util.LinkedList;
//import java.util.Random;

/**
 * An adapted implementation of the EA^2 algorithm lemonade stand 
 * game playing AI by Enrique et al
 * for a lemonade stand tournament in CSC370: Artificial Intelligence
 * 
 * @author James Ni
 * @author enrique
 * @author Alex Herron
 */
public class EASquaredModded implements Bot
{
	//The maximum size of history to save
	//The runtime slows down if we let the size of the history be unrestricted
	private final int CAPACITY = 20;
	//These were all final variables but not variable anymore for testing
	//with constructor 

	//RHO < 1 treats behaviors less like ideal types more equally
	//RHO > 1 values ideal types more greatly
	private double RHO = .5; //Scales the measure of distance between locations 

	//Larger gamma uses more history
	private double GAMMA = .75; //Response rate or discount factor

	//Tolerance of differences for specifying into a condition
	private double TOL = .1;

	//Initial strategy 
	private int INITIAL_STICK_COUNT = 5;
	//Number of times to stick when you find a good position
	private int GOOD_POSITION_STICK_COUNT = 1;
	//Chance to switch partners 
//	private double SWITCH_RATIO = .05;

	//Variables that change as the game progresses
	private int lastMove = 7;
	private int stickCount = INITIAL_STICK_COUNT; //Initial strategy of sticking to 5

//	private Random r = new Random();
	//Store the past moves of the players to decide on a next move
	private LinkedList<Integer> player1Moves;
	private LinkedList<Integer> player2Moves;
	private LinkedList<Integer> myMoves;
//	private Random r = new Random();
	private int p1Total = 0;
	private int p2Total = 0;

//	public EASquaredModded(double rho, double gamma, double tol, int initStick, int goodStick)
//	{
//		this.RHO = rho;
//		this.GAMMA = gamma;
//		this.TOL = tol;
//		this.INITIAL_STICK_COUNT = initStick;
//		this.GOOD_POSITION_STICK_COUNT = goodStick;
//	}
	
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
		//Update the total score of each opponent
		//Choose which one is gaining too far ahead and do switching if needed
		p1Total += Math.abs(player1LastMove - lastMove);
		p1Total += Math.abs(player1LastMove - player2LastMove);
		p2Total += Math.abs(player2LastMove - lastMove);
		p2Total += Math.abs(player2LastMove - player1LastMove);
		//Initialize move history
		if(player1Moves == null)
		{
			player1Moves = new LinkedList<Integer>();
			player2Moves = new LinkedList<Integer>();
			myMoves = new LinkedList<Integer>();
			myMoves.add(12); //Seems the tournament gives each other bot an inital value of 12 for the first move.
		}
		//Add new moves to the back
		player1Moves.add(player1LastMove);
		player2Moves.add(player2LastMove);
		
		//Manage size of taking out an element from the front
		if(player1Moves.size()>CAPACITY){
			player1Moves.poll();
			player2Moves.poll();
		}
		
		//Calculates the players tendency to stick and follow
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
			stickCount--;
		}
		// If stickCounter is set to less than -1, this is the signal to employ stick and carrot strategy
		else if (stickCount<= -1)
		{
			move = carrotStick(p1Follow, p2Follow); //Try to force people off of being rude to me
		}
		//If another player is sticking
		else if((p1Stick >= p1Follow+TOL) && (p1Stick>=p2Follow+TOL) && (p1Stick>=p2Stick+TOL))
		{
			
			move = opposite(player1LastMove); //Play opposite of a sticker
			//If they're approximately both sticky and one has more total points than the other
			//Move to sticking with the other. Try to edge out the two other bots
//			if(Math.abs(p1Stick-p2Stick) < TOL && p1Total<p2Total && player1LastMove==lastMove && r.nextDouble()<SWITCH_RATIO)
			if(Math.abs(p1Stick-p2Stick) < TOL && p1Total<p2Total && player1LastMove==lastMove)

			{
				move = opposite(player2LastMove);
			}
		}
		else if((p2Stick >= p2Follow+TOL) && (p2Stick>=p1Follow+TOL) && (p2Stick>=p1Stick+TOL))
		{
			move = opposite(player2LastMove); //Play opposite of a sticker
			//If they're approximately both sticky and one has more total points than the other
			//Move to sticking with the other. Try to edge out the two other bots
			if(Math.abs(p1Stick-p2Stick) < TOL && p1Total<p2Total && player2LastMove==lastMove)
			{
				move = opposite(player1LastMove);
			}

		}
		//Both players are stickers
		else if((p1Stick > p1Follow+TOL) && (p2Stick > p2Follow+TOL))
		{
			//If the previous gain was good, do nothing and stick
			//If it was bad, follow the stickier player
			if(lastGain()<8)
			{
				//Follow stickier
				if(p1Stick>p2Stick)
				{
					move = opposite(player1LastMove);
				}
				else
				{

					move = opposite(player2LastMove);
					stickCount = GOOD_POSITION_STICK_COUNT;
				}
			}
		}
		//If Player 1 tends to follow and it tends to follow harder than player 2 tends to stick
		else if( (p1Follow >= p1Stick + TOL) && (p1Follow >= p2Stick + TOL) && (p1Follow >= p2Follow + TOL))
		{
			//Stick onto the other player to force a move
			//If it is not following me
			//If it is, do nothing
			if(p1FollowMe < p1FollowP2)
			{
				//It's following the other player, so stick on the other player
				//to force a move
				move = player2LastMove;	
			}

		}
		//If player 2 tends to follow and it tends to follow more than player 1 tends to stick
		else if( (p2Follow >= p2Stick + TOL) && (p2Follow >= p1Stick + TOL) && (p2Follow >= p1Follow + TOL))
		{
			//Stick onto the other player to force a move
			//If it is not following me
			//If it is, do nothing
			if(p2FollowMe < p2FollowP1)
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
		//If they are playing opposites and I'm losing out
		else if( (player1LastMove%6==player2LastMove%6) && (player1LastMove!=player2LastMove) )
		{
			stickCount = -5;
			move = carrotStick(p1Follow, p2Follow);
		}
		//If no conditions are met, we are sticking to the previous choice.
		myMoves.add(move);
		if(myMoves.size()>CAPACITY)
		{
			myMoves.poll();
		}
//		System.out.println(player1LastMove + " " + move + " "  + player2LastMove);
		lastMove = move;

		return move;
	}

	/**
	 * Calculate the points gained last round
	 * @return the number of points gained last round
	 */
	private int lastGain()
	{
		int gain = 0;
		gain += Math.abs(myMoves.getLast() - player1Moves.getLast());
		gain += Math.abs(myMoves.getLast() - player2Moves.getLast());
		return gain;
	}

	/**
	 * Finds the position opposite of another position
	 * @return the position opposite of another position
	 */
	private int opposite(int position)
	{
		int move = (position+6)%12;
		if(move==0)
		{
			return 12;
		}
		return move;
	}

	/**
	 * Calculates the tendency of a player to stick to his position.
	 * A value of 0 corresponds to always sticking.
	 * @param the player to calculate the sticking tendency for
	 * @return a value of how often the player sticks to his position
	 */
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

	/** 
	 * Calculates the tendency of a player to follow myself 
	 * @param otherPlayer the other player not being calculated
	 * @return a calculated general follow index
	 */
	private double followIndex(List<Integer> player)
	{

		double followIndex = 0;
		double sumGamma = 0;
		double distance;
		double gammaRatio;
		int t = 0;
		for (int i = 0; i < player.size(); i++) 
			sumGamma += Math.pow(GAMMA, i);

		for(int i = player.size()-1; i>0 ; i--) {
			distance = Math.abs((myMoves.get(i) - player.get(i-1)+6)%12); 
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
	/**
	 * Calculate the general tendency of a player to follow another play
	 * @param player1 whether we are calculating the value for player 1 or 2
	 * @return the tendency of a player to follow another player
	 */
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

	/**
	 * Strategy for when we are in the losing position of a collaboration
	 * Attempts to force another player away from sticking to their position.
	 * @param p1Follow player 1's follow index
	 * @param p2Follow player 2's follow index
	 * @return the position to play next to force a movement
	 */
	private int carrotStick(double p1Follow, double p2Follow)
	{
		int carrot = -1;
		boolean p1Follows = true;
		if(p1Follow < p2Follow)
		{
			p1Follows = false;
		}
		LinkedList<Integer> sticker;
		if(p1Follows)
		{
			sticker = player2Moves;
		}
		else
		{
			sticker = player1Moves;
		}

		int bestSide = showingBias(p1Follows);
		if (stickCount < -4){
			// condition the best response to play on bestSide
			stickCount++;
			carrot =  ((opposite(sticker.get(sticker.size()-1)) + bestSide) % 12);
			carrot = Math.abs(carrot)+1;
		
		}
		else if (stickCount == -4){
			// condition the best response to exclude playing opposite sticker
			stickCount++;
			carrot =  opposite(sticker.get(sticker.size()-1));
		}
		else if (stickCount == -3){
			// exploit best response
			stickCount++;
			carrot = ((opposite(sticker.get(sticker.size()-1)) + 2*bestSide)% 12);
			carrot = Math.abs(carrot)+1;
		}
		else if (stickCount == -2){
			// exploit best response  
			stickCount++;
			carrot = opposite(sticker.get(sticker.size()-1));
		}
		else if (stickCount == -1){
			// exploit best response  
			stickCount = 2;
			carrot = ((opposite(sticker.get(sticker.size()-1)) + 4*bestSide)% 12);
			carrot = Math.abs(carrot)+1;
		}
		return carrot;
	}

	/**
	 * Attempt to figure out which direction the follower has a tendency towards
	 * To force a bias
	 * @param p1Follower whether p1 has greater tendency to follow than p2
	 * @return A direction either clockwise or counter-clockwise to figure out which side has a bias
	 */
	private int showingBias(boolean p1Follower)
	{
		int lookBack = CAPACITY/2;
		int side = 1;
		LinkedList<Integer> follower, sticker;
		if(p1Follower)
		{
			follower = player1Moves;
			sticker = player2Moves;
		}
		else
		{
			follower = player2Moves;
			sticker = player1Moves;
		}
		int stickerLastMove = sticker.getLast();
		int upSide = 0;
		int downSide = 0;
		for(int a = lookBack + lookBack/2; a >=0 ; a--)
		{
			int siding = Math.abs(follower.get(follower.size()-1-a) - stickerLastMove)%12;
			if( siding > 6)
			{
				upSide++;
			}
			else if( siding < 6 ) 
			{
				downSide++;
			}
		}
		if(!(upSide<downSide)){
			side = -1;
		}
		return side;

	}

}




