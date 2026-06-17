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

module.exports = [
  '$scope',
  '$q',
  'camAPI',
  'Notifications',
  'deploymentData',
  'deployment',
  '$translate',
  'configuration',
  function(
    $scope,
    $q,
    camAPI,
    Notifications,
    deploymentData,
    deployment,
    $translate,
    configuration
  ) {
    var Deployment = camAPI.resource('deployment');
    var ProcessInstance = camAPI.resource('process-instance');

    var deleteDeploymentData = deploymentData.newChild($scope);

    var SKIP_CUSTOM_LISTENERS = configuration.getSkipCustomListeners();
    var SKIP_IO_MAPPINGS = ($scope.SKIP_IO_MAPPINGS = configuration.getSkipIoMappings());
    var CASCADE = configuration.getCascade();

    var options = ($scope.options = {
      cascade: CASCADE.default,
      skipCustomListeners: SKIP_CUSTOM_LISTENERS.default,
      skipIoMappings: SKIP_IO_MAPPINGS.default
    });

    $scope.hideSkipCustomListeners = SKIP_CUSTOM_LISTENERS.hidden;

    $scope.deployment = deployment;
    $scope.status;

    $scope.$on('$routeChangeStart', function() {
      $scope.$dismiss();
    });

    // provide /////////////////////////////////////////////////////////

    deleteDeploymentData.provide('processInstanceCount', function() {
      var deferred = $q.defer();

      ProcessInstance.count(
        {
          deploymentId: deployment.id
        },
        function(err, res) {
          if (err) {
            // reject error but do not handle the error
            return deferred.reject(err);
          }

          deferred.resolve(res);
        }
      );

      return deferred.promise;
    });

    // observe /////////////////////////////////////////////////////////

    $scope.processInstanceCountState = deleteDeploymentData.observe(
      'processInstanceCount',
      function(count) {
        $scope.processInstanceCount = count;
      }
    );

    // delete deployment ///////////////////////////////////////////////

    $scope.countsLoaded = function() {
      return (
        $scope.processInstanceCountState &&
        ($scope.processInstanceCountState.$loaded ||
          $scope.processInstanceCountState.$error)
      );
    };

    var hasInstances = ($scope.hasInstances = function() {
      return (
        $scope.processInstanceCount && $scope.processInstanceCount.count > 0
      );
    });

    $scope.canDeleteDeployment = function() {
      return !options.cascade && hasInstances() ? false : true;
    };

    $scope.getInfoSnippet = function() {
      var info = [$translate.instant('REPOSITORY_DEPLOYMENTS_INFO_THERE_ARE')];

      if (
        $scope.processInstanceCount &&
        $scope.processInstanceCount.count > 0
      ) {
        info.push($scope.processInstanceCount.count);
        $scope.processInstanceCount && $scope.processInstanceCount.count > 1
          ? info.push(
              $translate.instant('REPOSITORY_DEPLOYMENTS_INFO_RUNNING_PLURAL')
            )
          : info.push(
              $translate.instant('REPOSITORY_DEPLOYMENTS_INFO_RUNNING_SINGULAR')
            );
      }

      if (
        $scope.processInstanceCount &&
        $scope.processInstanceCount.count > 0
      ) {
        info.push($translate.instant('REPOSITORY_DEPLOYMENTS_INFO_AND'));
      }

      info.push($translate.instant('REPOSITORY_DEPLOYMENTS_INFO_WHICH_BELONG'));
      info = info.join(' ');

      return info;
    };

    $scope.deleteDeployment = function() {
      $scope.status = 'PERFORM_DELETE';

      Deployment.delete(deployment.id, options, function(err) {
        $scope.status = null;

        if (err) {
          return Notifications.addError({
            status: $translate.instant(
              'REPOSITORY_DEPLOYMENTS_INFO_MSN_STATUS'
            ),
            message: $translate.instant('REPOSITORY_DEPLOYMENTS_INFO_MSN_MSN', {
              message: err.message
            }),
            exclusive: true
          });
        }

        $scope.$close();
      });
    };
  }
];
