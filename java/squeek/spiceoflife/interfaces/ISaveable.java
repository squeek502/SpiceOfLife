package squeek.spiceoflife.interfaces;

import net.minecraft.nbt.NBTTagCompound;

public interface ISaveable
{
	public abstract void writeToNBTData(NBTTagCompound data);
	
	public abstract void readFromNBTData(NBTTagCompound data);
}
