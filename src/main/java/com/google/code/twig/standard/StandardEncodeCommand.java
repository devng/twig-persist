package com.google.code.twig.standard;

import java.util.Collection;
import java.util.List;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.code.twig.Property;
import com.google.code.twig.util.Entities;
import com.vercer.util.reference.ObjectReference;

class StandardEncodeCommand extends StandardCommand
{
	StandardEncodeCommand(TranslatorObjectDatastore datastore)
	{
		super(datastore);
	}

	final Entity createEntity()
	{
		if (datastore.encodeKeySpec.isComplete())
		{
			// we have a complete key with id specified 
			return new Entity(datastore.encodeKeySpec.toKey());
		}
		else
		{
			// we have no id specified so must create entity for auto-generated id
			ObjectReference<Key> parentKeyReference = datastore.encodeKeySpec.getParentKeyReference();
			Key parentKey = parentKeyReference == null ? null : parentKeyReference.get();
			return Entities.createEntity(datastore.encodeKeySpec.getKind(), null, parentKey);
		}
	}

	final void transferProperties(Entity entity, Collection<Property> properties)
	{
		for (Property property : properties)
		{
			// dereference object references
			Object value = property.getValue();
			value = dereferencePropertyValue(value);

			if (property.isIndexed())
			{
				entity.setProperty(property.getPath().toString(), value);
			}
			else
			{
				entity.setUnindexedProperty(property.getPath().toString(), value);
			}
		}
	}

	final Object dereferencePropertyValue(Object value)
	{
		if (value instanceof ObjectReference<?>)
		{
			value = ((ObjectReference<?>)value).get();
		}
		else if (value instanceof List<?>)
		{
			// we know the value is a mutable list from ListTranslator
			@SuppressWarnings("unchecked")
			List<Object> values = (List<Object>) value;
			for (int i = 0; i < values.size(); i++)
			{
				Object item = values.get(i);
				if (item instanceof ObjectReference<?>)
				{
					// dereference the value and set it in-place
					Object dereferenced = ((ObjectReference<?>) item).get();
					values.set(i, dereferenced);  // replace the reference
				}
			}
		}
		return value;
	}
}
