'use strict';
(function(module) {

  var GameService = function($http, $log) {

    /*
    var createNewMovie = function(movie) {
      $log.info("Inside MovieService createNewMovie: " + movie.title);
      return $http.post("/api/movies", movie)
        .then(function(response) {
          return response.data;
        });
    };

    var getAllMovies = function() {
      return $http.get("/api/movies")
        .then(function (response) {
          return response.data;
        });
    };
    var getMovieById = function(id) {
      return $http.get("/api/movies/" + id)
        .then(function(response) {
          return response.data;
        });
    };
    return {
      getAllMovies: getAllMovies,
      createNewMovie: createNewMovie,
      getMovieById: getMovieById
    };
    */

      var joinGame = function(id) {
          return $http.put("http://localhost:8080/civilization/" + id + "join")
              .then(function(response) {
                  $log.info("Joining game");
                  return response.data;
              });
      };

      //Authorization header "Basic " + B64Code.encode("cash1981" + ":" + "foo", StringUtil.__ISO_8859_1)

    var getAllGames = function() {
      return $http.get("http://localhost:8080/civilization/game")
             .then(function(response) {
                $log.info("Getting all games");
                return response.data;
             });
    };

    var getGameById = function(id) {
      return $http.get("http://localhost:8080/civilization/game/" + id)
        .then(function(response) {
          $log.info("Getting all games");
          return response.data;
        });
    };
    return {
      getAllGames: getAllGames,
      getGameById: getGameById
    };
  };

  module.factory("GameService", ["$http", "$log", GameService]);

}(angular.module("civApp")));
