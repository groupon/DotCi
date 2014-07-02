/*
The MIT License (MIT)

Copyright (c) 2014, Groupon, Inc.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */
package com.groupon.jenkins.mongo;

import com.google.common.base.Function;
import com.groupon.jenkins.SetupConfig;
import com.mongodb.*;
import org.bson.types.ObjectId;

import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

public abstract class MongoRepository {
	/**
	 * System.setProperty("DEBUG.MONGO", "true"); Enable DB operation tracing
	 * System.setProperty("DB.TRACE", "true");
	 * 
	 * @return {@link MongoClient}
	 */
	protected static MongoClient getClient() {
		try {
			return new MongoClient(SetupConfig.get().getDbHost(), SetupConfig.get().getDbPort());
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}

	protected final String collectionName;

	public MongoRepository(String collectionName) {
		this.collectionName = collectionName;
	}

	protected void save(DBObject doc) {
		MongoClient client = getClient();
		try {
			getCollection(client).insert(doc);
		} finally {
			client.close();
		}
	}

	protected void update(DBObject query, DBObject object) {
		MongoClient client = getClient();
		try {
			getCollection(client).update(query, object);
		} finally {
			client.close();
		}
	}
    protected void update(DBObject query, DBObject object,boolean upsert , boolean multi ) {
        MongoClient client = getClient();
        try {
            getCollection(client).update(query, object,upsert,multi);
        } finally {
            client.close();
        }
    }

	protected ObjectId saveOrUpdate(BasicDBObject query, BasicDBObject doc) {
		DBObject exisistingObject = findOne(query);
		if (exisistingObject == null) {
			save(doc);
			return (ObjectId) doc.get("_id");
		} else {
			update(query, doc);
			return (ObjectId) exisistingObject.get("_id");
		}
	}

	private DB getDB(MongoClient client) {
		return client.getDB(SetupConfig.get().getDbName());
	}

	protected <T> Iterable<T> findAll(Function<DBObject, T> transformer) {
		return find(null, transformer);
	}

	protected <T> Iterable<T> find(DBObject query, DBObject sort, Integer limit, Function<DBObject, T> transformer) {
		return find(query, null, sort, limit, transformer);
	}

	protected <T> Iterable<T> find(DBObject query, DBObject fields, DBObject sort, Integer limit, Function<DBObject, T> transformer) {
		MongoClient client = getClient();
		try {
			DBCursor cursor = fields == null ? getCollection(client).find(query) : getCollection(client).find(query, fields);
			if (sort != null) {
				cursor = cursor.sort(sort);
			}
			if (limit != null) {
				cursor = cursor.limit(limit);
			}
			List<T> result = new LinkedList<T>();

			try {
				while (cursor.hasNext()) {
					result.add(transformer.apply(cursor.next()));
				}
			} finally {
				cursor.close();
			}

			return result;
		} finally {
			client.close();
		}
	}

	protected <T> Iterable<T> find(DBObject query, Function<DBObject, T> transformer) {
		return find(query, null, null, transformer);
	}

	protected <T> T findOne(BasicDBObject query, Function<DBObject, T> transformer) {
		return findOne(query, null, null, transformer);
	}

	protected <T> T findOne(BasicDBObject query, DBObject fields, DBObject orderBy, Function<DBObject, T> transformer) {
		return findOne(query, fields, orderBy, transformer, null);
	}

	protected <T> T findOne(BasicDBObject query, DBObject fields, DBObject orderBy, Function<DBObject, T> transformer, ReadPreference readPreference) {
		DBObject object = findOne(query, fields, orderBy, readPreference);
		return object == null ? null : transformer.apply(object);
	}

	protected <T> T findOne(BasicDBObject query, DBObject orderBy, Function<DBObject, T> transformer) {
		return findOne(query, null, orderBy, transformer);
	}

	protected <T> T findOne(BasicDBObject query, DBObject orderBy, Function<DBObject, T> transformer, ReadPreference readPreference) {
		return findOne(query, null, orderBy, transformer, readPreference);
	}

	protected DBObject findOne(BasicDBObject query) {
		MongoClient client = getClient();
		try {
			DBCollection collection = getCollection(client);
			return collection.findOne(query);
		} finally {
			client.close();
		}
	}

	protected DBCollection getCollection(MongoClient client) {
		return getDB(client).getCollection(this.collectionName);
	}

	protected DBObject findOne(BasicDBObject query, DBObject fields, DBObject orderBy, ReadPreference readPreference) {
		MongoClient client = getClient();
		try {
			DBCollection collection = getCollection(client);
			return readPreference == null ? collection.findOne(query, fields, orderBy) : collection.findOne(query, fields, orderBy, readPreference);
		} finally {
			client.close();
		}
	}

	protected int size(BasicDBObject query) {
		MongoClient client = getClient();
		try {
			return getCollection(client).find(query).count();
		} finally {
			client.close();
		}
	}

	protected void delete(DBObject token) {
		MongoClient client = getClient();
		try {
			getCollection(client).remove(token);
		} finally {
			client.close();
		}
	}

}
