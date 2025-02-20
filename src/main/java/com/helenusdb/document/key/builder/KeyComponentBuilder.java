package com.helenusdb.document.key.builder;

import java.util.function.UnaryOperator;

import com.helenusdb.document.key.KeyComponent;

public class KeyComponentBuilder
extends AbstractKeyComponentBuilder
{
	private KeyComponent keyComponent;

	public KeyComponentBuilder(KeyDefinitionBuilder parent, KeyComponent component)
	{
		super(parent);
		this.keyComponent = component;
	}

	public KeyComponent getKeyComponent()
	{
		return keyComponent;
    }

	public KeyComponentBuilder withExtractor(UnaryOperator<Object> extractor)
	{
		keyComponent.extractor(extractor);
		return this;
	}

	public KeyComponentBuilder withPartitionKey(String partitionKey)
	{
		return parent.withPartitionKey(partitionKey);
	}
}
