<#macro dto_macro docsUrl="">
<@lib.dto
    desc = "Represents the script security configuration of the engine.">

    <@lib.property
        name = "mode"
        type = "string"
        enumValues = ['"AUDIT"', '"ENFORCE"']
        nullable = true
        desc = "The enforcement mode. **AUDIT** records violations but allows script execution.
                **ENFORCE** blocks script execution on a violation." />

    <@lib.property
        name = "allowlistedKeys"
        type = "array"
        itemType = "string"
        nullable = true
        last = true
        desc = "Process definition keys that are exempt from script security checks." />

</@lib.dto>
</#macro>
