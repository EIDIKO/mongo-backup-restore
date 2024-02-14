mongodump --host localhost --port 27017  --db dump-db --out C:\Users\tonda\Downloads\mongodb-tools
mongorestore --host localhost --port 27017 --db dump-db C:\Users\tonda\Downloads\mongodb-tools\dump-db
mongorestore --host localhost --port 27017 --db dump-test C:\Users\tonda\Downloads\mongodb-tools\dump-db
