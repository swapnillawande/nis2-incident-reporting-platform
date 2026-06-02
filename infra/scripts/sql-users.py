import psycopg2
from faker import Faker
import random
import os

SEED_DATA = os.getenv("SEED_DATA", "false").lower() == "true"
DELETE_DATA = os.getenv("DELETE_DATA", "false").lower() == "true"

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

ROLE_NAMES = ["ADMIN", "SECURITY_ANALYST", "COMPLIANCE_OFFICER", "AUDITOR"]

try:
    print("🚀 Starting seeding process...")

    # -----------------------------
    # 🔥 DELETE DATA (NEW PART)
    # -----------------------------
    if DELETE_DATA:
        print("🧹 DELETE_DATA enabled → clearing users...")

        cursor.execute("DELETE FROM user_roles;")
        cursor.execute("DELETE FROM users;")

        print("✅ Old user data cleared")

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

        user_ids.append(cursor.fetchone()[0])

    print(f"✅ Created {len(user_ids)} users")

    # -----------------------------
    # 2. ASSIGN ROLES
    # -----------------------------
    for user_id in user_ids:
        assigned_roles = random.sample(ROLE_NAMES, k=random.randint(1, 2))

        for role in assigned_roles:
            cursor.execute("""
                INSERT INTO user_roles (user_id, role_name)
                VALUES (%s, %s)
            """, (user_id, role))

    print("✅ Assigned roles to users")

    # -----------------------------
    # COMMIT
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