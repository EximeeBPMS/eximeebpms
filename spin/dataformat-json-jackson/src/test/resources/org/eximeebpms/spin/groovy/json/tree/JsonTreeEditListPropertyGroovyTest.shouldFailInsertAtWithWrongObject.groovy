package org.eximeebpms.spin.groovy.json.tree

node = S(input, "application/json");
customers = node.prop("customers");
customers.insertAt(1, new Date());
