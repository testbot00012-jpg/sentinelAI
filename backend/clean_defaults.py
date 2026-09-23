import asyncio
from motor.motor_asyncio import AsyncIOMotorClient

async def main():
    client = AsyncIOMotorClient("mongodb+srv://testbot00012_db_user:ZWjW7Eidrlz3TwEn@cluster0.nwf6du7.mongodb.net/sentinel_db?appName=Cluster0&retryWrites=true&w=majority")
    db = client["sentinel_db"]
    
    # 1. Remove legacy demo threats and legacy apk scans
    r1 = await db["threat_logs"].delete_many({"description": {"$regex": "phish-test.xyz", "$options": "i"}})
    r2 = await db["threat_logs"].delete_many({"user_email": "sec-operator@sentinel.ai"})
    r3 = await db["apk_scans"].delete_many({"user_email": "sec-operator@sentinel.ai"})
    print(f"Deleted legacy demo threat_logs: {r1.deleted_count + r2.deleted_count}, apk_scans: {r3.deleted_count}")
    
    # 2. Update the user phone scan of www.google.com to test@gmail.com
    r4 = await db["url_scans"].update_many(
        {"url": "www.google.com", "user_email": {"$in": ["", None, "anonymous"]}},
        {"$set": {"user_email": "test@gmail.com", "user_id": "user_test@gmail.com"}}
    )
    print(f"Updated user scan of www.google.com to test@gmail.com: {r4.modified_count}")

    # 3. Print remaining scans for test@gmail.com
    scans = await db["url_scans"].find({"user_email": "test@gmail.com"}).to_list(10)
    print(f"Current actual scans for test@gmail.com: {len(scans)}")
    for s in scans:
        print("  -", s.get("url"), s.get("status"), s.get("scanned_at"))

    threats = await db["threat_logs"].find({"user_email": "test@gmail.com"}).to_list(10)
    print(f"Current actual threats for test@gmail.com: {len(threats)}")
    for t in threats:
        print("  -", t.get("threat_type"), t.get("description"), t.get("detected_at"))

if __name__ == "__main__":
    asyncio.run(main())
