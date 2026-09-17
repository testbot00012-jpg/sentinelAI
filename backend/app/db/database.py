import os
from dotenv import load_dotenv
from motor.motor_asyncio import AsyncIOMotorClient

load_dotenv()

DATABASE_URL = os.getenv(
    "DATABASE_URL",
    "mongodb+srv://testbot00012_db_user:ZWjW7Eidrlz3TwEn@cluster0.nwf6du7.mongodb.net/sentinel_db?appName=Cluster0&retryWrites=true&w=majority"
)

if "<db_username>" in DATABASE_URL or "cluster0.nwf6du7.mongodb.net" in DATABASE_URL:
    DATABASE_URL = "mongodb+srv://testbot00012_db_user:ZWjW7Eidrlz3TwEn@cluster0.nwf6du7.mongodb.net/sentinel_db?appName=Cluster0&retryWrites=true&w=majority"

client = AsyncIOMotorClient(
    DATABASE_URL,
    serverSelectionTimeoutMS=8000,
    connectTimeoutMS=8000
)
database = client.sentinel_db

async def get_db():
    return database
