$(function() {
	
	Server = new FancyWebSocket('ws://' + location.host + '/websocket/' + $('#userId')[0].type);

	$('.btn-lg').click(function() {
		command(this.id);
	});
	
	$('#new_round').click(function() {
		command("newRound");
	});

	$('#new_game').click(function() {
		command("newGame");
	});

	$('#undo_icon').click(function() {
		command("undo");
	});
	
	Server.bind('message', function(field) {
		var jsonField = JSON.parse(field);

		playerName = document.getElementById("userId").textContent
		current = 'Player1'
		if(jsonField.current == 'blue') {
			current = 'Player2'
		}
//		console.log(current == playerName)
		document.getElementById("p1wins").textContent = jsonField.p1wins
		document.getElementById("p2wins").textContent = jsonField.p2wins

		for(var i = 0; i < jsonField.field.length; ++i) {
			for(var j = 0; j < jsonField.field.length; ++j) {
				if(jsonField.field[i][j].name != 'none') {
					updateField(jsonField.field[i][j].id, jsonField.field[i][j].name);
				} else {
					updateField(jsonField.field[i][j].id, 'white');
				}
				if(current != playerName || jsonField.status == 'g') {
					$('#' + jsonField.field[i][j].id).prop('disabled', true);
				} else {
					$('#' + jsonField.field[i][j].id).prop('disabled', false);
				}
			}
		}
		if(jsonField.status == 'g') {
			winner = 'Player2'
			if(jsonField.current == 'blue') {
				winner = 'Player1'
			}
			document.getElementById("winner").textContent = winner
			$(".bs-winner-modal-sm").modal("show");
		}
	});

	Server.connect();
});

var changeField = function(x, y) {

};

var Server;

function command(command) {
	jsonCommand = {"command": command};
	Server.send('command', JSON.stringify(jsonCommand));
};

function updateField(source, color) {
	$('#' + source).css('background-color', color);
};