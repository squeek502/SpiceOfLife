package squeek.spiceoflife.asm;

import static org.objectweb.asm.Opcodes.*;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;
import squeek.spiceoflife.ModSpiceOfLife;
import static squeek.spiceoflife.asm.ASMException.*;

public class ClassTransformer implements IClassTransformer
{

	@Override
	public byte[] transform(String name, String transformedName, byte[] bytes)
	{
		if (transformedName.equals("net.minecraft.client.gui.GuiScreen"))
		{
			boolean isObfuscated = !name.equals(transformedName);
			ModSpiceOfLife.Log.debug("Patching GuiScreen...");

			ClassNode classNode = readClassFromBytes(bytes);
			
			MethodNode methodNode = findMethodNodeOfClass(classNode, "drawHoveringText", isObfuscated ? "(Ljava/util/List;IILbbu;)V" : "(Ljava/util/List;IILnet/minecraft/client/gui/FontRenderer;)V");

			if (methodNode != null)
			{
				addDrawHoveringTextHook(methodNode, Hooks.class, "onDrawHoveringText", "(IIII)V", isObfuscated);
				return writeClassToBytes(classNode);
			}
			else
			{
				throw new MethodNotFoundException("GuiScreen.drawHoveringText");
			}
		}

		if (name.equals("codechicken.lib.gui.GuiDraw"))
		{
			ModSpiceOfLife.Log.debug("Patching CodeChickenLib's GuiDraw...");

			ClassNode classNode = readClassFromBytes(bytes);
			MethodNode methodNode = findMethodNodeOfClass(classNode, "drawTooltipBox", "(IIII)V");

			if (methodNode != null)
			{
				addCodeChickenDrawHoveringTextHook(methodNode, Hooks.class, "onDrawHoveringText", "(IIII)V");
				return writeClassToBytes(classNode);
			}
			else
			{
				ModSpiceOfLife.Log.warn("drawTooltipBox method in GuiDraw not found");
			}
		}

		if (name.equals("tconstruct.client.gui.NewContainerGui"))
		{
			ModSpiceOfLife.Log.debug("Patching TConstruct's NewContainerGui...");

			ClassNode classNode = readClassFromBytes(bytes);
			MethodNode methodNode = findMethodNodeOfClass(classNode, "func_102021_a", "(Ljava/util/List;II)V");

			if (methodNode != null)
			{
				addTinkersDrawHoveringTextHook(methodNode, Hooks.class, "onDrawHoveringText", "(IIII)V", false);
				return writeClassToBytes(classNode);
			}
			else
			{
				ModSpiceOfLife.Log.warn("func_102021_a method in NewContainerGui not found");
			}
		}

		return bytes;
	}

	private ClassNode readClassFromBytes(byte[] bytes)
	{
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);
		return classNode;
	}

	private byte[] writeClassToBytes(ClassNode classNode)
	{
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classNode.accept(writer);
		return writer.toByteArray();
	}

	private MethodNode findMethodNodeOfClass(ClassNode classNode, String methodName, String methodDesc)
	{
		for (MethodNode method : classNode.methods)
		{
			if (method.name.equals(methodName) && method.desc.equals(methodDesc))
			{
				return method;
			}
		}
		return null;
	}

	private AbstractInsnNode findFirstInstruction(MethodNode method)
	{
		for (AbstractInsnNode instruction : method.instructions.toArray())
		{
			if (instruction.getType() != AbstractInsnNode.LABEL && instruction.getType() != AbstractInsnNode.LINE)
				return instruction;
		}
		return null;
	}

	private LocalVariableNode findLocalVariableOfMethod(MethodNode method, String varName, String varDesc)
	{
		for (LocalVariableNode localVar : method.localVariables)
		{
			if (localVar.name.equals(varName) && localVar.desc.equals(varDesc))
			{
				return localVar;
			}
		}
		return null;
	}

	public void addDrawHoveringTextHook(MethodNode method, Class<?> hookClass, String hookMethod, String hookDesc, boolean isObfuscated)
	{
		AbstractInsnNode targetNode = null;

		// get last drawGradientRect call
		for (AbstractInsnNode instruction : method.instructions.toArray())
		{
			if (instruction.getOpcode() == INVOKEVIRTUAL)
			{
				MethodInsnNode methodInsn = (MethodInsnNode) instruction;

				if (methodInsn.desc.equals("(IIIIII)V"))
					targetNode = instruction;
			}
		}
		if (targetNode == null)
		{
			throw new PatternNotFoundException(method.name);
		}

		LocalVariableNode x = findLocalVariableOfMethod(method, "j2", "I");
		LocalVariableNode y = findLocalVariableOfMethod(method, "k2", "I");
		LocalVariableNode w = findLocalVariableOfMethod(method, "k", "I");
		LocalVariableNode h = findLocalVariableOfMethod(method, "i1", "I");

		if (x == null || y == null || w == null || h == null)
		{
			ModSpiceOfLife.Log.warn("Could not patch " + method.name + "; local variables not found");
			return;
		}

		InsnList toInject = new InsnList();

		/*
		// equivalent to:
		Hooks.onDrawHoveringText(0, 0, 0, 0);
		*/

		toInject.add(new VarInsnNode(ILOAD, x.index));
		toInject.add(new VarInsnNode(ILOAD, y.index));
		toInject.add(new VarInsnNode(ILOAD, w.index));
		toInject.add(new VarInsnNode(ILOAD, h.index));
		toInject.add(new MethodInsnNode(INVOKESTATIC, hookClass.getName().replace('.', '/'), hookMethod, hookDesc));

		method.instructions.insert(targetNode, toInject);

		ModSpiceOfLife.Log.debug(" Patched " + method.name);
	}

	public void addTinkersDrawHoveringTextHook(MethodNode method, Class<?> hookClass, String hookMethod, String hookDesc, boolean isObfuscated)
	{
		AbstractInsnNode targetNode = null;

		// get last drawGradientRect call
		for (AbstractInsnNode instruction : method.instructions.toArray())
		{
			if (instruction.getOpcode() == INVOKEVIRTUAL)
			{
				MethodInsnNode methodInsn = (MethodInsnNode) instruction;

				if (methodInsn.desc.equals("(IIIIII)V"))
					targetNode = instruction;
			}
		}
		if (targetNode == null)
		{
			ModSpiceOfLife.Log.warn("Could not patch " + method.name + "; target node not found");
			return;
		}

		LocalVariableNode x = findLocalVariableOfMethod(method, "i1", "I");
		LocalVariableNode y = findLocalVariableOfMethod(method, "j1", "I");
		LocalVariableNode w = findLocalVariableOfMethod(method, "k", "I");
		LocalVariableNode h = findLocalVariableOfMethod(method, "k1", "I");

		if (x == null || y == null || w == null || h == null)
		{
			throw new InstructionNotFoundException("Local variables: " + x + y + w + h);
		}

		InsnList toInject = new InsnList();

		/*
		// equivalent to:
		Hooks.onDrawHoveringText(0, 0, 0, 0);
		*/

		toInject.add(new VarInsnNode(ILOAD, x.index));
		toInject.add(new VarInsnNode(ILOAD, y.index));
		toInject.add(new VarInsnNode(ILOAD, w.index));
		toInject.add(new VarInsnNode(ILOAD, h.index));
		toInject.add(new MethodInsnNode(INVOKESTATIC, hookClass.getName().replace('.', '/'), hookMethod, hookDesc));

		method.instructions.insert(targetNode, toInject);

		ModSpiceOfLife.Log.debug(" Hooked into " + method.name + " to get tooltip position/size");
	}

	public void addCodeChickenDrawHoveringTextHook(MethodNode method, Class<?> hookClass, String hookMethod, String hookDesc)
	{
		AbstractInsnNode targetNode = findFirstInstruction(method);

		if (targetNode == null)
		{
			throw new PatternNotFoundException(method.name);
		}

		InsnList toInject = new InsnList();

		/*
		// equivalent to:
		Hooks.onDrawHoveringText(0, 0, 0, 0);
		*/

		toInject.add(new VarInsnNode(ILOAD, 0));	// x
		toInject.add(new VarInsnNode(ILOAD, 1));	// y
		toInject.add(new VarInsnNode(ILOAD, 2));	// w
		toInject.add(new VarInsnNode(ILOAD, 3));	// h
		toInject.add(new MethodInsnNode(INVOKESTATIC, hookClass.getName().replace('.', '/'), hookMethod, hookDesc));

		method.instructions.insertBefore(targetNode, toInject);

		ModSpiceOfLife.Log.debug(" Hooked into " + method.name + " to get tooltip position/size");
	}

}
