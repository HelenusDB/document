package com.helenusdb.document.unitofwork;

import java.util.Objects;

import com.helenusdb.core.Identifiable;
import com.helenusdb.transact.Change;
import com.helenusdb.transact.EntityState;
import com.strategicgains.noschema.document.Document;

public class DocumentChange<T extends Identifiable>
extends Change<Document<T>>
{
	private String view;

	public DocumentChange(String view, Document<T> entity, EntityState state)
	{
		super(entity, state);
		this.view = view;
	}

	public String getView()
	{
		return view;
	}

	@Override
	public int hashCode()
	{
		return super.hashCode() + Objects.hash(view);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object that)
	{
		return super.equals(that) && Objects.equals(this.view, ((DocumentChange<T>) that).view);
	}
}
