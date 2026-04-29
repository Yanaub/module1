from kafka import KafkaProducer
import json

producer = KafkaProducer(
    bootstrap_servers=["hl15.zil:9094"],
    value_serializer=lambda v: json.dumps(v).encode("utf-8")
)

TOPIC = "hl08"

messages = [
    {
        "entity": "EXCURSION",
        "operation": "POST",
        "payload": json.dumps({
            "identifier": "11111111-1111-1111-1111-111111111111",
            "date": "2026-04-29",
            "guide": "Ivan Ivanov"
        })
    },
    {
        "entity": "EXHIBIT",
        "operation": "POST",
        "payload": json.dumps({
            "identifier": "22222222-2222-2222-2222-222222222222",
            "name": "Golden Cup",
            "epoch": "XIX century",
            "description": "Ancient exhibit"
        })
    },
    {
        "entity": "VISITOR",
        "operation": "POST",
        "payload": json.dumps({
            "identifier": "33333333-3333-3333-3333-333333333333",
            "fullName": "Petr Petrov",
            "age": 24,
            "ticketType": "FULL"
        })
    },
    {
        "entity": "VISITOR",
        "operation": "DEL",
        "payload": json.dumps({
            "identifier": "33333333-3333-3333-3333-333333333333"
        })
    }
]

for msg in messages:
    result = producer.send(TOPIC, value=msg).get(timeout=10)
    print(f"sent to partition={result.partition}, offset={result.offset}, message={msg}")

producer.flush()
producer.close()