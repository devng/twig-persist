package com.vercer.engine.persist;

import java.lang.reflect.Type;
import java.util.Set;

public interface PropertyTranslator
{
	// TODO pass in instance instead of type
	public Object propertiesToTypesafe(Set<Property> properties, Path path, Type type);
	
	// TODO use SortedSet? Could have optimised array based version which asserts order
	public Set<Property> typesafeToProperties(Object instance, Path path, boolean indexed);

	public static Object NULL_VALUE = new Object();
}
