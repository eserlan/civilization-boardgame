(function (civApp) {

  civApp.config(function ($provide) {
    $provide.provider("gameData", function () {

      this.$get = function (Restangular, $q, $log, $http) {
        var games = [];
        $log.info("Creating game data service");

        var createGame = function (game) {
          games = [];
          //TODO FIXME denne er ikke ferdig, den tar inn en json
          $log.info("Creating game");
          return Restangular.all('game').post(game);
        };

        var joinGame = function (game) {
          games = [];
          $log.info("Join game, doing put");
          return Restangular.one('game').one(game.id).one('join').put().$object;
        };

        var getGameById = function (id) {
          var url = baseUrl + id;
          return $http.get(url)
            .then(function (response) {
              return response.data;
            });
        };

        var getAllGames = function () {
          if (games.length > 0) {
            $log.info("Got games from cache");
            return $q.when(games);
          } else {
            $log.info("Got games from API");
            games = Restangular.all('game').getList();
            return games;
          }
        };

        var revealItem = function (gameId, logid) {
          var baseUrl = "http://localhost:8080/civilization/player/";
          var url = baseUrl + gameId + "/revealItem/" + logid;
          return $http.put(url)
            .success(function (response) {
              baseUrl = "http://localhost:8080/civilization/game/";
              return response;
            })
            .error(function(data) {
              baseUrl = "http://localhost:8080/civilization/game/";
              //TODO Sette error et sted
              return data;
            });
        };


        return {
          getAllGames: getAllGames,
          joinGame: joinGame,
          getGameById: getGameById,
          createGame: createGame,
          revealItem: revealItem
        };
      };

    });
  });

}(angular.module("civApp")));
