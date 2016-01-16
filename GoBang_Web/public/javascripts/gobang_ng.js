var goBangApp = angular.module('goBangApp', [ 'ngWebSocket' ]);

goBangApp.directive('gobangField', function() {
	return {
		templateUrl : 'html/gobangField.html'
	};
});

goBangApp.directive('gobangRow', function() {
	return {
		templateUrl : 'html/gobangRow.html'
	};
});

goBangApp.directive('gobangToken', function() {
	return {
		templateUrl : 'html/gobangToken.html'
	};
});

var Server;

goBangApp.controller('GoBangCtrl', function($scope, $http, $websocket) {
	$http.get('/json/field').success(function(data) {
		$scope.field = data.field
		$scope.p1wins = data.p1wins
		$scope.p2wins = data.p2wins
		$scope.current = data.current
		$scope.status = data.status
		$scope.cplayer = 'Player1'
		if(data.current == 'blue') {
			$scope.cplayer = 'Player2'
		}
		$scope.tokenclicked = function(id) {
			jsonCommand = {"command": id};
			Server.send(JSON.stringify(jsonCommand));
		}
		$scope.command = function(command) {
			jsonCommand = {"command": command};
			Server.send(JSON.stringify(jsonCommand));			
		}
		$http.get('user').success(function(user) {
			$scope.userId = user
			Server = $websocket('ws://' + location.host + '/websocket/' + user);
			Server.onOpen(function() {
	            console.log("got Socket");
	        });

			Server.onClose(function() {
	            console.log("Socket closed");
	        });

			Server.onError(function() {
	            console.log("got Socket Error");
	        });

			Server.onMessage(function(message) {
				var data = JSON.parse(message.data);
				if(data.command == 'playerLeft') {
					$http.get('/quitgame/' + $scope.room).success(function () {
						console.log("the other player left")
						alert("The other player left the game!")
						window.location.replace("/")
					});
				} else if(data.command == 'stayAlive') {
					console.log("staying alive")
				} else {
					$scope.field = data.field
					$scope.p1wins = data.p1wins
					$scope.p2wins = data.p2wins
					$scope.current = data.current
					$scope.status = data.status
					$scope.cplayer = 'Player1'
					if(data.current == 'blue') {
						$scope.cplayer = 'Player2'
					}
					if(data.status == 'g') {
						$scope.winner = 'Player2'
						if(data.current == 'blue') {
							$scope.winner = 'Player1'
						}

						$(".bs-winner-modal-sm").modal("show");
					}
				}
			});
		});
		$http.get('room').success(function(data) {
			$scope.room = data
			$http.get('player/' + $scope.room).success(function(data2) {
				$scope.name = data2
			});
		});
	});
});