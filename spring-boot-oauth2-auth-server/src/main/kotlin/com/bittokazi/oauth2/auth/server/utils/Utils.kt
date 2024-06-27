package com.bittokazi.oauth2.auth.server.utils

import com.zaxxer.hikari.HikariConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.jpa.domain.Specification
import java.io.*
import java.lang.reflect.Method
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import java.util.regex.Pattern
import java.util.stream.Collectors

/**
 * @author Bitto Kazi
 */
object Utils {
    private val logger: Logger = LoggerFactory.getLogger(Utils::class.java)

    @JvmStatic
    fun getDbConfig(
        dataSourceClassName: String?, url: String?, port: String?, databaseName: String?,
        user: String?, password: String?
    ): HikariConfig {
        val config = HikariConfig()
        config.dataSourceClassName = dataSourceClassName
        config.addDataSourceProperty("serverName", url)
        config.addDataSourceProperty("portNumber", port)
        config.addDataSourceProperty("databaseName", databaseName)
        config.addDataSourceProperty("user", user)
        config.addDataSourceProperty("password", password)
        config.minimumIdle = 2
        config.idleTimeout = 120000
        config.maximumPoolSize = 5
        return config
    }

    fun convertFileToByte(file: File): ByteArray {
        val bytesArray = ByteArray(file.length().toInt())
        var fis: FileInputStream? = null
        try {
            fis = FileInputStream(file)
            fis.read(bytesArray) // read file into bytes[]
            fis.close()
        } catch (e: IOException) {
            logger.error("ERROR converting file to byte ", e)
        } finally {
            if (fis != null) {
                try {
                    fis.close()
                } catch (e: IOException) {
                    logger.error("ERROR closing file input stream ", e)
                }
            }
        }
        return bytesArray
    }

    @JvmStatic
    fun randomNumberGenerator(length: Int): String {
        val SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890abcdefghijklmnopqrstuvwxyz"
        val salt = StringBuilder()
        val rnd = Random()
        while (salt.length < length) {
            val index = (rnd.nextFloat() * SALTCHARS.length).toInt()
            salt.append(SALTCHARS[index])
        }
        val saltStr = salt.toString()
        return saltStr
    }

    fun randomIntGenerator(length: Int): Int {
        val SALTCHARS = "123456789"
        val salt = StringBuilder()
        val rnd = Random()
        while (salt.length < length) {
            val index = (rnd.nextFloat() * SALTCHARS.length).toInt()
            salt.append(SALTCHARS[index])
        }
        val saltStr = salt.toString()
        return saltStr.toInt()
    }

    fun <T> searchSpecificationBuilder(search: String, c: Class<*>): Specification<T>? {
        try {
            val builder = Class.forName(c.name).getDeclaredConstructor().newInstance()
            var method: Method? = null
            for (m in builder.javaClass.methods) {
                if (m.name == "with") method = m
            }
            method!!.isAccessible = true

            val pattern = Pattern.compile("([a-zA-Z0-9]+)(:|<|>|=|~)([^,]+),")
            val matcher = pattern.matcher("$search,")
            while (matcher.find()) {
                method.invoke(builder, matcher.group(1), matcher.group(2), matcher.group(3))
            }

            val methodBuild = builder.javaClass.getMethod("build")
            methodBuild.isAccessible = true
            return methodBuild.invoke(builder) as Specification<T>
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    val currentMonthFirstDate: LocalDate
        get() = LocalDate.ofEpochDay(System.currentTimeMillis() / (24 * 60 * 60 * 1000)).withDayOfMonth(1)

    val currentMonthLastDate: LocalDate
        get() = LocalDate.ofEpochDay(System.currentTimeMillis() / (24 * 60 * 60 * 1000)).plusMonths(1).withDayOfMonth(1)
            .minusDays(1)

    val currentDate: LocalDate
        get() = LocalDate.ofEpochDay(System.currentTimeMillis())

    fun getNextDate(date: String?): Date? {
        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val c = Calendar.getInstance()
        try {
            c.time = sdf.parse(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        c.add(Calendar.DATE, 1)
        try {
            return SimpleDateFormat("dd/MM/yyyy").parse(sdf.format(c.time))
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return null
    }

    fun removeTime(date: Date?): Date {
        val cal = Calendar.getInstance()
        cal.time = date
        cal[Calendar.HOUR_OF_DAY] = 0
        cal[Calendar.MINUTE] = 0
        cal[Calendar.SECOND] = 0
        cal[Calendar.MILLISECOND] = 0
        return cal.time
    }

    fun entryCouponGenerator(length: Int): String {
        val SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"
        val salt = StringBuilder()
        val rnd = Random()
        while (salt.length < length) {
            val index = (rnd.nextFloat() * SALTCHARS.length).toInt()
            salt.append(SALTCHARS[index])
        }
        val saltStr = salt.toString()
        return saltStr
    }

    fun formatFileName(fileName: String?): String {
        val pattern = "([A-Z,a-z,.,0-9,_,-])"
        val r = Pattern.compile(pattern)
        val m = r.matcher(fileName)
        var newFileName = ""
        while (m.find()) {
            newFileName += m.group(1)
        }
        return randomNumberGenerator(30) + newFileName
    }

    @JvmStatic
    @Throws(NoSuchAlgorithmException::class)
    fun getMD5(data: String): String {
        val messageDigest = MessageDigest.getInstance("MD5")
        messageDigest.update(data.toByteArray())
        val digest = messageDigest.digest()
        val sb = StringBuffer()
        for (b in digest) {
            sb.append(Integer.toHexString((b.toInt() and 0xff)))
        }
        return sb.toString()
    }

    fun getResourceFileAsString(fileName: String?): String {
        val `is` = getResourceFileAsInputStream(fileName)
        if (`is` != null) {
            val reader = BufferedReader(InputStreamReader(`is`))
            return reader.lines().collect(Collectors.joining(System.lineSeparator())) as String
        } else {
            throw RuntimeException("resource not found")
        }
    }

    fun getResourceFileAsInputStream(fileName: String?): InputStream {
        val classLoader = Utils::class.java.classLoader
        return classLoader.getResourceAsStream(fileName)
    }
}
