$(function() {
	
	Server = new FancyWebSocket('ws://' + location.host + '/websocket');

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

		document.getElementById("p1wins").textContent = jsonField.p1wins
		document.getElementById("p2wins").textContent = jsonField.p2wins

		for(var i = 0; i < jsonField.field.length; ++i) {
			for(var j = 0; j < jsonField.field.length; ++j) {
				if(jsonField.field[i][j].name != 'none') {
					updateField(jsonField.field[i][j].id, jsonField.field[i][j].name);
				} else {
					updateField(jsonField.field[i][j].id, 'white');
				}
			}
		}
		if(jsonField.status == 'g') {
			winner = 'Player 2'
			if(jsonField.current == 'blue') {
				winner = 'Player 1'
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