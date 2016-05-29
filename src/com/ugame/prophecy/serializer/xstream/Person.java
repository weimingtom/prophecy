package com.ugame.prophecy.serializer.xstream;

public class Person {
    private String firstname;
    private String lastname;
    private PhoneNumber phone;
    private PhoneNumber fax;
    
    // ... constructors and methods
    public Person() {
	
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public PhoneNumber getPhone() {
        return phone;
    }

    public void setPhone(PhoneNumber phone) {
        this.phone = phone;
    }

    public PhoneNumber getFax() {
        return fax;
    }

    public void setFax(PhoneNumber fax) {
        this.fax = fax;
    }
    
    @Override
    public String toString() {
	StringBuffer sb = new StringBuffer();
	sb.append("firstname : " + firstname + "\n");
	sb.append("lastname : " + lastname + "\n");
	sb.append("phone : " + phone + "\n");
	sb.append("fax : " + fax + "\n");
	return sb.toString();
    }
}
