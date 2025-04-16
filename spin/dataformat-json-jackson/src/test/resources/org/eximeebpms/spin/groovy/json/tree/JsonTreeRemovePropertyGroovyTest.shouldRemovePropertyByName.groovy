package org.eximeebpms.spin.groovy.json.tree

node = S(input, "application/json")
node.deleteProp("order")
value = node.hasProp("order")