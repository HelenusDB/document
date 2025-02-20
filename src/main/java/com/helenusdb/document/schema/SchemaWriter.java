package com.helenusdb.document.schema;

public interface SchemaWriter<T>
{
	void ensureTables();
	void dropTables();
}
