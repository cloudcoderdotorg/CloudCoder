// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <david.hovemeyer@gmail.com>
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package org.cloudcoder.app.shared.model;

import java.io.Serializable;

/**
 * Model object representing a user.
 * 
 * @author David Hovemeyer
 */
public class User implements Serializable, IModelObject<User> {
	private static final long serialVersionUID = 1L;

	private int id;
	private String userName;
	private String firstname;
	private String lastname;
	private String email;
	private String passwordHash;
	private String website;
	private String consent;

	public static final ModelObjectField<? super User, Integer> ID = new ModelObjectField<User, Integer>("id", Integer.class, 0, ModelObjectIndexType.IDENTITY) {
		public void set(User obj, Integer value) { obj.setId(value); }
		public Integer get(User obj) { return obj.getId(); }
	};

	public static final ModelObjectField<? super User, String> USERNAME = new ModelObjectField<User, String>("username", String.class, 20, ModelObjectIndexType.UNIQUE) {
		public void set(User obj, String value) { obj.setUsername(value); }
		public String get(User obj) { return obj.getUsername(); }
	};

	public static final ModelObjectField<? super User, String> FIRSTNAME = new ModelObjectField<User, String>("firstname", String.class, 30) {
        public void set(User obj, String value) { obj.setFirstname(value); }
        public String get(User obj) { return obj.getFirstname(); }
    };

	public static final ModelObjectField<? super User, String> LASTNAME = new ModelObjectField<User, String>("lastname", String.class, 30) {
        public void set(User obj, String value) { obj.setLastname(value); }
        public String get(User obj) { return obj.getLastname(); }
    };

	public static final ModelObjectField<? super User, String> EMAIL = new ModelObjectField<User, String>("email", String.class, 50, ModelObjectIndexType.UNIQUE) {
        public void set(User obj, String value) { obj.setEmail(value); }
        public String get(User obj) { return obj.getEmail(); }
    };

	public static final ModelObjectField<? super User, String> PASSWORD_HASH =new ModelObjectField<User, String>("password_hash", String.class, 60) {
		public void set(User obj, String value) { obj.setPasswordHash(value); }
		public String get(User obj) { return obj.getPasswordHash(); }
	}; 
	
	public static final ModelObjectField<? super User, String> WEBSITE = new ModelObjectField<User, String>("website", String.class, 100) {
		public void set(User obj, String value) { obj.setWebsite(value); }
		public String get(User obj) { return obj.getWebsite(); }
	};
	
	public static final ModelObjectField<? super User, String> CONSENT = new ModelObjectField<User, String>("consent", String.class, 3) {
        public void set(User obj, String value) { obj.setConsent(value); }
        public String get(User obj) { return obj.getConsent(); }
    };
	
	/**
	 * Description of fields (schema version 0).
	 */
	public static final ModelObjectSchema<User> SCHEMA_V0 = new ModelObjectSchema<User>("user")
		.add(ID)
		.add(USERNAME)
		.add(FIRSTNAME)
        .add(LASTNAME)
        .add(EMAIL)
		.add(PASSWORD_HASH);
	
	/**
	 * Description of fields (schema version 1).
	 */
	public static final ModelObjectSchema<User> SCHEMA_V1 = ModelObjectSchema.basedOn(SCHEMA_V0)
		.addAfter(EMAIL, WEBSITE)
		.finishDelta();
	
	/**
     * Description of fields (schema version 1).
     */
    public static final ModelObjectSchema<User> SCHEMA_V2 = ModelObjectSchema.basedOn(SCHEMA_V1)
        .addAfter(WEBSITE, CONSENT)
        .finishDelta();
	
	/**
	 * Description of fields (current schema version).
	 */
	public static final ModelObjectSchema<User> SCHEMA = SCHEMA_V2;

	public static final String GIVEN_CONSENT = "Y";
	public static final String NO_CONSENT = "N";
	public static final String PENDING_CONSENT = "";

	/**
	 * Constructor.
	 */
	public User() {
	    this.website="";
	    this.consent="";
	}
	
	@Override
	public ModelObjectSchema<User> getSchema() {
		return SCHEMA;
	}
	
	/**
	 * Set the user's unique id.
	 * @param id the user's unique id
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * Get the user's unique id.
	 * @return the user's unique id
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Set the user's username.
	 * @param userName the user's username
	 */
	public void setUsername(String userName) {
		this.userName = userName;
	}
	
	/**
	 * Get the user's username.
	 * @return the user's username
	 */
	public String getUsername() {
		return userName;
	}
	
	/**
	 * Set the user's bcrypt password hash. 
	 * @param passwordHash the user's bcrypt password hash
	 */
	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}
	
	/**
	 * Get the user's bcrypt password hash.
	 * @return the user's brcypt password hash
	 */
	public String getPasswordHash() {
		return passwordHash;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Get the user's website URL.
     * 
     * @return the user's website URL
     */
    public String getWebsite() {
		return website;
	}
    
    /**
     * Set the user's website URL.
     * 
     * @param website the user's website URL
     */
    public void setWebsite(String website) {
		this.website = website;
	}

    public String getConsent() {
        return consent;
    }

    public void setConsent(String consent) {
        this.consent = consent;
    }
}
