package com.volmit.iris.command;

import org.bukkit.entity.Player;

import com.volmit.iris.Iris;
import com.volmit.iris.object.IrisStructure;
import com.volmit.iris.util.MortarCommand;
import com.volmit.iris.util.MortarSender;
import com.volmit.iris.util.StructureTemplate;

public class CommandIrisStructureOpen extends MortarCommand
{
	public CommandIrisStructureOpen()
	{
		super("load", "open", "o");
		requiresPermission(Iris.perm);
		setCategory("Structure");
		setDescription("Open an existing structure");
	}

	@Override
	public boolean handle(MortarSender sender, String[] args)
	{
		if(!sender.isPlayer())
		{
			sender.sendMessage("You don't have a wand");
			return true;
		}

		Player p = sender.player();

		IrisStructure structure = Iris.globaldata.getStructureLoader().load(args[0]);

		if(structure == null)
		{
			sender.sendMessage("Can't find " + args[0]);
			return true;
		}

		String dimensionGuess = structure.getLoadFile().getParentFile().getParentFile().getName();
		new StructureTemplate(structure.getName(), dimensionGuess, p, p.getLocation(), 9, structure.getGridSize(), structure.getGridHeight(), structure.getMaxLayers() > 1).loadStructures(structure);

		return true;
	}

	@Override
	protected String getArgsUsage()
	{
		return "<structure>";
	}
}