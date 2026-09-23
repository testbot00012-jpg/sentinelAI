import asyncio
from motor.motor_asyncio import AsyncIOMotorClient

async def main():
    client = AsyncIOMotorClient("mongodb+srv://testbot00012_db_user:ZWjW7Eidrlz3TwEn@cluster0.nwf6du7.mongodb.net/sentinel_db?appName=Cluster0&retryWrites=true&w=majority")
    db = client["sentinel_db"]
    docs = await db["url_scans"].find({}).to_list(10)
    print(f"Total in url_scans: {len(docs)}")
    for d in docs:
        print(d.get("_id"), repr(d.get("user_email")), repr(d.get("user_id")), d.get("url"))

    th = await db["threat_logs"].find({}).to_list(10)
    print(f"Total in threat_logs: {len(th)}")
    for t in th:
        print(t.get("_id"), repr(t.get("user_email")), repr(t.get("user_id")), t.get("threat_type"))

if __name__ == "__main__":
    asyncio.run(main())
