package squeek.spiceoflife.asm;

import static org.objectweb.asm.Opcodes.*;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;
import squeek.spiceoflife.ModSpiceOfLife;
import squeek.spiceoflife.foodtracker.FoodValues;

public class ClassTransformer implements IClassTransformer
{

	@Override
	public byte[] transform(String name, String transformedName, byte[] bytes)
	{
		if (transformedName.equals("net.minecraft.item.ItemStack"))
		{
			boolean isObfuscated = !name.equals(transformedName);
			ModSpiceOfLife.Log.info("Patching ItemStack...");

			ClassNode classNode = readClassFromBytes(bytes);

			// 1.7.2 obfuscated names
			MethodNode methodNode = findMethodNodeOfClass(classNode, isObfuscated ? "b" : "onFoodEaten", isObfuscated ? "(Lafn;Lxl;)Labp;" : "(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/item/ItemStack;");

			// try 1.7.10 obfuscated names
			if (methodNode == null && isObfuscated)
				methodNode = findMethodNodeOfClass(classNode, "b", "(Lahb;Lyz;)Ladd;");

			if (methodNode != null)
			{
				addOnEatenHook(methodNode, Hooks.class, "onFoodEaten", "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;)V");
				return writeClassToBytes(classNode);
			}
		}

		if (transformedName.equals("net.minecraft.util.FoodStats"))
		{
			boolean isObfuscated = !name.equals(transformedName);
			ModSpiceOfLife.Log.info("Patching FoodStats...");

			ClassNode classNode = readClassFromBytes(bytes);

			// 1.7.2/1.7.10 have the same signatures
			MethodNode methodNode = findMethodNodeOfClass(classNode, isObfuscated ? "a" : "addStats", "(IF)V");

			if (methodNode != null)
			{
				addFoodStatsHook(methodNode, Hooks.class, "getModifiedFoodValues", "(Lnet/minecraft/util/FoodStats;IF)Lsqueek/spiceoflife/foodtracker/FoodValues;");
				return writeClassToBytes(classNode);
			}
		}

		if (name.equals("iguanaman.hungeroverhaul.IguanaFoodStats"))
		{
			ModSpiceOfLife.Log.info("Patching IguanaFoodStats...");

			ClassNode classNode = readClassFromBytes(bytes);
			MethodNode methodNode = findMethodNodeOfClass(classNode, "func_75122_a", "(IF)V");

			if (methodNode == null)
				methodNode = findMethodNodeOfClass(classNode, "addStats", "(IF)V");

			if (methodNode != null)
			{
				addFoodStatsHook(methodNode, Hooks.class, "getModifiedFoodValues", "(Lnet/minecraft/util/FoodStats;IF)Lsqueek/spiceoflife/foodtracker/FoodValues;");
				patchIguanaFoodStats(methodNode);
				return writeClassToBytes(classNode);
			}
			else
			{
				ModSpiceOfLife.Log.warn("addStats method in IguanaFoodStats not found");
			}
		}

		if (transformedName.equals("net.minecraft.client.gui.GuiScreen"))
		{
			boolean isObfuscated = !name.equals(transformedName);
			ModSpiceOfLife.Log.info("Patching GuiScreen...");

			ClassNode classNode = readClassFromBytes(bytes);
			
			// 1.7.2 obfuscated names
			MethodNode methodNode = findMethodNodeOfClass(classNode, "drawHoveringText", isObfuscated ? "(Ljava/util/List;IILbag;)V" : "(Ljava/util/List;IILnet/minecraft/client/gui/FontRenderer;)V");

			// try 1.7.10 obfuscated names
			if (methodNode == null && isObfuscated)
				methodNode = findMethodNodeOfClass(classNode, "drawHoveringText", "(Ljava/util/List;IILbbu;)V");

			if (methodNode != null)
			{
				addDrawHoveringTextHook(methodNode, Hooks.class, "onDrawHoveringText", "(IIII)V", isObfuscated);
				return writeClassToBytes(classNode);
			}
			else
			{
				ModSpiceOfLife.Log.warn("drawHoveringText method in GuiContainer not found");
			}
		}

		if (name.equals("codechicken.lib.gui.GuiDraw"))
		{
			ModSpiceOfLife.Log.info("Patching CodeChickenLib's GuiDraw...");

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
			ModSpiceOfLife.Log.info("Patching TConstruct's NewContainerGui...");

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
				ModSpiceOfLife.Log.info(" Found target method: " + methodName);
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

	private AbstractInsnNode findFirstInstructionOfType(MethodNode method, int bytecode)
	{
		for (AbstractInsnNode instruction : method.instructions.toArray())
		{
			if (instruction.getOpcode() == bytecode)
				return instruction;
		}
		return null;
	}

	private AbstractInsnNode findNextInstructionOfType(AbstractInsnNode startInstruction, int bytecode)
	{
		if (startInstruction != null)
		{
			AbstractInsnNode instruction = startInstruction.getNext();
			while (instruction != null)
			{
				if (instruction.getOpcode() == bytecode)
					return instruction;

				instruction = instruction.getNext();
			}
		}
		return null;
	}

	private LabelNode findEndLabel(MethodNode method)
	{
		LabelNode lastLabel = null;
		for (AbstractInsnNode instruction : method.instructions.toArray())
		{
			if (instruction instanceof LabelNode)
				lastLabel = (LabelNode) instruction;
		}
		return lastLabel;
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

	public void addOnEatenHook(MethodNode method, Class<?> hookClass, String hookMethod, String hookDesc)
	{
		AbstractInsnNode targetNode = findFirstInstructionOfType(method, ALOAD);

		InsnList toInject = new InsnList();

		/*
		// equivalent to:
		Hooks.onFoodEaten(this, world, player);
		 */

		toInject.add(new VarInsnNode(ALOAD, 0)); 		// this
		toInject.add(new VarInsnNode(ALOAD, 1)); 		// param1: world
		toInject.add(new VarInsnNode(ALOAD, 2)); 		// param2: player
		toInject.add(new MethodInsnNode(INVOKESTATIC, hookClass.getName().replace('.', '/'), hookMethod, hookDesc));

		method.instructions.insertBefore(targetNode, toInject);

		ModSpiceOfLife.Log.info(" Patched " + method.name);
	}

	public void addFoodStatsHook(MethodNode method, Class<?> hookClass, String hookMethod, String hookDesc)
	{
		AbstractInsnNode targetNode = findFirstInstruction(method);

		InsnList toInject = new InsnList();

		/*
		// equivalent to:
		int par1=0; float par2=0f;
		
		squeek.spiceoflife.foodtracker.FoodValues modifiedFoodValues = Hooks.getModifiedFoodValues(null, par1, par2);
		
		par1 = modifiedFoodValues.hunger;
		par2 = modifiedFoodValues.saturationModifier;
		*/

		LabelNode varStartLabel = new LabelNode();
		LabelNode end = findEndLabel(method);
		LocalVariableNode localVar = new LocalVariableNode("modifiedFoodValues", "Lsqueek/spiceoflife/foodtracker/FoodValues;", null, varStartLabel, end, method.maxLocals);
		method.maxLocals += 1;
		method.localVariables.add(localVar);

		// get modifiedFoodValues
		toInject.add(new VarInsnNode(ALOAD, 0));					// this
		toInject.add(new VarInsnNode(ILOAD, 1));					// foodLevel
		toInject.add(new VarInsnNode(FLOAD, 2));					// foodSaturationModifier
		toInject.add(new MethodInsnNode(INVOKESTATIC, hookClass.getName().replace('.', '/'), hookMethod, hookDesc));
		toInject.add(new VarInsnNode(ASTORE, localVar.index));		// modifiedFoodValues = Hooks.getFoodModifier(...)
		toInject.add(varStartLabel);								// variable scope start

		// set foodLevel parameter
		toInject.add(new VarInsnNode(ALOAD, localVar.index));		// modifiedFoodValues
		toInject.add(new FieldInsnNode(GETFIELD, FoodValues.class.getName().replace('.', '/'), "hunger", "I"));
		toInject.add(new VarInsnNode(ISTORE, 1)); 					// param = modifiedFoodValues.hunger

		// set foodSaturationModifier parameter
		toInject.add(new VarInsnNode(ALOAD, localVar.index));		// modifiedFoodValues
		toInject.add(new FieldInsnNode(GETFIELD, FoodValues.class.getName().replace('.', '/'), "saturationModifier", "F"));
		toInject.add(new VarInsnNode(FSTORE, 2)); 					// param = modifiedFoodValues.saturationModifier

		method.instructions.insertBefore(targetNode, toInject);

		ModSpiceOfLife.Log.info(" Added " + hookMethod + " hook to " + method.name);
	}

	public boolean isFoodRegensHealthNode(FieldInsnNode node)
	{
		boolean isOwnerCorrect = node.owner.equals("iguanaman/hungeroverhaul/IguanaConfig");
		boolean isNameCorrect = node.name.equals("foodRegensHealth");
		boolean isDescCorrect = node.desc.equals("Z");
		return isOwnerCorrect && isNameCorrect && isDescCorrect;
	}

	public void patchIguanaFoodStats(MethodNode method)
	{
		AbstractInsnNode targetNode = findFirstInstructionOfType(method, GETSTATIC);
		while (targetNode != null && !isFoodRegensHealthNode((FieldInsnNode) targetNode))
		{
			targetNode = findNextInstructionOfType(targetNode, GETSTATIC);
		}

		if (targetNode == null || targetNode.getNext().getOpcode() != IFEQ)
		{
			ModSpiceOfLife.Log.fatal("Unable to patch " + method.name + "; this will likely cause a crash");
			return;
		}

		LabelNode ifLabel = ((JumpInsnNode) targetNode.getNext()).label;

		InsnList toInject = new InsnList();
		/*
		// changes:
			if (IguanaConfig.foodRegensHealth)
		// to:
			if (IguanaConfig.foodRegensHealth && entityplayer != null)
		*/

		toInject.add(new VarInsnNode(ALOAD, 0)); // this
		// GETFIELD iguanaman/hungeroverhaul/IguanaFoodStats.entityplayer : Ljava/lang/ref/WeakReference;
		toInject.add(new FieldInsnNode(GETFIELD, "iguanaman/hungeroverhaul/IguanaFoodStats", "entityplayer", "Ljava/lang/ref/WeakReference;"));
		toInject.add(new JumpInsnNode(IFNULL, ifLabel));

		ModSpiceOfLife.Log.info(" Patched " + method.name);
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
		if (targetNode != null)
		{
			ModSpiceOfLife.Log.info(" Found target node");
		}
		else
		{
			ModSpiceOfLife.Log.warn("Could not patch " + method.name + "; target node not found");
			return;
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

		ModSpiceOfLife.Log.info(" Patched " + method.name);
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
		if (targetNode != null)
		{
			ModSpiceOfLife.Log.info(" Found target node");
		}
		else
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

		ModSpiceOfLife.Log.info(" Patched " + method.name);
	}

	public void addCodeChickenDrawHoveringTextHook(MethodNode method, Class<?> hookClass, String hookMethod, String hookDesc)
	{
		AbstractInsnNode targetNode = findFirstInstruction(method);

		if (targetNode == null)
		{
			ModSpiceOfLife.Log.warn("Could not patch " + method.name + "; not able to find a suitable injection point");
			return;
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

		ModSpiceOfLife.Log.info(" Patched " + method.name);
	}

}
