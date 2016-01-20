$(function() {
	$('#playGame').click(function() {
		if(document.getElementById("playName").value == "") {
			document.getElementById("playName").value = "NewPlayGame";
		}
		window.location.replace('/game/' + document.getElementById("playName").value)
	});
	
	$('#ngGame').click(function() {
		if(document.getElementById("ngName").value == "") {
			document.getElementById("ngName").value = "NewAngularGame";
		}
		window.location.replace('/nggame/' + document.getElementById("ngName").value)
	});
	
	$('#createPlay').click(function() {
		if(document.getElementById("createPlayName").value == "") {
			document.getElementById("createPlayName").value = "NewPlayGame";
		}
		window.location.replace('/game/' + document.getElementById("createPlayName").value)
	});
	
	$('#createNGJS').click(function() {
		if(document.getElementById("createNGJSName").value == "") {
			document.getElementById("createNGJSName").value = "NewAngularGame";
		}
		window.location.replace('/nggame/' + document.getElementById("createNGJSName").value)
	});
});