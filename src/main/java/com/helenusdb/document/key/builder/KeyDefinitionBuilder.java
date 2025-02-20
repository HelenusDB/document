package com.helenusdb.document.key.builder;

import com.helenusdb.document.exception.KeyDefinitionException;
import com.helenusdb.document.key.ClusteringKeyComponent;
import com.helenusdb.document.key.DataTypes;
import com.helenusdb.document.key.KeyComponent;
import com.helenusdb.document.key.KeyDefinition;
import com.helenusdb.document.key.ClusteringKeyComponent.Ordering;

/**
 * Entry point for building KeyDefinitions using a fluent API.
 */
public class KeyDefinitionBuilder
{
	private KeyDefinition keyDefinition = new KeyDefinition();

	public KeyDefinitionBuilder()
	{
		super();
    }

	public KeyDefinitionBuilder(boolean isUnique)
	{
		super();
		keyDefinition.setUnique(isUnique);
	}

	public KeyComponentBuilder withPartitionKey(String partitionKey)
	throws KeyDefinitionException
	{
		KeyComponentBuilder builder = new KeyComponentBuilder(this, KeyComponent.parse(partitionKey));
		keyDefinition.addPartitionKey(builder.getKeyComponent());
		return builder;
	}

	public KeyComponentBuilder withPartitionKey(String columnName, DataTypes type)
	{
		KeyComponentBuilder builder = new KeyComponentBuilder(this, new KeyComponent(columnName, type));
		keyDefinition.addPartitionKey(builder.getKeyComponent());
		return builder;
	}

	public KeyComponentBuilder withPartitionKey(String columnName, String propertyName, DataTypes type)
	{
		KeyComponentBuilder builder = new KeyComponentBuilder(this, new KeyComponent(columnName, propertyName, type));
		keyDefinition.addPartitionKey(builder.getKeyComponent());
		return builder;
	}

	public ClusteringKeyComponentBuilder withClusteringKey(String clusteringKey)
	throws KeyDefinitionException
	{
		ClusteringKeyComponentBuilder builder = new ClusteringKeyComponentBuilder(this, ClusteringKeyComponent.parse(clusteringKey));
		keyDefinition.addClusteringKey(builder.getKeyComponent());
		return builder;
	}

	public ClusteringKeyComponentBuilder withClusteringKey(String columnName, DataTypes type, Ordering ordering)
	{
		ClusteringKeyComponentBuilder builder = new ClusteringKeyComponentBuilder(this, new ClusteringKeyComponent(columnName, type, ordering));
		keyDefinition.addClusteringKey(builder.getKeyComponent());
		return builder;
	}

	public ClusteringKeyComponentBuilder withClusteringKey(String columnName, String propertyName, DataTypes type, Ordering ordering)
	{
		ClusteringKeyComponentBuilder builder = new ClusteringKeyComponentBuilder(this, new ClusteringKeyComponent(columnName, propertyName, type, ordering));
		keyDefinition.addClusteringKey(builder.getKeyComponent());
		return builder;
	}

	public KeyDefinitionBuilder isUnique()
	{
		keyDefinition.setUnique(true);
		return this;
	}

	public KeyDefinitionBuilder notUnique()
	{
		keyDefinition.setUnique(false);
		return this;
	}

	public KeyDefinition build()
	{
		return keyDefinition;
	}
}
