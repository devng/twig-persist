package com.google.code.twig;

import java.util.Iterator;
import java.util.List;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.QueryResultIterator;

public interface FindCommand
{
	enum MergeOperator { OR, AND };
	
	<T> RootFindCommand<T> type(Class<? extends T> type);

	interface RestrictedFindCommand<C extends RestrictedFindCommand<C>>
	{
		C restrictEntities(Restriction<Entity> restriction);
		C restrictProperties(Restriction<Property> restriction);
	}

	interface CommonFindCommand<C extends CommonFindCommand<C>> extends RestrictedFindCommand<C>
	{
		C addFilter(String field, FilterOperator operator, Object value);
		C addRangeFilter(String field, Object from, Object to);
		BranchFindCommand branch(MergeOperator operator);
	}

	interface BranchFindCommand
	{
		ChildFindCommand addChildCommand();
	}
	
	interface ChildFindCommand extends CommonFindCommand<ChildFindCommand>
	{
	}
	
	/**
	 * @author John Patterson <john@vercer.com>
	 *
	 * @param <T>
	 */
	interface RootFindCommand<T> extends CommonFindCommand<RootFindCommand<T>>, CommandTerminator<QueryResultIterator<T>>
	{
		// methods that have side effects
		
		/**
		 * Passed to {@link Query#addSort(String)}
		 * @param field The name of the class field to sort on
		 * @return <code>this</code> for method chaining
		 */
		RootFindCommand<T> addSort(String field);
		
		/**
		 * Passed to {@link Query#addSort(String, SortDirection)}
		 * @param field The name of the class field to sort on
		 * @param sort Direction of sort
		 * @return <code>this</code> for method chaining
		 */
		RootFindCommand<T> addSort(String field, SortDirection sort);
		
		/**
		 * @param ancestor Passed to {@link Query#setAncestor(com.google.appengine.api.datastore.Key)}
		 * @return <code>this</code> for method chaining
		 */
		RootFindCommand<T> ancestor(Object ancestor);
		
		/**
		 * @param offset Set as {@link FetchOptions#offset(int)}
		 * @return <code>this</code> for method chaining
		 */
		RootFindCommand<T> startFrom(int offset);
		
		/**
		 * @param cursor Set as {@link FetchOptions#startCursor(Cursor)}
		 * @return <code>this</code> for method chaining
		 */
		RootFindCommand<T> continueFrom(Cursor cursor);
		
		/**
		 * @param cursor set as {@link FetchOptions#endCursor(Cursor)}
		 * @return <code>this</code> for method chaining
		 */
		RootFindCommand<T> finishAt(Cursor cursor);
		
		
		RootFindCommand<T> fetchMaximum(int limit);
		RootFindCommand<T> unactivated();
		RootFindCommand<T> fetchNextBy(int size);
		RootFindCommand<T> fetchFirst(int size);

		// terminating methods
		CommandTerminator<Integer> returnCount();
		CommandTerminator<List<T>> returnAll();
		CommandTerminator<T> returnUnique();
		
		<P> CommandTerminator<Iterator<P>> returnParents();
		<P> CommandTerminator<ParentsCommand<P>> returnParentsCommand();
	}
	
	interface ParentsCommand<P> extends RestrictedFindCommand<ParentsCommand<P>>, CommandTerminator<Iterator<P>>
	{
	}
}
