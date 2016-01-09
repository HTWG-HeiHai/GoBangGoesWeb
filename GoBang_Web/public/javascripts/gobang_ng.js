var goBangApp = angular.module('goBangApp', [ 'ngSanitize' ]);

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

goBangApp.controller('GoBangCtrl', function($scope, $http) {
	$http.get('/json/newgame').success(function(data) {
		$scope.field = data.field
		$scope.p1wins = data.p1wins
		$scope.p2wins = data.p2wins
		$scope.current = data.current
		$scope.status = data.status
		$scope.tokenclicked = function(id) {
			$http.get('/json/set/' + id.split('_')[0] + '/' + id.split('_')[1]).success(function(data) {
				$scope.field = data.field
				$scope.p1wins = data.p1wins
				$scope.p2wins = data.p2wins
				$scope.current = data.current
				$scope.status = data.status
			});
		}
		$scope.command = function(command) {
			$http.get('/json/' + command).success(function(data) {
				$scope.field = data.field;
				$scope.p1wins = data.p1wins
				$scope.p2wins = data.p2wins
				$scope.current = data.current
				$scope.status = data.status
			});
		}
		// $scope.candidateclicked = function( row, column, value) {
		// if (value >0) {
		// $http.get('/json/'+row+'/'+column+'/'+value).success(function(data) {
		// $scope.size = data.size;
		// $scope.grid = data.grid;
		// });
		// }
		// }
		// $scope.command = function( command) {
		// $http.get('/json/'+command).success(function(data) {
		// $scope.size = data.size;
		// $scope.grid = data.grid;
		// });
		// }
	});
});