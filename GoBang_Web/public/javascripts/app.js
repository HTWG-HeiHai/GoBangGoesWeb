$(function() {
	
	Server = new FancyWebSocket('ws://' + location.host + '/websocket/' + $('#userId')[0].type);
	
	$('#new_round').click(function() {
		command("newRound");
	});

	$('#undo_icon').click(function() {
		command("undo");
	});
	
	Server.bind('message', function(field) {
		var jsonField = JSON.parse(field);
		if(jsonField.command == 'playerLeft') {
			xhttp = new XMLHttpRequest();
			xhttp.open("GET", '/quitgame/' + document.getElementById("room").textContent, true);
			xhttp.send();
			xhttp.onreadystatechange = function() {
				if (xhttp.readyState == 4 && xhttp.status == 200) {
					console.log("the other player left")
					alert("The other player left the game!")
					window.location.replace("/")
				}
			}
		} else if(jsonField.command == 'stayAlive') {
			console.log("staying alive")
		} else if(jsonField.command == 'enterGame') {
			command("enterGame")
		} else if(jsonField.command == 'start') {
			location.reload()
			console.log("start game")
		} else {
			playerName = document.getElementById("player").textContent
			current = 'Player1'
			if(jsonField.current == 'blue') {
				current = 'Player2'
				$('#p1').css('border-style', 'none');
				$('#p2').css('border-style', 'solid');
			} else {
				$('#p1').css('border-style', 'solid');
				$('#p2').css('border-style', 'none');
			}
			if(current == document.getElementById("player").textContent) {
				$('#undo_icon').css('visibility', 'hidden');
			} else {
				$('#undo_icon').css('visibility', 'visible');
			}
			document.getElementById("p1wins").textContent = jsonField.p1wins
			document.getElementById("p2wins").textContent = jsonField.p2wins

			for(var i = 0; i < jsonField.field.length; ++i) {
				for(var j = 0; j < jsonField.field.length; ++j) {
					if(jsonField.field[i][j].name == 'blue') {
						updateField(jsonField.field[i][j].id, '#151515');
					} else if(jsonField.field[i][j].name == 'black') {
							updateField(jsonField.field[i][j].id, 'ivory');
					} else {
						updateField(jsonField.field[i][j].id, 'rgba(0, 0, 0, 0.1)');
					}
					if(current != playerName || jsonField.status == 'g') {
						$('#' + jsonField.field[i][j].id).prop('disabled', true);
					} else {
						$('#' + jsonField.field[i][j].id).prop('disabled', false);
					}
				}
			}
			if(jsonField.status == 'g') {
				$('#undo_icon').css('visibility', 'hidden');
				winner = 'Player2'
				if(jsonField.current == 'blue') {
					winner = 'Player1'
				}
				$('#p1').css('border-style', 'none');
				$('#p2').css('border-style', 'none');
				document.getElementById("winner").textContent = winner
				$(".bs-winner-modal-sm").modal("show");
			}
		}
	});

	Server.connect();
	setTimeout(function(){command("newRound")}, 100);
});

var Server;

function command(command) {
	jsonCommand = {"command": command};
	Server.send('command', JSON.stringify(jsonCommand));
};

function updateField(source, color) {
	$('#' + source).css('background-color', color);
};