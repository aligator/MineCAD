package de.jdcware.minecad.core.asm;

import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.AbstractInsnNode;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.FieldInsnNode;
import jdk.internal.org.objectweb.asm.tree.InsnList;
import jdk.internal.org.objectweb.asm.tree.InsnNode;
import jdk.internal.org.objectweb.asm.tree.LabelNode;
import jdk.internal.org.objectweb.asm.tree.LocalVariableNode;
import jdk.internal.org.objectweb.asm.tree.MethodInsnNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;
import jdk.internal.org.objectweb.asm.tree.VarInsnNode;
import net.minecraft.launchwrapper.IClassTransformer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
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
		//bytes = transformOnBakeVanilla(name, transformedName, bytes);
		//bytes = ransformOnBakeWeighted(name, transformedName, bytes);
		bytes = transformExtendIModel(name, transformedName, bytes);
		bytes = transformOnBake(name, transformedName, bytes);
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

	public byte[] transformOnBakeMultipart(String name, String transformedName, byte[] bytes) {
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

	private byte[] transformExtendIModel(String name, String transformedName, byte[] bytes) {
		if (name.equals("net.minecraftforge.client.model.IModel")) {
			ClassNode classNode = new ClassNode();
			ClassReader classReader = new ClassReader(bytes);
			classReader.accept(classNode, 0);

			// find method to inject into

			MethodNode MethodGetModel = new MethodNode(
					327680,
					Opcodes.ACC_PUBLIC,
					"getModel",
					"(Lnet/minecraftforge/common/model/IModelState;Lnet/minecraft/client/renderer/vertex/VertexFormat;Ljava/util/function/Function;)Ljava/util/Optional;",
					"(Lnet/minecraftforge/common/model/IModelState;Lnet/minecraft/client/renderer/vertex/VertexFormat;Ljava/util/function/Function<Lnet/minecraft/util/ResourceLocation;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;>;)Ljava/util/Optional<Lnet/minecraft/client/renderer/block/model/ModelBlock;>;(Lnet/minecraftforge/common/model/IModelState;Lnet/minecraft/client/renderer/vertex/VertexFormat;Ljava/util/function/Function<Lnet/minecraft/util/ResourceLocation;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;>;)Ljava/util/Optional<Lnet/minecraft/client/renderer/block/model/ModelBlock;>;",
					null
			);

			LabelNode labelBegin = new LabelNode(new Label());
			LabelNode labelEnd = new LabelNode(new Label());

			MethodGetModel.instructions.add(labelBegin);
			MethodGetModel.instructions.add(new VarInsnNode(ALOAD, 0));
			MethodGetModel.instructions.add(new MethodInsnNode(INVOKEINTERFACE, "net/minecraftforge/client/model/IModel", "asVanillaModel",
					"()Ljava/util/Optional;", true));
			MethodGetModel.instructions.add(new InsnNode(ARETURN));

			MethodGetModel.instructions.add(labelEnd);

			MethodGetModel.localVariables.add(new LocalVariableNode("this", "Lnet/minecraftforge/client/model/IModel;", null, labelBegin, labelEnd, 0));
			MethodGetModel.localVariables.add(new LocalVariableNode("state", "Lnet/minecraftforge/common/model/IModelState;", null, labelBegin, labelEnd, 1));
			MethodGetModel.localVariables.add(new LocalVariableNode("format", "Lnet/minecraft/client/renderer/vertex/VertexFormat;", null, labelBegin, labelEnd, 2));
			MethodGetModel.localVariables.add(new LocalVariableNode("bakedTextureGetter", "Ljava/util/function/Function;", null, labelBegin, labelEnd, 3));

			classNode.methods.add(MethodGetModel);

			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
			classNode.accept(writer);
			return writer.toByteArray();
		}
		return bytes;
	}

	private byte[] transformOnBake(String name, String transformedName, byte[] bytes) {
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
							if (((MethodInsnNode) method.instructions.get(i)).name.equals("bake")) {
								if (method.instructions.get(i).getNext() instanceof MethodInsnNode && ((MethodInsnNode) method.instructions.get(i).getNext()).name.equals("put")) {

									// insert after INVOKEINTERFACE bake, INVOKEINTERFACE put, POP
									AbstractInsnNode insertLocationNode = method.instructions.get(i).getNext();

									InsnList toInject = new InsnList();

									// construct instruction nodes for list
									// inject call to MineCAD.onBlockBake
									toInject.add(new LabelNode(new Label()));
									toInject.add(new VarInsnNode(ALOAD, 7));
									toInject.add(new VarInsnNode(ALOAD, 7));

									toInject.add(new MethodInsnNode(INVOKEINTERFACE, "net/minecraftforge/client/model/IModel", "getDefaultState",
											"()Lnet/minecraftforge/common/model/IModelState;", true));
									toInject.add(new FieldInsnNode(GETSTATIC, "net/minecraft/client/renderer/vertex/DefaultVertexFormats", "ITEM", "Lnet/minecraft/client/renderer/vertex/VertexFormat;"));
									toInject.add(new FieldInsnNode(GETSTATIC, "net/minecraftforge/client/model/ModelLoader$DefaultTextureGetter", "INSTANCE", "Lnet/minecraftforge/client/model/ModelLoader$DefaultTextureGetter;"));

									toInject.add(new MethodInsnNode(INVOKESTATIC, "de/jdcware/minecad/core/asm/MineCADCorePlugin", "onBlockBake",
											"(Lnet/minecraftforge/client/model/IModel;Lnet/minecraftforge/common/model/IModelState;Lnet/minecraft/client/renderer/vertex/VertexFormat;Ljava/util/function/Function;)V", false));

									method.instructions.insert(insertLocationNode, toInject);
								}
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
}
