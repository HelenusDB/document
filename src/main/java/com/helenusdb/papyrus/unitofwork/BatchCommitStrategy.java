package com.helenusdb.papyrus.unitofwork;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.BatchStatementBuilder;
import com.datastax.oss.driver.api.core.cql.BatchType;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.helenusdb.diago.exception.UnitOfWorkCommitException;
import com.helenusdb.diago.exception.UnitOfWorkRollbackException;

public class BatchCommitStrategy
implements UnitOfWorkCommitStrategy
{
	private BatchType batchType = BatchType.LOGGED;
	private CqlSession session;

	public BatchCommitStrategy(CqlSession session)
	{
		this(BatchType.LOGGED, session);
	}

	public BatchCommitStrategy(BatchType batchType, CqlSession session)
	{
		super();
		this.batchType = batchType;
		this.session = session;
	}

	@Override
	public CompletableFuture<AsyncResultSet> commit(List<BoundStatement> statements)
	throws UnitOfWorkCommitException
	{
		BatchStatementBuilder batch = new BatchStatementBuilder(batchType);
		statements.forEach(batch::addStatement);
		CompletionStage<AsyncResultSet> resultSet = session.executeAsync(batch.build());

		return resultSet
			.exceptionally(t -> {
				throw new UnitOfWorkCommitException("Commit failed", t);
			})
			.toCompletableFuture();
	}

	@Override
	public void rollback()
	throws UnitOfWorkRollbackException
	{
		// No-op for CassandraUnitOfWork since we're using a logged batch
		throw new UnitOfWorkRollbackException("Not Implemented.");
	}
}
