
function asArray(eventualArray) {
    if (!eventualArray)
        return [];
    return angular.isArray(eventualArray) ? eventualArray : [eventualArray];
}

var beerdb = angular.module('BeerDB', []);

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
beerdb.controller('BreweriesCtrl', [
    '$scope', '$http', function($scope, $http) {
        $http.get('/api/breweries').success(function(data) {
            $scope.breweries = asArray(data._embedded.brewery);
        });
    }
]);

beerdb.controller('NewBreweryCtrl', [
    '$scope', '$http', '$location', function($scope, $http, $location) {
        $scope.form_title = 'New brewery';
        $scope.data = {
            name: '',
            url: '',
            description: ''
        };
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
            $scope.brewery.beers = asArray(data._embedded.beer);
        });
        $scope.deleteBrewery = function() {
            $http.delete('/api/breweries/' + $routeParams.id).success(function() {
                $location.path('/api/breweries');
            });
        };
    }
]);

beerdb.controller('EditBreweryCtrl', [
    '$scope', '$http', '$location', '$routeParams', function($scope, $http, $location, $routeParams) {
        $scope.form_title = 'Edit brewery';
        $scope.data = {
            name: '',
            url: '',
            description: ''
        };
        $http.get('/api/breweries/' + $routeParams.id).success(function(data) {
            $scope.data = data;
        });
        $scope.submitForm = function() {
            $http.put('/api/breweries/' + $routeParams.id, $scope.data).success(function(data) {
                $location.path('/breweries/' + $routeParams.id);
            });
        };
    }
]);


beerdb.controller('BeersCtrl', [
    '$scope', '$http', function($scope, $http) {
        $http.get('/api/beers').success(function(data) {
            $scope.beers = asArray(data._embedded.beer);
        });
    }
]);

beerdb.controller('NewBeerCtrl', [
    '$scope', '$http', '$location', function($scope, $http, $location) {
        $scope.form_title = 'New beer';
        $scope.data = {
            brewery_id: $location.search().brewery_id ? parseInt($location.search().brewery_id) : undefined,
            name: '',
            abv: undefined,
            description: ''
        };
        $http.get('/api/breweries').success(function(data) {
            $scope.breweries = asArray(data._embedded.brewery);
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

beerdb.controller('EditBeerCtrl', [
    '$scope', '$http', '$location', '$routeParams', function($scope, $http, $location, $routeParams) {
        $scope.form_title = 'Edit beer';
        $scope.data = {
            brewery_id: undefined,
            name: '',
            abv: undefined,
            description: ''
        };
        $http.get('/api/breweries').success(function(data) {
            $scope.breweries = asArray(data._embedded.brewery);
        });
        $http.get('/api/beers/' + $routeParams.id).success(function(data) {
            $scope.data = data;
            $scope.data.brewery_id = data._embedded.brewery.id;
        });
        $scope.submitForm = function() {
            $http.put('/api/beers/' + $routeParams.id, $scope.data).success(function(data) {
                $location.path('/beers/' + $routeParams.id);
            });
        };
    }
]);
