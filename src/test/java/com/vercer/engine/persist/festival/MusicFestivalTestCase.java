package com.vercer.engine.persist.festival;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Test;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.datastore.Transaction;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.vercer.engine.persist.LocalDatastoreTestCase;
import com.vercer.engine.persist.annotation.AnnotationObjectDatastore;
import com.vercer.engine.persist.festival.Album.Track;
import com.vercer.engine.persist.festival.Band.HairStyle;

public class MusicFestivalTestCase extends LocalDatastoreTestCase
{
	private AnnotationObjectDatastore datastore;

	@Override
	public void setUp()
	{
		super.setUp();
		DatastoreService service = DatastoreServiceFactory.getDatastoreService();
		datastore = new AnnotationObjectDatastore(service);
	}

	public static MusicFestival createFestival() throws ParseException
	{
		DateFormat dateFormat = new SimpleDateFormat("d MMM yyyy", Locale.ENGLISH);

		MusicFestival musicFestival = new MusicFestival();

		RockBand ledzep = new RockBand();
		ledzep.name = "Led Zeppelin";
		ledzep.locale = Locale.UK;
		ledzep.hair = Band.HairStyle.LONG_LIKE_A_GIRL;
		ledzep.chargedForBrokenTelevisions = true;

		Musician page = new Musician();
		page.name = "Jimmy Page";
		page.birthday = dateFormat.parse("9 January 1944");
		ledzep.members = new ArrayList<Musician>();
		ledzep.members.add(page);

		Musician jones = new Musician();
		jones.name = "John Paul Jones";
		jones.birthday = dateFormat.parse("3 January 1946");
		ledzep.members.add(jones);

		Musician plant = new Musician();
		plant.name = "Robert Plant";
		plant.birthday = dateFormat.parse("20 August 1948");
		ledzep.members.add(plant);

		Musician bonham = new Musician();
		bonham.name = "John Bonham";
		bonham.birthday = dateFormat.parse("31 May 1948");
		ledzep.members.add(bonham);

		Album houses = new Album();
		houses.name = "Houses of the Holy";
		houses.released = dateFormat.parse("28 March 1973");
		houses.label = "Atlantic";
		houses.rocksTheHouse = true;
		houses.sold = 18000000;

		ledzep.albums = new ArrayList<Album>();
		ledzep.albums.add(houses);

		houses.tracks = new Album.Track[3];
		houses.tracks[0] = new Album.Track();
		houses.tracks[0].title = "The Song Remains the Same";
		houses.tracks[0].length = 5.32f;
		houses.tracks[1] = new Album.Track();
		houses.tracks[1].title = "The Rain Song";
		houses.tracks[1].length = 7.39f;
		houses.tracks[2] = new Album.Track();
		houses.tracks[2].title = "Over the Hills and Far Away";
		houses.tracks[2].length = 4.50f;
//		houses.band = ledzep;

		Album iv = new Album();
		iv.name = "Led Zeppelin IV";
		iv.released = dateFormat.parse("8 November 1971");
		iv.label = "Atlantic";
		iv.rocksTheHouse = true;
		iv.sold = 22000000;
//		iv.band = ledzep;

		ledzep.albums.add(iv);

		musicFestival.bands.add(ledzep);

		RockBand firm = new RockBand();
		firm.name = "The Firm";
		firm.hair = HairStyle.BALD;
		firm.members = new ArrayList<Musician>();

		firm.members.add(page);

		Musician rogers = new Musician();
		rogers.name = "Paul Rogers";
		rogers.birthday = dateFormat.parse("17 December 1949");

		firm.members.add(rogers);

		musicFestival.bands.add(firm);

		DanceBand soulwax = new DanceBand();
		soulwax.name = "Soulwax";
		soulwax.locale = new Locale("nl", "be");
		soulwax.members = new ArrayList<Musician>();
		soulwax.members.add(new Musician("Stephen Dewaele"));
		soulwax.members.add(new Musician("David Dewaele"));
		soulwax.hair = Band.HairStyle.UNKEMPT_FLOPPY;
		soulwax.tabletsConfiscated = 12; // but they are still acting suspiciously

		Album swradio = new Album();
		soulwax.albums = new ArrayList<Album>();
		soulwax.albums.add(swradio);
		swradio.name = "As Heard on Radio Soulwax Pt. 2";
		swradio.label = "Play It Again Sam";
		swradio.released = dateFormat.parse("17 February 2003");
		swradio.rocksTheHouse = true;
		swradio.sold = 500000;
//		swradio.band = soulwax;

		swradio.tracks = new Album.Track[2];
		swradio.tracks[0] = new Album.Track();
		swradio.tracks[0].title = "Where's Your Head At";
		swradio.tracks[0].length = 2.49f;
		swradio.tracks[1] = new Album.Track();
		swradio.tracks[1].title = "A really long track name that is certainly over 500 chars" +
				"long expecially because it is repeated again and again and again" +
				"long expecially because it is repeated again and again and again" +
				"long expecially because it is repeated again and again and again" +
				"long expecially because it is repeated again and again and again" +
				"long expecially because it is repeated again and again and again" +
				"long expecially because it is repeated again and again and again" +
				"long expecially because it is repeated again and again and again" +
				"long expecially because it is repeated again and again and again" +
				"long expecially because it is repeated again and again and again" +
				"long expecially because it is repeated again and again and again" +
				"long expecially because it is repeated again and again and again" +
				"long expecially because it is repeated again and again and again" +
				"long expecially because it is repeated again and again and again";

		swradio.tracks[1].length = 1.38f;

		musicFestival.bands.add(soulwax);

		return musicFestival;
	}

	@Test
	public void testLoadDifferentEqualInstances() throws ParseException
	{
		MusicFestival musicFestival = createFestival();

		Key key = datastore.store(musicFestival);

		DatastoreService service = DatastoreServiceFactory.getDatastoreService();
		AnnotationObjectDatastore typesafe2 = new AnnotationObjectDatastore(service);
		typesafe2.setActivationDepth(5);
		Object reloaded = typesafe2.load(key);

		// they should be different instances from distinct sessions
		assertNotSame(reloaded, musicFestival);

		// they should have the same data
		assertEquals(reloaded, musicFestival);
	}

	@Test
	public void testEntityFilter() throws ParseException
	{
		MusicFestival musicFestival = createFestival();
		datastore.store(musicFestival);

		Predicate<Entity> predicate = new Predicate<Entity>()
		{
			public boolean apply(Entity input)
			{
				return input.getKey().getName().equals("Led Zeppelin");
			}
		};
		Iterator<RockBand> results = datastore.find().type(RockBand.class).filterEntities(predicate).returnResultsNow();
		assertEquals(Iterators.size(results), 1);
	}


	@Test
	public void testDeleteAll() throws ParseException
	{
		MusicFestival musicFestival = createFestival();
		datastore.store(musicFestival);

		Iterator<Album> albums = datastore.find(Album.class);

		assertTrue(albums.hasNext());

		datastore.deleteAll(ImmutableList.copyOf(albums));

		albums = datastore.find(Album.class);

		assertFalse(albums.hasNext());
	}

	@Test
	public void testLists()
	{
		Album album = new Album();
		album.name = "Greatest Hits";
		album.tracks = new Track[1];
		album.tracks[0] = new Track();
		album.tracks[0].title = "Friday I'm in Love";

		datastore.store(album);

		datastore.disassociateAll();

		Album load = datastore.load(Album.class, album.name);

		assertEquals(album, load);

	}

	@Test
	public void batchedStoreMulti() throws ParseException, InterruptedException, ExecutionException
	{
		MusicFestival musicFestival = createFestival();
		Band band1 = musicFestival.bands.get(0);
		Band band2 = musicFestival.bands.get(1);

		Map<Band, Key> keys = datastore.store().instances(Arrays.asList(band1, band2)).batchRelated().returnKeysNow();

		assertTrue(keys.size() > 2);
	}

	@Test
	public void testNumericKeys()
	{
		LongKeyType longKeyType = new LongKeyType();
		longKeyType.key = 9l;

		datastore.store(longKeyType);
		datastore.disassociateAll();

		QueryResultIterator<LongKeyType> found = datastore.find(LongKeyType.class);
		while (found.hasNext())
		{
			System.out.println(found.next().key);
		}

		LongKeyType longKeyType2 = datastore.load(LongKeyType.class, 9l);
		assertEquals(longKeyType2.key.longValue(), 9l);

		PrimitiveLongKeyType plongKeyType = new PrimitiveLongKeyType();
		plongKeyType.key = 9l;

		datastore.store(plongKeyType);

		datastore.disassociateAll();

		PrimitiveLongKeyType plongKeyType2 = datastore.load(PrimitiveLongKeyType.class, 9l);
		assertEquals(plongKeyType2.key, 9l);

		IntKeyType intKeyType = new IntKeyType();
		intKeyType.key = 9;

		datastore.store(intKeyType);

		datastore.disassociateAll();

		IntKeyType intKeyType2 = datastore.load(IntKeyType.class, 9);
		assertEquals(intKeyType2.key.intValue(), 9);

	}

	public static class LongKeyType
	{
		@com.vercer.engine.persist.annotation.Key Long key;
	}
	public static class PrimitiveLongKeyType
	{
		@com.vercer.engine.persist.annotation.Key long key;
	}
	public static class IntKeyType
	{
		@com.vercer.engine.persist.annotation.Key Integer key;
	}
	public static class DoubleKeyType
	{
		@com.vercer.engine.persist.annotation.Key Double key;
	}
	@Test
	public void asyncQueryTest() throws ParseException, InterruptedException, ExecutionException
	{
		MusicFestival musicFestival = createFestival();
		datastore.store(musicFestival);

		Future<QueryResultIterator<RockBand>> frbs = datastore.find().type(RockBand.class).returnResultsLater();
		Future<QueryResultIterator<DanceBand>> fdbs = datastore.find().type(DanceBand.class).returnResultsLater();

		QueryResultIterator<RockBand> rbs = frbs.get();
		QueryResultIterator<DanceBand> dbs = fdbs.get();

		assertTrue(rbs.hasNext());
		assertTrue(dbs.hasNext());
	}

	@Test
	public void asyncStoreSingle() throws ParseException, InterruptedException, ExecutionException
	{
		MusicFestival musicFestival = createFestival();
		Band band1 = musicFestival.bands.get(0);
		Band band2 = musicFestival.bands.get(1);
		Future<Key> future1 = datastore.store().instance(band1).returnKeyLater();
		Future<Key> future2 = datastore.store().instance(band2).returnKeyLater();

		assertEquals("Led Zeppelin", future1.get().getName());
		assertEquals("The Firm", future2.get().getName());
	}

	@Test
	public void ensureUniqueKey()
	{
		Band band1 = new Band();
		band1.name = "Kasier Chiefs";

		Band band2 = new Band();
		band2.name = "Chemical Brothers";

		datastore.storeAll(Arrays.asList(band1, band2));

		Transaction txn = datastore.beginTransaction();

		// overwrite band1
		Band band3 = new Band();
		band3.name = "Kasier Chiefs";
		datastore.store().instance(band3);
		txn.commit();

		txn = datastore.beginTransaction();
		Band band4 = new Band();
		band4.name = "Kasier Chiefs";

		boolean threw = false;
		try
		{
			datastore.store().instance(band4).ensureUniqueKey().returnKeyNow();
		}
		catch (Exception e)
		{
			threw = true;
	    }
		txn.rollback();
		assertTrue(threw);
	}

	@Test
	public void asyncStoreMulti() throws ParseException, InterruptedException, ExecutionException
	{
		MusicFestival musicFestival = createFestival();
		Band band1 = musicFestival.bands.get(0);
		Band band2 = musicFestival.bands.get(1);

		Future<Map<Band, Key>> future1 = datastore.store().instances(Arrays.asList(band1, band2)).returnKeysLater();

		Map<Band, Key> map = future1.get();

		assertEquals(2, map.size());

		assertNotNull(map.get(band1));
		assertNotNull(map.get(band2));
	}

	@Test
	public void activationDepth() throws ParseException
	{
		MusicFestival musicFestival = createFestival();

		datastore.setActivationDepth(1);

		Key stored = datastore.store(musicFestival);
		datastore.disassociateAll();

		// musicians have depth 3
		MusicFestival reloaded = datastore.load(stored);
		assertNull(reloaded.bands.get(0).hair);

		datastore.refresh(reloaded.bands.get(0));

		assertNotNull(reloaded.bands.get(0).hair);

		datastore.setActivationDepth(2);
		datastore.disassociateAll();

		reloaded = datastore.load(stored);
		assertNotNull(reloaded.bands.get(0).hair);
	}

	@Test public void storeDuplicate()
	{
		Band band = new Band();
		band.name = "The XX";
		datastore.store().instance(band).ensureUniqueKey().returnKeyNow();

		band = new Band();
		band.name = "The XX";
		boolean threw = false;
		try
		{
			datastore.store().instance(band).ensureUniqueKey().returnKeyNow();
		}
		catch (Exception e)
		{
			threw = true;
		}
		assertTrue(threw);
	}
	
	@Test 
	public void countEntites() throws ParseException
	{
		MusicFestival musicFestival = createFestival();
		datastore.store(musicFestival);
		int count = datastore.find().type(Musician.class).countResultsNow();
		assertEquals(7, count);
	}
	
	@Test
	public void testClassNameEscape()
	{
		Type_with__under___scores instance = new Type_with__under___scores();
		instance.hello = "world";
		datastore.store(instance);
		datastore.refresh(instance);
	}
	
	@Test
	public void testUpdate()
	{
		Band band = new Band();
		band.name = "Pearl Jam";
		band.hair = HairStyle.BALD;
		
		datastore.store(band);

		assertNotNull(datastore.load(Band.class, "Pearl Jam"));
		
		band.hair = HairStyle.LONG_LIKE_A_GIRL;
		
		datastore.update(band);
		
		datastore.disassociateAll();
		
		assertEquals(HairStyle.LONG_LIKE_A_GIRL, datastore.load(Band.class, "Pearl Jam").hair);
	}

}
