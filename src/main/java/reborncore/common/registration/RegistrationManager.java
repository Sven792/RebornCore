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

package reborncore.common.registration;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.language.ModFileScanData;
import org.objectweb.asm.Type;
import reborncore.RebornCore;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Mark on 26/02/2017.
 */
public class RegistrationManager {

	static List<IRegistryFactory> factoryList = new ArrayList<>();
	static List<Class> registryClasses = new ArrayList<>();

	public static void init() {
		long start = System.currentTimeMillis();

		List<ModFileScanData> scanData = ModList.get().getAllScanData();
		loadFactorys(scanData);


		List<ModFileScanData.AnnotationData> registryData = scanData.stream()
				.map(ModFileScanData::getAnnotations)
				.flatMap(Collection::stream).
				filter(a -> Objects.equals(a.getClassType(), Type.getType(RebornRegister.class)))
				.collect(Collectors.toList());

		registryData.sort(Comparator.comparingInt(RegistrationManager::getPriority));

		for (ModFileScanData.AnnotationData data : registryData) {
			try {
				Class clazz = Class.forName(data.getClassType().getClassName());
				if(isEarlyReg(data)){
					handleClass(clazz, null);
					continue;
				}
				registryClasses.add(Class.forName(data.getClassType().getClassName()));
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		//Sorts all the classes to (try) and ensure they are loaded in the same oder on the client/server.
		//Hopefully this fixes the issue with packets being misaligned
		registryClasses.sort(Comparator.comparing(Class::getCanonicalName));
		RebornCore.LOGGER.info("Pre loaded registries in" + (System.currentTimeMillis() - start) + "ms");
	}

	private static int getPriority(ModFileScanData.AnnotationData annotationData){
		if(annotationData.getAnnotationData().containsKey("priority")){
			return -(int) annotationData.getAnnotationData().get("priority");
		}
		return 0;
	}

	private static boolean isEarlyReg(ModFileScanData.AnnotationData annotationData){
		if(annotationData.getAnnotationData().containsKey("earlyReg")){
			return (boolean) annotationData.getAnnotationData().get("earlyReg");
		}
		return false;
	}

	public static void load(Event event) {
		long start = System.currentTimeMillis();
		//TODO 1.13 and active mod container
		final ModContainer activeMod = null;

		List<IRegistryFactory> factoryList = getFactorysForSate(event.getClass());
		if (!factoryList.isEmpty()) {
			for (Class clazz : registryClasses) {
				handleClass(clazz, activeMod);
			}
			factoryList.forEach(IRegistryFactory::factoryComplete);
			setActiveModContainer(activeMod);
		}

		RebornCore.LOGGER.info("Loaded registrys for " + event.getClass().getName() + " in " + (System.currentTimeMillis() - start) + "ms");
	}

	private static void handleClass(Class clazz, ModContainer activeMod){
		RebornRegister annotation = (RebornRegister) getAnnoation(clazz.getAnnotations(), RebornRegister.class);
		if (annotation != null) {
			if (activeMod != null && !activeMod.getModId().equals(annotation.modID())) {
				setActiveMod(annotation.modID());
			}
		}
		for (IRegistryFactory regFactory : factoryList) {
			for (Field field : clazz.getDeclaredFields()) {
				if (!regFactory.getTargets().contains(RegistryTarget.FIELD)) {
					continue;
				}
				if (field.isAnnotationPresent(regFactory.getAnnotation())) {
					regFactory.handleField(field);
				}
			}
			for (Method method : clazz.getDeclaredMethods()) {
				if (!regFactory.getTargets().contains(RegistryTarget.MEHTOD)) {
					continue;
				}
				if (method.isAnnotationPresent(regFactory.getAnnotation())) {
					regFactory.handleMethod(method);
				}

			}
			if(regFactory.getTargets().contains(RegistryTarget.CLASS)){
				if(clazz.isAnnotationPresent(regFactory.getAnnotation())){
					regFactory.handleClass(clazz);
				}
			}
		}
	}

	private static List<IRegistryFactory> getFactorysForSate(Class<? extends net.minecraftforge.eventbus.api.Event> event) {
		return factoryList.stream().filter(iRegistryFactory -> iRegistryFactory.getProcessSate() == event).collect(Collectors.toList());
	}

	public static Annotation getAnnoationFromArray(Annotation[] annotations, IRegistryFactory factory) {
		for (Annotation annotation : annotations) {
			if (annotation.annotationType() == factory.getAnnotation()) {
				return annotation;
			}
		}
		return null;
	}

	public static Annotation getAnnoation(Annotation[] annotations, Class annoation) {
		for (Annotation annotation : annotations) {
			if (annotation.annotationType() == annoation) {
				return annotation;
			}
		}
		return null;
	}

	private static void loadFactorys(List<ModFileScanData> scanData) {
		List<ModFileScanData.AnnotationData> dataSet = scanData.stream()
				.map(ModFileScanData::getAnnotations)
				.flatMap(Collection::stream).
						filter(a -> Objects.equals(a.getClassType(), Type.getType(IRegistryFactory.RegistryFactory.class)))
				.collect(Collectors.toList());

		for (ModFileScanData.AnnotationData data : dataSet) {
			try {
				Class clazz = Class.forName(data.getClassType().getClassName());
				IRegistryFactory.RegistryFactory registryFactory = (IRegistryFactory.RegistryFactory) getAnnoation(clazz.getAnnotations(), IRegistryFactory.RegistryFactory.class);
				if (!registryFactory.side().canExcetue()) {
					continue;
				}
				//TODO 1.13 and new mod loader
//				if (!Loader.isModLoaded(registryFactory.modID())) {
//					continue;
//				}
				Object object = clazz.newInstance();
				if (object instanceof IRegistryFactory) {
					IRegistryFactory factory = (IRegistryFactory) object;
					factoryList.add(factory);
				}
			} catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
				e.printStackTrace();
			}
		}
	}

	private static void setActiveMod(String modID) {
		//TODO 1.13 mod loader and active mods
//		for (ModContainer modContainer : Loader.instance().getActiveModList()) {
//			if (modContainer.getModId().equals(modID)) {
//				setActiveModContainer(modContainer);
//				break;
//			}
//		}
	}

	private static void setActiveModContainer(ModContainer container) {
		//Loader.instance().setActiveModContainer(container);
	}

}
