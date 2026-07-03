<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getScriptSecurityConfig"
      tag = "Script Security"
      summary = "Get Script Security Configuration"
      desc = "Returns the current script security configuration: enforcement mode and the allowlisted
              process definition keys.
              Requires the user to have the **eximeebpms-admin** role or explicit admin authorization." />

  "parameters" : [],

  "responses" : {

    <@lib.response
        code = "200"
        dto = "ScriptSecurityConfigDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET /script-security/config",
                       "value": {
                         "mode": "AUDIT",
                         "allowlistedKeys": ["invoiceProcess", "legacyMigration"]
                       }
                     }'] />

    <@lib.response
        code = "403"
        dto = "ExceptionDto"
        last = true
        desc = "Returned if the current user is not authorized to manage script security." />

  }
}
</#macro>
