/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';

const angular = require('angular');
const searchConfig = require('../views/operation-log-search-config.json');

module.exports = [
  '$scope',
  'Views',
  'camAPI',
  'localConf',
  '$translate',
  '$location',
  'search',
  function($scope, Views, camAPI, localConf, $translate) {
    $scope.loadingState = 'LOADING';
    var historyService = camAPI.resource('history');

    $scope.searchId = 'pdSearch';
    $scope.paginationId = 'pdPage';
    $scope.pages = null;
    $scope.query = null;
    $scope.sortBy = loadLocal({sortBy: 'name', sortOrder: 'asc'});
    $scope.config = searchConfig;
    $scope.onSearchChange = params => updateView(params.query, params.pages);
    $scope.onSortChange = updateView;

    $scope.operationLogActions = Views.getProviders({
      component: 'cockpit.operationLog.action'
    });
    $scope.hasActionPlugin = $scope.operationLogActions.length > 0;

    // prettier-ignore
    $scope.headColumns = [
      { class: 'user',    request: '', sortable: false, content: $translate.instant('PLUGIN_OPERATION_LOG_USER')},
      { class: 'operation',    request: '', sortable: false, content: $translate.instant('PLUGIN_OPERATION_LOG_OPERATION')},
      { class: 'entity',    request: '', sortable: false, content: $translate.instant('PLUGIN_OPERATION_LOG_ENTITY')},
      { class: 'processDefinition',    request: '', sortable: false, content: $translate.instant('PLUGIN_OPERATION_LOG_PROCESS_DEFINITION')},
      { class: 'changeTime',    request: '', sortable: true, content: $translate.instant('PLUGIN_OPERATION_LOG_CHANGE_TIME')},
      { class: 'property',    request: '', sortable: false, content: $translate.instant('PLUGIN_OPERATION_LOG_PROPERTY')},
      { class: 'newValue',    request: '', sortable: false, content: $translate.instant('PLUGIN_OPERATION_LOG_NEW_VALUE')},
      { class: 'annotation',    request: '', sortable: false, content: $translate.instant('PLUGIN_OPERATION_LOG_ANNOTATION')},
    ];

    // only get count of process definitions
    const countUserOperations = function() {
      $scope.mainLoadingState = 'LOADING';
      historyService.userOperationCount({}, (err, count) => {
        if (err) $scope.mainLoadingState = 'ERROR';
        else $scope.mainLoadingState = count ? 'LOADED' : 'EMPTY';
        $scope.operationLogCount = count;
      });
    };

    // get full list of operation logs
    function updateView(queryObj, pagesObj, sortObj) {
      $scope.loadingState = 'LOADING';
      $scope.query = queryObj || $scope.query;
      $scope.pages = pagesObj || $scope.pages;
      $scope.sortBy = sortObj || $scope.sortBy;

      const queryParams = $scope.query;
      const sortParams = $scope.sortBy;
      const countParams = angular.extend({}, queryParams);

      saveLocal(sortParams);

      var pages = ($scope.pages = {
        size: 50,
        total: 0,
        current: 1
      });

      var operationLogQuery = angular.extend(
        {
          maxResults: pages.size,
          firstResult: pages.size * (pages.current - 1)
        },
        {}
      );

      return historyService
        .userOperationCount(countParams)
        .then(res => {
          $scope.total = res.count;

          historyService
            .userOperation(operationLogQuery)
            .then(operationLogs => {
              $scope.operationLogs = operationLogs;
              $scope.loadingState = res.count ? 'LOADED' : 'EMPTY';
              return operationLogs;
            })
            .catch(angular.noop);

          return $scope.total;
        })
        .catch(angular.noop);
    }

    $scope.activeTab = 'list';

    $scope.selectTab = function(tab) {
      $scope.activeTab = tab;
    };

    $scope.activeSection = localConf.get('operationLogActive', true);

    $scope.toggleSection = function toggleSection() {
      $scope.activeSection = !$scope.activeSection;
      localConf.set('operationLogActive', $scope.activeSection);

      if ($scope.activeSection) updateView();
    };

    function saveLocal(sortBy) {
      localConf.set('sortOperationLogTab', sortBy);
    }
    function loadLocal(defaultValue) {
      return localConf.get('sortOperationLogTab', defaultValue);
    }

    countUserOperations();
    updateView();
  }
];
