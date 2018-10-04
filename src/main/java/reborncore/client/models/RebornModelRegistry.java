/*
 * Copyright (c) 2018 modmuss50 and Gigabit101
 *
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package reborncore.client.models;

import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.state.IProperty;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class RebornModelRegistry {
	protected static List<ModelCompound> modelList = new ArrayList<>();

	public static void registerModel(ModelCompound modelCompound) {
		modelList.add(modelCompound);
	}

	public static void registerModels(String modid) {
		for (ModelCompound compound : modelList) {
			if (compound.getModid().equals(modid)) {
				if (compound.isBlock()) {
					if (compound.getFileName().equals("modelregistration.undefinedfilename"))
						registerItemModel(compound.getItem(), compound.getMeta(), compound.getBlockStatePath(), compound.getInventoryVariant());
					else
						registerItemModel(compound.getItem(), compound.getMeta(), compound.getFileName(), compound.getBlockStatePath(), compound.getInventoryVariant());
				}
				if (compound.isBlock()) {
					if (compound.getFileName().equals("modelregistration.undefinedfilename"))
						setBlockStateMapper(compound.getBlock(), compound.getBlockStatePath(), compound.getIgnoreProperties());
					else
						setBlockStateMapper(compound.getBlock(), compound.getFileName(), compound.getBlockStatePath(), compound.getInventoryVariant(), compound.getIgnoreProperties());
				}
			}
		}
	}

	public static void registerItemModel(Item item) {
		setMRL(item, 0, item.getRegistryName(), "inventory");
	}

	public static void registerItemModel(Item item, int meta) {
		setMRL(item, meta, item.getRegistryName(), "inventory");
	}

	public static void registerItemModel(Item item, String fileName) {
		ResourceLocation loc = new ResourceLocation(item.getRegistryName().getNamespace(), fileName);
		setMRL(item, 0, loc, "inventory");
	}

	public static void registerItemModel(Item item, int meta, String path, String invVariant) {
		String slash = "";
		if (!path.isEmpty())
			slash = "/";
		ResourceLocation loc = new ResourceLocation(item.getRegistryName().getNamespace(), path + slash + item.getRegistryName().getPath());
		setMRL(item, meta, loc, invVariant);
	}

	public static void registerItemModel(Item item, int meta, String fileName, String path, String invVariant) {
		String slash = "";
		if (!path.isEmpty())
			slash = "/";
		ResourceLocation loc = new ResourceLocation(item.getRegistryName().getNamespace(), path + slash + fileName);
		setMRL(item, meta, loc, invVariant);
	}

	public static void registerBlockState(Item item, int meta, String path, String property, String variant) {
		registerBlockState(item, meta, path, property + "=" + variant);
	}

	public static void registerBlockState(Item item, int meta, String path, String variant) {
		ResourceLocation loc = new ResourceLocation(item.getRegistryName().getNamespace(), path + "/" + item.getRegistryName().getPath());
		setMRL(item, meta, loc, variant);
	}

	public static void setMRL(Item item, int meta, ResourceLocation resourceLocation, String variant) {
		//TODO 1.13
		//ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(resourceLocation, variant));
	}
	//TODO 1.13
//	public static void setCustomStateMapper(Block block, IStateMapper mapper) {
//
//		//ModelLoader.setCustomStateMapper(block, mapper);
//	}

	public static void setBlockStateMapper(Block block, IProperty<?>... ignoredProperties) {
		setBlockStateMapper(block, block.getRegistryName().getPath(), ignoredProperties);
	}

	public static void setBlockStateMapper(Block block, String blockstatePath, IProperty<?>... ignoredProperties) {
		setBlockStateMapper(block, block.getRegistryName().getPath(), blockstatePath, ignoredProperties);
	}

	public static void setBlockStateMapper(Block block, String fileName, String path, IProperty<?>... ignoredProperties) {
		final String slash = !path.isEmpty() ? "/" : "";
		//TODO 1.13
//		ModelLoader.setCustomStateMapper(block, new DefaultStateMapper() {
//			@Override
//			protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
//				Map<IProperty<?>, Comparable<?>> map = Maps.<IProperty<?>, Comparable<?>>newLinkedHashMap(state.getProperties());
//				for (IProperty<?> iproperty : ignoredProperties) {
//					map.remove(iproperty);
//				}
//				return new ModelResourceLocation(new ResourceLocation(block.getRegistryName().getNamespace(), path + slash + fileName), this.getPropertyString(map));
//			}
//		});
	}
	
	public static void setBlockStateMapper(Block block, String fileName, String path, String invVariant, IProperty<?>... ignoredProperties) {
		final String slash = !path.isEmpty() ? "/" : "";
		//TODO 1.13
//		ModelLoader.setCustomStateMapper(block, new DefaultStateMapper() {
//			@Override
//			protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
//				Map<IProperty<?>, Comparable<?>> map = Maps.<IProperty<?>, Comparable<?>>newLinkedHashMap(state.getProperties());
//				String propertyString = "";
//				for (IProperty<?> iproperty : ignoredProperties) {
//					map.remove(iproperty);
//				}
//				if (map.size() == 0) {
//					propertyString = invVariant;
//				}
//				else {
//					propertyString = this.getPropertyString(map) + invVariant;
//				}
//				return new ModelResourceLocation(new ResourceLocation(block.getRegistryName().getNamespace(), path + slash + fileName), propertyString);
//			}
//		});
	}
}
