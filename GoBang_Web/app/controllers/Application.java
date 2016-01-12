package controllers;

import views.html.*;
import de.htwg.gobang.controller.IGbLogic;
import de.htwg.gobang.entities.IGameField;
import de.htwg.gobang.entities.IGameToken;
import de.htwg.gobang.game.GoBangGame;
import de.htwg.gobang.ui.TUI;
import de.htwg.gobang.observer.IObserver;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import play.libs.F.Callback0;
import play.libs.F.Callback;

import java.io.File;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsonorg.JsonOrgModule;

import org.json.JSONArray;
import org.json.JSONObject;

public class Application extends Controller {

	GoBangGame games;
	IGbLogic controller;
	TUI printer;
	char lastAction = 'n';

	public Result game() {
		games = GoBangGame.getInstance();
		controller = games.getController();
		printer = games.getTui();
		String tField = printer.drawField();
		String tHead = printer.pTurn();
		tHead = tHead.replaceAll("\n", "<br>");
		tField = tField.replaceAll("\n", "<br>");
		tField = tField.replaceAll(" ", "&nbsp;");
		return ok(game.render(tHead, tField));
	}

	public Result gobang() {
		return ok(gobang.render("GoBang", gobangMain.render()));
	}
	
	public Result gobang_game(){
	    return ok(gobang.render("GoBang", gobangGame.render()));
	}

	public Result gobang_help(){
	    return ok(gobang.render("GoBang", gobangHelp.render()));
	}

	public Result gobang_about(){
	    return ok(gobang.render("GoBang", gobangAbout.render()));
	}

	public void channel(WebSocket.Out<JsonNode> out, JsonNode field) {
		out.write(field);
	}

	public WebSocket<JsonNode> webSocket() {
		return new WebSocket<JsonNode>() {
			public void onReady(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out) {
				new Watcher(controller, out);

				in.onMessage(new Callback<JsonNode>() {
					public void invoke(JsonNode json) {
						String command = json.get("command").textValue();
						if (command.equals("newRound")) {
							startNewRound();
						} else if (command.equals("newGame")) {
							game();
						} else {
							int x = Integer.parseInt(command.split("_")[0]) - 1;
							int y = Integer.parseInt(command.split("_")[1]) - 1;
							lastAction = controller.setToken(x, y);
							channel(out, jsonField());// im controller: in
														// newGame() fehlt
														// notifyObservers()
						}
						// if(lastAction == 'e') {
						// channel(out, x + 1, y + 1,
						// controller.getcPlayer().getName());
						// channel(out, toJson(controller.getField()));
						// }
						// gewinnen fehlt noch
					}
				});
				in.onClose(new Callback0() {
					public void invoke() {

					}
				});
				// out.write("message");
			}
		};
	}

	public class Watcher implements IObserver {
		private Object out;

		public Watcher(IGbLogic engine, WebSocket.Out<JsonNode> out) {
			engine.addObserver(this);
			this.out = out;
		}

		@Override
		public void update() {
			// channel(out)
			// System.out.println("heyyo");
			channel((WebSocket.Out<JsonNode>) out, jsonField());
		}
	}

	//helper functions

	private void startNewRound() {
		if (controller.getcPlayer() == controller.getPlayer1()) {
			controller.newGame(true);
		} else {
			controller.newGame(false);
		}
	}

	private JsonNode jsonField() {
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
		json.put("status", String.valueOf(lastAction));
		json.put("field", f);
		return mapper.valueToTree(json);
	}

	//methods used by Angular.js

	public Result setToken(int x, int y) {
		// if(coord.length() != 2) {
		// return
		// }
		lastAction = controller.setToken(x - 1, y - 1);
		return ok(jsonField());
	}

	public Result newGame() {
//		games.exit(); //muss erst implementiert werden in gobanggame (exit(): instance = null)
		games = GoBangGame.getInstance();
		controller = games.getController();
		return ok(jsonField());
	}

	public Result newRound() {
		startNewRound();
		lastAction = 'e';
		return ok(jsonField());
	}

	public Result getJson() {
		return ok(jsonField());
	}
}
