"""
Seed the BusTracking database with realistic Ghana-focused data.

Requirements:
  pip install faker bcrypt mysql-connector-python

Environment variables:
  DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD
  or DATABASE_URL=mysql://user:pass@host:3306/dbname
  SEED_PASSWORD (optional, defaults to SmartBus@123)
"""

from __future__ import annotations

import os
import random
from dataclasses import dataclass
from datetime import date, datetime, time, timedelta
from decimal import Decimal

import bcrypt
import mysql.connector
from faker import Faker

fake = Faker("en_GB")
random.seed(42)
Faker.seed(42)

GHANA_FIRST_NAMES = [
    "Kwame", "Kwesi", "Kofi", "Kojo", "Yaw", "Kwaku", "Kwabena", "Ama", "Akosua", "Adwoa",
    "Abena", "Yaa", "Efua", "Esi", "Nana", "Kobby", "Yawson", "Afi", "Mansa", "Afia",
]
GHANA_LAST_NAMES = [
    "Mensah", "Owusu", "Asante", "Boateng", "Osei", "Agyemang", "Addo", "Appiah", "Sarpong",
    "Antwi", "Nyarko", "Ankomah", "Boadu", "Bonsu", "Darko", "Acheampong",
]

CORRIDORS = [
    ("Circle", "Tema"),
    ("Circle", "Madina"),
    ("Circle", "Adenta"),
    ("Circle", "Weija"),
    ("Circle", "Amasaman"),
    ("Circle", "Ashaiman"),
    ("Kaneshie", "Weija"),
    ("Kaneshie", "Amasaman"),
    ("Kaneshie", "Dome"),
    ("Madina", "Legon"),
    ("Madina", "Adenta"),
    ("Tema", "Spintex"),
    ("Tema", "Ashaiman"),
    ("Achimota", "Haatso"),
    ("Achimota", "Pokuase"),
    ("Lapaz", "Awoshie"),
    ("Lapaz", "Odorkor"),
    ("East Legon", "Legon"),
    ("Dzorwulu", "Airport"),
]

STOPS = [
    ("Circle", 5.5600, -0.2057),
    ("Tudu", 5.5481, -0.2055),
    ("Ridge", 5.5669, -0.1870),
    ("Osu", 5.5560, -0.1800),
    ("Labone", 5.5620, -0.1680),
    ("Airport", 5.6050, -0.1710),
    ("Dzorwulu", 5.6100, -0.2100),
    ("37 Military Hospital", 5.5760, -0.1825),
    ("Kaneshie", 5.5740, -0.2450),
    ("Bubiashie", 5.5840, -0.2590),
    ("Odorkor", 5.5870, -0.2720),
    ("Awoshie", 5.5924, -0.2745),
    ("Mallam Junction", 5.5584, -0.3100),
    ("Weija Junction", 5.5795, -0.3395),
    ("Weija", 5.5810, -0.3350),
    ("Lapaz", 5.6032, -0.2623),
    ("Achimota", 5.6308, -0.2307),
    ("Achimota Overhead", 5.6315, -0.2290),
    ("Tesano", 5.6200, -0.2350),
    ("Abelenkpe", 5.6260, -0.2190),
    ("North Industrial Area", 5.6150, -0.2500),
    ("Haatso", 5.6780, -0.2140),
    ("Dome", 5.6650, -0.2200),
    ("Kwabenya", 5.7000, -0.2400),
    ("Pokuase", 5.6830, -0.2850),
    ("Ofankor", 5.6740, -0.2730),
    ("Amasaman", 5.7020, -0.3000),
    ("Taifa", 5.6710, -0.2570),
    ("Legon", 5.6500, -0.1870),
    ("East Legon", 5.6505, -0.1500),
    ("Madina Zongo Junction", 5.6817, -0.1775),
    ("Madina", 5.6830, -0.1720),
    ("Adenta Barrier", 5.7002, -0.1671),
    ("Adenta", 5.7050, -0.1630),
    ("Spintex", 5.6030, -0.0900),
    ("Baatsona", 5.6200, -0.0800),
    ("Sakumono", 5.6250, -0.0600),
    ("Teshie", 5.5830, -0.1000),
    ("Nungua", 5.6010, -0.0700),
    ("Tema Station", 5.5458, -0.2011),
    ("Tema Community 1", 5.6250, -0.0100),
    ("Tema Community 25", 5.6400, 0.0000),
    ("Ashaiman", 5.7000, -0.0300),
    ("Trade Fair", 5.5660, -0.2620),
    ("Dansoman", 5.5780, -0.3000),
]

CORRIDOR_STOPS = {
    ("Circle", "Tema"): ["Circle", "37 Military Hospital", "Airport", "Spintex", "Tema Station"],
    ("Circle", "Madina"): ["Circle", "37 Military Hospital", "Legon", "Madina Zongo Junction"],
    ("Circle", "Adenta"): ["Circle", "Legon", "Madina Zongo Junction", "Adenta Barrier"],
    ("Circle", "Weija"): ["Circle", "Kaneshie", "Mallam Junction", "Weija Junction"],
    ("Circle", "Amasaman"): ["Circle", "Kaneshie", "Lapaz", "Amasaman"],
    ("Circle", "Ashaiman"): ["Circle", "37 Military Hospital", "Spintex", "Ashaiman"],
    ("Kaneshie", "Weija"): ["Kaneshie", "Odorkor", "Mallam Junction", "Weija Junction"],
    ("Kaneshie", "Amasaman"): ["Kaneshie", "Lapaz", "Awoshie", "Amasaman"],
    ("Kaneshie", "Dome"): ["Kaneshie", "Achimota", "Dome", "Pokuase"],
    ("Madina", "Legon"): ["Madina Zongo Junction", "Legon", "East Legon"],
    ("Madina", "Adenta"): ["Madina Zongo Junction", "Adenta Barrier"],
    ("Tema", "Spintex"): ["Tema Station", "Tema Community 1", "Spintex"],
    ("Tema", "Ashaiman"): ["Tema Station", "Tema Community 25", "Ashaiman"],
    ("Achimota", "Haatso"): ["Achimota", "Haatso", "Legon"],
    ("Achimota", "Pokuase"): ["Achimota", "Dome", "Ofankor", "Pokuase"],
    ("Lapaz", "Awoshie"): ["Lapaz", "Awoshie", "Odorkor"],
    ("Lapaz", "Odorkor"): ["Lapaz", "Odorkor"],
    ("East Legon", "Legon"): ["East Legon", "Legon"],
    ("Dzorwulu", "Airport"): ["Dzorwulu", "Airport", "37 Military Hospital"],
}


@dataclass
class Ctx:
    conn: mysql.connector.MySQLConnection
    cur: mysql.connector.cursor.MySQLCursor


def connect():
    url = os.getenv("DATABASE_URL")
    if url:
        try:
            from urllib.parse import urlparse
            result = urlparse(url)
            return mysql.connector.connect(
                host=result.hostname,
                port=result.port or 3306,
                user=result.username,
                password=result.password,
                database=result.path.lstrip('/')
            )
        except Exception:
            pass
            
    return mysql.connector.connect(
        host=os.getenv("DB_HOST", "localhost"),
        port=int(os.getenv("DB_PORT", "3306")),
        database=os.getenv("DB_NAME", "bus_tracking"),
        user=os.getenv("DB_USER", "root"),
        password=os.getenv("DB_PASSWORD", "mosh584"),
    )


def hash_password(shared_password: str) -> str:
    return bcrypt.hashpw(shared_password.encode("utf-8"), bcrypt.gensalt(rounds=12)).decode("utf-8")


def seed_users(ctx: Ctx, total_passengers=1200, total_drivers=220):
    shared_password = os.getenv("SEED_PASSWORD", "SmartBus@123")
    hashed = hash_password(shared_password)

    users = [("admin@smartbus.gh", "System", "Admin", "0240000000", "ADMIN", "ACTIVE", hashed)]
    for i in range(total_drivers):
        fn = random.choice(GHANA_FIRST_NAMES)
        ln = random.choice(GHANA_LAST_NAMES)
        users.append((f"driver{i+1}@smartbus.gh", fn, ln, fake.numerify("024#######"), "DRIVER", "ACTIVE", hashed))
    for i in range(total_passengers):
        fn = random.choice(GHANA_FIRST_NAMES)
        ln = random.choice(GHANA_LAST_NAMES)
        status = random.choices(["ACTIVE", "INACTIVE", "SUSPENDED"], weights=[92, 6, 2])[0]
        users.append((f"passenger{i+1}@smartbus.gh", fn, ln, fake.numerify("02########"), "PASSENGER", status, hashed))

    now = datetime.utcnow()
    rows = [(u[0], u[1], u[2], u[3], u[4], u[5], u[6], now) for u in users]
    
    ctx.cur.executemany(
        """
        INSERT IGNORE INTO users (email, first_name, last_name, phone_number, role, status, password_hash, created_at)
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
        """,
        rows
    )

    ctx.cur.execute("SELECT id FROM users WHERE role='DRIVER' ORDER BY id")
    driver_ids = [r[0] for r in ctx.cur.fetchall()]
    ctx.cur.execute("SELECT id FROM users WHERE role='PASSENGER' ORDER BY id")
    passenger_ids = [r[0] for r in ctx.cur.fetchall()]
    return driver_ids, passenger_ids


def seed_stops(ctx: Ctx):
    rows = []
    now = datetime.utcnow()
    for name, lat, lng in STOPS:
        rows.append((name, Decimal(str(lat)), Decimal(str(lng)), f"{name} station", now))
    
    ctx.cur.executemany(
        """
        INSERT INTO stops (name, latitude, longitude, description, created_at)
        VALUES (%s, %s, %s, %s, %s)
        """,
        rows,
    )
    ctx.cur.execute("SELECT id, name, latitude, longitude FROM stops ORDER BY id")
    return ctx.cur.fetchall()


def seed_routes_and_route_stops(ctx: Ctx, stop_rows, total_routes=140):
    stop_id_by_name = {name: stop_id for stop_id, name, *_ in stop_rows}

    route_rows = []
    route_meta = []
    now = datetime.utcnow()

    for i in range(total_routes):
        origin, destination = random.choice(CORRIDORS)
        route_kind = random.choice(["Express", "Local", "Peak", "Feeder"])
        name = f"{origin} - {destination} {route_kind}"
        number = f"GA-{100 + i}"
        distance = round(random.uniform(4.0, 38.0), 2)
        duration = int(distance * random.uniform(2.4, 4.8))

        route_rows.append((number, name, Decimal(str(distance)), duration, now))
        route_meta.append((number, origin, destination))

    ctx.cur.executemany(
        """
        INSERT IGNORE INTO routes (number, name, distance_km, estimated_duration_minutes, created_at)
        VALUES (%s, %s, %s, %s, %s)
        """,
        route_rows,
    )

    ctx.cur.execute("SELECT id, number FROM routes ORDER BY id")
    route_id_by_number = {number: route_id for route_id, number in ctx.cur.fetchall()}

    junction_rows = []
    for number, origin, destination in route_meta:
        route_id = route_id_by_number.get(number)
        if not route_id:
            continue

        stop_names = CORRIDOR_STOPS.get((origin, destination), [origin, destination])
        for seq, stop_name in enumerate(stop_names, start=1):
            stop_id = stop_id_by_name.get(stop_name)
            if not stop_id:
                continue
            eta = seq * random.randint(6, 14)
            junction_rows.append((route_id, stop_id, seq, eta))

    ctx.cur.executemany(
        """
        INSERT INTO route_stops (route_id, stop_id, stop_sequence, estimated_arrival_minutes)
        VALUES (%s, %s, %s, %s)
        """,
        junction_rows,
    )

    return list(route_id_by_number.values())


def seed_buses(ctx: Ctx, driver_ids, total_buses=320):
    bus_rows = []
    now = datetime.utcnow()
    
    # Ensure every driver gets a bus to test driver flow
    assigned_drivers = random.sample(driver_ids, k=min(len(driver_ids), total_buses))
    
    for i in range(total_buses):
        region = "GR"
        reg = f"{region}-{random.randint(1000, 9999)}-{random.randint(21, 26)}"
        status = random.choices(["ACTIVE", "INACTIVE", "MAINTENANCE"], [85, 10, 5])[0]
        
        driver_id = assigned_drivers[i] if i < len(assigned_drivers) else None
        
        bus_rows.append((reg, random.randint(24, 65), "Yutong", random.choice(["ZK6118", "King Long", "Nissan Civilian"]), driver_id, status, now))

    ctx.cur.executemany(
        """
        INSERT IGNORE INTO buses (registration_number, capacity, make, model, driver_id, status, created_at)
        VALUES (%s, %s, %s, %s, %s, %s, %s)
        """,
        bus_rows,
    )
    ctx.cur.execute("SELECT id, driver_id FROM buses ORDER BY id")
    return ctx.cur.fetchall()


def seed_schedules(ctx: Ctx, route_ids, buses):
    active_buses = [b for b in buses if b[1] is not None]
    rows = []
    now = datetime.utcnow()
    # Create more schedules to ensure plenty of data
    for bus_id, _ in active_buses:
        effective = date.today() - timedelta(days=random.randint(1, 90))
        expiry = date.today() + timedelta(days=random.randint(15, 210))
        rows.append((bus_id, random.choice(route_ids), effective, expiry, "ACTIVE", now))
        
    ctx.cur.executemany(
        """
        INSERT INTO schedules (bus_id, route_id, effective_date, expiry_date, status, created_at)
        VALUES (%s, %s, %s, %s, %s, %s)
        """,
        rows,
    )
    ctx.cur.execute("SELECT id, bus_id FROM schedules ORDER BY id")
    schedule_data = ctx.cur.fetchall()
    schedule_ids = [r[0] for r in schedule_data]

    day_rows = []
    for sid in schedule_ids:
        # Every schedule gets at least 5 days
        for dow in random.sample(["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"], k=random.randint(5, 7)):
            dep_hour = random.randint(4, 20)
            dep = time(dep_hour, random.choice([0, 15, 30, 45]))
            arr_dt = datetime.combine(date.today(), dep) + timedelta(minutes=random.randint(30, 210))
            day_rows.append((sid, dow, dep, arr_dt.time()))
            
    ctx.cur.executemany(
        """
        INSERT INTO schedule_days (schedule_id, day, departure_time, arrival_time)
        VALUES (%s, %s, %s, %s)
        """,
        day_rows,
    )
    return schedule_data


def seed_trips_tickets_locations(ctx: Ctx, schedule_data, passenger_ids):
    # schedule_data is list of (id, bus_id)
    ctx.cur.execute("SELECT route_id, stop_id FROM route_stops ORDER BY route_id, stop_sequence")
    route_stops = {}
    for route_id, stop_id in ctx.cur.fetchall():
        route_stops.setdefault(route_id, []).append(stop_id)

    ctx.cur.execute("SELECT id, bus_id, route_id FROM schedules")
    full_schedules = ctx.cur.fetchall()

    trips = []
    now = datetime.utcnow()
    
    # Create trips for all schedules to ensure drivers have active content
    for schedule_id, bus_id, _ in full_schedules:
        # One completed trip yesterday
        dep_past = now - timedelta(days=1, hours=random.randint(1, 8))
        arr_past = dep_past + timedelta(minutes=random.randint(45, 120))
        tickets_sold = random.randint(10, 40)
        trips.append((schedule_id, bus_id, tickets_sold, float(tickets_sold * 12.5), "COMPLETED", dep_past, arr_past, dep_past))
        
        # One active trip now for 30% of buses
        if random.random() < 0.3:
            dep_now = now - timedelta(minutes=random.randint(5, 45))
            trips.append((schedule_id, bus_id, random.randint(5, 25), 0.0, "ACTIVE", dep_now, None, dep_now))
        
    ctx.cur.executemany(
        """
        INSERT INTO trips (schedule_id, bus_id, tickets_sold, revenue, status, actual_departure_time, actual_arrival_time, created_at)
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
        """,
        trips,
    )

    ticket_rows = []
    for _ in range(3000):
        schedule_id, _, route_id = random.choice(full_schedules)
        stops = route_stops.get(route_id, [])
        if len(stops) < 2:
            continue
        origin_idx = random.randint(0, len(stops) - 2)
        destination_idx = random.randint(origin_idx + 1, len(stops) - 1)
        travel_date = date.today() + timedelta(days=random.randint(-2, 7))
        status = random.choices(["PENDING", "PAID", "USED", "EXPIRED", "CANCELLED"], [10, 50, 20, 10, 10])[0]
        ticket_rows.append(
            (
                random.choice(passenger_ids),
                schedule_id,
                f"SB-{random.randint(100000, 999999)}",
                stops[origin_idx],
                stops[destination_idx],
                Decimal(str(round(random.uniform(8.0, 45.0), 2))),
                travel_date,
                time(hour=random.randint(4, 21), minute=random.choice([0, 30])),
                status,
                travel_date,
                random.choice(["CARD", "MOMO"]),
                fake.uuid4()[:20],
                now if status == "USED" else None,
                now,
            )
        )
        
    ctx.cur.executemany(
        """
        INSERT IGNORE INTO tickets (passenger_id, schedule_id, code, origin_stop_id, destination_stop_id, price, date, boarding_time, status, validity_date, payment_method, payment_reference, validated_at, created_at)
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
        """,
        ticket_rows,
    )

    bus_locs = []
    # Seed current locations for all active buses
    for schedule_id, bus_id, _ in full_schedules:
        # Ghana bounds
        base_lat = random.uniform(5.55, 5.68)
        base_lng = random.uniform(-0.25, -0.10)
        
        bus_locs.append(
            (
                bus_id,
                Decimal(str(round(base_lat, 8))),
                Decimal(str(round(base_lng, 8))),
                Decimal(str(round(random.uniform(10, 55), 2))),
                Decimal(str(round(random.uniform(0, 359), 2))),
                now,
                now,
            )
        )
            
    ctx.cur.executemany(
        """
        INSERT INTO bus_locations (bus_id, latitude, longitude, speed, heading, timestamp, created_at)
        VALUES (%s, %s, %s, %s, %s, %s, %s)
        """,
        bus_locs,
    )


def main():
    conn = connect()
    conn.autocommit = False
    cur = conn.cursor()
    ctx = Ctx(conn=conn, cur=cur)
    try:
        print("Cleaning up existing data...")
        cur.execute("SET FOREIGN_KEY_CHECKS = 0")
        tables = [
            "bus_locations", "tickets", "trips", "schedule_days", "schedules",
            "route_stops", "routes", "stops", "buses", "users"
        ]
        for table in tables:
            cur.execute(f"TRUNCATE TABLE {table}")
        cur.execute("SET FOREIGN_KEY_CHECKS = 1")
        
        print("Seeding users...")
        driver_ids, passenger_ids = seed_users(ctx)
        print("Seeding stops...")
        stop_rows = seed_stops(ctx)
        print("Seeding routes...")
        route_ids = seed_routes_and_route_stops(ctx, stop_rows)
        print("Seeding buses and assigning drivers...")
        buses = seed_buses(ctx, driver_ids)
        print("Seeding schedules...")
        schedule_data = seed_schedules(ctx, route_ids, buses)
        print("Seeding trips, tickets and locations...")
        seed_trips_tickets_locations(ctx, schedule_data, passenger_ids)
        
        conn.commit()
        print("\n" + "="*40)
        print("SEEDING COMPLETED SUCCESSFULLY")
        print("="*40)
        print(f"Drivers:    {len(driver_ids)}")
        print(f"Passengers: {len(passenger_ids)}")
        print(f"Shared PWD: {os.getenv('SEED_PASSWORD', 'SmartBus@123')}")
        print("="*40)
    except Exception as e:
        conn.rollback()
        print(f"ERROR: {e}")
        raise
    finally:
        cur.close()
        conn.close()


if __name__ == "__main__":
    main()
