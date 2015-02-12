package it.uspread.core

import it.uspread.core.type.ReportType

class Report {

    User reporter
    ReportType type

    Report(User reporter) {
        this(reporter, ReportType.INAPPROPRIATE)
    }

    Report(User reporter, ReportType type) {
        this.reporter = reporter
        this.type = type
    }
    static constraints = {
        reporter(nullable: false)
        type(nullable: false)
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof Report)) return false

        Report report = (Report) o

        if (reporter != report.reporter) return false

        return true
    }

    int hashCode() {
        return reporter.hashCode()
    }
}
