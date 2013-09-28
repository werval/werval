
var beerdb = angular.module('BeerDB', []);

beerdb.config([
    '$routeProvider', function($routeProvider) {
        $routeProvider.
                when('/', {redirectTo: '/breweries'}).
                when('/breweries', {templateUrl: 'breweries.html', controller: 'BreweriesCtrl'}).
                when('/breweries/new', {templateUrl: 'new-brewery.html', controller: 'NewBreweryCtrl'}).
                when('/breweries/:id', {templateUrl: 'brewery.html', controller: 'BreweryCtrl'}).
                when('/beers', {templateUrl: 'beers.html', controller: 'BeersCtrl'}).
                when('/beers/new', {templateUrl: 'new-beer.html', controller: 'NewBeerCtrl'}).
                when('/beers/:id', {templateUrl: 'beer.html', controller: 'BeerCtrl'}).
                otherwise({templateUrl: 'not-found.html'});
    }
]);

beerdb.filter('markdown', function() {
    return function(input) {
        if (!input)
            return "";
        return marked(input);
    };
});

beerdb.controller('NavBarCtrl', [
    '$scope', '$location', function($scope, $location) {
        $scope.breweries = false;
        $scope.beers = false;
        $scope.$on('$routeChangeSuccess', function() {
            $scope.breweries = $location.path().indexOf('/breweries') === 0;
            $scope.beers = $location.path().indexOf('/beers') === 0;
        });
    }
]);
beerdb.controller('BreweriesCtrl', [
    '$scope', '$http', function($scope, $http) {
        $http.get('/api/breweries').success(function(data) {
            $scope.breweries = angular.isArray(data._embedded.brewery)
                    ? data._embedded.brewery
                    : [data._embedded.brewery];
        });
    }
]);

beerdb.controller('NewBreweryCtrl', [
    '$scope', '$http', '$location', function($scope, $http, $location) {
        $scope.data = {};
        $scope.submitForm = function() {
            $http.post('/api/breweries', $scope.data).success(function(data) {
                $location.path('/breweries/' + data.id);
            });
        };
    }
]);


beerdb.controller('BreweryCtrl', [
    '$scope', '$http', '$location', '$routeParams', function($scope, $http, $location, $routeParams) {
        $http.get('/api/breweries/' + $routeParams.id).success(function(data) {
            $scope.brewery = data;
            if (data._embedded && data._embedded.beer)
                $scope.brewery.beers = angular.isArray(data._embedded.beer)
                        ? data._embedded.beer
                        : [data._embedded.beer];
            else
                $scope.brewery.beers = [];
        });
        $scope.deleteBrewery = function() {
            $http.delete('/api/breweries/' + $routeParams.id).success(function() {
                $location.path('/api/breweries');
            });
        };
    }
]);


beerdb.controller('BeersCtrl', [
    '$scope', '$http', function($scope, $http) {
        $http.get('/api/beers').success(function(data) {
            $scope.beers = angular.isArray(data._embedded.beer)
                    ? data._embedded.beer
                    : [data._embedded.beer];
        });
    }
]);

beerdb.controller('NewBeerCtrl', [
    '$scope', '$http', '$location', function($scope, $http, $location) {
        $scope.data = {};
        $http.get('/api/breweries').success(function(data) {
            $scope.breweries = angular.isArray(data._embedded.brewery)
                    ? data._embedded.brewery
                    : [data._embedded.brewery];
        });
        $scope.submitForm = function() {
            $http.post('/api/beers', $scope.data).success(function(data) {
                $location.path('/beers/' + data.id);
            });
        };
    }
]);

beerdb.controller('BeerCtrl', [
    '$scope', '$http', '$location', '$routeParams', function($scope, $http, $location, $routeParams) {
        $http.get('/api/beers/' + $routeParams.id).success(function(data) {
            $scope.beer = data;
            $scope.beer.brewery = data._embedded.brewery;
        });
        $scope.deleteBeer = function() {
            $http.delete('/api/beers/' + $routeParams.id).success(function() {
                $location.path('/api/beers');
            });
        };
    }
]);
