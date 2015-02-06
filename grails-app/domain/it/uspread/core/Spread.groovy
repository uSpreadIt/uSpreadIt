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

    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof Spread)) return false

        Spread spread = (Spread) o

        if (user != spread.user) return false

        return true
    }

    int hashCode() {
        return user.hashCode()
    }
}
