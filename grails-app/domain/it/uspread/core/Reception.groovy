package it.uspread.core


class Reception {

    User user
    Date dateReception

    Reception(User user) {
        this(user, new Date())
    }

    Reception(User user, Date dateReception) {
        this.user = user
        this.dateReception = dateReception
    }

    static constraints = {
        user(nullable: false)
        dateReception(nullable: false)
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof Reception)) return false

        Reception reception = (Reception) o

        if (user != reception.user) return false

        return true
    }

    int hashCode() {
        return user.hashCode()
    }
}
