package hkust.comp3111h.ballcraft.server;

import hkust.comp3111h.ballcraft.BallCraft;
import hkust.comp3111h.ballcraft.client.Client;
import hkust.comp3111h.ballcraft.client.ClientGameState;
import hkust.comp3111h.ballcraft.client.GameInput;
import hkust.comp3111h.ballcraft.client.GameUpdater;

import java.util.ArrayList;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class Server extends IntentService {
	//debug
	public static boolean D = false;
	public final static String TAG="Intentservice";
    
    static private ServerGameState gameState = null;

    static private GameUpdater gameUpdater;

    static private GameInput[] gameInputs;

    static private long lastRun;

    static int skill = 0;

    static public boolean inited = false;

    static private boolean running = false;

    static private String msg;
    
    static private boolean clientInited = false;
    
    static private int clientBall = 0;
    
    static private String gameMode;
    
    static private int limitValue = 100;
    
    static private int currScore;
    
    static private long initTime;
    static private long currTime;
    
    public Server() 
    {
        super("Server");
    }

    public static void setState(String string)
    {
    	if (string.equals("")) return;
        String[] str = string.split(";");
        try
        {
	        ServerGameState.getStateInstance().processPlayerInput(Integer.parseInt(str[0]), 
	                GameInput.fromSerializedString(str[1]));
        }
        catch(Exception e)
        {
        	if(D) {
        	    Log.e("Error setting state for server", e.toString() + " : " + string);
        	}
        }
        
    }
    
	public static synchronized void extraMessage(String string)
	{
		Log.w(TAG,"extramessage "+string+msg);
		if(!msg.equals(""))
		{
			msg += "/";
		}
		msg += string;
	}
	
	public static GameUpdater generateGameUpdater() 
	{
		gameUpdater.balls = gameState.getBalls();
		return gameUpdater;
	}

    public void run() {
        while (running) {
            long time = System.currentTimeMillis();
            gameState.onEveryFrame((int) (time - lastRun));
            lastRun = System.currentTimeMillis();

    		ArrayList<Ball> balls = gameState.getBalls();
    		if (gameMode.equals("LIMITED_TIME")) {
	            extraMessage("Time:" + -((currTime - initTime) / 1000 + 60 * limitValue));
    		}
            extraMessage("Score:" + balls.get(0).getScore() + "&" + balls.get(1).getScore());
                        
            String temp = new String(msg);
			msg = "";

	        
	        for (int i = 0; i < BallCraft.maxPlayer; i++)
	        {		        
				ServerAdapter.processServerMsg(temp + ";" 
				        + generateGameUpdater().toSerializedString(), i);	
	        }

            try {
                currTime = System.currentTimeMillis();
                long sleep = 30 + time - currTime;
                if (sleep > 0 ) {
                    Thread.sleep(sleep);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            if (isGameEnded()) {
                ServerAdapter.sendEndGameMessageToClient();
            }
        }

    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
    	msg = "";
        ServerGameState.init();
        
        int time = 0;
        while (!clientInited && BallCraft.maxPlayer == 2)
        {
        	Log.w("Server", "Waiting for client");
            try 
            {
				Thread.sleep(1000);
			} 
            catch (InterruptedException e) 
            {
				Log.w("Server", e.toString());
			}
            time++;
            if (time > 30)
            {
            	Server.stop();
                Client.stop();
                ClientGameState.clear();
                if (BallCraft.isServer) {
                    ServerGameState.clear();
                }
            	return;
            }
        }
        
        gameState = ServerGameState.getStateInstance();
        gameState.loadMap(intent.getStringExtra("map"), intent.getIntExtra("ball", 0), clientBall);
        gameMode = intent.getStringExtra("GAME_MODE");
        if (gameMode == "LIMITED_TIME") {
	        limitValue = intent.getIntExtra("TIME_LIMIT", 1);
        } else {
	        limitValue = intent.getIntExtra("SCORE_LIMIT", 10);
        }
        gameUpdater = new GameUpdater();

        gameInputs = new GameInput[BallCraft.maxPlayer];
        for (int i = 0; i < BallCraft.maxPlayer; i++)
        {
            gameInputs[i] = new GameInput();
        }

        inited = true;
        lastRun = System.currentTimeMillis();
        running = true;
        
        initTime = System.currentTimeMillis();
        currTime = initTime;
        
        run();
    }
    
    public boolean isGameEnded() {
        if (gameMode.equals("LIMITED_TIME")) { // limited time
            return currTime - initTime > limitValue * 60 * 1000;
        } else { // limited score
            return currScore >= limitValue;
        }
    }
    
    public static void serClientBall(int ball)
    {
    	clientBall = ball;
    	clientInited = true;
    }
    
    public static void setScore(int score) {
        currScore = score;
    }

    public static void stop() {
        running = false;
    }

}
