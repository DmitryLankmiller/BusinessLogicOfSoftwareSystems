SHOP_IP=1301903
API_KEY=test_IEOqnyv6X31GfBM7BXi8hiTDnfDCbBLoLbbhx8KSHxo

# Without capture:
curl https://api.yookassa.ru/v3/payments \
  -X POST \
  -u 1301903:test_IEOqnyv6X31GfBM7BXi8hiTDnfDCbBLoLbbhx8KSHxo \
  -H 'Idempotence-Key: 987654678' \
  -H 'Content-Type: application/json' \
  -d '{
        "amount": {
          "value": "2.00",
          "currency": "RUB"
        },
        "capture": false,
        "payment_method_data": {
          "type": "bank_card",
          "card": {
            "csc": "213",
            "expiry_month": "01",
            "expiry_year": "2030",
            "number": "2202474301322987"
          }
        },
        "confirmation": {
          "type": "redirect",
          "return_url": "https://www.example.com/return_url"
        },
        "description": "Заказ №72"
      }'


curl https://api.yookassa.ru/v3/payments/{payment_id}/capture \
  -X POST \
  -u <Идентификатор магазина>:<Секретный ключ> \
  -H 'Idempotence-Key: <Ключ идемпотентности>' \
  -H 'Content-Type: application/json'

# With capture:
curl https://api.yookassa.ru/v3/payments \
  -X POST \
  -u <Идентификатор магазина>:<Секретный ключ> \
  -H 'Idempotence-Key: <Ключ идемпотентности>' \
  -H 'Content-Type: application/json' \
  -d '{
        "amount": {
          "value": "2.00",
          "currency": "RUB"
        },
        "capture": true,
        "payment_method_data": {
          "type": "bank_card",
          "card": {
            "cardholder": "MR CARDHOLDER",
            "csc": "213",
            "expiry_month": "01",
            "expiry_year": "2030",
            "number": "5555555555554477"
          }
        },
        "confirmation": {
          "type": "redirect",
          "return_url": "https://www.example.com/return_url"
        },
        "description": "Заказ №72"
      }'
