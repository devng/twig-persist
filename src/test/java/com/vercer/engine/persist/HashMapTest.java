package com.vercer.engine.persist;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.vercer.engine.persist.annotation.AnnotationObjectDatastore;
import com.vercer.engine.persist.annotation.Embed;
import com.vercer.engine.persist.annotation.Key;
import com.vercer.engine.persist.annotation.Type;

public class HashMapTest
{
	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
			new LocalDatastoreServiceTestConfig()).setEnvIsAdmin(true).setEnvIsLoggedIn(true)
			.setEnvEmail("test@test.com").setEnvAuthDomain("google.com");

	@Before
	public void setUp() throws Exception
	{
		helper.setUp();
	}

	@After
	public void tearDown() throws Exception
	{
		helper.tearDown();
	}

	public static class InnerFoo implements Serializable
	{
		public InnerFoo(String name)
		{
			myName = name;
		}

		public InnerFoo()
		{
		}

		public String myName;
		private static final long serialVersionUID = 1L;
	}

	public static class Foo
	{
		@Key
		String myKey;
		@Embed
		InnerFoo innerFoo;
		@Type(Blob.class)
		HashMap<String, InnerFoo> moreInnerFoos;
	}

	@Test
	public void embeddedQueryTest()
	{
		{
			ObjectDatastore datastore = new AnnotationObjectDatastore(false);

			Foo foo = new Foo();
			foo.myKey = "foo1";
			foo.innerFoo = new InnerFoo("foo1Name");
			foo.moreInnerFoos = new HashMap<String, InnerFoo>();
			foo.moreInnerFoos.put("hello", new InnerFoo("helloFoo"));
			foo.moreInnerFoos.put("goodbye", new InnerFoo("goodbyeFoo"));

			datastore.store(foo);
		}

		{
			ObjectDatastore datastore = new AnnotationObjectDatastore(false);
			Foo foundFoo = datastore.load(Foo.class, "foo1");

			assertEquals("foo1", foundFoo.myKey);
			assertEquals("foo1Name", foundFoo.innerFoo.myName);
			assertEquals(2, foundFoo.moreInnerFoos.size());
			assertEquals("helloFoo", foundFoo.moreInnerFoos.get("hello").myName);
			assertEquals("goodbyeFoo", foundFoo.moreInnerFoos.get("goodbye").myName);
		}
	}
}