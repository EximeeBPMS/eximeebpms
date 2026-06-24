package org.eximeebpms.spin.groovy.xml.dom
def map = [
  a:"http://eximeebpms.com"
]

query = S(input).xPath(expression).ns(map)
