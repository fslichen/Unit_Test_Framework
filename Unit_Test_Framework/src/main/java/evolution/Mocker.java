package evolution;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

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
		Double result = Math.random() * 100;
		return result.intValue();
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
	
	public Object mockInvokingMethod(Method method, Object currentInstance) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<?>[] parameterTypes = method.getParameterTypes();
		Object[] arguments = new Object[parameterTypes.length];
		int i = 0;
		for (Class<?> parameterType : parameterTypes) {
			if (parameterType == String.class) {
				arguments[i] = mockString();
			} else if (parameterType == int.class || parameterType == Integer.class) {
				arguments[i] = mockInt();
			} else {
				arguments[i] = mockPojo(parameterType);
			}
			i++;
		}
		return method.invoke(currentInstance, arguments);
	}
	
	@Test
	public void test() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		AnyPojo anyPojo = mockPojo(AnyPojo.class);
		System.out.println(anyPojo);
//		mockInvokingMethod();
	}
}
