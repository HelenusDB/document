package com.helenusdb.papyrus.schema;

public interface SchemaWriter<T>
{
	void ensureTables();
	void dropTables();
}
