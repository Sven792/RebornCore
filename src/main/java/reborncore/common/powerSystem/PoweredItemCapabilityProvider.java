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

package reborncore.common.powerSystem;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.OptionalCapabilityInstance;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import reborncore.api.power.IEnergyItemInfo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author drcrazy
 *
 */

public class PoweredItemCapabilityProvider implements ICapabilitySerializable<NBTTagInt> {
	
	private IEnergyStorage energyStorage = null;
	

	public PoweredItemCapabilityProvider(ItemStack stack) {
		// Done to ensure that the item that is being handled is only one of TechReborns, this shouldn't be false but this protects against it.
		if(stack.getItem() instanceof IEnergyItemInfo){
			IEnergyItemInfo poweredItem = (IEnergyItemInfo) stack.getItem();
			// TODO Fix existing energy amnt
			this.energyStorage = new EnergyStorage(poweredItem.getCapacity(), poweredItem.getMaxInput(), poweredItem.getMaxOutput(), 0);
		}
	}
	
	@Nullable
	@Override
	public <T> OptionalCapabilityInstance<T> getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
		if (energyStorage != null && capability == CapabilityEnergy.ENERGY) {
			//TODO 1.13
			//return CapabilityEnergy.ENERGY.cast(energyStorage);
		}
		return null;
	}

	@Override
	public NBTTagInt serializeNBT() {
		NBTTagInt nbt = new NBTTagInt(0);
		if (energyStorage != null) {
			nbt =  (NBTTagInt) CapabilityEnergy.ENERGY.getStorage().writeNBT(CapabilityEnergy.ENERGY, energyStorage, null);
		}
		return nbt;
	}

	@Override
	public void deserializeNBT(NBTTagInt nbt) {
		if (energyStorage != null) {
			CapabilityEnergy.ENERGY.getStorage().readNBT(CapabilityEnergy.ENERGY, energyStorage, null, nbt);	
		}
	}

}
