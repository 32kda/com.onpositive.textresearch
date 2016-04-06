package com.onpositive.semantic.wikipedia2.docclasses;

import java.io.Serializable;
import java.util.HashSet;

public class PersonNaming implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected static PersonNaming instance=new PersonNaming();
	
	protected static HashSet<String>firstNames=new HashSet<String>();
	protected static HashSet<String>lastNames=new HashSet<String>();
	

	public static class Person implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		protected String firstName;
		protected String lastName;

		public Person(String fString, String lName) {
			this.firstName = fString;
			this.lastName = lName;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((firstName == null) ? 0 : firstName.hashCode());
			result = prime * result
					+ ((lastName == null) ? 0 : lastName.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Person other = (Person) obj;
			if (firstName == null) {
				if (other.firstName != null)
					return false;
			} else if (!firstName.equals(other.firstName))
				return false;
			if (lastName == null) {
				if (other.lastName != null)
					return false;
			} else if (!lastName.equals(other.lastName))
				return false;
			return true;
		}
	}

	protected HashSet<Person> ps = new HashSet<PersonNaming.Person>();
	protected HashSet<String>fullNames=new HashSet<String>();

	public static PersonNaming getInstance() {
		
		return instance;
	}

	public void add(String pageTitle) {
		if (pageTitle==null){
			return ;
		}
		String replace = pageTitle.toLowerCase().replace('_', ' ');
		if( fullNames.contains(replace)){
			return;
		}
		fullNames.add(replace);
		Person person = buildPerson(pageTitle);
		if (person.firstName!=null&&person.firstName.length()==0&&person.lastName!=null&&person.lastName.length()==0){
			return;
		}
		if (person.firstName.length()>3){
			firstNames.add(person.firstName);
			}
		if (person.lastName==null||person.lastName.length()==0){
			return;
		}
		
		
		if (person.lastName.length()>3){
		lastNames.add(person.lastName);
		}
		ps.add(person);
	}

	private Person buildPerson(String pageTitle) {		
		StringBuilder bld = new StringBuilder();
		String fString = null;
		String lName = null;
		boolean in = true;
		boolean hasC=false;
		for (int a = 0; a < pageTitle.length(); a++) {
			char charAt = pageTitle.charAt(a);
			if (charAt==','){
				hasC=true;
			}
			if (!Character.isLetter(charAt)) {
				if (in) {
					String string = bld.toString();
					if (string.equals("бен")){
						bld = new StringBuilder();
						in = false;
						continue;
					}
					if (string.equals("ибн")){
						bld = new StringBuilder();
						in = false;
						continue;
					}
					if (string.equals("бин")){
						bld = new StringBuilder();
						in = false;
						continue;
					}
					
					if (fString == null) {
						
						fString = string;
						
					} else {
						if (lName == null) {
							lName = string;
						}
					}
					bld = new StringBuilder();
				}
				in = false;
				continue;
			}
			in = true;
			char c = Character.toLowerCase(charAt);
			if(lName!=null){
				hasC=true;
				break;
			}
			bld.append(c);
		}
		if (lName==null){
			lName=bld.toString();
		}
		if (hasC){
			String b=fString;
			fString=lName;
			lName=b;
		}
		if (lName!=null&&fString==null){
			fString=lName;
			lName=null;
		}
		Person person = new Person(fString, lName);
		return person;
	}

	public boolean isPerson(String catName) {
		if (catName==null){
			return false;
		}		
		if (fullNames.contains(catName.toLowerCase().replace('_', '_'))){
			return true;
		}
		
		Person buildPerson = buildPerson(catName);
		if(isPerson(buildPerson)){
			return true;
		}
		Person person = new Person(buildPerson.lastName, buildPerson.firstName);
		if (isPerson(person)){
			return true;
		}
		
		return false;
	}

	private boolean isPerson(Person buildPerson) {
		if (ps.contains(buildPerson)){
			return true;
		}
		if (firstNames.contains(buildPerson.firstName)){
			if (lastNames.contains(buildPerson.lastName)){
				return true;
			}
		}
		return false;
	}
}