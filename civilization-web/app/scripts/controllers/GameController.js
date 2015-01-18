'use strict';
(function (module) {
  var GameController = function ($log, $routeParams, gameData, currentUser, $filter, ngTableParams, $scope, growl) {
    var model = this;
    model.user = currentUser.profile;
    $scope.userHasAccess = false;
    model.yourTurn = false;

    var gamePromise = gameData.getGameById($routeParams.id)
      .then(function (game) {
        model.game = game;
        $scope.userHasAccess = game.player && game.player.username === model.user.username;
        model.yourTurn = game.player && game.player.yourTurn;

        if(model.yourTurn) {
          growl.info("<strong>It's your turn!</strong>", {disableCountDown: true, ttl: -1});
        }

        return game;
      });

    model.tableParams = new ngTableParams({
      page: 1,            // show first page
      count: 10,          // count per page
      sorting: {
        created: 'desc'     // initial sorting
      }
    }, {
      total: 0, // length of data
      getData: function ($defer, params) {
        // use build-in angular filter
        // update table params

        gamePromise.then(function (game) {
          var orderedData = params.sorting() ? $filter('orderBy')(game.publicLogs, params.orderBy()) : game.publicLogs;
          params.total(game.publicLogs.length);
          $defer.resolve(orderedData.slice((params.page() - 1) * params.count(), params.page() * params.count()));
        });
      }
    });

  };

  module.controller("GameController",
    ["$log", "$routeParams", "gameData", "currentUser", "$filter", "ngTableParams", "$scope", "growl", GameController]);

}(angular.module("civApp")));
