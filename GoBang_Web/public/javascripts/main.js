$(function() {
	$('#playGame').click(function() {
		window.location.replace('/game/' + document.getElementById("playName").value)
	});
	
	$('#ngGame').click(function() {
		window.location.replace('/nggame/' + document.getElementById("ngName").value)
	});
	
	$('#createPlay').click(function() {
		window.location.replace('/game/' + document.getElementById("createPlayName").value)
	});
	
	$('#createNGJS').click(function() {
		window.location.replace('/nggame/' + document.getElementById("createNGJSName").value)
	});
});