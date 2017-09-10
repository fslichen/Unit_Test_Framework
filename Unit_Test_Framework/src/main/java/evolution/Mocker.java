package evolution;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

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
	
	public Object mockInvokingMethod(Method method, Object currentInstance) throws Exception {
		Class<?>[] parameterTypes = method.getParameterTypes();
		Object[] arguments = new Object[parameterTypes.length];
		int i = 0;
		for (Class<?> parameterType : parameterTypes) {
			if (parameterType == List.class || parameterType == Map.class) {
				arguments[i] = mockObject(method, i);// TODO Add mockObject for method and parameter index.
			} else {
				arguments[i] = mockObject(parameterType);
			}
			i++;
		}
		return method.invoke(currentInstance, arguments);
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
	
	public <T> List<String> reverseEngineer(String objectName, T t) throws Exception {
		List<String> codes = new LinkedList<>();
		Class<?> clazz = t.getClass();
		if (clazz == String.class) {// TODO Add more criteria.
			codes.add(String.format("String %s = \"%s\";", objectName, t));
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
					int i = 0;
					StringBuilder itemNames = new StringBuilder();
					for (Object item : (List<?>) fieldObject) {
						String itemName = fieldName + i++;
						codes.addAll(reverseEngineer(itemName, item));
						itemNames.append(itemName + ", ");
					}
					codes.add(String.format("%s.set%s(Arrays.asList(%s));", objectName, capitalizeFirstCharacter(fieldName), itemNames.substring(0, itemNames.length() - 2)));
				} else if (fieldType == Map.class) {
					int i = 0;
					for (Entry<?, ?> entry : ((Map<?, ?>) fieldObject).entrySet()) {
						String keyName = fieldName + "Key" + i;
						String valueName = fieldName + "Value" + i;
						codes.addAll(reverseEngineer(keyName, entry.getKey()));
						codes.addAll(reverseEngineer(valueName, entry.getValue()));
						codes.add(String.format("%s.put(%s, %s);", objectName, keyName, valueName));
						i++;
					}
				}
				else {// POJO
					codes.addAll(reverseEngineer(fieldName, fieldObject));
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
	
//	@Test
	public void testMockingInvokingMethod() throws Exception {
		AnyClass anyClass = new AnyClass();
		Method method = AnyClass.class.getMethod("anyMethod", AnyPojo.class);
		Object result = mockInvokingMethod(method, anyClass);
		System.out.println(result);
	}
	
//	@Test
	public <T, V> void testMockObject() throws Exception {
		Method method = AnyClass.class.getMethod("anotherMethod", List.class, Map.class);
		List<T> list = mockList(method, 0);
		System.out.println(list);
		Map<T, V> map = mockMap(method, 1);
		System.out.println(map);
		AnyPojo anyPojo = mockPojo(AnyPojo.class);
		System.out.println(anyPojo);
	}
	
	@Test
	public void testReverseEngineer() throws Exception {
//		reverseEngineer("anyInt", 3);
//		reverseEngineer("anyString", "PlayBoy");
		reverseEngineer("anyPojo", mockObject(AnyPojo.class)).forEach(System.out::println);
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
