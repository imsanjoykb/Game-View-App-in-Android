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

import java.util.HashMap;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Vibrator;
import android.text.InputType;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This class initializes the View used by the game to draw the board and stones
 * during the game.
 * 
 * @author Yuriy Stanchev
 * 
 * @email i_stanchev@ml1.net
 * 
 * @date 11 Mar 2012
 */
public class GameView extends View {

	/**
	 * Initializes the width of the board variable.
	 */
	private int width = 0;

	/**
	 * Initializes the height of the board variable.
	 */
	private int height = 0;

	/**
	 * Used for cell size calculation.
	 */
	private int cell = 0;

	/**
	 * Achieved points.
	 */
	private int points = 0;

    /**
     * Number of moves.
     */
    private int moves = 0;

	/**
	 * Color of the background.
	 */
	private Paint background;

	/**
	 * Color of the board lines.
	 */
	private Paint line;

	/**
	 * Color of "positive" player.
	 */
	private Paint positive;

	/**
	 * Color of "negative" player.
	 */
	private Paint negative;

	/**
	 * Stones images.
	 */
	Bitmap pulls[] = new Bitmap[6];

	/**
	 * Activity object to be used where activities are needed.
	 */
	private static Activity context;

	/**
	 * Sound for the game.
	 */
	private SoundPool soundPool;

	/**
	 * Stores the values of the sounds resources.
	 */
	private HashMap<Integer, Integer> soundPoolMap;

	/**
	 * Preferences.
	 */
	SharedPreferences preferences = getContext().getSharedPreferences(
			Menu.PREFS_NAME, 0);

	/**
	 * Board object.
	 */
	Board board = null;

	/**
	 * Used to initialize the Easy AI.
	 */
	AI ai = null;

	/**
	 * Sound preferences boolean.
	 */
	private boolean sound = preferences.getBoolean("sound", true);

	/**
	 * Vibration preferences boolean.
	 */
	private boolean toVibrate = preferences.getBoolean("vibrate", true);

	/**
	 * Preference if the player is playing against the computer.
	 */
	private boolean oneplayer = (preferences.getBoolean("easy", true)
			| preferences.getBoolean("normal", true) | preferences.getBoolean(
			"hard", true));

	/**
	 * Vibration for the game.
	 */
	Vibrator vibrator = (Vibrator) getContext().getSystemService(
			Context.VIBRATOR_SERVICE);

	/**
	 * The sound used when a stone is moved.
	 */
	public static final int SOUND_MOVEDSTONE = 1;

	/**
	 * The sound used if somebody won the game.
	 */
	public static final int SOUND_WON = 2;

	/**
	 * The sound used if somebody made a wrong move.
	 */
	public static final int SOUND_WRONGMOVE = 3;

	/**
	 * Plays a sound on incorrect move and vibrates.
	 * 
	 * @param humanDidMove
	 *            Defines if the move was made by human or A.I.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 9 April 2012
	 */
	private void actionsOnIncorrectMove(boolean humanDidMove) {
		if (humanDidMove == false) {
			/*
			 * Play the sound of the game and vibrate.
			 */
			if (sound == true) {
				playSound(SOUND_WRONGMOVE);
			}
			if (toVibrate == true) {
				vibrator.vibrate(300);
			}
		}
	}

	/**
	 * Plays a sound on a correct move and vibrates.
	 * 
	 * @param humanDidMove
	 *            Defines if the move was made by human or A.I.
	 * 
	 * @return humanDidMove
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 11 Mar 2012
	 */
	private boolean actionsOnCorrectMove(boolean humanDidMove) {
		if (humanDidMove == false) {
			return (false);
		} else {moves++;}

		/*
		 * Play the sound of the game and vibrate.
		 */
		if (sound == true) {
			playSound(SOUND_MOVEDSTONE);
		}
		if (toVibrate == true) {
			vibrator.vibrate(300);
		}

		/*
		 * End of playing the sound of the game and vibrate
		 */
		invalidate();

		/*
		 * Did the game end? If yes call the onHighscore method and check if we
		 * have a high score.
		 */
		if (board.end() == true) {
			String whoWon = "";
			if (board.getWho() < 0) {
				whoWon = context.getString(R.string.red_won);
			}
			if (board.getWho() > 0) {
				whoWon = context.getString(R.string.blue_won);
			}
			keepHighscore(whoWon);

			/*
			 * It is not so important to store ANN fitness value, because
			 * heuristic algorithms are not so strict.
			 */
			try {
				HardAI hard = ((HardAI) ai);
				hard.storeAnnFitness(board.getWho() * 1 / (double) points);
				// TODO Check if it is correct.
				// ANN3Layers ann = hard.getAnn();
				// final HardAISQLAdapter HardAISQL = new
				// HardAISQLAdapter(context);
				// HardAISQL.openToWrite();
				// HardAISQL.updateByID(ann.getId(), ann.getFitness());
				// HardAISQL.close();
			} catch (Exception ex) {
			}
		}

		humanDidMove = false;

		return (humanDidMove);
	}

	/**
	 * Initialize the SoundPool for play back and put the audio file.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 14 April 2012
	 */
	private void initSounds() {
		soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 100);
		soundPoolMap = new HashMap<Integer, Integer>();
		soundPoolMap.put(SOUND_MOVEDSTONE,
				soundPool.load(getContext(), R.raw.bloop4, 1));
		soundPoolMap.put(SOUND_WON,
				soundPool.load(getContext(), R.raw.cartoon008, 2));
		soundPoolMap.put(SOUND_WRONGMOVE,
				soundPool.load(getContext(), R.raw.cartoon004, 3));
	}

	/**
	 * A method which collects all needed data so that if a TOP 10 high-score is
	 * achieved it can be written to high-score database.
	 * 
	 * @param winner
	 *            Used to indicate the winner of the game.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 18 April 2012
	 */
	private void keepHighscore(String winner) {
		/*
		 * Input high score.
		 * 16.03.2015 - score according the number of the moves.
		 */
        final EditText scorein = new EditText(context);
        //TODO
		final int achieved = points - moves;
        scorein.setInputType(InputType.TYPE_CLASS_TEXT);
		/*
		 * Initialization of the Alert Dialog and SQLite Adapter for high score.
		 */
		AlertDialog alertDialog = new AlertDialog.Builder(context).create();
		final SQLiteAdapter mySQLiteAdapter = new SQLiteAdapter(context);

		mySQLiteAdapter.openToWrite();
		final long countRow = mySQLiteAdapter.count();
		int minimalscore = 0;

		if (countRow > 1) {
			minimalscore = mySQLiteAdapter.minimal();
		}

		/*
		 * If there is an achieved score write it in the DB. If there is a score
		 * lower than the previous 10 just toast.
		 */
		if ((countRow >= 10)&&(minimalscore > achieved)) {
			if ((oneplayer == true)&&(board.getWinner() == Board.NEGATIVE_PLAYER)) {
				winner = context.getString(R.string.computer_won);
			}
			String toast_end = context.getString(R.string.toast_game_end);
			Toast toast = Toast.makeText(context,
					String.format(toast_end, winner), 10000);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			mySQLiteAdapter.close();
			context.finish();
		} else {
			String highscore_title = context
					.getString(R.string.highscore_title);
			String OK_button = context.getString(R.string.OK_button);
			alertDialog.setTitle(String.format(highscore_title, winner));
			alertDialog.setMessage(context.getString(R.string.won_message));
			alertDialog.setView(scorein);
			alertDialog.setButton(OK_button,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							final String scored = scorein.getText().toString();

							try {
								if (countRow == 0) {
									mySQLiteAdapter.insert(scored, achieved);
								} else {
									int minid = mySQLiteAdapter.minimalId();
									if (countRow < 10) {
										mySQLiteAdapter
												.insert(scored, achieved);
									} else {
										if (mySQLiteAdapter.minimal() < achieved) {
											mySQLiteAdapter.updateByID(minid,
													scored, achieved);
										}
									}
								}
							} finally {
								/*
								 * Finally end the game and return to menu.
								 */
								mySQLiteAdapter.close();
								/*
								 * Score for ScoreNinja.
								 */
//								Menu.onGameOver(achieved);
								context.finish();

							}
						}
					});
		}

		/*
		 * Set the icon for the dialog.
		 */
		alertDialog.setIcon(R.drawable.ic_launcher);
		/*
		 * Play the winning sound.
		 */
		if (sound == true) {
			playSound(SOUND_WON);
		}
		/*
		 * Show the dialog.
		 */
		alertDialog.show();
	}

	/**
	 * If the Size of the board has been changed - adjust. For example if we
	 * flip the screen.
	 * 
	 * @param newWidth
	 *            Current width of this view.
	 * 
	 * @param newHeight
	 *            Current height of this view.
	 * 
	 * @param oldWidth
	 *            Old width of this view.
	 * 
	 * @param oldHeight
	 *            Old height of this view.
	 * 
	 * @see android.view.View#onSizeChanged(int, int, int, int)
	 */
	protected void onSizeChanged(int newWidth, int newHeight, int oldWidth,
			int oldHeight) {
		this.width = (newWidth < newHeight) ? newWidth : newHeight;
		this.height = width;
		cell = this.height / 8;
		super.onSizeChanged(newWidth, newHeight, oldWidth, oldHeight);
	}

	/**
	 * Start drawing on View's Canvas. Where actual visualization happens.
	 * 
	 * @param canvas
	 *            Object on which the actual drawing happens.
	 * 
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 11 Mar 2012
	 */
	protected void onDraw(Canvas canvas) {

		/*
		 * Initialization of parameters showing who's turn is it.
		 */
		String whoIs = null;

		int stones[][] = board.getStones();
		for (int j = 0; j < Board.BOARD_SIZE; j++) {
			for (int i = 0; i < Board.BOARD_SIZE; i++) {
				Bitmap stone = null;
				if (stones[i][j] < 0) {
					switch (Math.abs(stones[i][j])) {
					case 1:
						stone = pulls[3];
						points += 1;
						break;
					case 2:
						stone = pulls[4];
						points += 2;
						break;
					case 3:
						stone = pulls[5];
						points += 3;
						break;
					}
				} else if (stones[i][j] > 0) {
					switch (Math.abs(stones[i][j])) {
					case 1:
						stone = pulls[2];
						points += 1;
						break;
					case 2:
						stone = pulls[1];
						points += 2;
						break;
					case 3:
						stone = pulls[0];
						points += 3;
						break;
					}
				}

				/*
				 * Draw stone image.
				 */
				if (stone != null) {
					canvas.drawBitmap(stone,
							(i * cell) + (cell - stone.getWidth()) / 2,
							(j * cell) + (cell - stone.getHeight()) / 2, null);
				}

				/*
				 * Who's turn is it.
				 */
				if (board.getWho() > 0)
					whoIs = context.getString(R.string.red_turn);
				if (board.getWho() < 0)
					whoIs = context.getString(R.string.blue_turn);
			}
		}

		/*
		 * Initialization of board lines.
		 */
		for (int l = 0; l <= Board.BOARD_SIZE; l++) {
			/*
			 * Draw vertical lines.
			 */
			canvas.drawLine(l * height / Board.BOARD_SIZE, 0, l * height
					/ Board.BOARD_SIZE, height, line);

			/*
			 * Draw horizontal lines.
			 */
			canvas.drawLine(0, l * height / Board.BOARD_SIZE, height, l
					* height / Board.BOARD_SIZE, line);
		}

		LinearLayout layout = new LinearLayout(context);

		/*
		 * We add here the TextView that displays who is going to make a move -
		 * red or blue one.
		 */
		TextView textView = new TextView(context);
		textView.setVisibility(View.VISIBLE);
		String message = context.getString(R.string.player_turn);
        if (moves > 3){
            /**
             * Tips. 2015.03.14
             */
        textView.setText(String.format(message, whoIs));} else {
            switch (moves){
            case 1:
                textView.setText("Tip: Click on 3 cells to place a fish.");
            break;
            case 2:
                textView.setText("Tip: After you place all 3 fishes. Click on them to expand.");
            break;
            case 3:
                textView.setText("Tip: When you expand- you will overflow" +"\n"+" in cells next to you. ");
            break;
            }



        }

		textView.setTextColor(Color.BLACK);

		/*
		 * Add the TextView to the layout.
		 */
		layout.addView(textView);
		layout.measure(canvas.getWidth(), canvas.getHeight());
		layout.layout(0, 0, canvas.getWidth(), canvas.getHeight());

		/*
		 * Check the orientation of the device and place the TextView.
		 */

		if (getResources().getConfiguration().orientation == 2) {
			canvas.translate(width, 0);
			canvas.rotate(0);
		} else {
			canvas.translate(0, height);
		}

		layout.draw(canvas);

	}

	/**
	 * We use the context object of the GameView so we can return a coefficient
	 * to the AI and it can make a correct move.
	 * 
	 * @param keys
	 *            This is the variable that holds the current combination of
	 *            stones that is checked.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 11 Mar 2012
	 */
	public static int obtainNormalAi(long keys[]) {
		final NormalAISQLAdapter sqlitehandle = new NormalAISQLAdapter(context);

		sqlitehandle.openToRead();
		Integer evaluation = sqlitehandle.obtainCoefficient(keys);
		sqlitehandle.close();

		return (evaluation);
	}

	/**
	 * Calculate the current volume in a scale of 0.0 to 1.0.
	 * 
	 * @param sound
	 *            Used to identify which sound resource needs to be loaded.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 10 April 2012
	 */
	public void playSound(int sound) {
		AudioManager mgr = (AudioManager) getContext().getSystemService(
				Context.AUDIO_SERVICE);
		float streamVolumeCurrent = mgr
				.getStreamVolume(AudioManager.STREAM_MUSIC);
		float streamVolumeMax = mgr
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		float volume = streamVolumeCurrent / streamVolumeMax;

		/*
		 * Play the sound with the correct volume.
		 */
		soundPool.play(soundPoolMap.get(sound), volume, volume, 1, 0, 1f);
	}

	/**
	 * User Touch Screen interaction. We play sounds on touch, vibration on
	 * touch, write the high score to the database when the game has ended.
	 * 
	 * @param event
	 *            Used to capture the events for the user on-screen interaction.
	 * 
	 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 27 Mar 2012
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean humanDidMove = false;
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			humanDidMove = board.move(
					(int) (Board.BOARD_SIZE * event.getX() / width),
					(int) (Board.BOARD_SIZE * event.getY() / height), 0);

			if (humanDidMove == true && oneplayer == true) {
				// TODO Give time to the human player to see his/her move, by
				// using timer.
				try {
					Thread.sleep(0);
				} catch (InterruptedException e) {
				}

				try {
					Point coordinates = ai.move(board.getStones(),
							board.getWho(), board.getTurn());
					board.move(coordinates.x, coordinates.y, 0);
				} catch (Exception e) {
				}
			}
		} else {
			return (super.onTouchEvent(event));
		}

		/*
		 * Do some actions on incorrect move.
		 */
		actionsOnIncorrectMove(humanDidMove);

		/*
		 * Do some actions on correct move.
		 */
		humanDidMove = actionsOnCorrectMove(humanDidMove);

		return (true);

	}

	/**
	 * Constructor of the Class. We get here the context and board instance that
	 * we passed from the game menu.
	 * 
	 * @param context
	 *            Passes the Overflow Activity context to the Game View.
	 * 
	 * @param board
	 *            Passes the created from the Overflow Activity to the Game
	 *            View.
	 * 
	 * @author Yuriy Stanchev
	 * 
	 * @email i_stanchev@ml1.net
	 * 
	 * @date 11 Mar 2012
	 */
	public GameView(Context context, Board board) {
		super(context);

		GameView.context = (Activity) context;

		this.board = board;

		if (oneplayer == true) {
			if (preferences.getBoolean("easy", true) == true) {
				ai = new EasyAI();
			}
			if (preferences.getBoolean("normal", false) == true) {
				ai = new NormalAI();
			}
			if (preferences.getBoolean("hard", false) == true) {
				ai = new HardAI();
			}
		}

		background = new Paint();
		background.setColor(getResources().getColor(R.color.background));

		positive = new Paint();
		positive.setColor(getResources().getColor(R.color.positive));

		negative = new Paint();
		negative.setColor(getResources().getColor(R.color.negative));

		line = new Paint();
		line.setColor(getResources().getColor(R.color.line));

		initSounds();
		setFocusable(true);
		setFocusableInTouchMode(true);

		pulls[0] = BitmapFactory.decodeResource(getResources(),
				R.drawable.positive3pull);
		pulls[1] = BitmapFactory.decodeResource(getResources(),
				R.drawable.positive2pull);
		pulls[2] = BitmapFactory.decodeResource(getResources(),
				R.drawable.positive1pull);
		pulls[3] = BitmapFactory.decodeResource(getResources(),
				R.drawable.negative1pull);
		pulls[4] = BitmapFactory.decodeResource(getResources(),
				R.drawable.negative2pull);
		pulls[5] = BitmapFactory.decodeResource(getResources(),
				R.drawable.negative3pull);

		// TODO Start ANN training by using DE.
		/* AI training. */{
			// new Thread();
			// DETrainer trainer = new DETrainer(populationSize,
			// chromosomeSize);
			// trainer.loadPopulation(population, fitness);
			// trainer.evolve();
			// trainer.obtainPopulation();
		}
	}
}
