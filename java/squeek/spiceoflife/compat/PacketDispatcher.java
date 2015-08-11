package squeek.spiceoflife.compat;

public class PacketDispatcher
{
	public static IPacketDispatcher get()
	{
		return new PacketDispatcherNetty();
	}

	// based on FML's NetworkRegistry.TargetPoint
	public static class PacketTarget
	{
		public PacketTarget(int dimension, double x, double y, double z, double range)
		{
			this.x = x;
			this.y = y;
			this.z = z;
			this.range = range;
			this.dimension = dimension;
		}

		public final double x;
		public final double y;
		public final double z;
		public final double range;
		public final int dimension;
	}
}
