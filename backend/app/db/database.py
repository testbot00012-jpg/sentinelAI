import os
from motor.motor_asyncio import AsyncIOMotorClient

DATABASE_URL = os.getenv("DATABASE_URL", "mongodb+srv://adamalok79_db_user:kondabot@cluster0.wjo1gru.mongodb.net/?appName=Cluster0")

client = AsyncIOMotorClient(DATABASE_URL)
database = client.sentinel_db

def get_db():
    return database
