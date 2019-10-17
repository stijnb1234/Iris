package ninja.bytecode.iris;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.generator.ChunkGenerator;

import ninja.bytecode.iris.atomics.AtomicChunkData;
import ninja.bytecode.shuriken.Shuriken;
import ninja.bytecode.shuriken.execution.ChronoLatch;
import ninja.bytecode.shuriken.execution.NastyRunnable;
import ninja.bytecode.shuriken.execution.TaskExecutor.TaskGroup;
import ninja.bytecode.shuriken.execution.TaskExecutor.TaskResult;
import ninja.bytecode.shuriken.format.F;
import ninja.bytecode.shuriken.math.RollingSequence;

public abstract class ParallelChunkGenerator extends ChunkGenerator
{
	private int i;
	private int j;
	private int wx;
	private int wz;
	private AtomicChunkData data;
	private TaskGroup tg;
	private TaskGroup tb;
	private boolean ready = false;
	private ChronoLatch cl = new ChronoLatch(1000);
	private RollingSequence rs = new RollingSequence(512);

	public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome)
	{
		Shuriken.profiler.start("chunkgen-" + world.getName());
		data = new AtomicChunkData(world);

		try
		{
			if(!ready)
			{
				onInit(world, random);
				ready = true;
			}

			tg = Iris.noisePool.startWork();
			tb = Iris.blockPool.startWork();

			for(i = 0; i < 16; i++)
			{
				wx = (x * 16) + i;

				for(j = 0; j < 16; j++)
				{
					wz = (z * 16) + j;
					int a = wx;
					int b = wz;
					int c = i;
					int d = j;
					tg.queue(() ->
					{
						biome.setBiome(c, d, genColumn(a, b, c, d));
					});
				}
			}

			TaskResult r = tg.execute();
			
			rs.put(r.timeElapsed + tb.execute().timeElapsed);
			Shuriken.profiler.stop("chunkgen-" + world.getName());

			if(cl.flip())
			{
				System.out.print("Avg: " + F.duration(rs.getAverage(), 2) + " " + F.duration(rs.getMax(), 2) + " / " + F.duration(rs.getMedian(), 2) + " / " + F.duration(rs.getMin(), 2));
			}
		}

		catch(Throwable e)
		{
			e.printStackTrace();
			for(int i = 0; i < 16; i++)
			{
				for(int j = 0; j < 16; j++)
				{
					data.setBlock(i, 0, j, Material.RED_GLAZED_TERRACOTTA);
				}
			}
		}

		return data.toChunkData();
	}

	public boolean isParallelCapable()
	{
		return true;
	}

	public abstract void onInit(World world, Random random);

	public abstract Biome genColumn(int wx, int wz, int x, int z);

	protected void queueSets(NastyRunnable r)
	{
		tb.queue(r);
	}

	protected void setBlock(int x, int y, int z, BlockData b)
	{
		if(b.getMaterial().equals(Material.AIR))
		{
			return;
		}

		tb.queue(() -> data.setBlock(x, y, z, b));
	}
}