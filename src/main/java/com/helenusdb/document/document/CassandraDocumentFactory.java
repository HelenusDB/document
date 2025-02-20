package com.helenusdb.document.document;

import java.nio.ByteBuffer;
import java.sql.Date;

import com.datastax.oss.driver.api.core.cql.Row;
import com.helenusdb.core.Identifiable;
import com.helenusdb.core.Identifier;
import com.helenusdb.core.exception.InvalidIdentifierException;
import com.helenusdb.document.document.DocumentSchemaProvider.Columns;
import com.helenusdb.document.exception.KeyDefinitionException;
import com.helenusdb.document.key.KeyDefinition;
import com.strategicgains.noschema.document.AbstractDocumentFactory;
import com.strategicgains.noschema.document.Document;
import com.strategicgains.noschema.document.ObjectCodec;

public class CassandraDocumentFactory<T extends Identifiable>
extends AbstractDocumentFactory<T>
{
	private KeyDefinition keys;

	public CassandraDocumentFactory(KeyDefinition keys, ObjectCodec<T> codec)
	{
		super(codec);
		setKeyDefinition(keys);
	}

	public CassandraDocumentFactory(ObjectCodec<T> codec, KeyDefinition keys)
	{
		super(codec);
		setKeyDefinition(keys);
	}

	public Document<T> asDocument(Row row)
	{
		if (row == null)
		{
			return null;
		}

		Document<T> d = new Document<>();
		ByteBuffer b = row.getByteBuffer(Columns.OBJECT);

		if (b != null && b.hasArray())
		{
			//Force the reading of all the bytes.
			d.setObject((b.array()));
		}

		d.setType(row.getString(Columns.TYPE));
		d.setMetadata(row.getMap(Columns.METADATA, String.class, String.class));
		d.setCreatedAt(new Date(row.getInstant(Columns.CREATED_AT).getEpochSecond()));
		d.setUpdatedAt(new Date(row.getInstant(Columns.UPDATED_AT).getEpochSecond()));
		return d;
	}

	private void setKeyDefinition(KeyDefinition keys)
	{
		this.keys = keys;
	}

	@Override
	protected Identifier extractIdentifier(T entity)
	throws InvalidIdentifierException, KeyDefinitionException
	{
		return keys.identifier(entity);
	}
}
