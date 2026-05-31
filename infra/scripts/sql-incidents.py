import psycopg2
import random
import time
from faker import Faker
import os

SEED_DATA = os.getenv("SEED_DATA", "false").lower() == "true"

if not SEED_DATA:
    print("🚫 Seeding disabled")
    exit(0)

fake = Faker()

# =========================
# DB CONFIG (Docker-safe)
# =========================
DB_CONFIG = {
    "host": "postgres",
    "database": "nis2_platform",
    "user": "postgres",
    "password": "postgres",
    "port": 5432
}

# =========================
# MATCHING JAVA ENUMS
# =========================
SEVERITIES = ["LOW", "MEDIUM", "HIGH", "CRITICAL"]

STATUSES = [
    "OPEN",
    "IN_PROGRESS",
    "RESOLVED",
    "CLOSED"
]

# =========================
# INCIDENT TEMPLATES
# =========================
TITLES = [
    "Multiple failed login attempts detected",
    "Unusual spike in API latency",
    "Database connection pool exhaustion",
    "Unauthorized privilege escalation attempt",
    "Suspicious outbound network traffic",
    "High CPU usage on backend service",
    "Memory leak detected in authentication service",
    "Kafka consumer lag increasing rapidly",
    "Possible DDoS attack detected",
    "Unexpected service restart in production",
]

DESCRIPTIONS = [
    "Security monitoring detected abnormal authentication behavior.",
    "Observability stack reports degraded system performance.",
    "Repeated failures indicate potential brute-force attack.",
    "Infrastructure metrics show instability in backend services.",
    "Network anomalies detected requiring investigation.",
    "Tracing shows increased latency across services.",
    "Error rates exceeded safe thresholds.",
    "Resource utilization has spiked beyond limits.",
]

# =========================
# CONNECT WITH RETRY
# =========================
def connect():
    for i in range(15):
        try:
            conn = psycopg2.connect(**DB_CONFIG)
            print("✅ Connected to PostgreSQL")
            return conn
        except Exception as e:
            print(f"⏳ Waiting for DB... ({i+1}/15)")
            time.sleep(3)

    raise Exception("❌ DB connection failed after retries")

# =========================
# INCIDENT GENERATOR
# =========================
def generate_incident():
    return (
        random.choice(TITLES),
        random.choice(DESCRIPTIONS),
        random.choice(SEVERITIES),
        random.choice(STATUSES),
        fake.email()
    )

# =========================
# MAIN SEED LOGIC
# =========================
def main():
    conn = connect()
    cur = conn.cursor()

    try:
        # Safe insert query
        insert_query = """
            INSERT INTO incidents
            (title, description, severity, status, reported_by_email, created_at, updated_at)
            VALUES (%s, %s, %s, %s, %s, NOW(), NOW())
        """

        incidents = []

        # =========================
        # Generate 30 incidents
        # =========================
        for _ in range(30):
            incidents.append(generate_incident())

        # =========================
        # INSERT BATCH
        # =========================
        cur.executemany(insert_query, incidents)

        conn.commit()

        print(f"🚀 Successfully inserted {len(incidents)} incidents!")

    except Exception as e:
        conn.rollback()
        print("❌ ERROR OCCURRED → ROLLBACK EXECUTED")
        print(str(e))

    finally:
        cur.close()
        conn.close()

# =========================
# RUN
# =========================
if __name__ == "__main__":
    main()