package it.uspread.core


class Spread {

    User user
    Date date

    Spread(User user) {
        this(user, new Date())
    }

    Spread(User user, Date date) {
        this.user = user
        this.date = date
    }

    static constraints = {
        user(nullable: false)
        date(nullable: false)
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
