package it.uspread.core

class User {

	transient springSecurityService

	String username
	String password
	boolean enabled = true
	boolean accountExpired
	boolean accountLocked
	boolean passwordExpired
	String email

    static hasMany = [messages: Message]
    static mappedBy = [messages: 'author']

	static transients = ['springSecurityService']

	static constraints = {
		username blank: false, unique: true
		password blank: false
		email(unique: true, email: true, blank: false)
	}

	static mapping = {
		password column: '`password`'
		messages cascade: 'all-delete-orphan'
	}

	Set<Role> getAuthorities() {
		UserRole.findAllByUser(this).collect { it.role }
	}

	def beforeInsert() {
		encodePassword()
	}

	def beforeUpdate() {
		if (isDirty('password')) {
			encodePassword()
		}
	}

	protected void encodePassword() {
		password = springSecurityService?.passwordEncoder ? springSecurityService.encodePassword(password) : password
	}
	
	String toString(){
        return null != email ? email : "<EMPTY>"
    }
}
