'use strict';
angular.module('civApp').controller('RegisterController', function ($scope, $modalInstance, $log, growl) {
  var model = this;

  model.gameTypes = [
    { label: 'Base Game', value: 'BASE', disabled: true },
    { label: 'Fame and Fortune (FAF)', value: 'FAF', disabled: true },
    { label: 'Wisdom and Warfare (WAW) & Fame and Fortune', value: 'WAW', disabled: false },
    { label: 'Dawn of Civilization', value: 'DOC', disabled: true }
  ];

  model.selectedGametype = model.gameTypes[2];

  $scope.createGameOk = function() {
    if(model.selectedGametype && model.selectedGametype.value !== 'WAW') {
      growl.error('We only support Wisdom and Warfare the time being');
    }

    var createNewGame = {
      'name' : model.gamename,
      'type' : model.selectedGametype.value,
      'numOfPlayers' : model.numOfPlayers,
      'color' : model.color
    };

    $modalInstance.close(createNewGame);
  };

  $scope.registerOk = function() {
    if(!model.verification && !model.password && model.password !== model.verification) {
      growl.error("Passwords did not match");
      return;
    }

    var register = {
      'username' : model.registerUsername,
      'email' : model.registerEmail,
      'password' : $scope.password
    };

    $modalInstance.close(register);
  };

  $scope.registerCancel = function () {
    $modalInstance.dismiss('cancel');
  };
});
