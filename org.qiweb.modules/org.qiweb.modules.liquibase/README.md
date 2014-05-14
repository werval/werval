# Liquibase Database Migrations

This plugin depends on the JDBC plugin to be registered in your application and a proper DataSource configured.

If you don't use the default DataSource, you can then tell Liquibase the DataSource name you want to use by setting the
`liquibase.datasource` configuration property.

By default, migrations are loaded from `db-changelog.xml` in the application classpath.
This can be overriden using the `liquibase.changelog` configuration property.

By default, all data is dropped on passivation in `TEST` mode.
If you want to prevent this, set the `liquibase.drop_after_tests` configuration property to `no`.

See http://www.liquibase.org/ for database migration insights.
