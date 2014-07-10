package squeek.spiceoflife.asm;

public class ASMException
{
	public static class MethodNotFoundException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;

		public MethodNotFoundException(String info)
		{
			super(info);
		}
	}

	public static class InstructionNotFoundException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;

		public InstructionNotFoundException(String info)
		{
			super(info);
		}
	}

	public static class PatternNotFoundException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;

		public PatternNotFoundException(String info)
		{
			super(info);
		}
	}
}
