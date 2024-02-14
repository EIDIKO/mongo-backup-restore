# Backup
mongodump --host localhost --port 27017  --db dump-db --out C:\Users\tonda\Downloads\mongodb-tools

# Restore
mongorestore --host localhost --port 27017 --db dump-db C:\Users\tonda\Downloads\mongodb-tools\dump-db

# Restore
mongorestore --host localhost --port 27017 --db dump-test C:\Users\tonda\Downloads\mongodb-tools\dump-db
