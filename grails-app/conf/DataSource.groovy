dataSource {
    pooled = true
    jmxExport = true
    driverClassName = "org.h2.Driver"
    username = "sa"
    password = ""
    // logSql = true
}
hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = false
//    cache.region.factory_class = 'net.sf.ehcache.hibernate.EhCacheRegionFactory' // Hibernate 3
    cache.region.factory_class = 'org.hibernate.cache.ehcache.EhCacheRegionFactory' // Hibernate 4
    singleSession = true // configure OSIV singleSession mode
    flush.mode = 'manual' // OSIV session flush mode outside of transactional context
}

// environment specific settings
environments {
    development {
        dataSource {
            dbCreate = "create-drop" // one of 'create', 'create-drop', 'update', 'validate', ''
            url = "jdbc:h2:mem:devDb;MVCC=TRUE;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE"
        }
    }
    test {
        dataSource {
            dbCreate = "update"
            url = "jdbc:h2:mem:testDb;MVCC=TRUE;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE"
        }
    }
    production {
        // default grails
//        dataSource {
//            dbCreate = "update"
//            url = "jdbc:h2:prodDb;MVCC=TRUE;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE"
//            properties {
//                // See http://grails.org/doc/latest/guide/conf.html#dataSource for documentation
//                jmxEnabled = true
//                initialSize = 5
//                maxActive = 50
//                minIdle = 5
//                maxIdle = 25
//                maxWait = 10000
//                maxAge = 10 * 60000
//                timeBetweenEvictionRunsMillis = 5000
//                minEvictableIdleTimeMillis = 60000
//                validationQuery = "SELECT 1"
//                validationQueryTimeout = 3
//                validationInterval = 15000
//                testOnBorrow = true
//                testWhileIdle = true
//                testOnReturn = false
//                jdbcInterceptors = "ConnectionState"
//                defaultTransactionIsolation = java.sql.Connection.TRANSACTION_READ_COMMITTED
//            }
//        }

        // postgres heroku
        dataSource {
            dbCreate = "update"
            driverClassName = "org.postgresql.Driver"
            dialect = org.hibernate.dialect.PostgreSQLDialect

            uri = new URI(System.env.DATABASE_URL?:"postgres://test:test@localhost/test")

            url = "jdbc:postgresql://"+uri.host+uri.path
            username = uri.userInfo.split(":")[0]
            password = uri.userInfo.split(":")[1]
        }

        // heroku mongo (des requêtes ne fonctionnent pas)
        // Join queries are not supported by MongoDB
//        grails {
//            mongo {
//                host = "lennon.mongohq.com"
//                port = 10022
//                username = "herokumongo"
//                password="7t7Hyc9x8S5A9FUN"
//                databaseName = "app29570423"
//            }
//        }
    }
}
