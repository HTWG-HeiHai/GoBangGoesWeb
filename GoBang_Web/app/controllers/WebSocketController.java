package controllers;


import de.htwg.gobang.controller.IGbLogic;
import de.htwg.gobang.controller.impl.GbLogic;
import de.htwg.gobang.entities.IGameToken;
import de.htwg.gobang.game.GoBangGame;
import play.api.i18n.DefaultMessagesApi;
import play.libs.F;
import play.libs.F.Callback;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import play.mvc.WebSocket.Out;
import play.twirl.api.Html;
import securesocial.core.java.SecuredAction;
import services.DemoUser;
import de.htwg.gobang.observer.IObserver;
import de.htwg.gobang.ui.TUI;
import views.html.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.stream.Stream;

import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsonorg.JsonOrgModule;

@SecuredAction
public class WebSocketController implements IObserver {


    private IGbLogic controller;
    private TUI tui = GoBangGame.getInstance().getTui();
    private DemoUser player1;
    private DemoUser player2;

    private WebSocket<JsonNode> socketPlayer1;
    private Out<JsonNode> outPlayer1;
    private WebSocket<JsonNode> socketPlayer2;
    private Out<JsonNode> outPlayer2;
    private String roomName;
    private boolean running;

    public WebSocketController(IGbLogic controller, DemoUser player1, String roomName) {
        this.controller = controller;
        this.player1 = player1;
        this.roomName = roomName;
        this.running = true;

        socketPlayer1 = new WebSocket<JsonNode>() {
            @Override
            public void onReady(In<JsonNode> in, Out<JsonNode> out) {
                System.out.println("Init Socket for Player1");
                outPlayer1=out;
                in.onMessage(new Callback<JsonNode>() {
                	public void invoke(JsonNode json) {
                		System.out.println("message incoming from Player1");
        				String command = json.get("command").textValue();
        				if (command.equals("newRound")) {
        					startNewRound();
        				} else if (command.equals("newGame")) {
        					startNewGame();
        				} else if (command.equals("undo")) {
        					undo();
        				} else {
        					int x = Integer.parseInt(command.split("_")[0]) - 1;
        					int y = Integer.parseInt(command.split("_")[1]) - 1;
        					set(x, y);
        				}
        			}
                });
                in.onClose(new F.Callback0() {
                    @Override
                    public void invoke() throws Throwable {
                        System.out.println("Player1 has quit the game");
                        running = false;
                        quitEvent(outPlayer2);
                    }
                });

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (running) {
                            System.out.println("stay alive Player2");
                            try {
                                Thread.sleep(30000);
                            } catch (InterruptedException e) {
                            }
                            ObjectMapper mapper = new ObjectMapper();
                    		mapper.registerModule(new JsonOrgModule());
                    		JSONObject json = new JSONObject();
                    		json.append("command", "stayAlive");
                            outPlayer1.write(mapper.valueToTree(json));
                        }

                    }
                }).start();
            }
        };

        socketPlayer2 = new WebSocket<JsonNode>() {
            @Override
            public void onReady(In<JsonNode> in, Out<JsonNode> out) {
                System.out.println("Init Socket for Player2");
                outPlayer2 = out;
                in.onMessage(new Callback<JsonNode>() {
                	public void invoke(JsonNode json) {
                		System.out.println("message incoming from Player2");
        				String command = json.get("command").textValue();
        				if (command.equals("newRound")) {
        					startNewRound();
        				} else if (command.equals("newGame")) {
        					startNewGame();
        				} else if (command.equals("undo")) {
        					undo();
        					controller.removeToken();
        				} else {
        					int x = Integer.parseInt(command.split("_")[0]) - 1;
        					int y = Integer.parseInt(command.split("_")[1]) - 1;
        					set(x, y);
        					controller.setToken(x, y);
        				}
        			}
                });
                in.onClose(new F.Callback0() {
                    @Override
                    public void invoke() throws Throwable {
                        System.out.println("Player2 has quit the game");
                        running = false;
                        quitEvent(outPlayer1);
                    }
                });

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (running) {
                            System.out.println("stay alive Player2");
                            try {
                                Thread.sleep(30000);
                            } catch (InterruptedException e) {
                            }
                        	ObjectMapper mapper = new ObjectMapper();
                    		mapper.registerModule(new JsonOrgModule());
                    		JSONObject json = new JSONObject();
                    		json.append("command", "stayAlive");
                            outPlayer2.write(mapper.valueToTree(json));
                        }

                    }
                }).start();
            }
        };

        System.out.println("Adding Observer");
        controller.addObserver(this);
    }

    private void quitEvent(Out<JsonNode> otherPlayer) {
        try {
        	ObjectMapper mapper = new ObjectMapper();
    		mapper.registerModule(new JsonOrgModule());
    		JSONObject json = new JSONObject();
    		json.append("command", "playerLeft");
            otherPlayer.write(mapper.valueToTree(json));
        } catch (NullPointerException npe) {
        	Application.quit1Player(this.roomName);
        }
    }

    public void setPlayer2(DemoUser user) {
        this.player2 = user;
    }

    public DemoUser getPlayer1() {
        return player1;
    }

    public DemoUser getPlayer2() {
        return player2;
    }

    public WebSocket<JsonNode> getSocket(DemoUser du) {
        if (du.equals(player1)) {
            return socketPlayer1;
        }
        return socketPlayer2;
    }

    public String getUIField() {
    	String tField = tui.drawField();
		tField = tField.replaceAll("\n", "<br>");
		tField = tField.replaceAll(" ", "&nbsp;");
        return tField;
    }

    public String getUIHead() {
		String tHead = tui.pTurn();
		tHead = tHead.replaceAll("\n", "<br>");
        return tHead;
    }

    public void set(int x, int y) {
    	controller.setToken(x, y);
    }

    public void undo() {
    	controller.removeToken();
    }
    
    public Html startNewGame() {
    	controller = new GbLogic();
        System.out.println("Adding Observer");
        controller.addObserver(this);
        return game.render(getUIHead(), getUIField(), "");
    }

	public void startNewRound() {
		if (controller.getcPlayer() == controller.getPlayer1()) {
			controller.newGame(true);
		} else {
			controller.newGame(false);
		}
	}

	public JsonNode jsonField() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JsonOrgModule());
		JSONObject json = new JSONObject();
		JSONArray f = new JSONArray();
		int i = 1;
		for (IGameToken[] row : controller.getField()) {
			JSONArray r = new JSONArray();
			int j = 1;
			for (IGameToken token : row) {
				JSONObject t = new JSONObject();
				t.put("id", i + "_" + j);
				t.put("name", token.getName());
				r.put(t);
				j++;
			}
			f.put(r);
			i++;
		}
		json.put("current", controller.getcPlayer().getName());
		json.put("p1wins", controller.getWinPlayer1());
		json.put("p2wins", controller.getWinPlayer2());
		json.put("status", String.valueOf(controller.getStatus()));
		json.put("field", f);
		return mapper.valueToTree(json);
	}
	
    public void updateAll() {
        if(outPlayer1 != null) {
            outPlayer1.write(jsonField());
        }
        if(outPlayer2 != null) {
            outPlayer2.write(jsonField());
        }
    }

    @Override
    public void update() {
    	System.out.println("updating");
        updateAll();
    }
}
