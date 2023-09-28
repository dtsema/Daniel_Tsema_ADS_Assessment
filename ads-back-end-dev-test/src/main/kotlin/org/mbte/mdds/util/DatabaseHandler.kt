package org.mbte.mdds.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.mbte.mdds.tests.Contact
import java.sql.Connection
import java.sql.DriverManager

/**
 * A handler class for managing database operations related to Contacts.
 * @property url The database URL.
 */
class DatabaseHandler(private val url: String) {

    init {
        // Register the SQLite JDBC driver
        Class.forName("org.sqlite.JDBC")
    }

    /**
     * Initializes the contacts table if it does not exist.
     */
    suspend fun initContactsTable() = withContext(Dispatchers.IO) {
        getConnection()?.use { connection ->
            val sql = """CREATE TABLE IF NOT EXISTS Contact (
                    CustomerID VARCHAR(255) PRIMARY KEY,
                    CompanyName VARCHAR(255),
                    ContactName VARCHAR(255),
                    ContactTitle VARCHAR(255),
                    Address VARCHAR(255),
                    City VARCHAR(255),
                    Email VARCHAR(255),
                    Region VARCHAR(255),
                    PostalCode VARCHAR(255),
                    Country VARCHAR(255),
                    Phone VARCHAR(255),
                    Fax VARCHAR(255)
                );"""
            connection.prepareStatement(sql).execute()
        }
    }

    /**
     * Inserts a contact into the database or updates if already exists.
     * @param contact The contact to be inserted.
     */
    suspend fun insertOrUpdateContact(contact: Contact) = withContext(Dispatchers.IO) {
        getConnection()?.use { connection ->
            val sql = """
                INSERT OR REPLACE INTO Contact (
                    CustomerID, CompanyName, ContactName, ContactTitle,
                    Address, City, Email, Region, PostalCode, Country, Phone, Fax
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
            """
            connection.prepareStatement(sql).apply {
                setString(1, contact.id)
                setString(2, contact.companyName)
                setString(3, contact.name)
                setString(4, contact.title)
                setString(5, contact.address)
                setString(6, contact.city)
                setString(7, contact.email)
                setString(8, contact.region)
                setString(9, contact.zip)
                setString(10, contact.country)
                setString(11, contact.phone)
                setString(12, contact.fax)
                execute()
            }
        }
    }

    /**
     * Fetches all the contacts from the database.
     * @return A list of contacts.
     */
    suspend fun getAllContacts(): List<Contact> = withContext(Dispatchers.IO) {
        val contacts = mutableListOf<Contact>()
        getConnection()?.use { connection ->
            val sql = "SELECT * FROM Contact;"
            val resultSet = connection.prepareStatement(sql).executeQuery()
            while (resultSet.next()) {
                contacts.add(
                    Contact(
                        id = resultSet.getString("CustomerID"),
                        companyName = resultSet.getString("CompanyName"),
                        name = resultSet.getString("ContactName"),
                        title = resultSet.getString("ContactTitle"),
                        address = resultSet.getString("Address"),
                        city = resultSet.getString("City"),
                        email = resultSet.getString("Email"),
                        zip = resultSet.getString("PostalCode"),
                        country = resultSet.getString("Country"),
                        phone = resultSet.getString("Phone"),
                        fax = resultSet.getString("Fax"),
                        region = resultSet.getString("Region")
                    )
                )
            }
        }
        return@withContext contacts
    }

    /**
     * Adds a new column to the Contact table.
     * @param columnName Name of the new column.
     * @param columnType Type of the new column.
     */
    suspend fun addColumnToContactTable(columnName: String, columnType: String) = withContext(Dispatchers.IO) {
        getConnection()?.use { connection ->
            val sql = "ALTER TABLE Contact ADD COLUMN $columnName $columnType;"
            @Suppress("SqlSourceToSinkFlow")
            connection.prepareStatement(sql).execute()
        }
    }

    /**
     * Establishes a connection to the database.
     * @return A connection instance, or null if an error occurred.
     */
    private fun getConnection(): Connection? {
        return try {
            DriverManager.getConnection(url)
        } catch (e: Exception) {
            System.err.println("Failed to get connection to SQLite!")
            e.printStackTrace()
            null
        }
    }
}
