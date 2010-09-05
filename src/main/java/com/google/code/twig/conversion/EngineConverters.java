package com.google.code.twig.conversion;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Type;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Text;
import com.google.code.twig.conversion.CoreConverters.DateToString;
import com.google.code.twig.conversion.CoreConverters.StringToDate;
import com.google.code.twig.util.io.NoDescriptorObjectInputStream;
import com.google.code.twig.util.io.NoDescriptorObjectOutputStream;

public class EngineConverters
{
	public static void registerAll(CombinedConverter converter)
	{
		converter.append(new StringToText());
		converter.append(new TextToString());
		
		converter.append(new StringToDate());
		converter.append(new DateToString());

		converter.append(new ByteArrayToBlob());
		converter.append(new BlobToByteArray());

		converter.append(new SerializableToBlob());
		converter.append(new BlobToAnything());
	}
	
	public static class StringToText implements SpecificConverter<String, Text>
	{
		public Text convert(String source)
		{
			return new Text(source);
		}
	}
	
	public static class TextToString implements SpecificConverter<Text, String>
	{
		public String convert(Text source)
		{
			return source.getValue();
		}
	}

	public static class ByteArrayToBlob implements SpecificConverter<byte[], Blob>
	{
		public Blob convert(byte[] source)
		{
			return new Blob(source);
		}
	}

	public static class BlobToByteArray implements SpecificConverter<Blob, byte[]>
	{
		public byte[] convert(Blob source)
		{
			return source.getBytes();
		}
	}

	public static class SerializableToBlob implements SpecificConverter<Serializable, Blob>
	{
		public Blob convert(Serializable source)
		{
			try
			{
				ByteArrayOutputStream baos = new ByteArrayOutputStream(256);
				ObjectOutputStream stream = createObjectOutputStream(baos);
				stream.writeObject(source);
				return new Blob(baos.toByteArray());
			}
			catch (Exception e)
			{
				throw new IllegalStateException(e);
			}
		}

		protected ObjectOutputStream createObjectOutputStream(ByteArrayOutputStream baos) throws IOException
		{
			return new ObjectOutputStream(baos);
		}

	}
	
	public static class NoDescriptorSerializableToBlob extends SerializableToBlob
	{
		@Override
		protected ObjectOutputStream createObjectOutputStream(ByteArrayOutputStream baos) throws IOException
		{
			return new NoDescriptorObjectOutputStream(baos);
		}
	}
	
	public static class BlobToAnything implements TypeConverter
	{
		public Object convert(Blob blob)
		{
			try
			{
				ByteArrayInputStream bais = new ByteArrayInputStream(blob.getBytes());
				ObjectInputStream stream = createObjectInputStream(bais);
				return stream.readObject();
			}
			catch (Exception e)
			{
				throw new IllegalStateException(e);
			}
		}

		protected ObjectInputStream createObjectInputStream(ByteArrayInputStream bais) throws IOException
		{
			return new ObjectInputStream(bais);
		}

		@SuppressWarnings("unchecked")
		public <T> T convert(Object source, Type type)
		{
			if (source != null && source.getClass() == Blob.class)
			{
				return (T) convert((Blob) source);
			}
			return null;
		}
	}
	
	public static class NoDescriptorBlobToAnything extends BlobToAnything
	{
		@Override
		protected ObjectInputStream createObjectInputStream(ByteArrayInputStream bais) throws IOException
		{
			return new NoDescriptorObjectInputStream(bais);
		}
	}
}
