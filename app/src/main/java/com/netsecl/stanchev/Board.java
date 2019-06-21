/*==============================================================================
 =                                                                             =
 = Overflow is very simple but very addictive board game. The game is for two  =
 = players who try to conquer all stones of the opposite player. The game was  =
 = developed as master thesis in New Bulgarian University, Sofia, Bulgaria.    =
 =                                                                             =
 = Copyright (C) 2012 by Yuriy Stanchev  ( i_stanchev@ml1.net )                =
 =                                                                             =
 = This program is free software: you can redistribute it and/or modify        =
 = it under the terms of the GNU General Public License as published by        =
 = the Free Software Foundation, either version 3 of the License, or           =
 = (at your option) any later version.                                         =
 =                                                                             =
 = This program is distributed in the hope that it will be useful,             =
 = but WITHOUT ANY WARRANTY; without even the implied warranty of              =
 = MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the               =
 = GNU General Public License for more details.                                =
 =                                                                             =
 = You should have received a copy of the GNU General Public License           =
 = along with this program. If not, see <http://www.gnu.org/licenses/>.        =
 =                                                                             =
 =============================================================================*/

package com.netsecl.stanchev;

import java.io.Serializable;

/**
 * This class is used for the game logic.
 * 
 * @author Yuriy Stanchev
 * 
 * @email i_stanchev@ml1.net
 * 
 * @date 11 Mar 2012
 */
public class Board implements Serializable {

	/**
	 * What kind of stones are on the board.
	 */
	private int stones[][] = new int[BOARD_SIZE][BOARD_SIZE];

	/**
	 * First six turns players are putting stones with size of two.
	 */
	private int turn = 0;

	/**
	 * Defines the board size. Default 8x8.
	 */
	public static final int BOARD_SIZE = 8;

	/**
	 * Defines the minimum board index. The board "boarder".
	 */
	public static final int BOARD_MIN_INDEX = 0;

	/**
	 * Defines the maximum board index. The board "boarder".
	 */
	public static final int BOARD_MAX_INDEX = 8;

	/**
	 * This is the positive player constant.
	 */
	public static final int POSITIVE_PLAYER = +1;

	/**
	 * This is the negative player constant.
	 */
	public static final int NEGATIVE_PLAYER = -1;

	/**
	 * Describes a positive piece of size 3.
	 */
	public static final int POSITIVE_PIECE_SIZE_3 = +3;

	/**
	 * Describes a positive piece of size 2.
	 */
	public static final int POSITIVE_PIECE_SIZE_2 = +2;

	/**
	 * Describes a positive piece of size 1.
	 */
	public static final int POSITIVE_PIECE_SIZE_1 = +1;

	/**
	 * This is the empty cell constant.
	 */
	public static final int EMPTY_CELL = 0;

	/**
	 * Describes a negative piece of size 3.
	 */
	public static final int NEGATIVE_PIECE_SIZE_3 = -3;

	/**
	 * Describes a negative piece of size 2.
	 */
	public static final int NEGATIVE_PIECE_SIZE_2 = -2;

	/**
	 * Describes a negative piece of size 1.
	 */
	public static final int NEGATIVE_PIECE_SIZE_1 = -1;

	/**
	 * Used as a version control in the class.
	 */
	public static final long serialVersionUID = 1L;

	/**
	 * Defines the number of initial turns, first 6 (default) turns are with
	 * size 2. 3 from negative and 3 from positive player.
	 */
	public static final int NUMBER_OF_DEPLOYMENT_MOVES = 6;

	/**
	 * Use +1 for the positive player and -1 for the negative player.
	 */
	public int who = 0;

	/**
	 * Refills the cells in the case of "overflowing". Handles the logic used
	 * for setting the stones.
	 * 
	 * @param x
	 *            index of a cell in the 2 dimensional board array.
	 * 
	 * @param y
	 *            index of a cell in the 2 dimensional board array.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 11 Mar 2012
	 */
	private void refill(int x, int y) {
		/*
		 * Case of overflowing outside the margins of the board.
		 */

		if (x < BOARD_MIN_INDEX) {
			return;
		}

		if (y < BOARD_MIN_INDEX) {
			return;
		}

		if (x >= BOARD_MAX_INDEX) {
			return;
		}

		if (y >= BOARD_MAX_INDEX) {
			return;
		}

		/*
		 * If the value of a cell is positive and it is negatives players turn -
		 * we change the value of the cell.
		 */
		if (stones[x][y] > 0 && who < 0) {
			stones[x][y] = -stones[x][y];
		}
		/*
		 * Analogical, but for the other player.
		 */
		if (stones[x][y] < 0 && who > 0) {
			stones[x][y] = -stones[x][y];
		}

		stones[x][y] += who;

		// TODO May be it can be more than 4.
		int amount = Math.abs(stones[x][y]);
		if (amount < 4) {
			return;
		} else if (amount == 4) {
			stones[x][y] = EMPTY_CELL;

			refill(x - 1, y);
			refill(x, y - 1);
			refill(x + 1, y);
			refill(x, y + 1);
		} else {
			System.err.println("More than four stones in the cell!!!");
		}
	}

	/**
	 * Used as class constructor. Board initialization - sets all stones to 0
	 * and defines who will begin first.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 11 Mar 2012
	 */
	public Board() {
		super();
		who = POSITIVE_PLAYER;
		turn = 0;
		for (int i = 0; i < stones.length; i++) {
			for (int j = 0; j < stones[i].length; j++) {
				stones[i][j] = EMPTY_CELL;
			}
		}
	}

	/**
	 * This method is used by the game to check who has won at the end of the
	 * game.
	 * 
	 * @return Returns a positive or negative number from which we decide who
	 *         has won.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 08 Apr 2012
	 */
	public int getWinner() {
		for (int i = 0; i < stones.length; i++) {
			for (int j = 0; j < stones[i].length; j++) {
				if (stones[i][j] != EMPTY_CELL) {
					return (stones[i][j] / Math.abs(stones[i][j]));
				}
			}
		}

		return (0);
	}

	/**
	 * This will tell us how many turns between the players were made.
	 * 
	 * @return turn Returns the number of turns taken.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 11 Mar 2012
	 */
	public int getTurn() {
		return (turn);
	}

	/**
	 * Handles the movement of the stones.
	 * 
	 * @param x
	 *            Index of a cell in the 2 dimensional board array.
	 * 
	 * @param y
	 *            Index of a cell in the 2 dimensional board array.
	 * 
	 * @param who
	 *            Defines who will be next on the move.
	 * 
	 * @return Returns if the stone has been moved.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 11 Mar 2012
	 */
	public boolean move(int x, int y, int who) {

		if (x < BOARD_MIN_INDEX) {
			return (false);
		}

		if (y < BOARD_MIN_INDEX) {
			return (false);
		}

		if (x >= BOARD_MAX_INDEX) {
			return (false);
		}

		if (y >= BOARD_MAX_INDEX) {
			return (false);
		}

		if (who == 0) {
			who = this.who;
		}

		if (this.who != who) {
			return (false);
		}

		/*
		 * Initialization game move.
		 */
		if (turn < NUMBER_OF_DEPLOYMENT_MOVES && stones[x][y] == EMPTY_CELL) {
			stones[x][y] = 2 * who;
			this.who = -this.who;
			turn++;

			return (true);
		}

		/*
		 * Regular game move.
		 */
		if (turn >= NUMBER_OF_DEPLOYMENT_MOVES && stones[x][y] != EMPTY_CELL
				&& stones[x][y] / Math.abs(stones[x][y]) == who) {
			refill(x, y);
			this.who = -this.who;
			turn++;

			return (true);
		}

		return (false);
	}

	/**
	 * Get the values of all stones in a cloned array.
	 * 
	 * @return Returns a clone of the array holding all the stones.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 11 Mar 2012
	 */
	public int[][] getStones() {
		int stones[][] = new int[this.stones.length][];

		for (int i = 0; i < this.stones.length; i++) {
			stones[i] = new int[this.stones[i].length];

			for (int j = 0; j < this.stones[i].length; j++) {
				stones[i][j] = this.stones[i][j];
			}
		}
		return (stones);
	}

	/**
	 * Who's turn it is.
	 * 
	 * @return Returns who is next to move a stone.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 11 Mar 2012
	 */
	public int getWho() {
		return (this.who);
	}

	/**
	 * Decide if the game has reached it's end.
	 * 
	 * @return Returns if all cells are occupied by the negative or the positive
	 *         player.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 11 Mar 2012
	 */
	public boolean end() {

		if (turn < NUMBER_OF_DEPLOYMENT_MOVES) {
			return (false);
		}

		boolean hasNegative = false;
		boolean hasPositive = false;

		for (int j = 0; j < BOARD_SIZE; j++) {
			for (int i = 0; i < BOARD_SIZE; i++) {
				if (stones[i][j] < 0) {
					hasNegative = true;
				}

				if (stones[i][j] > 0) {
					hasPositive = true;
				}
			}
		}

		return (!(hasNegative && hasPositive));
	}
}
