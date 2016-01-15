package controllers;

import views.html.*;
import de.htwg.gobang.controller.IGbLogic;
import de.htwg.gobang.controller.impl.GbLogic;
import de.htwg.gobang.entities.IGameField;
import de.htwg.gobang.entities.IGameToken;
import de.htwg.gobang.game.GoBangGame;
import de.htwg.gobang.ui.TUI;
import models.Players;
import de.htwg.gobang.observer.IObserver;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import play.libs.F.Callback0;
import play.libs.F.Callback;

import securesocial.core.BasicProfile;
import securesocial.core.RuntimeEnvironment;
import securesocial.core.java.SecureSocial;
import securesocial.core.java.SecuredAction;
import securesocial.core.java.UserAwareAction;
import services.DemoUser;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsonorg.JsonOrgModule;

import org.json.JSONArray;
import org.json.JSONObject;

public class Application extends Controller {

	public static Map<String, WebSocketController> gameControllerMap = new HashMap<>();
    public static Map<String, Players> roomPlayerMap = new HashMap<>();
    
//    public static Map<String, Integer> availableLobbies = new HashMap<>();
    public static Semaphore createGameSem = new Semaphore(1);
    public static Semaphore socketSem = new Semaphore(1);
//    public static Semaphore updateSem = new Semaphore(1);
    
	GoBangGame games;
	IGbLogic controller;
	TUI printer;
	char lastAction = 'n';

	@SecuredAction
	public Result game() {
		DemoUser user = (DemoUser) ctx().args.get(SecureSocial.USER_KEY);
		Result session = ok(game.render("something", "went", "wrong"));
		try {
			session = createGame("Deathmatch");
		} catch(InterruptedException ex) { }
		return session;
	}

	public Result gobang() {
		return ok(gobang.render("GoBang", gobangMain.render(), "1"));
	}
	
	public Result gobang_game(){
	    return ok(gobang.render("GoBang", gobangGame.render(), "2"));
	}

	public Result gobang_help(){
	    return ok(gobang.render("GoBang", gobangHelp.render(), "3"));
	}

	public Result gobang_about(){
	    return ok(gobang.render("GoBang", gobangAbout.render(), "4"));
	}

//    @SecuredAction
//    public Result goToChatRoom(String roomName){
//        DemoUser user = (DemoUser) ctx().args.get(SecureSocial.USER_KEY);
//        if (availableLobbies.containsKey(roomName)) {
//            availableLobbies.put(roomName, 2);
//        } else {
//            availableLobbies.put(roomName, 1);
//        }
//        return chat.chatRoom(user.main.fullName().get(), roomName);
//    }

    @SecuredAction
    public Result quitGame(String roomName) {
        System.out.println("Player left the game");
        gameControllerMap.remove(roomName);
        roomPlayerMap.remove(roomName);
        return ok();
    }

    @SecuredAction
    public String getRoomNameOfPlayer(DemoUser player) {
        String roomName = "";
        System.out.println(roomPlayerMap.toString());
        for(String room : roomPlayerMap.keySet()) {
            if(roomPlayerMap.get(room).getPlayer1().equals(player) || roomPlayerMap.get(room).getPlayer2().equals(player)) {
                roomName = room;
                break;
            }
        }
        return roomName;
    }

    @SecuredAction
    public Result getRoomName() {
    	DemoUser player = (DemoUser) ctx().args.get(SecureSocial.USER_KEY);
        String roomName = "";
        System.out.println(roomPlayerMap.toString());
        for(String room : roomPlayerMap.keySet()) {
            if(roomPlayerMap.get(room).getPlayer1().equals(player) || roomPlayerMap.get(room).getPlayer2().equals(player)) {
                roomName = room;
                break;
            }
        }
        return ok(roomName);
    }

    @SecuredAction
    public synchronized Result createGame(String roomName) throws InterruptedException {
        try {
            System.out.println("Creating a new Game");
            createGameSem.acquire();
            System.out.println("Got createGame Mutex");

            if(gameControllerMap.containsKey(roomName)) {
                //System.out.println("Adding Player 2 to Game");
                Players players = roomPlayerMap.get(roomName);
                DemoUser newPlayer = (DemoUser) ctx().args.get(SecureSocial.USER_KEY);
                if(players.getPlayer1().equals(newPlayer) ) {
                    System.out.println("redirecting Player1");
                    return ok(game.render(newPlayer.main.userId(), roomName, "Player1"));
                }
                try {
                    if(players.getPlayer2().equals(newPlayer)) {
                        return ok(game.render(newPlayer.main.userId(), roomName, "Player2"));
                    }
                } catch (NullPointerException npe) {}

                players.addPlayer2(newPlayer);
                System.out.println("Player 2 is: " + newPlayer.main.fullName());
                gameControllerMap.get(roomName).setPlayer2(newPlayer);
                return ok(game.render(newPlayer.main.userId(), roomName, "Player2"));
            } else {
                System.out.println("Creating a new Game Controller");
                DemoUser player1 = (DemoUser) ctx().args.get(SecureSocial.USER_KEY);
                System.out.println("Player 1 is: " + player1.main.fullName());
                IGbLogic controller = new GbLogic();
                WebSocketController wsController = new WebSocketController(controller, player1);
                System.out.println("Mapping Room and Players");
                gameControllerMap.put(roomName, wsController);

                Players players = new Players(player1);
                roomPlayerMap.put(roomName, players);
                System.out.println(roomPlayerMap.toString());
                System.out.println(gameControllerMap.toString());
                System.out.println("init game ready");

                return ok(game.render(player1.main.userId(), roomName, "Player1"));
            }
        } finally {
            System.out.println("release create Game Mutex");
            createGameSem.release();
        }
    }
    
    @SecuredAction
    public synchronized void createNgGame(String roomName) throws InterruptedException {
    	try {
    		System.out.println("Creating a new Game");
    		createGameSem.acquire();
    		System.out.println("Got createGame Mutex");
    		
    		if(gameControllerMap.containsKey(roomName)) {
    			//System.out.println("Adding Player 2 to Game");
    			Players players = roomPlayerMap.get(roomName);
    			DemoUser newPlayer = (DemoUser) ctx().args.get(SecureSocial.USER_KEY);
    			if(players.getPlayer1().equals(newPlayer) ) {
    				System.out.println("redirecting Player1");
    				return;
    			}
    			try {
    				if(players.getPlayer2().equals(newPlayer)) {
    					return;
    				}
    			} catch (NullPointerException npe) {}
    			
    			players.addPlayer2(newPlayer);
    			System.out.println("Player 2 is: " + newPlayer.main.fullName());
    			gameControllerMap.get(roomName).setPlayer2(newPlayer);
    		} else {
    			System.out.println("Creating a new Game Controller");
    			DemoUser player1 = (DemoUser) ctx().args.get(SecureSocial.USER_KEY);
    			System.out.println("Player 1 is: " + player1.main.fullName());
    			IGbLogic controller = new GbLogic();
    			WebSocketController wsController = new WebSocketController(controller, player1);
    			System.out.println("Mapping Room and Players");
    			gameControllerMap.put(roomName, wsController);
    			
    			Players players = new Players(player1);
    			roomPlayerMap.put(roomName, players);
    			System.out.println(roomPlayerMap.toString());
    			System.out.println(gameControllerMap.toString());
    			System.out.println("init game ready");
    		}
    	} finally {
    		System.out.println("release create Game Mutex");
    		createGameSem.release();
    	}
    }

    @SecuredAction
    public Result getUserId() {
        DemoUser player = (DemoUser) ctx().args.get(SecureSocial.USER_KEY);
        return ok(player.main.userId());
    }

    @SecuredAction
    public Result getPlayerName(String room) {
    	DemoUser player = (DemoUser) ctx().args.get(SecureSocial.USER_KEY);
    	if(roomPlayerMap.get(room).getPlayer1().equals(player)) {
    		return ok("Player1");
    	}
    	return ok("Player2");
    }

    @SecuredAction
    public synchronized WebSocket<JsonNode> getWebSocket(String userID) throws InterruptedException {
        try {
            System.out.println("Get Socket Called");
            socketSem.acquire();
            System.out.println("Got Socket Mutex");
            WebSocketController wsController = null;
            DemoUser player = null;
            for (WebSocketController wsc : gameControllerMap.values()) {
                System.out.println("is Player1:" + wsc.getPlayer1().main.userId().equals(userID));
                if (wsc.getPlayer1().main.userId().equals(userID)) {
                	wsController = wsc;
                    player = wsController.getPlayer1();
                    break;
                }
                try {
                    System.out.println("is Player2: " + wsc.getPlayer2().main.userId().equals(userID));
                    if (wsc.getPlayer2().main.userId().equals(userID)) {
                    	wsController = wsc;
                        player = wsController.getPlayer2();
                        break;
                    }
                } catch (NullPointerException npe) {
                    //player 2 is not in the game yet
                }
            }
            return wsController.getSocket(player);
        } finally {
            System.out.println("Release Socket Mutex");
            socketSem.release();
        }
    }

	//methods used by Angular.js
	
	@SecuredAction
	public Result setToken(int x, int y) {
		DemoUser player = (DemoUser) ctx().args.get(SecureSocial.USER_KEY);
        gameControllerMap.get(getRoomNameOfPlayer(player)).set(x-1, y-1);
		return ok(gameControllerMap.get(getRoomNameOfPlayer(player)).jsonField());
	}
	
	@SecuredAction
	public Result newGame() {
		DemoUser player = (DemoUser) ctx().args.get(SecureSocial.USER_KEY);
        gameControllerMap.get(getRoomNameOfPlayer(player)).startNewGame();
		return ok(gameControllerMap.get(getRoomNameOfPlayer(player)).jsonField());
	}
	
	@SecuredAction
	public Result createSession(String roomName) {
		DemoUser player = (DemoUser) ctx().args.get(SecureSocial.USER_KEY);
		try {
			createNgGame(roomName);
		} catch(InterruptedException ex) { }
		return ok(gameControllerMap.get(getRoomNameOfPlayer(player)).jsonField());
	}

	@SecuredAction
	public Result newRound() {
		DemoUser player = (DemoUser) ctx().args.get(SecureSocial.USER_KEY);
        gameControllerMap.get(getRoomNameOfPlayer(player)).startNewRound();
		return ok(gameControllerMap.get(getRoomNameOfPlayer(player)).jsonField());
	}

	@SecuredAction
	public Result undo() {
		DemoUser player = (DemoUser) ctx().args.get(SecureSocial.USER_KEY);
        gameControllerMap.get(getRoomNameOfPlayer(player)).undo();
		return ok(gameControllerMap.get(getRoomNameOfPlayer(player)).jsonField());
	}

	@SecuredAction
	public Result getJson() {
		DemoUser player = (DemoUser) ctx().args.get(SecureSocial.USER_KEY);
		return ok(gameControllerMap.get(getRoomNameOfPlayer(player)).jsonField());
	}

    @UserAwareAction
    public Result userAware() {
        DemoUser demoUser = (DemoUser) ctx().args.get(SecureSocial.USER_KEY);
        String userName;
        if (demoUser != null) {
            BasicProfile user = demoUser.main;
            if (user.firstName().isDefined()) {
                userName = user.firstName().get();
            } else if (user.fullName().isDefined()) {
                userName = user.fullName().get();
            } else {
                userName = "authenticated user";
            }
        } else {
            userName = "guest";
        }
        return ok("Hello " + userName + ", you are seeing a public page");
    }


    @SecuredAction(authorization = WithProvider.class, params = {"github"})
    public Result onlyGithub() {
        return ok("You are seeing this because you logged in using Github");
    }

    @SecuredAction
    public Result linkResult() {
        DemoUser current = (DemoUser) ctx().args.get(SecureSocial.USER_KEY);
        return ok(linkResult.render(current, current.identities));
    }

}
