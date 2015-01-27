'use strict';

angular.module('civApp').provider('BaseUrlProvider', function () {
  var baseUrl = 'http://localhost:8080/civilization';

  return {
    setBaseUrl: function (newBaseUrl) {
      baseUrl = newBaseUrl || baseUrl;
    },
    $get: function () {
      return baseUrl;
    }
  };
});
