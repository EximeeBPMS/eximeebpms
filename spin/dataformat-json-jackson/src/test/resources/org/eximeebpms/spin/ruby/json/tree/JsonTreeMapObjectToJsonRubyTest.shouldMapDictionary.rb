def customer(name, date)
  return {"name" => name, "contractStartDate" => date}
end

order = {"order" => "order1", "dueUntil" => 20150112, "id" => 1234567890987654321, "active" => true, "nullValue" => nil}

customers = [customer("Kermit", 1354539722), customer("Waldo", 1320325322), customer("Johnny", 1286110922)]
order["customers"] = customers

orderDetails = {"article" => "camundaBPM", "price" => 1234567.13, "roundedPrice" => 1234567, "currencies" => ["euro", "dollar"], "paid" => false}
order["orderDetails"] = orderDetails

$json = S(order, "application/json").toString()
