package org.eximeebpms.spin.groovy.json.tree

jsonNode = S(input, "application/json");

nodeList = jsonNode.jsonPath('$.customers[*].name').elementList();