package com.helenusdb.document.unitofwork;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.helenusdb.transact.exception.UnitOfWorkCommitException;
import com.helenusdb.transact.exception.UnitOfWorkRollbackException;

public interface UnitOfWorkCommitStrategy
{
	CompletableFuture<?> commit(List<BoundStatement> statements)
	throws UnitOfWorkCommitException;

	void rollback()
	throws UnitOfWorkRollbackException;
}
