package com.vercer.engine.persist.standard;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.vercer.engine.persist.FindCommand.RootFindCommand;

final class StandardRootFindCommand<T> extends StandardTypedFindCommand<T, RootFindCommand<T>> implements RootFindCommand<T>
{
	private final Type type;
	private FetchOptions options;
	Object ancestor;

	class Sort
	{
		public Sort(String field, SortDirection direction)
		{
			super();
			this.direction = direction;
			this.field = field;
		}
		SortDirection direction;
		String field;
	}
	List<Sort> sorts;
	private boolean keysOnly;

	public StandardRootFindCommand(Type type, StrategyObjectDatastore datastore)
	{
		super(datastore);
		this.type = type;
	}

	@Override
	protected FetchOptions getFetchOptions()
	{
		return options;
	}

	public RootFindCommand<T> withAncestor(Object ancestor)
	{
		this.ancestor = ancestor;
		return this;
	}

	public RootFindCommand<T> fetchNoFields()
	{
		keysOnly = true;
		return this;
	}

	public RootFindCommand<T> addSort(String field)
	{
		return addSort(field, SortDirection.ASCENDING);
	}

	public RootFindCommand<T> continueFrom(Cursor cursor)
	{
		if (this.options == null)
		{
			this.options = FetchOptions.Builder.withCursor(cursor);
		}
		else
		{
			this.options.cursor(cursor);
		}
		return this;
	}

	public RootFindCommand<T>  fetchResultsBy(int size)
	{
		if (this.options == null)
		{
			this.options = FetchOptions.Builder.withChunkSize(size);
		}
		else
		{
			this.options.chunkSize(size);
		}
		return this;
	}

	public RootFindCommand<T> startFrom(int offset)
	{
		if (this.options == null)
		{
			this.options = FetchOptions.Builder.withOffset(offset);
		}
		else
		{
			this.options.offset(offset);
		}
		return this;
	}


	public RootFindCommand<T> addSort(String field, SortDirection direction)
	{
		if (this.sorts == null)
		{
			this.sorts = new ArrayList<Sort>(2);
		}
		this.sorts.add(new Sort(field, direction));
		return this;
	}

	public Future<QueryResultIterator<T>> returnResultsLater()
	{
		return futureSingleQueryInstanceIterator();
	}

	public int countResultsNow()
	{
		Collection<Query> queries = getValidatedQueries();
		if (queries.size() > 1)
		{
			throw new IllegalStateException("Too many queries");
		}

		Transaction txn = this.datastore.getTransaction();
		Query query = queries.iterator().next();
		PreparedQuery prepared = this.datastore.getService().prepare(txn, query);
		return prepared.countEntities();
	}
	
	public QueryResultIterator<T> returnResultsNow()
	{
		if (children == null)
		{
			return nowSingleQueryInstanceIterator();
		}
		else
		{
			try
			{
				final Iterator<T> result = this.<T>futureMultiQueryInstanceIterator().get();
				return new QueryResultIterator<T>()
				{
					public Cursor getCursor()
					{
						throw new IllegalStateException("Cannot use cursor with merged queries");
					}

					public boolean hasNext()
					{
						return result.hasNext();
					}

					public T next()
					{
						return result.next();
					}

					public void remove()
					{
						result.remove();
					}
				};
			}
			catch (Exception e)
			{
				if (e instanceof RuntimeException)
				{
					throw (RuntimeException) e;
				}
				else
				{
					throw (RuntimeException) e.getCause();
				}
			}
		}
	}

	@Override
	protected Query newQuery()
	{
		if (this.ancestor == null && this.datastore.getTransaction() != null)
		{
			throw new IllegalStateException("You must set an ancestor if you run a find this in a transaction");
		}

		Query query = new Query(datastore.typeToKind(type));
		applyFilters(query);
		if (sorts != null)
		{
			for (Sort sort : sorts)
			{
				query.addSort(sort.field, sort.direction);
			}
		}
		if (ancestor != null)
		{
			Key key = datastore.associatedKey(ancestor);
			if (key == null)
			{
				throw new IllegalArgumentException("Ancestor must be loaded in same session");
			}
			query.setAncestor(key);
		}
		if (keysOnly)
		{
			query.setKeysOnly();
		}
		return query;
	}
}