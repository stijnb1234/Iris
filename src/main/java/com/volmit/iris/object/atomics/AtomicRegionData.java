package com.volmit.iris.object.atomics;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.bukkit.World;

import com.volmit.iris.util.ByteArrayTag;
import com.volmit.iris.util.CompoundTag;
import com.volmit.iris.util.KMap;
import com.volmit.iris.util.NBTInputStream;
import com.volmit.iris.util.NBTOutputStream;
import com.volmit.iris.util.Tag;

public class AtomicRegionData
{
	private final World world;
	private KMap<String, Tag> tag;

	public AtomicRegionData(World world)
	{
		this.world = world;
		tag = new KMap<>();
	}

	public int size()
	{
		return tag.size();
	}

	public void read(InputStream in) throws IOException
	{
		NBTInputStream nin = new NBTInputStream(in);
		tag = new KMap<>();
		tag.putAll(((CompoundTag) nin.readTag()).getValue());
		nin.close();
	}

	public void write(OutputStream out) throws IOException
	{
		NBTOutputStream nos = new NBTOutputStream(out);
		nos.writeTag(new CompoundTag("imca", tag));
		nos.close();
	}

	public boolean contains(int rx, int rz)
	{
		return tag.containsKey(rx + "." + rz);
	}

	public void delete(int rx, int rz)
	{
		tag.remove(rx + "." + rz);
	}

	public void set(int rx, int rz, AtomicSliverMap data) throws IOException
	{
		if(data == null)
		{
			return;
		}

		ByteArrayOutputStream boas = new ByteArrayOutputStream();
		data.write(boas);
		tag.put(rx + "." + rz, new ByteArrayTag(rx + "." + rz, boas.toByteArray()));
	}

	public AtomicSliverMap get(int rx, int rz) throws IOException
	{
		AtomicSliverMap data = new AtomicSliverMap();

		if(!contains(rx, rz))
		{
			return data;
		}

		try
		{
			ByteArrayTag btag = (ByteArrayTag) tag.get(rx + "." + rz);
			ByteArrayInputStream in = new ByteArrayInputStream(btag.getValue());
			data.read(in);
		}

		catch(Throwable e)
		{

		}

		return data;
	}

	public World getWorld()
	{
		return world;
	}
}