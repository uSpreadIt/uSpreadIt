package it.uspread.core


class Spread {

    User user
    Date dateSpread

    Spread(User user) {
        this(user, new Date())
    }

    Spread(User user, Date dateSpread) {
        this.user = user
        this.dateSpread = dateSpread
    }

    static constraints = {
        user(nullable: false)
        dateSpread(nullable: false)
    }
}
