package com.helenusdb.document.unitofwork;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.helenusdb.core.Identifiable;
import com.helenusdb.core.Identifier;

/**
 * This class provides transactional context for managing database changes. It allows
 * registering new entities, marking entities as "dirty" or "deleted". Commit and
 * rollback are left for extenders of the class.
 */
public class UnitOfWorkChangeSet<T extends Identifiable>
{
	// This identity map is used to keep track of entities that have changed and
	// need to be persisted during the transaction.
	private Map<Identifier, EntityChanges<T>> changes = new HashMap<>();

	/**
	 * Returns a stream containing all the changed entities (excluding CLEAN).
	 */
	public Stream<Change<T>> stream()
	{
	    return changes.values().stream().flatMap(s -> s.asChange().stream());
	}

	public UnitOfWorkChangeSet<T> registerChange(Change<T> change)
	{
		EntityChanges<T> changeSet = getChangesFor(change.getEntity());
		changeSet.add(change);
		return this;
	}

    /**
     * Clears or deregisters all the previously-registered changes and resets the unit of work to it's initial, empty state.
     */
    public void reset()
	{
		changes.clear();
	}

	private EntityChanges<T> getChangesFor(T entity)
	{
		return changes.computeIfAbsent(entity.getIdentifier(), a -> new EntityChanges<>());
	}

	public T findClean(Identifier id)
	{
		EntityChanges<T> s = changes.get(id);

		if (s != null)
		{
			Change<T> change = s.get(EntityState.CLEAN);

			if (change != null) return change.getEntity();
		}

		return null;
	}
}
