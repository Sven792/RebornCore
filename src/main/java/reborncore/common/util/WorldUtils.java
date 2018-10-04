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

package reborncore.common.util;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;


/**
 * Created by Mark on 13/03/2016.
 */
public class WorldUtils {

	public static void updateBlock(World world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		world.notifyBlockUpdate(pos, state, state, 3);
	}

	public static boolean chunkExists(World world, int x, int z) {
		return world.isBlockLoaded(new BlockPos(x << 4, 64, z << 4));
	}
	
	public static void dropItem(ItemStack itemStack, World world, BlockPos pos) {
		Random rand = new Random();

		float dX = rand.nextFloat() * 0.8F + 0.1F;
		float dY = rand.nextFloat() * 0.8F + 0.1F;
		float dZ = rand.nextFloat() * 0.8F + 0.1F;

		EntityItem entityItem = new EntityItem(world, pos.getX() + dX, pos.getY() + dY, pos.getZ() + dZ,
				itemStack.copy());

		if (itemStack.hasTag()) {
			entityItem.getItem().setTag(itemStack.getTag().copy());
		}

		float factor = 0.05F;
		entityItem.motionX = rand.nextGaussian() * factor;
		entityItem.motionY = rand.nextGaussian() * factor + 0.2F;
		entityItem.motionZ = rand.nextGaussian() * factor;
		if (!world.isRemote) {
			world.spawnEntity(entityItem);
		}
	}

	public static void dropItems(List<ItemStack> itemStackList, World world, BlockPos pos) {
		for (final ItemStack itemStack : itemStackList) {
			WorldUtils.dropItem(itemStack, world, pos);
			itemStack.setCount(0);
		}
	}
}
