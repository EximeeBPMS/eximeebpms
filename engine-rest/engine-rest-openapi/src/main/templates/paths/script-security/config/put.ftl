<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "updateScriptSecurityConfig"
      tag = "Script Security"
      summary = "Update Script Security Configuration"
      desc = "Updates the script security configuration at runtime. Changes take effect immediately
              without an engine restart.
              Requires the user to have the **eximeebpms-admin** role or explicit admin authorization." />

  "parameters" : [],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "ScriptSecurityConfigDto"
      examples = ['"example-1": {
                     "summary": "PUT /script-security/config",
                     "description": "Switch to ENFORCE mode and allowlist one process",
                     "value": {
                       "mode": "ENFORCE",
                       "allowlistedKeys": ["trustedProcess"]
                     }
                   }'] />

  "responses" : {

    <@lib.response
        code = "200"
        dto = "ScriptSecurityConfigDto"
        desc = "Request successful. Returns the updated configuration."
        examples = ['"example-1": {
                       "summary": "Status 200 Response",
                       "value": {
                         "mode": "ENFORCE",
                         "allowlistedKeys": ["trustedProcess"]
                       }
                     }'] />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if the provided mode value is invalid." />

    <@lib.response
        code = "403"
        dto = "ExceptionDto"
        last = true
        desc = "Returned if the current user is not authorized to manage script security." />

  }
}
</#macro>
