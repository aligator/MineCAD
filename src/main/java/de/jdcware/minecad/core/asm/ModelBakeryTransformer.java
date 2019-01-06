package de.jdcware.minecad.core.asm;

import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.tree.AbstractInsnNode;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.FieldInsnNode;
import jdk.internal.org.objectweb.asm.tree.InsnList;
import jdk.internal.org.objectweb.asm.tree.LabelNode;
import jdk.internal.org.objectweb.asm.tree.MethodInsnNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;
import jdk.internal.org.objectweb.asm.tree.VarInsnNode;
import net.minecraft.launchwrapper.IClassTransformer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

public class ModelBakeryTransformer implements IClassTransformer {

	public static final Map<String, String> obfMapping = new HashMap<>();

	public ModelBakeryTransformer() {
		obfMapping.put("setupModelRegistry", "a");
		obfMapping.put("()Lnet/minecraft/util/registry/IRegistry;", "()Lfm;");
		obfMapping.put("loadVariantItemModels", "c");
		obfMapping.put("(Lnet/minecraftforge/common/model/IModelState;Lnet/minecraft/client/renderer/vertex/VertexFormat;Ljava/util/function/Function;)Lnet/minecraft/client/renderer/block/model/IBakedModel;", "(Lnet/minecraftforge/common/model/IModelState;Lcea;Ljava/util/function/Function;)Lcfy;");
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] bytes) {
		// TODO: performance improvements

		bytes = transformOnBlocksLoaded(name, transformedName, bytes);
		bytes = transformOnBakeVanilla(name, transformedName, bytes);
		bytes = ransformOnBakeWeighted(name, transformedName, bytes);

		return bytes;
	}


	public byte[] transformOnBlocksLoaded(String name, String transformedName, byte[] bytes) {
		if (name.equals("net.minecraftforge.client.model.ModelLoader")) {
			ClassNode classNode = new ClassNode();
			ClassReader classReader = new ClassReader(bytes);
			classReader.accept(classNode, 0);

			// find method to inject into
			Iterator<MethodNode> methods = classNode.methods.iterator();
			while (methods.hasNext()) {
				MethodNode method = methods.next();
				if (method.name.equals("setupModelRegistry") && method.desc.equals("()Lnet/minecraft/util/registry/IRegistry;")
						|| method.name.equals(obfMapping.get("setupModelRegistry")) && method.desc.equals(obfMapping.get("()Lnet/minecraft/util/registry/IRegistry;"))) {

					// find loadVariantItemModels() call to inject code after it
					for (int i = 0; i < method.instructions.size(); i++) {
						if (method.instructions.get(i).getType() == AbstractInsnNode.METHOD_INSN) {
							MethodInsnNode loadVariantItemModelsNode = (MethodInsnNode) method.instructions.get(i);
							if (loadVariantItemModelsNode.name.equals("loadVariantItemModels") || loadVariantItemModelsNode.name.equals(obfMapping.get("loadVariantItemModels"))) {

								// make a new label node for the end of our code
								LabelNode lmm1Node = new LabelNode(new Label());

								// make new instruction list
								InsnList toInject = new InsnList();

								// construct instruction nodes for list
								// inject call to MineCAD.onBlocksLoaded after the blocks were loaded.
								toInject.add(lmm1Node);
								toInject.add(new VarInsnNode(ALOAD, 0));
								toInject.add(new FieldInsnNode(GETFIELD, "net/minecraftforge/client/model/ModelLoader", "stateModels", "Ljava/util/Map;"));
								toInject.add(new VarInsnNode(ALOAD, 0));
								toInject.add(new FieldInsnNode(GETFIELD, "net/minecraftforge/client/model/ModelLoader", "multipartDefinitions", "Ljava/util/Map;"));
								toInject.add(new VarInsnNode(ALOAD, 0));
								toInject.add(new FieldInsnNode(GETFIELD, "net/minecraftforge/client/model/ModelLoader", "multipartModels", "Ljava/util/Map;"));
								toInject.add(new MethodInsnNode(INVOKESTATIC, "de/jdcware/minecad/core/asm/MineCADCorePlugin", "onBlocksLoaded",
										"(Ljava/util/Map;Ljava/util/Map;Ljava/util/Map;" + ")V", false));

								method.instructions.insertBefore(method.instructions.get(i + 1), toInject);
								break;
							}
						}
					}
				}
			}

			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			classNode.accept(writer);
			return writer.toByteArray();
		}
		return bytes;
	}

	public byte[] transformOnBakeVanilla(String name, String transformedName, byte[] bytes) {
		if (name.equals("net.minecraftforge.client.model.ModelLoader$VanillaModelWrapper")) {
			ClassNode classNode = new ClassNode();
			ClassReader classReader = new ClassReader(bytes);
			classReader.accept(classNode, 0);

			// find method to inject into
			Iterator<MethodNode> methods = classNode.methods.iterator();
			while (methods.hasNext()) {
				MethodNode method = methods.next();

				if (method.name.equals("bake") && method.desc.equals("(Lnet/minecraftforge/common/model/IModelState;Lnet/minecraft/client/renderer/vertex/VertexFormat;Ljava/util/function/Function;)Lnet/minecraft/client/renderer/block/model/IBakedModel;")
						|| method.name.equals("bake") && method.desc.equals(obfMapping.get("(Lnet/minecraftforge/common/model/IModelState;Lnet/minecraft/client/renderer/vertex/VertexFormat;Ljava/util/function/Function;)Lnet/minecraft/client/renderer/block/model/IBakedModel;"))) {

					for (int i = 0; i < method.instructions.size(); i++) {
						if (method.instructions.get(i).getType() == AbstractInsnNode.METHOD_INSN) {
							MethodInsnNode currentNode = (MethodInsnNode) method.instructions.get(i);

							// inject code at the beginning of the method

							// make new instruction list
							InsnList toInject = new InsnList();

							// construct instruction nodes for list
							// inject call to MineCAD.onBlocksLoaded after the blocks were loaded.
							toInject.add(new LabelNode(new Label()));
							toInject.add(new VarInsnNode(ALOAD, 0)); // this
							toInject.add(new VarInsnNode(ALOAD, 1)); // state param

							toInject.add(new VarInsnNode(ALOAD, 0));
							toInject.add(new FieldInsnNode(GETFIELD, "net/minecraftforge/client/model/ModelLoader$VanillaModelWrapper", "location", "Lnet/minecraft/util/ResourceLocation;"));

							toInject.add(new VarInsnNode(ALOAD, 0));
							toInject.add(new FieldInsnNode(GETFIELD, "net/minecraftforge/client/model/ModelLoader$VanillaModelWrapper", "model", "Lnet/minecraft/client/renderer/block/model/ModelBlock;"));

							toInject.add(new VarInsnNode(ALOAD, 0));
							toInject.add(new FieldInsnNode(GETFIELD, "net/minecraftforge/client/model/ModelLoader$VanillaModelWrapper", "uvlock", "Z"));

							toInject.add(new VarInsnNode(ALOAD, 0));
							toInject.add(new FieldInsnNode(GETFIELD, "net/minecraftforge/client/model/ModelLoader$VanillaModelWrapper", "animation", "Lnet/minecraftforge/client/model/animation/ModelBlockAnimation;"));

							toInject.add(new MethodInsnNode(INVOKESTATIC, "de/jdcware/minecad/core/asm/MineCADCorePlugin", "onVanillaBlockBake",
									"(Lnet/minecraftforge/client/model/IModel;Lnet/minecraftforge/common/model/IModelState;Lnet/minecraft/util/ResourceLocation;Lnet/minecraft/client/renderer/block/model/ModelBlock;ZLnet/minecraftforge/client/model/animation/ModelBlockAnimation;)V", false));

							method.instructions.insertBefore(method.instructions.get(i + 1), toInject);
							break;
						}
					}
				}
			}

			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			classNode.accept(writer);
			return writer.toByteArray();
		}
		return bytes;
	}

	public byte[] ransformOnBakeWeighted(String name, String transformedName, byte[] bytes) {
		if (name.equals("net.minecraftforge.client.model.ModelLoader$WeightedRandomModel")) {
			ClassNode classNode = new ClassNode();
			ClassReader classReader = new ClassReader(bytes);
			classReader.accept(classNode, 0);

			// find method to inject into
			Iterator<MethodNode> methods = classNode.methods.iterator();
			while (methods.hasNext()) {
				MethodNode method = methods.next();

				if (method.name.equals("bake") && method.desc.equals("(Lnet/minecraftforge/common/model/IModelState;Lnet/minecraft/client/renderer/vertex/VertexFormat;Ljava/util/function/Function;)Lnet/minecraft/client/renderer/block/model/IBakedModel;")
						|| method.name.equals("bake") && method.desc.equals(obfMapping.get("(Lnet/minecraftforge/common/model/IModelState;Lnet/minecraft/client/renderer/vertex/VertexFormat;Ljava/util/function/Function;)Lnet/minecraft/client/renderer/block/model/IBakedModel;"))) {

					for (int i = 0; i < method.instructions.size(); i++) {
						if (method.instructions.get(i).getType() == AbstractInsnNode.METHOD_INSN) {
							MethodInsnNode currentNode = (MethodInsnNode) method.instructions.get(i);
							// inject code at the beginning of the method

							// make new instruction list
							InsnList toInject = new InsnList();

							// construct instruction nodes for list
							// inject call to MineCAD.onBlocksLoaded after the blocks were loaded.
							toInject.add(new LabelNode(new Label()));
							toInject.add(new VarInsnNode(ALOAD, 0)); // this
							toInject.add(new VarInsnNode(ALOAD, 1)); // state param

							toInject.add(new VarInsnNode(ALOAD, 0));
							toInject.add(new FieldInsnNode(GETFIELD, "net/minecraftforge/client/model/ModelLoader$WeightedRandomModel", "variants", "Ljava/util/List;"));

							toInject.add(new VarInsnNode(ALOAD, 0));
							toInject.add(new FieldInsnNode(GETFIELD, "net/minecraftforge/client/model/ModelLoader$WeightedRandomModel", "locations", "Ljava/util/List;"));

							toInject.add(new VarInsnNode(ALOAD, 0));
							toInject.add(new FieldInsnNode(GETFIELD, "net/minecraftforge/client/model/ModelLoader$WeightedRandomModel", "textures", "Ljava/util/Set;"));

							toInject.add(new VarInsnNode(ALOAD, 0));
							toInject.add(new FieldInsnNode(GETFIELD, "net/minecraftforge/client/model/ModelLoader$WeightedRandomModel", "models", "Ljava/util/List;"));

							toInject.add(new VarInsnNode(ALOAD, 0));
							toInject.add(new FieldInsnNode(GETFIELD, "net/minecraftforge/client/model/ModelLoader$WeightedRandomModel", "defaultState", "Lnet/minecraftforge/common/model/IModelState;"));

							toInject.add(new MethodInsnNode(INVOKESTATIC, "de/jdcware/minecad/core/asm/MineCADCorePlugin", "onWeightedRandomBlockBake",
									"(Lnet/minecraftforge/client/model/IModel;Lnet/minecraftforge/common/model/IModelState;Ljava/util/List;Ljava/util/List;Ljava/util/Set;Ljava/util/List;Lnet/minecraftforge/common/model/IModelState;)V", false));

							method.instructions.insertBefore(method.instructions.get(i + 1), toInject);
							break;
						}
					}
				}
			}

			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			classNode.accept(writer);
			return writer.toByteArray();
		}
		return bytes;
	}

	public byte[] ransformOnBakeMultipart(String name, String transformedName, byte[] bytes) {
		if (name.equals("net.minecraftforge.client.model.ModelLoader$MultipartModel")) {
			ClassNode classNode = new ClassNode();
			ClassReader classReader = new ClassReader(bytes);
			classReader.accept(classNode, 0);

			// find method to inject into
			Iterator<MethodNode> methods = classNode.methods.iterator();
			while (methods.hasNext()) {
				MethodNode method = methods.next();

				if (method.name.equals("bake") && method.desc.equals("(Lnet/minecraftforge/common/model/IModelState;Lnet/minecraft/client/renderer/vertex/VertexFormat;Ljava/util/function/Function;)Lnet/minecraft/client/renderer/block/model/IBakedModel;")
						|| method.name.equals("bake") && method.desc.equals(obfMapping.get("(Lnet/minecraftforge/common/model/IModelState;Lnet/minecraft/client/renderer/vertex/VertexFormat;Ljava/util/function/Function;)Lnet/minecraft/client/renderer/block/model/IBakedModel;"))) {

					for (int i = 0; i < method.instructions.size(); i++) {
						if (method.instructions.get(i).getType() == AbstractInsnNode.METHOD_INSN) {
							MethodInsnNode currentNode = (MethodInsnNode) method.instructions.get(i);
							// inject code at the beginning of the method

							// make new instruction list
							InsnList toInject = new InsnList();

							// construct instruction nodes for list
							// inject call to MineCAD.onBlocksLoaded after the blocks were loaded.
							toInject.add(new LabelNode(new Label()));
							toInject.add(new VarInsnNode(ALOAD, 0)); // this
							toInject.add(new VarInsnNode(ALOAD, 1)); // state param
							// TODO: dfffdfdf
							toInject.add(new VarInsnNode(ALOAD, 0));
							toInject.add(new FieldInsnNode(GETFIELD, "net/minecraftforge/client/model/ModelLoader$MultipartModel", "variants", "Ljava/util/List;"));

							toInject.add(new VarInsnNode(ALOAD, 0));
							toInject.add(new FieldInsnNode(GETFIELD, "net/minecraftforge/client/model/ModelLoader$WeightedRandomModel", "locations", "Ljava/util/List;"));

							toInject.add(new VarInsnNode(ALOAD, 0));
							toInject.add(new FieldInsnNode(GETFIELD, "net/minecraftforge/client/model/ModelLoader$WeightedRandomModel", "textures", "Ljava/util/Set;"));

							toInject.add(new VarInsnNode(ALOAD, 0));
							toInject.add(new FieldInsnNode(GETFIELD, "net/minecraftforge/client/model/ModelLoader$WeightedRandomModel", "models", "Ljava/util/List;"));

							toInject.add(new VarInsnNode(ALOAD, 0));
							toInject.add(new FieldInsnNode(GETFIELD, "net/minecraftforge/client/model/ModelLoader$WeightedRandomModel", "defaultState", "Lnet/minecraftforge/common/model/IModelState;"));

							toInject.add(new MethodInsnNode(INVOKESTATIC, "de/jdcware/minecad/core/asm/MineCADCorePlugin", "onWeightedRandomBlockBake",
									"(Lnet/minecraftforge/client/model/IModel;Lnet/minecraftforge/common/model/IModelState;Ljava/util/List;Ljava/util/List;Ljava/util/Set;Ljava/util/List;Lnet/minecraftforge/common/model/IModelState;)V", false));

							method.instructions.insertBefore(method.instructions.get(i + 1), toInject);
							break;
						}
					}
				}
			}

			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			classNode.accept(writer);
			return writer.toByteArray();
		}
		return bytes;
	}
}
