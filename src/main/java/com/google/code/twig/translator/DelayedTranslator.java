package com.google.code.twig.translator;

import java.lang.reflect.Type;
import java.util.Set;

import com.google.code.twig.Path;
import com.google.code.twig.Property;
import com.google.code.twig.PropertyTranslator;
import com.google.code.twig.util.SinglePropertySet;
import com.vercer.util.reference.ObjectReference;
import com.vercer.util.reference.ReadOnlyObjectReference;

public class DelayedTranslator extends DecoratingTranslator
{

	public DelayedTranslator(PropertyTranslator chained)
	{
		super(chained);
	}

	public Object decode(Set<Property> properties, Path path, Type type)
	{
		return chained.decode(properties, path, type);
	}

	public Set<Property> encode(final Object object, final Path path, final boolean indexed)
	{
		ObjectReference<Object> reference = new ReadOnlyObjectReference<Object>()
		{
			public Object get()
			{
				Set<Property> properties = chained.encode(object, path, indexed);
				return properties.iterator().next().getValue();
			}
		};
		return new SinglePropertySet(path, reference, indexed);
	}

}
