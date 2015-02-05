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
}
