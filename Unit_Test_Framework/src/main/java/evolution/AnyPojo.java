package evolution;

import java.util.List;
import java.util.Map;

public class AnyPojo {
	private String name;
	private String gender;
	private AnotherPojo anotherPojo;
	private List<AnotherPojo> anotherPojos;
	private Map<String, AnotherPojo> anotherPojoMap;
	public Map<String, AnotherPojo> getAnotherPojoMap() {
		return anotherPojoMap;
	}
	public void setAnotherPojoMap(Map<String, AnotherPojo> anotherPojoMap) {
		this.anotherPojoMap = anotherPojoMap;
	}
	public List<AnotherPojo> getAnotherPojos() {
		return anotherPojos;
	}
	public void setAnotherPojos(List<AnotherPojo> anotherPojos) {
		this.anotherPojos = anotherPojos;
	}
	public AnotherPojo getAnotherPojo() {
		return anotherPojo;
	}
	public void setAnotherPojo(AnotherPojo anotherPojo) {
		this.anotherPojo = anotherPojo;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	@Override
	public String toString() {
		return "AnyPojo [name=" + name + ", gender=" + gender + ", anotherPojo=" + anotherPojo + ", anotherPojos="
				+ anotherPojos + ", anotherPojoMap=" + anotherPojoMap + "]";
	}
}
