package com.helenusdb.document.unitofwork;

import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import com.helenusdb.core.Identifiable;

public class EntityChanges<T extends Identifiable>
{
	Map<EntityState, Change<T>> changes = new EnumMap<>(EntityState.class);

	public EntityChanges<T> add(Change<T> change)
	{
		changes.put(change.getState(), change);
		return this;
	}

	public Change<T> get(EntityState state)
	{
		return changes.get(state);
	}

	public Optional<Change<T>> asChange()
	{
		return changes.entrySet()
			.stream()
			.filter(e -> (e.getKey() != EntityState.CLEAN))
			.findFirst()
			.map(Entry::getValue);
	}
}
