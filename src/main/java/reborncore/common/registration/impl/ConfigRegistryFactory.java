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

package reborncore.common.registration.impl;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import org.apache.commons.lang3.tuple.Pair;
import reborncore.RebornCore;
import reborncore.common.RebornCoreConfig;
import reborncore.common.registration.*;
import reborncore.common.util.serialization.SerializationUtil;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;

@IRegistryFactory.RegistryFactory
public class ConfigRegistryFactory implements IRegistryFactory {

	private static File configDir = null;
	private static HashMap<String, Configuration> configMap = new HashMap<>();
	private static HashMap<Configuration, ConfigData> configDataMap = new HashMap<>();
	private static NBTTagCompound configVersionTag = null;

	@Override
	public Class<? extends Annotation> getAnnotation() {
		return ConfigRegistry.class;
	}

	@Override
	public void handleField(Field field) {
		try {
			RebornRegister rebornRegistry = (RebornRegister) RegistrationManager.getAnnoation(field.getDeclaringClass().getAnnotations(), RebornRegister.class);
			ConfigRegistry annotation = (ConfigRegistry) RegistrationManager.getAnnoationFromArray(field.getAnnotations(), this);

			Configuration configuration = getOrCreateConfig(annotation, rebornRegistry);

			if (!Modifier.isStatic(field.getModifiers())) {
				throw new RuntimeException("Field " + field.getName() + " must be static");
			}

			if (!Modifier.isPublic(field.getModifiers())) {
				field.setAccessible(true);
			}

			String key = annotation.key();
			if (key.isEmpty()) {
				key = field.getName();
			}
			Object defaultValue = field.get(null);
			String comment = annotation.comment();
			comment = comment + " [Default=" + defaultValue + "]";
			Property property = get(annotation.category(), key, defaultValue, comment, field.getType(), configuration);
			if(shouldReset(property, annotation, rebornRegistry.modID()) && RebornCoreConfig.configUpdating){
				property.setToDefault();
			}
			Object value;
			try {
				value = getObjectFromProperty(property, field);
			} catch (Exception e){
				RebornCore.LOGGER.error("Failed to read config value " + annotation.category() + "." + annotation.key() + " resetting to default value");
				e.printStackTrace();
				value = defaultValue;
			}
			field.set(null, value);

			ConfigData configData;
			if(configDataMap.containsKey(configuration)){
				configData = configDataMap.get(configuration);
			} else {
				configData = new ConfigData(configuration);
				configDataMap.put(configuration, configData);
			}

			configData.fieldMap.put(configData.getName(annotation.category(), property), field);

		} catch (IllegalAccessException e) {
			throw new Error("Failed to load config", e);
		}
	}

	@Override
	public void factoryComplete() {
		MinecraftForge.EVENT_BUS.post(new RebornRegistryEvent());
		saveAll();
	}

	private static Object getObjectFromProperty(Property property, @Nullable Field field) {
		if (property.getType() == Property.Type.STRING) {
			if(property.isList()){
				return getList(property, field);
			}
			return property.getString();
		}
		if (property.getType() == Property.Type.BOOLEAN) {
			return property.getBoolean();
		}
		if (property.getType() == Property.Type.INTEGER) {
			return property.getInt();
		}
		if (property.getType() == Property.Type.DOUBLE) {
			return property.getDouble();
		}
		throw new RuntimeException("Type not supported");
	}

	public static Property get(String category, String key, Object defaultValue, String comment, Class<?> type, Configuration configuration) {
		if (type == String.class) {
			return configuration.get(category, key, (String) defaultValue, comment);
		}
		if (type == boolean.class) {
			return configuration.get(category, key, (Boolean) defaultValue, comment);
		}
		if (type == int.class) {
			return configuration.get(category, key, (Integer) defaultValue, comment);
		}
		if (type == double.class) {
			return configuration.get(category, key, (Double) defaultValue, comment);
		}
		if(type == List.class){
			return getListProperty(category, key, defaultValue, comment, type, configuration);
		}
		throw new RuntimeException("Type not supported: " + type);
	}

	public static Configuration getOrCreateConfig(ConfigRegistry annotation, RebornRegister rebornRegistry){
		return getOrCreateConfig(rebornRegistry.modID(), annotation.config());
	}

	private static Configuration getOrCreateConfig(String modId, String config) {
		String configIdent = modId;
		if (!config.isEmpty()) {
			configIdent = configIdent + ":" + config;
		}
		Configuration configuration;
		if (configMap.containsKey(configIdent)) {
			configuration = configMap.get(configIdent);
		} else {
			File modConfigDir = new File(configDir, modId);
			String configName = "config.cfg";
			if (!config.isEmpty()) {
				configName = config + ".cfg";
			}
			configuration = new Configuration(new File(modConfigDir, configName));
			configMap.put(configIdent, configuration);
		}
		return configuration;
	}

	public static List<Pair<Configuration, String>> getConfigs(String modid){
		return configMap.entrySet().stream()
			.filter(entry -> entry.getKey().startsWith(modid))
			.map(entry -> Pair.of(entry.getValue(), entry.getKey()))
			.collect(Collectors.toList());
	}

	public static void saveAll() {
		for (Map.Entry<String, Configuration> configurationEntry : configMap.entrySet()) {
			configurationEntry.getValue().save();
		}
		try {
			CompressedStreamTools.write(configVersionTag, new File(configDir, "configData.nbt"));
		} catch (IOException e) {
			throw new RuntimeException("Failed to save config data", e);
		}
	}

	@SubscribeEvent
	public static void onChange(ConfigChangedEvent.OnConfigChangedEvent event){
		List<Pair<Configuration, String>> configs = getConfigs(event.getModID());
		if(configs.isEmpty()){
			return;
		}
		configs.forEach(pair -> reload(pair.getLeft()));
	}

	public static void reload(Configuration configuration){
		if(!configDataMap.containsKey(configuration)){
			return;
		}
		ConfigData data = configDataMap.get(configuration);
		for(String category : configuration.getCategoryNames()){
			for(Property property : configuration.getCategory(category).values()){
				Field field = data.fieldMap.get(data.getName(category, property));
				if(field == null){
					//This is a possible outcome, as some configs dont have a field for them.
					RebornCore.LOGGER.debug("failed to find field for " + property.getName());
					continue;
				}
				try {
					field.set(null, getObjectFromProperty(property, field));
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		configuration.save();
	}


	public static void setConfigDir(File configDir) {
		ConfigRegistryFactory.configDir = configDir;
		File dataFile =  new File(configDir, "configData.nbt");
		if(dataFile.exists()){
			try {
				configVersionTag = CompressedStreamTools.read(dataFile);
			} catch (IOException e) {
				e.printStackTrace();
				RebornCore.LOGGER.error("Failed to read config data");
				RebornCore.LOGGER.error(e);
				//Just reset it, I cannot be dealing with crashes for things being off.
				dataFile.delete();
				configVersionTag = new NBTTagCompound();
			}
		} else {
			configVersionTag = new NBTTagCompound();
		}
	}

	public boolean shouldReset(Property property, ConfigRegistry configRegistry, String modID){
		NBTTagCompound propertyTag = configVersionTag.getCompound(getPropertyString(configRegistry, modID));
		propertyTag.setInt("version", 0);
		propertyTag.setString("comment", configRegistry.comment());
		propertyTag.setString("key", configRegistry.key());
		propertyTag.setString("config", configRegistry.config());

		String configValue = property.getString();
		String savedDefault = propertyTag.getString("default");
		boolean shouldReset = false;
		if(configValue.equals(savedDefault) && !configValue.equals(property.getDefault())){
			RebornCore.LOGGER.info(configRegistry.key() + " is being reset to new mod default:" + property.getDefault());
			shouldReset = true;
		} else {
			propertyTag.setString("default", property.getDefault());
		}
		configVersionTag.setTag(getPropertyString(configRegistry, modID), propertyTag);
		return shouldReset;
	}

	private String getPropertyString(ConfigRegistry configRegistry, String modid){
		return modid + "." + configRegistry.config() + "." + configRegistry.category() + "." + configRegistry.key();
	}

	private static class ConfigData {

		Configuration configuration;

		Map<String, Field> fieldMap;

		public ConfigData(Configuration configuration) {
			this.configuration = configuration;
			this.fieldMap = new HashMap<>();
		}

		public String getName(String category, Property property){
			return category + ":" + property.getName();
		}

	}

	@Override
	public List<RegistryTarget> getTargets() {
		return Collections.singletonList(RegistryTarget.FIELD);
	}

	@Override
	public Class<? extends Event> getProcessSate() {
		return RegistryConstructionEvent.class;
	}

	private static List getList(Property property, Field field){
		ParameterizedType genericType = (ParameterizedType) field.getGenericType();
		Class<?> listClass = (Class<?>) genericType.getActualTypeArguments()[0];
		if(listClass == String.class){
			return Arrays.asList(property.getStringList());
		} else if (listClass == ItemStack.class){
			List<ItemStack> stacks = Arrays.stream(property.getStringList())
				.map(s -> SerializationUtil.GSON_FLAT.fromJson(s, ItemStack.class))
				.collect(Collectors.toList());
			return stacks;
		}
		throw new UnsupportedOperationException("List type " + listClass.getName() + " not supported");
	}

	private static Property getListProperty(String category, String key, Object defaultValue, String comment, Class<?> type, Configuration configuration){
		List defaultList = (List) defaultValue;
		if(defaultList.isEmpty()){
			return configuration.get(category, key, new String[]{}, comment);
		} if (defaultList.get(0) instanceof ItemStack){
			String[] stackList = (String[]) defaultList.stream().map(o -> {
				ItemStack itemStack = (ItemStack) o;
				return SerializationUtil.GSON_FLAT.toJson(itemStack);
			}).toArray(String[]::new);
			return configuration.get(category, key, stackList, comment);
		}
		throw new UnsupportedOperationException("List type " + defaultList.get(0).getClass().getName() + " not supported");
	}

	/**
	 * Use to handle config settings similar to the traditional way of doing things.
	 *
	 * This needs to be registed before the configs are loaded. It might be best not to use this unless you need to.
	 */
	public class RebornRegistryEvent extends Event {

		public Configuration getConfiguration(String modid, String config){
			return getOrCreateConfig(modid, config);
		}

	}

}
