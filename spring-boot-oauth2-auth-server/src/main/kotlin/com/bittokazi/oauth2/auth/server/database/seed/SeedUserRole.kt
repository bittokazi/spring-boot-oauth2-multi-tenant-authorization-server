package com.bittokazi.oauth2.auth.server.database.seed

import liquibase.change.custom.CustomTaskChange
import liquibase.database.Database
import liquibase.database.jvm.JdbcConnection
import liquibase.exception.CustomChangeException
import liquibase.exception.SetupException
import liquibase.exception.ValidationErrors
import liquibase.resource.ResourceAccessor
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.bcrypt.BCrypt
import java.io.InputStreamReader
import java.io.Reader
import java.sql.Date
import java.time.LocalDate
import java.util.*
import java.util.stream.Collectors

/**
 * @author Bitto Kazi
 */
class SeedUserRole : CustomTaskChange {
    private var resourceAccessor: ResourceAccessor? = null

    private var userFileName: String? = null

    fun setUserFileName(userFileName: String?) {
        this.userFileName = userFileName
    }

    @Throws(CustomChangeException::class)
    override fun execute(database: Database) {
        val databaseConnection = database.connection as JdbcConnection
        try {
            logger.info(userFileName)
            val stream = resourceAccessor!!.openStream(null, userFileName)
            val `in`: Reader = InputStreamReader(stream)

            var roleId = 1L
            for (role in Arrays.asList<String>(ROLE_ + "SUPER_ADMIN")) {
                val sql = "INSERT INTO role(id,name,title) VALUES(?,?,?)"
                val statement = databaseConnection.prepareStatement(sql)
                var title = role.lowercase(Locale.getDefault())
                title = title.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].substring(0, 1)
                    .uppercase(Locale.getDefault()) + title.split("_".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()[1].substring(1)
                if (role == ROLE_ + "SUPER_ADMIN") {
                    title = "Super Admin"
                }
                statement.setString(1, UUID.randomUUID().toString())
                statement.setString(2, role)
                statement.setString(3, title)
                statement.executeUpdate()
                statement.close()
                roleId++
            }

            val records: Iterable<CSVRecord> =
                CSVFormat.EXCEL.withHeader().parse(`in`).records.stream().collect(Collectors.toSet())
            var userId = 1L
            for (record in records) {
                var sql =
                    "INSERT INTO users (id,first_name,last_name,user_name,password,email,enabled,change_password,email_verified,created_date) VALUES(?,?,?,?,?,?,?,?,?,?)"

                var statement = databaseConnection.prepareStatement(sql)
                statement.setString(1, UUID.randomUUID().toString())
                statement.setString(2, record["firstName"])
                statement.setString(3, record["lastName"])
                statement.setString(4, record["username"])
                statement.setString(5, BCrypt.hashpw(record["password"], BCrypt.gensalt()))
                statement.setString(6, record["email"])
                statement.setBoolean(7, true)
                statement.setBoolean(8, false)
                statement.setBoolean(9, true)
                statement.setDate(10, Date.valueOf(LocalDate.now()))

                statement.executeUpdate()
                databaseConnection.commit()
                statement.close()

                for (role in record["roles"].split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
                    sql = ("insert into user_role (user_id, role_id) VALUES (" + "(select id from users where email='"
                            + record["email"] + "'), (select id from role where name='" + role + "') )")
                    logger.info(sql)
                    statement = databaseConnection.prepareStatement(sql)
                    statement.executeUpdate()
                    databaseConnection.commit()
                    statement.close()
                }
                userId++
            }
            `in`.close()
        } catch (e: Exception) {
            throw CustomChangeException(e)
        }
    }

    override fun getConfirmationMessage(): String? {
        return null
    }

    @Throws(SetupException::class)
    override fun setUp() {
    }

    override fun setFileOpener(resourceAccessor: ResourceAccessor) {
        this.resourceAccessor = resourceAccessor
    }

    override fun validate(database: Database): ValidationErrors? {
        return null
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(SeedUserRole::class.java)

        private const val ROLE_ = "ROLE_"
    }
}
