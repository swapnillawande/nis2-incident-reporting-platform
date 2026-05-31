import psycopg2
from faker import Faker
import random
import os

SEED_DATA = os.getenv("SEED_DATA", "false").lower() == "true"

if not SEED_DATA:
    print("🚫 Seeding disabled")
    exit(0)

fake = Faker()

conn = psycopg2.connect(
    host="postgres",
    database="nis2_platform",
    user="postgres",
    password="postgres",
    port=5432
)

conn.autocommit = False
cursor = conn.cursor()

# Your real enum roles from RoleName
ROLE_NAMES = ["ADMIN", "SECURITY_ANALYST", "COMPLIANCE_OFFICER", "AUDITOR"]

try:
    print("🚀 Starting seeding process...")

    # -----------------------------
    # 1. CREATE USERS
    # -----------------------------
    user_ids = []

    for _ in range(30):
        cursor.execute("""
            INSERT INTO users (full_name, email, password_hash, status, created_at, updated_at)
            VALUES (%s, %s, %s, %s, NOW(), NOW())
            RETURNING id
        """, (
            fake.name(),
            fake.unique.email(),
            "hashed_password",
            "ACTIVE"
        ))

        user_id = cursor.fetchone()[0]
        user_ids.append(user_id)

    print(f"✅ Created {len(user_ids)} users")

    # -----------------------------
    # 2. INSERT USER ROLES (FIXED PART)
    # -----------------------------
    for user_id in user_ids:
        # each user gets 1–2 roles randomly
        assigned_roles = random.sample(ROLE_NAMES, k=random.randint(1, 2))

        for role in assigned_roles:
            cursor.execute("""
                INSERT INTO user_roles (user_id, role_name)
                VALUES (%s, %s)
            """, (user_id, role))

    print("✅ Assigned roles to users")
    # -----------------------------
    # SUCCESS COMMIT
    # -----------------------------
    conn.commit()
    print("🎉 Seeding completed successfully!")

except Exception as e:
    conn.rollback()
    print("❌ ERROR OCCURRED → ROLLBACK EXECUTED")
    print(str(e))

finally:
    cursor.close()
    conn.close()
    print("🔒 Connection closed")