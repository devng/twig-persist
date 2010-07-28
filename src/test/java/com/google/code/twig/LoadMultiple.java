package com.google.code.twig;

import java.util.Arrays;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import com.google.code.twig.annotation.AnnotationObjectDatastore;
import com.google.code.twig.annotation.Id;

public class LoadMultiple extends LocalDatastoreTestCase
{
	static class Item
	{
		@Id long id;
		String name;
		@SuppressWarnings("unused")
		private Item () {};
		public Item(String name)
		{
			this.name = name;
		}
	}
	
	@Test
	public void test()
	{
		AnnotationObjectDatastore datastore = new AnnotationObjectDatastore();
		Item hello = new Item("hello");
		Item world = new Item("world");
		
		datastore.storeAll(Arrays.asList(hello, world));
		
		datastore.disassociateAll();
		
		Map<Long, Item> results = datastore.load().type(Item.class).ids(hello.id, world.id).now();
		
		// show that we loaded both
		Assert.assertEquals(2, results.size());
		
		// show that new instance was created
		Assert.assertNotSame(hello, results.get(hello.id));
	}
}
