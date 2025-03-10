package com.helenusdb.document.document;

import com.helenusdb.document.AbstractTable;
import com.helenusdb.document.key.KeyDefinition;
import com.helenusdb.document.schema.AbstractSchemaProvider;

public class DocumentSchemaProvider
extends AbstractSchemaProvider
{
	public static class Columns
	{
		public static final String OBJECT = "object";
		public static final String TYPE = "type";
		public static final String METADATA = "metadata";
		public static final String CREATED_AT = "created_at";
		public static final String UPDATED_AT = "updated_at";

		private Columns()
		{
			// prevents instantiation.
		}
	}

	private static final String DROP_TABLE = "drop table if exists %s.%s;";
	private static final String CREATE_TABLE = "create table if not exists %s.%s" +
	"(" +
		"%s," +									// identifying properties
	    Columns.OBJECT + " blob," +
		Columns.TYPE + " text," +
	    Columns.METADATA + " map<text,text>," +
	    // Add Location details, if needed, to Document.
	    // Add Lucene index capability if needed to Document.
		Columns.CREATED_AT + " timestamp," +
	    Columns.UPDATED_AT + " timestamp," +
		"%s" +									// primary key
	")" +
	" %s";										// clustering order (optional)

	private String keyspace;
	private String table;
	private KeyDefinition keys;

	public DocumentSchemaProvider(AbstractTable table)
	{
		super();
		this.keyspace = table.keyspace();
		this.table = table.asTableName();
		this.keys = table.keys();
	}

	@Override
	public String asCreateScript()
	{
		return String.format(CREATE_TABLE, keyspace, table, keys.asColumns(), keys.asPrimaryKey(), keys.asClusteringKey());
	}

	@Override
	public String asDropScript()
	{
		return String.format(DROP_TABLE, keyspace, table);
	}
}
