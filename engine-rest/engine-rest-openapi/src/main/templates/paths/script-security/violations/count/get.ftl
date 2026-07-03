<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getScriptViolationsCount"
      tag = "Script Security"
      summary = "Get Script Violations Count"
      desc = "Returns the total number of script security violations recorded since the engine started.
              Requires the user to have the **eximeebpms-admin** role or explicit admin authorization." />

  "parameters" : [],

  "responses" : {

    <@lib.response
        code = "200"
        dto = "CountResultDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET /script-security/violations/count",
                       "value": {
                         "count": 42
                       }
                     }'] />

    <@lib.response
        code = "403"
        dto = "ExceptionDto"
        last = true
        desc = "Returned if the current user is not authorized to access script violation data." />

  }
}
</#macro>
