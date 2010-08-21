package com.google.code.twig.translator;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import com.google.code.twig.Path;
import com.google.code.twig.Property;
import com.google.code.twig.PropertyTranslator;
import com.google.code.twig.conversion.TypeConverter;
import com.google.code.twig.util.PropertySets;
import com.google.code.twig.util.PropertySets.PrefixPropertySet;
import com.google.code.twig.util.SimpleProperty;
import com.google.code.twig.util.generic.GenericTypeReflector;
import com.vercer.util.Reflection;
import com.vercer.util.collections.MergeSet;

/**
 * @author John Patterson <john@vercer.com>
 *
 */
public abstract class ObjectFieldTranslator implements PropertyTranslator
{
	private static final Comparator<Field> comparator = new Comparator<Field>()
	{
		public int compare(Field o1, Field o2)
		{
			return o1.getName().compareTo(o2.getName());
		}
	};
	private final TypeConverter converters;

	// permanent cache of class fields to reduce reflection
	private static Map<Class<?>, List<Field>> classFields = new ConcurrentHashMap<Class<?>, List<Field>>();
	private static Map<Class<?>, Constructor<?>> constructors = new ConcurrentHashMap<Class<?>, Constructor<?>>();

	public ObjectFieldTranslator(TypeConverter converters)
	{
		this.converters = converters;
	}

	public final Object decode(Set<Property> properties, Path path, Type type)
	{
		if (properties.size() == 1)
		{
			Property property = PropertySets.firstProperty(properties);
			if (property.getValue() == null && property.getPath().equals(path))
			{
				return NULL_VALUE;
			}
		}
		
		// create the instance
		Class<?> clazz = GenericTypeReflector.erase(type);
		Object instance = createInstance(clazz);
		
		// ensure the properties are sorted
		if (properties instanceof SortedSet<?> == false)
		{
			properties = new TreeSet<Property>(properties);
		}

		// both fields and properties are sorted by name
		List<Field> fields = getSortedFields(instance);
		Iterator<PrefixPropertySet> ppss = PropertySets.prefixPropertySets(properties, path).iterator();
		PrefixPropertySet pps = null;
		for (Field field : fields)
		{
			if (stored(field))
			{
				String name = fieldToPartName(field);
				Path fieldPath = new Path.Builder(path).field(name).build();
				
				// handle missing class fields by ignoring the properties
				while (ppss.hasNext() && (pps == null || pps.getPrefix().compareTo(fieldPath) < 0))
				{
					pps = ppss.next();
				}
				
				// if there are no properties for the field we must still
				// run a translator because some translators do not require
				// any fields to set a field value e.g. KeyTranslator
				Set<Property> childProperties;
				if (pps == null || !fieldPath.equals(pps.getPrefix()))
				{
					// there were no properties for this field
					childProperties = Collections.emptySet();
				}
				else
				{
					childProperties = pps.getProperties();
				}

				decode(instance, field, fieldPath, childProperties);
			}
		}
		
		return instance;
	}

	protected void decode(Object instance, Field field, Path path, Set<Property> properties)
	{
		// get the correct translator for this field
		PropertyTranslator translator = decoder(field, properties);

		// get the type that we need to store
		Type fieldType = typeFromField(field);

		onBeforeDecode(field, properties);

		// create instance
		Object value;
		try
		{
			value = translator.decode(properties, path, fieldType);
		}
		catch (Exception e)
		{
			// add a bit of context to the trace
			throw new IllegalStateException("Problem translating field " + field + " with properties " + properties, e);
		}

		if (value == null)
		{
			throw new IllegalStateException("Could not translate path " + path);
		}

		if (value == NULL_VALUE)
		{
			value = null;
		}
		
		setFieldValue(instance, field, value);
		
		onAfterDecode(field, value);
	}

	private void setFieldValue(Object instance, Field field, Object value)
	{
		// check for a default implementations of collections and reuse
		if (Collection.class.isAssignableFrom(field.getType()))
		{
			try
			{
				// see if there is a default value
				Collection<?> existing = (Collection<?>) field.get(instance);
				if (existing != null && value!= null && existing.getClass() != value.getClass())
				{
					// make sure the value is a list - could be a blob
					value = converters.convert(value, ArrayList.class);
					
					existing.clear();
					typesafeAddAll((Collection<?>) value, existing);
					return;
				}
			}
			catch (Exception e)
			{
				throw new IllegalStateException(e);
			}
		}
		else if (Map.class.isAssignableFrom(field.getType()))
		{
			try
			{
				// see if there is a default value
				Map<?, ?> existing = (Map<?, ?>) field.get(instance);
				if (existing != null && value!= null && existing.getClass() != value.getClass())
				{
					// make sure the value is a map - could be a blob
					value = converters.convert(value, HashMap.class);
					
					existing.clear();
					typesafePutAll((Map<?, ?>) value, existing);
					return;
				}
			}
			catch (Exception e)
			{
				throw new IllegalStateException(e);
			}
		}

		// the stored type may not be the same as the declared type
		// due to the ability to define what type to store an instance
		// as using FieldTypeStrategy.type(Field) or @Type annotation
		
		// convert value to actual field type before setting
		value = converters.convert(value, field.getGenericType());
		
		try
		{
			field.set(instance, value);
		}
		catch (Exception e)
		{
			throw new IllegalStateException("Could not set value " + value + " to field " + field, e);
		}
	}

	@SuppressWarnings("unchecked")
	private <K, V> void typesafePutAll(Map<?, ?> value, Map<?, ?> existing)
	{
		((Map<K, V>) existing).putAll((Map<K, V>) value);
	}

	@SuppressWarnings("unchecked")
	private <T> void typesafeAddAll(Collection<?> value, Collection<?> existing)
	{
		((Collection<T>) existing).addAll((Collection<T>) value);
	}


	protected void onAfterDecode(Field field, Object value)
	{
	}

	protected void onBeforeDecode(Field field, Set<Property> childProperties)
	{
	}

	protected String fieldToPartName(Field field)
	{
		return field.getName();
	}

	protected Type typeFromField(Field field)
	{
		return field.getType();
	}

	protected Object createInstance(Class<?> clazz)
	{
		try
		{
			Constructor<?> constructor = getNoArgsConstructor(clazz);
			return constructor.newInstance();
		}
		catch (NoSuchMethodException e)
		{
			throw new IllegalArgumentException("Could not find no args constructor in " + clazz, e);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Could not construct instance of " + clazz, e);
		}
	}

	private Constructor<?> getNoArgsConstructor(Class<?> clazz) throws NoSuchMethodException
	{
		Constructor<?> constructor = constructors.get(clazz);
		if (constructor == null)
		{
			// use no-args constructor
			constructor = clazz.getDeclaredConstructor();
	
			// allow access to private constructor
			if (!constructor.isAccessible())
			{
				constructor.setAccessible(true);
			}
			
			constructors.put(clazz, constructor);
		}
		return constructor;
	}

	public final Set<Property> encode(Object object, Path path, boolean indexed)
	{
		onBeforeEncode(path, object);
		if (object == null)
		{
			return Collections.emptySet();
		}

		try
		{
			List<Field> fields = getSortedFields(object);
			MergeSet<Property> merged = new MergeSet<Property>(fields.size());
			for (Field field : fields)
			{
				if (stored(field))
				{
					// get the type that we need to store
					Type type = typeFromField(field);

					Path childPath = new Path.Builder(path).field(fieldToPartName(field)).build();

					// we may need to convert the object if it is not assignable
					Object value = field.get(object);
					if (value == null)
					{
						if (isNullStored())
						{
							merged.add(new SimpleProperty(childPath, null, indexed(field)));
						}
						continue;
					}

					value = converters.convert(value, type);

					onBeforeEncode(field, value);
					
					PropertyTranslator translator = encoder(field, value);
					Set<Property> properties = translator.encode(value, childPath, indexed(field));
					if (properties == null)
					{
						throw new IllegalStateException("Could not translate value to properties: " + value);
					}
					merged.addAll(properties);
					
					onAfterEncode(field, properties);
				}
			}

			onAfterEncode(path, merged);
			
			return merged;
		}
		catch (IllegalAccessException e)
		{
			throw new IllegalStateException(e);
		}
	}

	protected void onAfterEncode(Path path, Set<Property> properties)
	{
	}

	protected void onBeforeEncode(Path path, Object object)
	{
	}

	protected void onAfterEncode(Field field, Set<Property> properties)
	{
	}

	protected void onBeforeEncode(Field field, Object value)
	{
	}

	private List<Field> getSortedFields(Object object)
	{
		// fields are cached and stored as a map because reading more common than writing
		List<Field> fields = classFields.get(object.getClass());
		if (fields == null)
		{
			fields = Reflection.getAccessibleFields(object.getClass());

			// sort the fields by name
			Collections.sort(fields, comparator);

			// cache because reflection is costly
			classFields.put(object.getClass(), fields);
		}
		return fields;
	}

	protected abstract boolean isNullStored();

	protected abstract boolean indexed(Field field);

	protected abstract boolean stored(Field field);

	protected abstract PropertyTranslator encoder(Field field, Object instance);
	
	protected abstract PropertyTranslator decoder(Field field, Set<Property> properties);

}
