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

var angular = require('angular');
var template = require('./operation-log-table.html?raw');

var debouncePromiseFactory = require('eximeebpms-bpm-sdk-js').utils
  .debouncePromiseFactory;
var debounceQuery = debouncePromiseFactory();

module.exports = [
  'ViewsProvider',
  function(ViewsProvider) {
    ViewsProvider.registerDefaultView('cockpit.decisionDefinition.tab', {
      id: 'decision-instances-table',
      label: 'DECISION_DEFINITION_LABEL',
      template: template,
      controller: [
        '$scope',
        '$location',
        'search',
        'routeUtil',
        'camAPI',
        'Views',
        '$translate',
        'localConf',
        function(
          $scope,
          $location,
          search,
          routeUtil,
          camAPI,
          Views,
          $translate,
          localConf
        ) {
          // prettier-ignore
          $scope.headColumns = [
            {
              class: 'operation-id',
              request: '',
              sortable: false,
              content: $translate.instant('PLUGIN_OPERATION_ID')
            },
          ];

          var historyService = camAPI.resource('history');

          $scope.onSearchChange = updateView;
          $scope.onSortChange = updateView;

          function updateView(searchQuery, pages, sortObj) {
            $scope.pagesObj = pages || $scope.pagesObj;
            $scope.sortObj = sortObj || $scope.sortObj;

            // Add default sorting param
            if (sortObj) {
              saveLocal(sortObj);
            }

            var page = $scope.pagesObj.current,
              count = $scope.pagesObj.size,
              firstResult = (page - 1) * count;

            $scope.operationLogs = null;
            $scope.loadingState = 'LOADING';

            var operationLogQuery = angular.extend(
              {
                firstResult: firstResult,
                maxResults: count,
                sortBy: $scope.sortObj.sortBy,
                sortOrder: $scope.sortObj.sortOrder
              },
              searchQuery
            );

            var countQuery = angular.extend(
              {
                decisionDefinitionId: $scope.decisionDefinition.id
              },
              searchQuery
            );

            return debounceQuery(
              historyService
                .userOperationCount(countQuery)
                .then(function(count) {
                  var total = count.count;

                  return historyService
                    .userOperation(operationLogQuery)
                    .then(function(data) {
                      return {total, data};
                    });
                })
            )
              .then(({total, data}) => {
                $scope.operationLogs = data;
                $scope.loadingState = data.length ? 'LOADED' : 'EMPTY';

                var phase = $scope.$root.$$phase;
                if (phase !== '$apply' && phase !== '$digest') {
                  $scope.$apply();
                }

                return total;
              })
              .catch(angular.noop);
          }

          function saveLocal(sortObj) {
            localConf.set('sortOperationLogInstTab', sortObj);
          }
        }
      ],
      priority: 10
    });
  }
];
