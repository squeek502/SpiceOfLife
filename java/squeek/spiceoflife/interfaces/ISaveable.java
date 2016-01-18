package squeek.spiceoflife.interfaces;

import net.minecraft.nbt.NBTTagCompound;

public interface ISaveable
{
	void writeToNBTData(NBTTagCompound data);

	void readFromNBTData(NBTTagCompound data);
}
