package com.eidiko.mongo;
import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class MongoDBReader {

    public static void main(String[] args) {
        // Connect to MongoDB server running on localhost
        MongoClient mongoClient = MongoClients.create(new ConnectionString("mongodb://localhost:27017"));

        // Get the database
        MongoDatabase database = mongoClient.getDatabase("dump-dev");

        // Get the collection
        MongoCollection<Document> collection = database.getCollection("dump-col");

        // Find all documents in the collection
        MongoCursor<Document> cursor = collection.find().iterator();

        // Iterate over the documents
        try {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                System.out.println(doc.toJson());
            }
        } finally {
            cursor.close();
        }

        // Close the MongoDB connection
        mongoClient.close();
    }
}
