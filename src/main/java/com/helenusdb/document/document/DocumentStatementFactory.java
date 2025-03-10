package com.helenusdb.document.document;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.helenusdb.core.Identifiable;
import com.helenusdb.core.Identifier;
import com.helenusdb.core.exception.InvalidIdentifierException;
import com.helenusdb.document.AbstractTable;
import com.helenusdb.document.CqlStatementFactory;
import com.helenusdb.document.PrimaryTable;
import com.helenusdb.document.document.DocumentSchemaProvider.Columns;
import com.helenusdb.document.exception.KeyDefinitionException;
import com.strategicgains.noschema.exception.InvalidObjectIdException;

public final class DocumentStatementFactory<T extends Identifiable>
implements CqlStatementFactory<T>
{
	private static final String SELECT_COLUMNS = String.join(",", Columns.OBJECT, Columns.TYPE, Columns.METADATA, Columns.CREATED_AT, Columns.UPDATED_AT);
	private static final String CREATE_CQL = "insert into %s.%s (%s, %s, %s, %s, %s, %s) values (%s)";
	private static final String DELETE_CQL = "delete from %s.%s where %s";
	private static final String EXISTS_CQL = "select count(*) from %s.%s  where %s limit 1";
	private static final String READ_CQL = "select " + SELECT_COLUMNS + " from %s.%s where %s limit 1";
	private static final String READ_ALL_CQL = "select " + SELECT_COLUMNS + " from %s.%s where %s";
	private static final String UPDATE_CQL = "update %s.%s set %s = ?, %s = ?, %s = ?, %s = ? where %s";

	// These are used IFF there is a single primary table (with no views) and it is unique.
	private static final String DELETE_UNIQUE_CQL = DELETE_CQL + " if exists";
	private static final String CREATE_UNIQUE_CQL = CREATE_CQL + " if not exists";
	private static final String UPDATE_UNIQUE_CQL = UPDATE_CQL + " if exists";

	private static final String CREATE = "create";
	private static final String DELETE = "delete";
	private static final String EXISTS = "exists";
	private static final String READ = "read";
	private static final String READ_ALL = "readAll_";
	private static final String UPDATE = "update";
	private static final String UPSERT = "upsert";

	private CqlSession session;
	private AbstractTable table;
	private Map<String, PreparedStatement> statements = new ConcurrentHashMap<>();
	private CassandraDocumentFactory<T> documentFactory;
	private boolean useLightweightTxns;

	public DocumentStatementFactory(CqlSession session, AbstractTable table, ObjectCodec<T> codec)
	{
		this(session, table, new CassandraDocumentFactory<>(table.keys(), codec));
	}

	public DocumentStatementFactory(CqlSession session, AbstractTable table, CassandraDocumentFactory<T> factory)
	{
		super();
		this.session = session;
		this.table = table;
		this.documentFactory = factory;

		if ((table instanceof PrimaryTable primary) && !primary.hasViews())
		{
			this.useLightweightTxns = true;
		}
	}

	private PreparedStatement prepareCreate()
	{
		String cql = useLightweightTxns ? CREATE_UNIQUE_CQL : CREATE_CQL;
		return statements.computeIfAbsent(CREATE, k -> 
			session.prepare(
				String.format(cql,
					table.keyspace(),
					table.asTableName(),
					table.keys().asSelectProperties(),
					Columns.OBJECT,
					Columns.TYPE,
					Columns.METADATA,
					Columns.CREATED_AT,
					Columns.UPDATED_AT,
					table.keys().asQuestionMarks(5)))
		);
	}

	private PreparedStatement prepareDelete()
	{
		String cql = useLightweightTxns ? DELETE_UNIQUE_CQL : DELETE_CQL;
		return statements.computeIfAbsent(DELETE, k ->
			session.prepare(
				String.format(cql,
					table.keyspace(),
					table.asTableName(),
					table.keys().asIdentityClause()))
		);
	}

	private PreparedStatement prepareExists()
	{
		return statements.computeIfAbsent(EXISTS, k -> 
			session.prepare(
				String.format(EXISTS_CQL,
					table.keyspace(),
					table.asTableName(),
					table.keys().asIdentityClause()))
		);
	}

	private PreparedStatement prepareUpdate()
	{
		String cql = useLightweightTxns ? UPDATE_UNIQUE_CQL : UPDATE_CQL;
		return statements.computeIfAbsent(UPDATE, k -> 
			session.prepare(
				String.format(cql,
					table.keyspace(),
					table.asTableName(),
					Columns.OBJECT,
					Columns.TYPE,
					Columns.METADATA,
					Columns.UPDATED_AT,
					table.keys().asIdentityClause()))
		);
	}

	private PreparedStatement prepareUpsert()
	{
		return statements.computeIfAbsent(UPSERT, k -> 
		session.prepare(
			String.format(CREATE_CQL,
				table.keyspace(),
				table.asTableName(),
				table.keys().asSelectProperties(),
				Columns.OBJECT,
				Columns.TYPE,
				Columns.METADATA,
				Columns.CREATED_AT,
				Columns.UPDATED_AT,
				table.keys().asQuestionMarks(5)))
		);
	}

	private PreparedStatement prepareRead()
	{
		return statements.computeIfAbsent(READ, k -> 
		session.prepare(
			String.format(READ_CQL,
				table.keyspace(),
				table.asTableName(),
				table.keys().asIdentityClause()))
		);
	}

	private PreparedStatement prepareReadAll(int keyCount)
	{
		return statements.computeIfAbsent(READ_ALL + keyCount, k -> 
		session.prepare(
			String.format(READ_ALL_CQL,
				table.keyspace(),
				table.asTableName(),
				table.keys().asIdentityClause(keyCount)))
		);
	}

	@Override
	public BoundStatement create(T entity)
	{
		return bindCreate(prepareCreate(), entity);
	}

	@Override
	public BoundStatement delete(Identifier id)
	{
		return bindIdentity(prepareDelete(), id);
	}

	@Override
	public BoundStatement exists(Identifier id)
	{
		return bindIdentity(prepareExists(), id);
	}

	@Override
	public BoundStatement update(T entity)
	{
		return bindUpdate(prepareUpdate(), entity);
	}

	@Override
	public BoundStatement upsert(T entity)
	{
		return bindCreate(prepareUpsert(), entity);
	}

	@Override
	public BoundStatement read(Identifier id)
	{
		return bindIdentity(prepareRead(), id);
	}

	@Override
	public BoundStatement readAll(Object... parameters)
	{
		return prepareReadAll(parameters.length).bind(parameters);
	}

	protected BoundStatement bindIdentity(PreparedStatement bs, Identifier id)
	{
		return bs.bind(id.components().toArray());
	}

	protected BoundStatement bindCreate(PreparedStatement ps, T entity)
	{
		Document<T> document = asDocument(entity);
		Date now = new Date();
		document.setCreatedAt(now);
		document.setUpdatedAt(now);
		Identifier id = document.getIdentifier();
		Object[] values = new Object[id.size() + 5]; // Identifier + object + metadata + createdAt + updatedAt.
		fill(values, 0, id.components().toArray());
		fill(values, id.size(),
			(document.hasObject() ? ByteBuffer.wrap(document.getObject()) : ByteBuffer.wrap(new byte[0])),
				document.getType(),
				document.getMetadata(),
				document.getCreatedAt().toInstant(),
			    document.getUpdatedAt().toInstant());
		return ps.bind(values);
	}

	protected BoundStatement bindUpdate(PreparedStatement ps, T entity)
	{
		Document<T> document = asDocument(entity);
		document.setUpdatedAt(new Date());
		Identifier id = document.getIdentifier();
		Object[] values = new Object[id.size() + 4];
			fill(values, 0,
				(document.hasObject() ? ByteBuffer.wrap(document.getObject()) : ByteBuffer.wrap(new byte[0])),
					document.getType(),
					document.getMetadata(),
				    document.getUpdatedAt().toInstant());
			fill(values, 4, id.components().toArray());
			return ps.bind(values);
	}

	private void fill(Object[] array, int offset, Object... values)
	{
		for (int i = offset; i < values.length + offset; i++)
		{
			array[i] = values[i - offset];
		}
	}

	@SuppressWarnings("unchecked")
	private Document<T> asDocument(T entity)
	{
		if (entity instanceof Document<?>) return (Document<T>) entity;

		try
		{
			return documentFactory.asDocument(entity);
		}
		catch (InvalidIdentifierException | KeyDefinitionException e)
		{
			throw new InvalidObjectIdException(e);
		}
	}
}
