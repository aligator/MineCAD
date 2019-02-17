package de.jdcware.minecad.core.asm;

import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.*;
import net.minecraft.launchwrapper.IClassTransformer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

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
        // TODO: anti code duplication

		bytes = transformOnBlocksLoaded(name, transformedName, bytes);

        // Extend interface IModel and implement in various places
		bytes = transformExtendIModel(name, transformedName, bytes);
        bytes = transformExtendWeightedRandomModel(name, transformedName, bytes);
        bytes = transformExtendMultipartModel(name, transformedName, bytes);

        // call interface when baking block
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

	private byte[] transformExtendIModel(String name, String transformedName, byte[] bytes) {
		if (name.equals("net.minecraftforge.client.model.IModel")) {
			ClassNode classNode = new ClassNode();
			ClassReader classReader = new ClassReader(bytes);
			classReader.accept(classNode, 0);

            // create getModelMethod to inject it into the interface
			MethodNode MethodGetModel = new MethodNode(
					327680,
					Opcodes.ACC_PUBLIC,
					"getModel",
					"(Lnet/minecraftforge/common/model/IModelState;Lnet/minecraft/client/renderer/vertex/VertexFormat;Ljava/util/function/Function;)Ljava/util/Optional;",
                    "(Lnet/minecraftforge/common/model/IModelState;Lnet/minecraft/client/renderer/vertex/VertexFormat;Ljava/util/function/Function<Lnet/minecraft/util/ResourceLocation;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;>;)Ljava/util/Optional<Lde/jdcware/minecad/core/ICADModel;>;",
					null
			);

			LabelNode labelBegin = new LabelNode(new Label());
			LabelNode labelEnd = new LabelNode(new Label());

			MethodGetModel.instructions.add(labelBegin);
			MethodGetModel.instructions.add(new VarInsnNode(ALOAD, 0));
            MethodGetModel.instructions.add(new VarInsnNode(ALOAD, 1));
            MethodGetModel.instructions.add(new VarInsnNode(ALOAD, 2));
            MethodGetModel.instructions.add(new VarInsnNode(ALOAD, 3));
            MethodGetModel.instructions.add(new MethodInsnNode(INVOKESTATIC, "de/jdcware/minecad/core/asm/MineCADCorePlugin", "defaultGetModel",
                    "(Lnet/minecraftforge/client/model/IModel;Lnet/minecraftforge/common/model/IModelState;Lnet/minecraft/client/renderer/vertex/VertexFormat;Ljava/util/function/Function;)Ljava/util/Optional;", false));
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

    /**
     * public Optional<ICADModel> getModel(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
     * return MineCADCorePlugin.onWeightedRandomBlock(state, format, bakedTextureGetter, variants, locations, textures, models, defaultState);
     * }
     *
     * @param name
     * @param transformedName
     * @param bytes
     * @return
     */
    private byte[] transformExtendWeightedRandomModel(String name, String transformedName, byte[] bytes) {
        if (name.equals("net.minecraftforge.client.model.ModelLoader$WeightedRandomModel")) {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(bytes);
            classReader.accept(classNode, 0);

            // create getModelMethod to inject it into the the class
            MethodNode MethodGetModel = new MethodNode(
                    327680,
                    Opcodes.ACC_PUBLIC,
                    "getModel",
                    "(Lnet/minecraftforge/common/model/IModelState;Lnet/minecraft/client/renderer/vertex/VertexFormat;Ljava/util/function/Function;)Ljava/util/Optional;",
                    "(Lnet/minecraftforge/common/model/IModelState;Lnet/minecraft/client/renderer/vertex/VertexFormat;Ljava/util/function/Function<Lnet/minecraft/util/ResourceLocation;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;>;)Ljava/util/Optional<Lde/jdcware/minecad/core/ICADModel;>;",
                    null
            );

            LabelNode labelBegin = new LabelNode(new Label());
            LabelNode labelEnd = new LabelNode(new Label());

            MethodGetModel.instructions.add(labelBegin);
            MethodGetModel.instructions.add(new VarInsnNode(ALOAD, 1));
            MethodGetModel.instructions.add(new VarInsnNode(ALOAD, 2));
            MethodGetModel.instructions.add(new VarInsnNode(ALOAD, 3));

            MethodGetModel.instructions.add(new VarInsnNode(ALOAD, 0));
            MethodGetModel.instructions.add(new FieldInsnNode(GETFIELD, "net/minecraftforge/client/model/ModelLoader$WeightedRandomModel", "variants", "Ljava/util/List;"));

            MethodGetModel.instructions.add(new VarInsnNode(ALOAD, 0));
            MethodGetModel.instructions.add(new FieldInsnNode(GETFIELD, "net/minecraftforge/client/model/ModelLoader$WeightedRandomModel", "locations", "Ljava/util/List;"));

            MethodGetModel.instructions.add(new VarInsnNode(ALOAD, 0));
            MethodGetModel.instructions.add(new FieldInsnNode(GETFIELD, "net/minecraftforge/client/model/ModelLoader$WeightedRandomModel", "textures", "Ljava/util/Set;"));

            MethodGetModel.instructions.add(new VarInsnNode(ALOAD, 0));
            MethodGetModel.instructions.add(new FieldInsnNode(GETFIELD, "net/minecraftforge/client/model/ModelLoader$WeightedRandomModel", "models", "Ljava/util/List;"));

            MethodGetModel.instructions.add(new VarInsnNode(ALOAD, 0));
            MethodGetModel.instructions.add(new FieldInsnNode(GETFIELD, "net/minecraftforge/client/model/ModelLoader$WeightedRandomModel", "defaultState", "Lnet/minecraftforge/common/model/IModelState;"));


            MethodGetModel.instructions.add(new MethodInsnNode(INVOKESTATIC, "de/jdcware/minecad/core/asm/MineCADCorePlugin", "onWeightedRandomBlock",
                    "(Lnet/minecraftforge/common/model/IModelState;Lnet/minecraft/client/renderer/vertex/VertexFormat;Ljava/util/function/Function;Ljava/util/List;Ljava/util/List;Ljava/util/Set;Ljava/util/List;Lnet/minecraftforge/common/model/IModelState;)Ljava/util/Optional;", false));
            MethodGetModel.instructions.add(new InsnNode(ARETURN));

            MethodGetModel.instructions.add(labelEnd);

            MethodGetModel.localVariables.add(new LocalVariableNode("this", "Lnet/minecraftforge/client/model/ModelLoader$WeightedRandomModel;", null, labelBegin, labelEnd, 0));
            MethodGetModel.localVariables.add(new LocalVariableNode("state", "Lnet/minecraftforge/common/model/IModelState;", null, labelBegin, labelEnd, 1));
            MethodGetModel.localVariables.add(new LocalVariableNode("format", "Lnet/minecraft/client/renderer/vertex/VertexFormat;", null, labelBegin, labelEnd, 2));
            MethodGetModel.localVariables.add(new LocalVariableNode("bakedTextureGetter", "Ljava/util/function/Function;", null, labelBegin, labelEnd, 3));

            classNode.methods.add(MethodGetModel);

            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            classNode.accept(writer);
            return writer.toByteArray();
        }
        return bytes;
    }

    /**
     * public Optional<ICADModel> getModel(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
     * return MineCADCorePlugin.onMultipartBlock(state, format, bakedTextureGetter, variants, locations, textures, models, defaultState);
     * }
     *
     * @param name
     * @param transformedName
     * @param bytes
     * @return
     */
    private byte[] transformExtendMultipartModel(String name, String transformedName, byte[] bytes) {
        if (name.equals("net.minecraftforge.client.model.ModelLoader$MultipartModel")) {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(bytes);
            classReader.accept(classNode, 0);

            // create getModelMethod to inject it into the the class
            MethodNode MethodGetModel = new MethodNode(
                    327680,
                    Opcodes.ACC_PUBLIC,
                    "getModel",
                    "(Lnet/minecraftforge/common/model/IModelState;Lnet/minecraft/client/renderer/vertex/VertexFormat;Ljava/util/function/Function;)Ljava/util/Optional;",
                    "(Lnet/minecraftforge/common/model/IModelState;Lnet/minecraft/client/renderer/vertex/VertexFormat;Ljava/util/function/Function<Lnet/minecraft/util/ResourceLocation;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;>;)Ljava/util/Optional<Lde/jdcware/minecad/core/ICADModel;>;",
                    null
            );

            LabelNode labelBegin = new LabelNode(new Label());
            LabelNode labelEnd = new LabelNode(new Label());

            MethodGetModel.instructions.add(labelBegin);
            MethodGetModel.instructions.add(new VarInsnNode(ALOAD, 1));
            MethodGetModel.instructions.add(new VarInsnNode(ALOAD, 2));
            MethodGetModel.instructions.add(new VarInsnNode(ALOAD, 3));

            MethodGetModel.instructions.add(new VarInsnNode(ALOAD, 0));
            MethodGetModel.instructions.add(new FieldInsnNode(GETFIELD, "net/minecraftforge/client/model/ModelLoader$MultipartModel", "location", "Lnet/minecraft/util/ResourceLocation;"));

            MethodGetModel.instructions.add(new VarInsnNode(ALOAD, 0));
            MethodGetModel.instructions.add(new FieldInsnNode(GETFIELD, "net/minecraftforge/client/model/ModelLoader$MultipartModel", "multipart", "Lnet/minecraft/client/renderer/block/model/multipart/Multipart;"));

            MethodGetModel.instructions.add(new VarInsnNode(ALOAD, 0));
            MethodGetModel.instructions.add(new FieldInsnNode(GETFIELD, "net/minecraftforge/client/model/ModelLoader$MultipartModel", "partModels", "Lcom/google/common/collect/ImmutableMap;"));

            MethodGetModel.instructions.add(new MethodInsnNode(INVOKESTATIC, "de/jdcware/minecad/core/asm/MineCADCorePlugin", "onMultipartBlock",
                    "(Lnet/minecraftforge/common/model/IModelState;Lnet/minecraft/client/renderer/vertex/VertexFormat;Ljava/util/function/Function;Lnet/minecraft/util/ResourceLocation;Lnet/minecraft/client/renderer/block/model/multipart/Multipart;Lcom/google/common/collect/ImmutableMap;)Ljava/util/Optional;", false));
            MethodGetModel.instructions.add(new InsnNode(ARETURN));

            MethodGetModel.instructions.add(labelEnd);

            MethodGetModel.localVariables.add(new LocalVariableNode("this", "Lnet/minecraftforge/client/model/ModelLoader$MultipartModel;", null, labelBegin, labelEnd, 0));
            MethodGetModel.localVariables.add(new LocalVariableNode("state", "Lnet/minecraftforge/common/model/IModelState;", null, labelBegin, labelEnd, 1));
            MethodGetModel.localVariables.add(new LocalVariableNode("format", "Lnet/minecraft/client/renderer/vertex/VertexFormat;", null, labelBegin, labelEnd, 2));
            MethodGetModel.localVariables.add(new LocalVariableNode("bakedTextureGetter", "Ljava/util/function/Function;", null, labelBegin, labelEnd, 3));

            classNode.methods.add(MethodGetModel);

            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            classNode.accept(writer);
            return writer.toByteArray();
        }
        return bytes;
    }
}
