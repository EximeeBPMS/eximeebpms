'use strict';
module.exports = [
  'ViewsProvider',
  function(ViewsProvider) {
    ViewsProvider.registerDefaultView('cockpit.navigation', {
      id: 'tasks',
      label: 'COCKPIT_OPERATION_LOG',
      template: '<!-- nothing to show, but needed -->',
      pagePath: '#/operationLog',
      checkActive: function(path) {
        return path.indexOf('#/operationLog') > -1;
      },
      controller: function() {},

      priority: 20
    });
  }
];
