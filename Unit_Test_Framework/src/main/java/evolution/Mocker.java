package evolution;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class Mocker {
	private List<String> stringVocabulary;
	
	public Mocker() {
		stringVocabulary = Arrays.asList("Donald Trump", "cat", "dog", "Abraham Lincoln");
	}
	
	public String capitalizeFirstCharacter(String string) {
		return string.substring(0, 1).toUpperCase() + string.substring(1);
	}
	
	public String fieldName(Method method) {// Getter or Setter
		String methodName = method.getName();
		int startIndex = 3;
		if ((methodName.startsWith("set") || methodName.startsWith("get"))) {
			return methodName.substring(startIndex, startIndex + 1).toLowerCase() + methodName.substring(startIndex + 1);
		}
		return null;
	}
	
	public <T> List<Method> getters(Class<T> clazz) throws Exception {
		Method[] methods = clazz.getMethods();
		List<Method> setters = new LinkedList<>();
		for (Method method : methods) {
			String methodName = method.getName();
			if (methodName.startsWith("get") && 
					!"getClass".equals(methodName)) {
				if (method.getParameterTypes().length == 0) {
					setters.add(method);
				}
			}
		}
		return setters;
	}
	
	public Double mockDouble() {
		return (Math.random() < .5 ? 1 : -1) * Math.random() * Double.MAX_VALUE;
	}
	
	public Integer mockInt() {
		Double result = (Math.random() < .5 ? 1 : -1) * Math.random() * Integer.MAX_VALUE;
		return result.intValue();
	}
	
	public List<String> mockInvokingMethod(Method method, Object currentInstance) throws Exception {
		List<String> codes = new LinkedList<>();
		String currentInstanceClassName = currentInstance.getClass().getSimpleName();
		codes.add(String.format("%s currentInstance = new %s();", currentInstanceClassName, currentInstanceClassName));
		Class<?>[] parameterTypes = method.getParameterTypes();
		Object[] arguments = new Object[parameterTypes.length];
		int i = 0;
		StringBuilder argumentNames = new StringBuilder();
		for (Class<?> parameterType : parameterTypes) {
			if (parameterType == List.class || parameterType == Map.class) {
				arguments[i] = mockObject(method, i);
			} else {
				arguments[i] = mockObject(parameterType);
			}
			String argumentName = "argument" + i;
			codes.addAll(reverseEngineerObject(argumentName, arguments[i]));
			argumentNames.append(argumentName + ", ");
			i++;
		}
		Object result = method.invoke(currentInstance, arguments);
		String argumentNamesInString = argumentNames.substring(0, argumentNames.length() - 2);
		codes.add(String.format("Object result = currentInstance.%s(%s);", method.getName(), argumentNamesInString));
		codes.add(String.format("assert currentInstance.%s(%s).equals(result);", method.getName(), argumentNamesInString));
		System.out.println("The result = " + result);
		return codes;
	}
	
	@SuppressWarnings("unchecked")
	public <T> List<T> mockList(Method method, int parameterIndex) throws Exception {
		Class<?> clazz = typeArguments(method, parameterIndex).get(0);
		List<T> list = new LinkedList<>();
		for (int i = 0; i < 5; i++) {
			T t = (T) mockObject(clazz);
			list.add(t);
		}
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public <T, V> Map<T, V> mockMap(Method method, int parameterIndex) throws Exception {
		List<Class<?>> typeArguments = typeArguments(method, parameterIndex);
		Class<?> keyClass = typeArguments.get(0);
		Class<?> valueClass = typeArguments.get(1);
		Map<T, V> map = new HashMap<>();
		for (int i = 0; i < 5; i++) {// TODO Make the size random.
			T t = (T) mockObject(keyClass);
			V v = (V) mockObject(valueClass);
			map.put(t, v);
		}
		return map;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T mockObject(Class<T> clazz) throws Exception {
		if (clazz == int.class || clazz == Integer.class) {
			return (T) mockInt();
		} else if (clazz == String.class) {
			return (T) mockString();
		} else if (clazz == double.class || clazz == Double.class) {
			return (T) mockDouble();
		} else if (clazz == List.class) {
			System.out.println("Unable to mock list due to type erasure in JVM.");
			return null;
		} else if (clazz == Map.class) {
			System.out.println("Unable to mock map due to type erasure in JVM.");
			return null;
		} else {
			return mockPojo(clazz);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> T mockObject(Method method, int parameterIndex) throws Exception {
		Class<?> parameterType = method.getParameterTypes()[parameterIndex];
		if (parameterType == List.class) {
			return (T) mockList(method, parameterIndex);
		} else if (parameterType == Map.class) {
			return (T) mockMap(method, parameterIndex); 
		}
		return null;
	}
	
	public <T> T mockPojo(Class<T> clazz) throws Exception {
		T t = clazz.newInstance();
		for (Method setter : setters(clazz)) {
			Class<?> parameterType = setter.getParameterTypes()[0];
			if (parameterType == String.class) {
				setter.invoke(t, mockString());
			} else if (parameterType == int.class || parameterType == Integer.class) {
				setter.invoke(t, mockInt());
			} else if (parameterType == List.class) {
				setter.invoke(t, mockList(setter, 0));
			} else if (parameterType == Map.class) {
				setter.invoke(t, mockMap(setter, 0));
			} else {// Invoke recursion if the field is also a POJO.
				setter.invoke(t, mockPojo(parameterType));
			}
		}
		return t;
	}
	
	public String mockString() {// Make it smart.
		return stringVocabulary.get(((Double) Math.floor(Math.random() * stringVocabulary.size())).intValue());
	}
	
	public String reverseEngineerList(String itemBaseName, List<?> list, List<String> codes) throws Exception {
		int i = 0;
		StringBuilder itemNames = new StringBuilder();
		for (Object item : list) {
			String itemName = itemBaseName + i++;
			codes.addAll(reverseEngineerObject(itemName, item));
			itemNames.append(itemName + ", ");
		}
		return itemNames.substring(0, itemNames.length() - 2);
	}
	
	public List<String> reverseEngineerMap(String itemBaseName, Map<?, ?> map, List<String> codes) throws Exception {
		int i = 0;
		String keyClassName = map.keySet().toArray()[0].getClass().getSimpleName();
		String valueClassName = map.values().toArray()[0].getClass().getSimpleName();
		codes.add(String.format("Map<%s, %s> %s = new HashMap<>();", keyClassName, valueClassName, itemBaseName));
		List<String> keyValuePairNames = new LinkedList<>();
		for (Entry<?, ?> entry : map.entrySet()) {
			String keyName = itemBaseName + "Key" + i;
			String valueName = itemBaseName + "Value" + i;
			codes.addAll(reverseEngineerObject(keyName, entry.getKey()));
			codes.addAll(reverseEngineerObject(valueName, entry.getValue()));
			codes.add(String.format("%s.put(%s, %s)", itemBaseName, keyName, valueName));
			keyValuePairNames.add(keyName + ", " + valueName);
			i++;
		}
		return keyValuePairNames;
	}

	public <T> List<String> reverseEngineerObject(String objectName, T t) throws Exception {
		List<String> codes = new LinkedList<>();
		Class<?> clazz = t.getClass();
		if (clazz == String.class) {// TODO Add more criteria. List and Map
			codes.add(String.format("String %s = \"%s\";", objectName, t));
		} else if (clazz == int.class || clazz == Integer.class) {
			codes.add(String.format("Integer %s = %s;", objectName, t));
		} else if (clazz == LinkedList.class || clazz == ArrayList.class) {
			List<?> list = (List<?>) t;
			codes.add(String.format("List<%s> %s = Arrays.asList(%s);", list.get(0).getClass().getSimpleName(), objectName, reverseEngineerList(objectName, list, codes)));
		} else if (clazz == Map.class || clazz == HashMap.class || clazz == TreeMap.class || clazz == LinkedHashMap.class) {
			reverseEngineerMap(objectName, (Map<?, ?>) t, codes);
		} else {// POJO
			String objectClassName = t.getClass().getSimpleName();
			codes.add(String.format("%s %s = %s;", objectClassName, objectName, String.format("new %s()", objectClassName)));
			for (Method getter : getters(clazz)) {
				Class<?> fieldType = getter.getReturnType();
				String fieldName = fieldName(getter);
				Object fieldObject = getter.invoke(t);
				if (fieldType == String.class) {// TODO Add more criteria.
					codes.add(String.format("%s.set%s(\"%s\");", objectName, capitalizeFirstCharacter(fieldName), fieldObject));
				} else if (fieldType == int.class || fieldType == Integer.class) {
					codes.add(String.format("%s.set%s(%s);", objectName, capitalizeFirstCharacter(fieldName), fieldObject));
				} else if (fieldType == List.class) {
					codes.add(String.format("%s.set%s(Arrays.asList(%s));", objectName, capitalizeFirstCharacter(fieldName), reverseEngineerList(fieldName, (List<?>) fieldObject, codes)));
				} else if (fieldType == Map.class) {
					reverseEngineerMap(fieldName, (Map<?, ?>) fieldObject, codes);
					codes.add(String.format("%s.set%s(%s);", objectName, capitalizeFirstCharacter(fieldName), fieldName));
				} else {// POJO
					codes.addAll(reverseEngineerObject(fieldName, fieldObject));
					codes.add(String.format("%s.set%s(%s);", objectName, capitalizeFirstCharacter(fieldName), fieldName));
				}
			}
		}
		return codes;
	}
	
	public <T> List<Method> setters(Class<T> clazz) throws Exception {
		Method[] methods = clazz.getMethods();
		List<Method> setters = new LinkedList<>();
		for (Method method : methods) {
			if (method.getName().startsWith("set")) {
				if (method.getParameterTypes().length == 1) {
					setters.add(method);
				}
			}
		}
		return setters;
	}
		
	public List<Class<?>> typeArguments(Method method, int parameterIndex) throws NoSuchMethodException, SecurityException, ClassNotFoundException {
		Type[] types = method.getGenericParameterTypes();
		String typeName = types[parameterIndex].getTypeName();
		String[] typeArgumentNames = typeName.substring(typeName.indexOf("<") + 1, typeName.indexOf(">")).split(",");
		List<Class<?>> typeArguments = new LinkedList<>();
		for (String typeArgumentName : typeArgumentNames) {
			Class<?> clazz = Class.forName(typeArgumentName.replace(" ", ""));
			typeArguments.add(clazz);
		}
		return typeArguments;
	}
}
