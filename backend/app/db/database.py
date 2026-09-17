import os
from dotenv import load_dotenv
from motor.motor_asyncio import AsyncIOMotorClient

load_dotenv()

DATABASE_URL = os.getenv("DATABASE_URL", "mongodb+srv://<db_username>:ZWjW7Eidrlz3TwEn@cluster0.nwf6du7.mongodb.net/?appName=Cluster0")

client = AsyncIOMotorClient(DATABASE_URL)
database = client.sentinel_db

async def get_db():
    return database
