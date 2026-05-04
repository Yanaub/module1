import argparse
import random
import uuid
from datetime import date, timedelta

import requests
from faker import Faker

BASE_URL = "http://localhost:8080"
fake = Faker("ru_RU")

def make_visitor():
    return {
        "identifier": str(uuid.uuid4()),
        "fullName": fake.name(),
        "age": random.randint(6, 80),
        "ticketType": random.choice(["FULL", "DISCOUNTED"]),
    }


def make_exhibit(used_names: set):
    epochs = [
        "Древний Египет", "Античная Греция", "Средние века",
        "Эпоха Возрождения", "XIX век", "XX век", "Современность",
    ]
    for _ in range(100):
        name = f"{fake.word().capitalize()} {fake.word()} №{random.randint(1, 999)}"
        if name not in used_names:
            used_names.add(name)
            return {
                "identifier": str(uuid.uuid4()),
                "name": name,
                "epoch": random.choice(epochs),
                "description": fake.sentence(nb_words=7),
            }
    raise RuntimeError("Не удалось сгенерировать уникальное имя экспоната")


def make_excursion():
    start = date(2023, 1, 1)
    random_days = random.randint(0, 730)
    return {
        "identifier": str(uuid.uuid4()),
        "date": (start + timedelta(days=random_days)).isoformat(),
        "guide": fake.name(),
    }

def post_one(endpoint: str, payload: dict) -> dict | None:
    url = f"{BASE_URL}/{endpoint}/"
    try:
        r = requests.post(url, json=payload, timeout=10)
        r.raise_for_status()
        return r.json()
    except requests.RequestException as e:
        print(f"Ошибка при создании записи: {e}")
        return None


def clear_endpoint(endpoint: str):
    url = f"{BASE_URL}/{endpoint}/clear"
    r = requests.delete(url, timeout=10)
    r.raise_for_status()

def seed_visitors(count: int):
    #clear_endpoint("excursions")
    #clear_endpoint("visitors")
    ok = 0
    for i in range(count):
        result = post_one("visitors", make_visitor())
        if result:
            ok += 1
    print(f"[visitors] Готово: создано {ok}/{count}")


def seed_exhibits(count: int):
    #clear_endpoint("excursions")
    #clear_endpoint("exhibits")
    used_names: set = set()
    ok = 0
    for i in range(count):
        result = post_one("exhibits", make_exhibit(used_names))
        if result:
            ok += 1
    print(f"[exhibits] Готово: создано {ok}/{count}")


def seed_excursions(count: int):
    #clear_endpoint("excursions")
    visitors_resp = requests.get(f"{BASE_URL}/visitors", timeout=10)
    exhibits_resp = requests.get(f"{BASE_URL}/exhibits", timeout=10)
    visitors_resp.raise_for_status()
    exhibits_resp.raise_for_status()

    visitors = visitors_resp.json()
    exhibits = exhibits_resp.json()

    if not visitors:
        print("Нет посетителей! Сначала запустите: --endpoint visitors")
        return
    if not exhibits:
        print("Нет экспонатов! Сначала запустите: --endpoint exhibits")
        return
    visitor_ids = [v["identifier"] for v in visitors]
    exhibit_ids = [e["identifier"] for e in exhibits]
    ok = 0
    for i in range(count):
        excursion = post_one("excursions", make_excursion())
        if not excursion:
            continue

        exc_id = excursion["identifier"]

        for vid in random.sample(visitor_ids, min(random.randint(2, 5), len(visitor_ids))):
            requests.post(
                f"{BASE_URL}/excursions/{exc_id}/visitors/{vid}", timeout=10
            )

        for eid in random.sample(exhibit_ids, min(random.randint(2, 4), len(exhibit_ids))):
            requests.post(
                f"{BASE_URL}/excursions/{exc_id}/exhibits/{eid}", timeout=10
            )

        ok += 1
    print(f"[excursions] Готово: создано {ok}/{count}")


def main():
    parser = argparse.ArgumentParser(description="Seed museum REST API with test data")
    parser.add_argument(
        "--count", type=int, default=500,
    )
    parser.add_argument(
        "--endpoint", required=True,
        choices=["visitors", "exhibits", "excursions", "clear","visitors-clear","exhibits-clear","excursions-clear"],
    )


    args = parser.parse_args()

    global BASE_URL
    BASE_URL = BASE_URL.rstrip("/")

    if args.endpoint == "visitors":
        seed_visitors(args.count)
    elif args.endpoint == "exhibits":
        seed_exhibits(args.count)
    elif args.endpoint == "excursions":
        seed_excursions(args.count)
    elif args.endpoint =="clear":
        clear_endpoint("excursions")
        clear_endpoint("visitors")
        clear_endpoint("exhibits")
    elif args.endpoint =="visitors-clear":
        clear_endpoint("excursions")
        clear_endpoint("visitors")
    elif args.endpoint =="exhibits-clear":
        clear_endpoint("excursions")
        clear_endpoint("exhibits")
    elif args.endpoint =="excursions-clear":
        clear_endpoint("excursions")


if __name__ == "__main__":
    main()