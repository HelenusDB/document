package com.helenusdb.papyrus.key.builder;

import java.util.function.UnaryOperator;

import com.helenusdb.papyrus.key.ClusteringKeyComponent;

public class ClusteringKeyComponentBuilder
extends AbstractKeyComponentBuilder
{
	private ClusteringKeyComponent keyComponent;

	public ClusteringKeyComponentBuilder(KeyDefinitionBuilder parent, ClusteringKeyComponent component)
	{
		super(parent);
		this.keyComponent = component;
	}

	public ClusteringKeyComponent getKeyComponent()
	{
		return keyComponent;
    }

	public ClusteringKeyComponentBuilder withExtractor(UnaryOperator<Object> extractor)
	{
		keyComponent.extractor(extractor);
		return this;
	}
}
