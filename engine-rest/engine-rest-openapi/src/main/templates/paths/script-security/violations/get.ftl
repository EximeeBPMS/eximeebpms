<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getScriptViolations"
      tag = "Script Security"
      summary = "Get Script Violations"
      desc = "Returns a list of recent script security violations recorded by the engine.
              Requires the user to have the **eximeebpms-admin** role or explicit admin authorization." />

  "parameters" : [

    <#assign last = true />
    <#include "/lib/commons/pagination-params.ftl" >

  ],

  "responses" : {

    <@lib.response
        code = "200"
        dto = "ScriptViolationEventDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET /script-security/violations?firstResult=0&maxResults=10",
                       "value": [
                         {
                           "timestamp": "2026-06-26T10:00:00Z",
                           "processDefinitionKey": "myProcess",
                           "activityId": "serviceTask1",
                           "language": "javascript",
                           "sourceType": "INLINE_SOURCE",
                           "origin": "USER",
                           "ruleCode": "SCRIPT_SECURITY_SYSTEM_GETENV",
                           "reason": "Access to environment variables is forbidden"
                         }
                       ]
                     }'] />

    <@lib.response
        code = "403"
        dto = "ExceptionDto"
        last = true
        desc = "Returned if the current user is not authorized to access script violation data." />

  }
}
</#macro>
