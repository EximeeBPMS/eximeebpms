<#macro dto_macro docsUrl="">
<@lib.dto
    desc = "Represents a single script security violation recorded by the engine.">

    <@lib.property
        name = "timestamp"
        type = "string"
        format = "date-time"
        nullable = true
        desc = "The time at which the violation occurred, in ISO 8601 format." />

    <@lib.property
        name = "processDefinitionKey"
        type = "string"
        nullable = true
        desc = "The key of the process definition in which the violation occurred.
                Null for deployment-time violations." />

    <@lib.property
        name = "activityId"
        type = "string"
        nullable = true
        desc = "The ID of the BPMN activity (e.g. service task) in which the violation occurred." />

    <@lib.property
        name = "language"
        type = "string"
        nullable = true
        desc = "The scripting language of the script that triggered the violation (e.g. javascript, groovy)." />

    <@lib.property
        name = "sourceType"
        type = "string"
        enumValues = ['"INLINE_SOURCE"', '"DYNAMIC_SOURCE"', '"RESOURCE"', '"DYNAMIC_RESOURCE"', '"EXPRESSION"', '"UNKNOWN"']
        nullable = true
        desc = "Describes how the script source was provided." />

    <@lib.property
        name = "origin"
        type = "string"
        enumValues = ['"USER"', '"PROCESS_APPLICATION"', '"PLATFORM"', '"UNKNOWN"']
        nullable = true
        desc = "Indicates who authored the script." />

    <@lib.property
        name = "ruleCode"
        type = "string"
        nullable = true
        desc = "The identifier of the security rule that was violated (e.g. SCRIPT_SECURITY_SYSTEM_GETENV)." />

    <@lib.property
        name = "reason"
        type = "string"
        nullable = true
        last = true
        desc = "Human-readable explanation of why the rule was violated." />

</@lib.dto>
</#macro>
