package org.eximeebpms.spin.groovy.json.tree

jsonNode = S(input, "application/json");

jsonNode.jsonPath('$.active').numberValue();