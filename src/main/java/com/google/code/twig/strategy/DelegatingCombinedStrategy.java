package com.google.code.twig.strategy;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

public class DelegatingCombinedStrategy implements CombinedStrategy
{
	private final CombinedStrategy delegate;

	public DelegatingCombinedStrategy(CombinedStrategy delegate)
	{
		this.delegate = delegate;
	}

	public int activationDepth(Field field, int depth)
	{
		return delegate.activationDepth(field, depth);
	}

	public boolean parent(Field field)
	{
		return delegate.parent(field);
	}

	public String name(Field field)
	{
		return delegate.name(field);
	}

	public boolean child(Field field)
	{
		return delegate.child(field);
	}

	public boolean embed(Field field)
	{
		return delegate.embed(field);
	}

	public String typeToKind(Type type)
	{
		return delegate.typeToKind(type);
	}

	public boolean key(Field field)
	{
		return delegate.key(field);
	}

	public Type kindToType(String kind)
	{
		return delegate.kindToType(kind);
	}

	public boolean entity(Field field)
	{
		return delegate.entity(field);
	}

	public Type typeOf(Field field)
	{
		return delegate.typeOf(field);
	}

	public boolean index(Field field)
	{
		return delegate.index(field);
	}

	public boolean store(Field field)
	{
		return delegate.store(field);
	}

	public boolean polymorphic(Field field)
	{
		return delegate.polymorphic(field);
	}
}
