package evolution;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class Mocker {
	private List<String> stringVocabulary;
	
	public Mocker() {
		stringVocabulary = Arrays.asList("Donald Trump", "cat", "dog", "Abraham Lincoln");
	}
	
	public String mockString() {// Make it smart.
		return stringVocabulary.get(((Double) Math.floor(Math.random() * stringVocabulary.size())).intValue());
	}
	
	public Integer mockInt() {
		Double result = (Math.random() < .5 ? 1 : -1) * Math.random() * Integer.MAX_VALUE;
		return result.intValue();
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
	
	@SuppressWarnings("unchecked")
	public <T> List<T> mockList(Method method, int parameterIndex) throws NoSuchMethodException, SecurityException, ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<?> clazz = typeArguments(method, parameterIndex).get(0);
		List<T> list = new LinkedList<>();
		for (int i = 0; i < 5; i++) {
			T t = (T) mockObject(clazz);
			list.add(t);
		}
		return list;
	}
	
	public Double mockDouble() {
		return (Math.random() < .5 ? 1 : -1) * Math.random() * Double.MAX_VALUE;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T mockObject(Method method, int parameterIndex) throws NoSuchMethodException, SecurityException, ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<?> parameterType = method.getParameterTypes()[parameterIndex];
		if (parameterType == List.class) {
			return (T) mockList(method, parameterIndex);
		} else if (parameterType == Map.class) {
			return (T) mockMap(method, parameterIndex); 
		}
		return null;// TODO Add Set Support
	}
	
	@SuppressWarnings("unchecked")
	public <T> T mockObject(Class<T> clazz) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (clazz == int.class || clazz == Integer.class) {
			return (T) mockInt();
		} else if (clazz == String.class) {
			return (T) mockString();
		} else if (clazz == double.class || clazz == Double.class) {
			return (T) mockDouble();
		} else {
			return mockPojo(clazz);
		}
		// TODO Also consider the list and map case.
	}
	
	@SuppressWarnings("unchecked")
	public <T, V> Map<T, V> mockMap(Method method, int parameterIndex) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {
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
	
	public <T> T mockPojo(Class<T> clazz) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		T t = clazz.newInstance();
		Method[] methods = clazz.getMethods();
		for (Method method : methods) {
			if (method.getName().startsWith("set")) {
				Class<?>[] parameterTypes = method.getParameterTypes();
				if (parameterTypes.length == 1) {
					Class<?> parameterType = parameterTypes[0];
					if (parameterType == String.class) {
						method.invoke(t, mockString());
					} else if (parameterType == int.class || parameterType == Integer.class) {
						method.invoke(t, mockInt());
					} else {// Invoke recursion if the field is also a POJO.
						method.invoke(t, mockPojo(parameterType));
					}
				}
			}
		}
		return t;
	}
	
	public Object mockInvokingMethod(Method method, Object currentInstance) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {
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
	
	@Test
	public <T, V> void testMockObject() throws NoSuchMethodException, SecurityException, ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Method method = AnyClass.class.getMethod("anotherMethod", List.class, Map.class);
		List<T> list = mockList(method, 0);
		System.out.println(list);
		Map<T, V> map = mockMap(method, 1);
		System.out.println(map);
		AnyPojo anyPojo = mockPojo(AnyPojo.class);
		System.out.println(anyPojo);
	}
	
	@Test
	public void testMockingInvokingMethod() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {
		AnyClass anyClass = new AnyClass();
		Method method = AnyClass.class.getMethod("anyMethod", AnyPojo.class);
		Object result = mockInvokingMethod(method, anyClass);
		System.out.println(result);
	}
}
