package evolution;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class AnyTest {
//	@Test
	public void testReverseEngineer() throws Exception {
		Mocker mocker = new Mocker();
		mocker.reverseEngineerObject("anyInt", 3).forEach(System.out::println);
		mocker.reverseEngineerObject("anyString", "PlayBoy").forEach(System.out::println);
		mocker.reverseEngineerObject("anyPojo", mocker.mockObject(AnyPojo.class)).forEach(System.out::println);
	}
	
//	@Test
	public <T, V> void testMockObject() throws Exception {
		Mocker mocker = new Mocker();
		Method method = AnyClass.class.getMethod("anotherMethod", List.class, Map.class);
		List<T> list = mocker.mockList(method, 0);
		System.out.println(list);
		Map<T, V> map = mocker.mockMap(method, 1);
		System.out.println(map);
		AnyPojo anyPojo = mocker.mockPojo(AnyPojo.class);
		System.out.println(anyPojo);
	}
	
//	@Test
	public void testReverseEngineerList() throws Exception {
		Mocker mocker = new Mocker();
		List<String> codes = new LinkedList<>();
		List<AnyPojo> anyPojos = new LinkedList<>();
		anyPojos.addAll(Arrays.asList(mocker.mockObject(AnyPojo.class), mocker.mockObject(AnyPojo.class)));
		System.out.println(mocker.reverseEngineerList("anyPojos", anyPojos, codes));
		codes.forEach(System.out::println);
	}
	
	@Test
	public void testMockingInvokingMethod() throws Exception {
		Mocker mocker = new Mocker();
		AnyClass anyClass = new AnyClass();
		Method method = AnyClass.class.getMethod("anyMethod", AnyPojo.class, List.class, Map.class);
		mocker.mockInvokingMethod(method, anyClass).forEach(System.out::println);
	}
}
