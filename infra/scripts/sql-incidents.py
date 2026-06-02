import psycopg2
import random
import time
from faker import Faker
import os


from datetime import datetime, timedelta

SEED_DATA = os.getenv("SEED_DATA", "false").lower() == "true"

if not SEED_DATA:
    print("🚫 Seeding disabled")
    exit(0)



DB_CONFIG = {
    "host": "postgres",
    "database": "nis2_platform",
    "user": "postgres",
    "password": "postgres",
    "port": 5432
}

conn = psycopg2.connect(**DB_CONFIG)
conn.autocommit = False
cursor = conn.cursor()

if os.getenv("DELETE_DATA", "false").lower() == "true":
    print("🧹 Clearing incidents...")

    cursor.execute("""
        TRUNCATE TABLE incidents RESTART IDENTITY CASCADE;
    """)

    print("✅ Incidents cleared")
fake = Faker()

# MUST match your Java enums EXACTLY
SEVERITIES = ["LOW", "MEDIUM", "HIGH", "CRITICAL"]

STATUSES = ["OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED"]

ANALYSTS = [
    "alice@nisync.com",
    "bob@nisync.com",
    "charlie@nisync.com",
    "diana@nisync.com"
]

TITLES = [
    "Multiple failed login attempts detected",
    "API latency spike observed",
    "Database connection pool exhaustion",
    "Unauthorized access attempt blocked",
    "High CPU usage on service",
    "Memory leak detected in auth service",
    "Suspicious outbound traffic detected",
    "Service restart in production cluster",
    "Kafka lag increasing rapidly",
    "Potential DDoS pattern detected"
]

DESCRIPTIONS = [
    "Security system detected abnormal authentication behavior.",
    "Observability stack reports degraded system performance.",
    "Error rates exceeded acceptable thresholds.",
    "Infrastructure metrics show instability.",
    "Network traffic anomaly detected requiring investigation.",
    "Service health checks failing intermittently."
]


def connect():
    for _ in range(10):
        try:
            conn = psycopg2.connect(**DB_CONFIG)
            print("✅ Connected to PostgreSQL")
            return conn
        except Exception as e:
            print("⏳ Waiting for DB...")
            print(e)
    raise Exception("DB connection failed")


def generate_due_date(severity):
    now = datetime.utcnow()

    # SLA logic (real SOC behavior)
    if severity == "CRITICAL":
        return now + timedelta(minutes=15)
    elif severity == "HIGH":
        return now + timedelta(hours=4)
    elif severity == "MEDIUM":
        return now + timedelta(hours=24)
    else:
        return now + timedelta(days=3)


def main():
    conn = connect()
    cur = conn.cursor()

    try:
        print("🚀 Starting incident seeding...")

        insert_query = """
            INSERT INTO incidents (
                title,
                description,
                severity,
                status,
                reported_by_email,
                assigned_to_email,
                due_at,
                created_at,
                updated_at
            )
            VALUES (%s, %s, %s, %s, %s, %s, %s, NOW(), NOW())
        """

        incidents = []

        # 30 incidents guaranteed coverage
        for _ in range(30):
            severity = random.choice(SEVERITIES)

            incidents.append((
                random.choice(TITLES),
                random.choice(DESCRIPTIONS),
                severity,
                random.choice(STATUSES),
                fake.email(),
                random.choice(ANALYSTS),
                generate_due_date(severity)
            ))

        cur.executemany(insert_query, incidents)
        conn.commit()

        print(f"✅ Inserted {len(incidents)} incidents successfully!")
        print("🎉 Seeding completed!")

    except Exception as e:
        conn.rollback()
        print("❌ ERROR OCCURRED → ROLLBACK EXECUTED")
        print(str(e))

    finally:
        cur.close()
        conn.close()
        print("🔒 Connection closed")


if __name__ == "__main__":
    main()