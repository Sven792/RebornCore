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

package reborncore.common.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import reborncore.RebornCore;

import java.util.ArrayList;

public class NetworkManager {

	private static SimpleChannel channel;

	public static void load() {
		channel = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(RebornCore.MOD_ID, "networking"))
				.simpleChannel();

		MinecraftForge.EVENT_BUS.post(new RegisterPacketEvent());
	}

	public static ArrayList<PacketDetails> packetList = new ArrayList<>();

	public static void sendToServer(INetworkPacket packet) {
		checkPacket(packet);
		channel.sendToServer(new PacketWrapper(packet));
	}

	//TODO 1.13 networking on most of this, the basic outline is there ,but i want the game running before I start on this

//	public static void sendToAllAround(INetworkPacket packet, NetworkRegistry.TargetPoint point) {
//		checkPacket(packet);
//		getWrapperForPacket(packet.getClass()).sendToAllAround(new PacketWrapper(packet), point);
//	}

	public static void sendToAll(INetworkPacket packet) {
//		checkPacket(packet);
//		getWrapperForPacket(packet.getClass()).sendToAll(new PacketWrapper(packet));
	}

	public static void sendToPlayer(INetworkPacket packet, EntityPlayerMP playerMP) {
//		checkPacket(packet);
//		getWrapperForPacket(packet.getClass()).sendTo(new PacketWrapper(packet), playerMP);
	}

	public static void sendToWorld(INetworkPacket packet, World world) {
//		checkPacket(packet);
//		getWrapperForPacket(packet.getClass()).sendToDimension(new PacketWrapper(packet), world.provider.getDimension());
	}

	public static void checkPacket(INetworkPacket packet){
		if (getPacketDetails(packet.getClass()) == null) {
			throw new RuntimeException("Packet " + packet.getClass().getName() + " has not been registered");
		}
	}

	public static PacketDetails getPacketDetails(Class<? extends INetworkPacket> clazz){
		return packetList.stream().filter(packetDetails -> packetDetails.packetClass.equals(clazz)).findAny().orElse(null);
	}

	public static PacketDetails registerPacket(Class<? extends INetworkPacket> packetClass, LogicalSide side){
//		SimpleNetworkWrapper wrapper = createOrGetNetworkWrapper(packetClass);
//		int id = getNextIDForWrapper(wrapper);
//		wrapper.registerMessage(PacketWrapper.PacketWrapperHandler.class, PacketWrapper.class, id, side);
//		packetWrapperMap.put(packetClass, wrapper);
//		RebornCore.LOGGER.info("Registed packet to " + getWrapperName(packetClass) + " side: " + side + " id:" + id);
		PacketDetails packetDetails = new PacketDetails(packetClass, 0, channel);
		packetList.add(packetDetails);
		return packetDetails;
	}

	//TODO lets use resource locations
	public static int getNextID(){
//		if(wrapperIdList.containsKey(networkWrapper)){
//			wrapperIdList.get(networkWrapper).id++;
//			return wrapperIdList.get(networkWrapper).id;
//		} else {
//			wrapperIdList.put(networkWrapper, new IntStore());
//			return 0;
//		}
		return -1;
	}

	private static class IntStore {
		int id = 0;
	}

	public static class PacketDetails {
		public Class<? extends INetworkPacket> packetClass;
		public int id;
		SimpleChannel simpleChannel;

		public PacketDetails(Class<? extends INetworkPacket> packetClass, int id, SimpleChannel simpleChannel) {
			this.packetClass = packetClass;
			this.id = id;
			this.simpleChannel = simpleChannel;
		}
	}

}
