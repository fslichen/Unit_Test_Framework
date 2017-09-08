package evolution;

public class AnyPojo {
	private String name;
	private String gender;
	private AnotherPojo anotherPojo;
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
		return "AnyPojo [name=" + name + ", gender=" + gender + ", anotherPojo=" + anotherPojo + "]";
	}
}
