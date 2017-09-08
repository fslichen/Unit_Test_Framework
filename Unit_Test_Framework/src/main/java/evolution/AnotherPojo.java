package evolution;

public class AnotherPojo {
	private String address;
	private Integer age;
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public Integer getAge() {
		return age;
	}
	public void setAge(Integer age) {
		this.age = age;
	}
	@Override
	public String toString() {
		return "AnotherPojo [address=" + address + ", age=" + age + "]";
	}
}
