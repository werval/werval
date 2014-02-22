
$(function() {
    $(".collapse").collapse();
    $("body").tooltip({
        selector: 'a,button'
    });
});

function asArray(eventualArray) {
    if (!eventualArray)
        return [];
    return angular.isArray(eventualArray) ? eventualArray : [eventualArray];
}

var beerdb = angular.module('BeerDB', ['ngRoute', 'ngSanitize']);

beerdb.config([
    '$routeProvider', function($routeProvider) {
        $routeProvider.
                when('/', {redirectTo: '/breweries'}).
                when('/breweries', {templateUrl: 'breweries.html', controller: 'BreweriesCtrl'}).
                when('/breweries/new', {templateUrl: 'brewery-form.html', controller: 'NewBreweryCtrl'}).
                when('/breweries/:id', {templateUrl: 'brewery.html', controller: 'BreweryCtrl'}).
                when('/breweries/:id/edit', {templateUrl: 'brewery-form.html', controller: 'EditBreweryCtrl'}).
                when('/beers', {templateUrl: 'beers.html', controller: 'BeersCtrl'}).
                when('/beers/new', {templateUrl: 'beer-form.html', controller: 'NewBeerCtrl'}).
                when('/beers/:id', {templateUrl: 'beer.html', controller: 'BeerCtrl'}).
                when('/beers/:id/edit', {templateUrl: 'beer-form.html', controller: 'EditBeerCtrl'}).
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

beerdb.controller('FooterCtrl', [
    '$scope', '$http', function($scope, $http) {
        $http.get('/api').success(function(data) {
            $scope.version = data.detailed_version;
        });
    }
]);


beerdb.controller('BreweriesCtrl', [
    '$scope', '$http', function($scope, $http) {
        $http.get('/api/breweries').success(function(data) {
            $scope.breweries = data;
            $scope.loaded = true;
        });
    }
]);

beerdb.controller('NewBreweryCtrl', [
    '$scope', '$http', '$location', function($scope, $http, $location) {
        $scope.form_title = 'New brewery';
        $scope.loaded = true;
        $scope.submitForm = function() {
            $http.post('/api/breweries', $scope.data).success(function(data) {
                $location.path('/breweries/' + data.id);
            });
        };
        $scope.cancelForm = function() {
            $location.path('/breweries');
        };
    }
]);

beerdb.controller('BreweryCtrl', [
    '$scope', '$http', '$location', '$routeParams', function($scope, $http, $location, $routeParams) {
        $http.get('/api/breweries/' + $routeParams.id).success(function(data) {
            $scope.brewery = data;
            $scope.loaded = true;
        });
        $scope.deleteBrewery = function() {
            if (confirm("Delete Brewery?")) {
                $http.delete('/api/breweries/' + $routeParams.id).success(function() {
                    $location.path('/breweries');
                });
            }
        };
    }
]);

beerdb.controller('EditBreweryCtrl', [
    '$scope', '$http', '$location', '$routeParams', function($scope, $http, $location, $routeParams) {
        $scope.form_title = 'Edit brewery';
        $http.get('/api/breweries/' + $routeParams.id).success(function(data) {
            $scope.data = data; // Rename to $scope.brewery
            $scope.loaded = true;
        });
        $scope.submitForm = function() {
            $http.put('/api/breweries/' + $routeParams.id, $scope.data).success(function(data) {
                $location.path('/breweries/' + $routeParams.id);
            });
        };
        $scope.cancelForm = function() {
            $location.path('/breweries/' + $routeParams.id);
        };
    }
]);


beerdb.controller('BeersCtrl', [
    '$scope', '$http', function($scope, $http) {
        $http.get('/api/beers').success(function(data) {
            $scope.beers = data;
            $scope.loaded = true;
        });
    }
]);

beerdb.controller('NewBeerCtrl', [
    '$scope', '$http', '$location', function($scope, $http, $location) {
        $scope.form_title = 'New beer';
        $scope.data = {
            brewery_id: $location.search().brewery_id ? parseInt($location.search().brewery_id) : undefined
        };
        $http.get('/api/breweries').success(function(data) {
            $scope.breweries = data;
            $scope.loaded = true;
        });
        $scope.submitForm = function() {
            $http.post('/api/beers', $scope.data).success(function(data) {
                $location.path('/beers/' + data.id);
            });
        };
        $scope.cancelForm = function() {
            $location.
                    path($location.search().brewery_id ? '/breweries/' + $location.search().brewery_id : '/beers').
                    search({});
        };
    }
]);

beerdb.controller('BeerCtrl', [
    '$scope', '$http', '$location', '$routeParams', function($scope, $http, $location, $routeParams) {
        $http.get('/api/beers/' + $routeParams.id).success(function(data) {
            $scope.beer = data;
            $scope.loaded = true;
        });
        $scope.deleteBeer = function() {
            if (confirm("Delete Beer?")) {
                $http.delete('/api/beers/' + $routeParams.id).success(function() {
                    $location.path('/breweries/' + $scope.beer.brewery.id);
                });
            }
        };
    }
]);

beerdb.controller('EditBeerCtrl', [
    '$scope', '$q', '$http', '$location', '$routeParams', function($scope, $q, $http, $location, $routeParams) {
        $scope.form_title = 'Edit beer';
        $scope.brewery_disabled = true;
        $http.get('/api/breweries').success(function(data) {
            $scope.breweries = data;
            if ($scope.data)
                $scope.loaded = true;
        });
        $http.get('/api/beers/' + $routeParams.id).success(function(data) {
            $scope.data = data;
            $scope.data.brewery_id = data.brewery.id;
            if ($scope.breweries)
                $scope.loaded = true;
        });
        $scope.submitForm = function() {
            $http.put('/api/beers/' + $routeParams.id, $scope.data).success(function(data) {
                $location.path('/beers/' + $routeParams.id);
            });
        };
        $scope.cancelForm = function() {
            $location.path('/beers/' + $routeParams.id);
        };
    }
]);
