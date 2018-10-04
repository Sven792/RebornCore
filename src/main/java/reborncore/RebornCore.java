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

package reborncore;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.CrashReportExtender;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.javafmlmod.FMLModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reborncore.api.ToolManager;
import reborncore.common.blocks.BlockWrenchEventHandler;
import reborncore.common.multiblock.MultiblockEventHandler;
import reborncore.common.multiblock.MultiblockServerTickHandler;
import reborncore.common.network.NetworkManager;
import reborncore.common.network.RegisterPacketEvent;
import reborncore.common.network.packet.*;
import reborncore.common.powerSystem.PowerSystem;
import reborncore.common.registration.RegistrationManager;
import reborncore.common.registration.RegistryConstructionEvent;
import reborncore.common.registration.impl.ConfigRegistryFactory;
import reborncore.common.shields.RebornCoreShields;
import reborncore.common.shields.json.ShieldJsonLoader;
import reborncore.common.util.*;

import java.io.File;

@Mod(RebornCore.MOD_ID)
public class RebornCore {

	public static final String MOD_NAME = "Reborn Core";
	public static final String MOD_ID = "reborncore";
	public static final String MOD_VERSION = "@MODVERSION@";
	public static final String WEB_URL = "https://files.modmuss50.me/";

	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	//TODO proxys
	public static CommonProxy proxy = new CommonProxy();
	public static File configDir;

	public RebornCore() {
		FMLModLoadingContext.get().getModEventBus().addListener(this::preInit);
		FMLModLoadingContext.get().getModEventBus().addListener(this::init);
		FMLModLoadingContext.get().getModEventBus().addListener(this::postInit);
	}

	public void preInit(FMLPreInitializationEvent event) {
		CrashReportExtender.registerCrashCallable(new CrashHandler());
		configDir = new File(FMLPaths.CONFIGDIR.get().toFile(), "teamreborn");
		if (!configDir.exists()) {
			configDir.mkdir();
		}
		MinecraftForge.EVENT_BUS.register(ConfigRegistryFactory.class);
		ConfigRegistryFactory.setConfigDir(configDir);
		RegistrationManager.init();
		RegistrationManager.load(new RegistryConstructionEvent());
		ConfigRegistryFactory.saveAll();
		PowerSystem.priorityConfig = (new File(configDir, "energy_priority.json"));
		PowerSystem.reloadConfig();
		CalenderUtils.loadCalender(); //Done early as some features need this
		proxy.preInit(event);
		ShieldJsonLoader.load(event);
		MinecraftForge.EVENT_BUS.register(this);

		RegistrationManager.load(event);

		ToolManager.INSTANCE.customToolHandlerList.add(new GenericWrenchHelper(new ResourceLocation("ic2:wrench"), true));
		ToolManager.INSTANCE.customToolHandlerList.add(new GenericWrenchHelper(new ResourceLocation("forestry:wrench"), false));
		ToolManager.INSTANCE.customToolHandlerList.add(new GenericWrenchHelper(new ResourceLocation("actuallyadditions:item_laser_wrench"), false));
		ToolManager.INSTANCE.customToolHandlerList.add(new GenericWrenchHelper(new ResourceLocation("thermalfoundation:wrench"), false));
		ToolManager.INSTANCE.customToolHandlerList.add(new GenericWrenchHelper(new ResourceLocation("charset:wrench"), false));
		ToolManager.INSTANCE.customToolHandlerList.add(new GenericWrenchHelper(new ResourceLocation("teslacorelib:wrench"), false));
		ToolManager.INSTANCE.customToolHandlerList.add(new GenericWrenchHelper(new ResourceLocation("rftools:smartwrench"), false));
		ToolManager.INSTANCE.customToolHandlerList.add(new GenericWrenchHelper(new ResourceLocation("intergrateddynamics:smartwrench"), false));
		ToolManager.INSTANCE.customToolHandlerList.add(new GenericWrenchHelper(new ResourceLocation("correlated:weldthrower"), false));
		ToolManager.INSTANCE.customToolHandlerList.add(new GenericWrenchHelper(new ResourceLocation("chiselsandbits:wrench_wood"), false));
		ToolManager.INSTANCE.customToolHandlerList.add(new GenericWrenchHelper(new ResourceLocation("redstonearsenal:tool.wrench_flux"), false));
	}

	public void init(FMLInitializationEvent event) {
		// packets
		NetworkManager.load();

		RebornCoreShields.init();

		// Multiblock events
		MinecraftForge.EVENT_BUS.register(new MultiblockEventHandler());
		MinecraftForge.EVENT_BUS.register(new MultiblockServerTickHandler());
		MinecraftForge.EVENT_BUS.register(BlockWrenchEventHandler.class);

		proxy.init(event);
		RegistrationManager.load(event);
	}

	public void postInit(FMLPostInitializationEvent event) {
		proxy.postInit(event);
		RegistrationManager.load(event);
	}

	@Mod.EventHandler
	public void onFingerprintViolation(FMLFingerprintViolationEvent event) {
		LOGGER.error("Invalid fingerprint detected for Reborn Core!");
		RebornCore.proxy.invalidFingerprints.add("Invalid fingerprint detected for Reborn Core!");
	}

	@SubscribeEvent
	public void registerPackets(RegisterPacketEvent event) {
		event.registerPacket(CustomDescriptionPacket.class, LogicalSide.CLIENT);
		event.registerPacket(PacketSlotSave.class, LogicalSide.SERVER);
		event.registerPacket(PacketFluidConfigSave.class, LogicalSide.SERVER);
		event.registerPacket(PacketConfigSave.class, LogicalSide.SERVER);
		event.registerPacket(PacketSlotSync.class, LogicalSide.CLIENT);
		event.registerPacket(PacketFluidConfigSync.class, LogicalSide.CLIENT);
		event.registerPacket(PacketIOSave.class, LogicalSide.SERVER);
		event.registerPacket(PacketFluidIOSave.class, LogicalSide.SERVER);
	}
}
