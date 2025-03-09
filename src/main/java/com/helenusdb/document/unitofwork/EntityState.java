package com.helenusdb.document.unitofwork;

public enum EntityState
{
	CLEAN,
	NEW,
	DIRTY,
	DELETED,
	UNKNOWN
}
