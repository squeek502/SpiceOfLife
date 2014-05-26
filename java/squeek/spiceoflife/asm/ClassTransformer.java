package squeek.spiceoflife.asm;

import static org.objectweb.asm.Opcodes.*;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import squeek.spiceoflife.ModSpiceOfLife;

public class ClassTransformer implements IClassTransformer
{

	@Override
	public byte[] transform(String name, String transformedName, byte[] bytes)
	{
		if (name.equals("net.minecraft.item.ItemStack") || name.equals("ye"))
		{
			boolean isObfuscated = name.equals("ye");
			ModSpiceOfLife.Log.info("Patching ItemStack...");

			ClassNode classNode = readClassFromBytes(bytes);
			MethodNode methodNode = findMethodNodeOfClass(classNode, isObfuscated ? "b" : "onFoodEaten", isObfuscated ? "(Labw;Luf;)Lye;" : "(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/item/ItemStack;");
			if (methodNode != null)
			{
				addOnEatenHook(methodNode, Hooks.class, "onFoodEaten", "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;)V");
				return writeClassToBytes(classNode);
			}
		}

		if (name.equals("net.minecraft.util.FoodStats") || name.equals("ux"))
		{
			boolean isObfuscated = name.equals("ux");
			ModSpiceOfLife.Log.info("Patching FoodStats...");

			ClassNode classNode = readClassFromBytes(bytes);
			MethodNode methodNode = findMethodNodeOfClass(classNode, isObfuscated ? "a" : "addStats", "(IF)V");
			if (methodNode != null)
			{
				addFoodStatsHook(methodNode, Hooks.class, "getFoodModifier", "(Lnet/minecraft/util/FoodStats;IF)F");
				return writeClassToBytes(classNode);
			}
		}
		
		if (name.equals("iguanaman.hungeroverhaul.IguanaFoodStats"))
		{
			ModSpiceOfLife.Log.info("Patching IguanaFoodStats...");
			
			ClassNode classNode = readClassFromBytes(bytes);
			MethodNode methodNode = findMethodNodeOfClass(classNode, "addStats", "(IF)V");
			if (methodNode != null)
			{
				addFoodStatsHook(methodNode, Hooks.class, "getFoodModifier", "(Lnet/minecraft/util/FoodStats;IF)F");
				return writeClassToBytes(classNode);
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

	private AbstractInsnNode findFirstInstructionOfType(MethodNode method, int bytecode)
	{
		for (AbstractInsnNode instruction : method.instructions.toArray())
		{
			if (instruction.getOpcode() == bytecode)
				return instruction;
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
		AbstractInsnNode targetNode = findFirstInstructionOfType(method, ALOAD);

		InsnList toInject = new InsnList();

		/*
		// equivalent to:
		int par1=0; float par2=0f;
		
		float modifier = Hooks.getFoodModifier(null, par1, par2);
		
		par1 *= modifier;
		if (par2 > 0)
			par2 *= modifier;
		 */

		LabelNode varStartLabel = new LabelNode();
		LabelNode end = findEndLabel(method);
		LocalVariableNode localVar = new LocalVariableNode("modifier", Type.FLOAT_TYPE.getDescriptor(), method.signature, varStartLabel, end, method.maxLocals);
		method.maxLocals += Type.FLOAT_TYPE.getSize();
		method.localVariables.add(localVar);

		// get modifier
		toInject.add(new VarInsnNode(ALOAD, 0));					// this
		toInject.add(new VarInsnNode(ILOAD, 1));					// foodLevel
		toInject.add(new VarInsnNode(FLOAD, 2));					// foodSaturationModifier
		toInject.add(new MethodInsnNode(INVOKESTATIC, hookClass.getName().replace('.', '/'), hookMethod, hookDesc));
		toInject.add(new VarInsnNode(FSTORE, localVar.index));		// modifier = Hooks.getFoodModifier(...)
		toInject.add(varStartLabel);								// variable scope start

		// modify foodLevel parameter
		toInject.add(new VarInsnNode(ILOAD, 1)); 					// add the parameter to the stack
		toInject.add(new InsnNode(I2F)); 							// convert to float for the multiplication
		toInject.add(new VarInsnNode(FLOAD, localVar.index)); 		// add the modifier to the stack
		toInject.add(new InsnNode(FMUL)); 							// param * modifier
		toInject.add(new InsnNode(F2I)); 							// back to int
		toInject.add(new VarInsnNode(ISTORE, 1)); 					// param = result

		// modify foodSaturationModifier parameter (if it's greater than 0)
		toInject.add(new VarInsnNode(FLOAD, 2));					// param
		toInject.add(new InsnNode(FCONST_0));						// 0
		toInject.add(new InsnNode(FCMPL));							// compare floats
		LabelNode labelIfNotGreaterThan0 = new LabelNode();			// label if the if condition fails
		toInject.add(new JumpInsnNode(IFLE, labelIfNotGreaterThan0)); // if par2 <= 0 jump to label
		toInject.add(new VarInsnNode(FLOAD, 2)); 					// add the parameter to the stack
		toInject.add(new VarInsnNode(FLOAD, localVar.index));		// add the modifier to the stack
		toInject.add(new InsnNode(FMUL)); 							// param * modifier
		toInject.add(new VarInsnNode(FSTORE, 2)); 					// param = result
		toInject.add(labelIfNotGreaterThan0);						// if par2 <=, jump here

		method.instructions.insertBefore(targetNode, toInject);

		ModSpiceOfLife.Log.info(" Patched " + method.name);
	}

}
