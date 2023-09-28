package org.mbte.mdds.util

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mbte.mdds.tests.Contact
import java.io.File

class DatabaseHandlerTest {

    private lateinit var databaseHandler: DatabaseHandler
    private val testDbPath = "testdb.sqlite"

    @BeforeEach
    fun setUp() {
        databaseHandler = DatabaseHandler("jdbc:sqlite:$testDbPath")
        runBlocking { databaseHandler.initContactsTable() }
    }

    @AfterEach
    fun tearDown() {
        File(testDbPath).delete()
    }

    @Test
    fun testInsertOrUpdateContact() = runBlocking {
        val contact = Contact(
            id = "1",
            companyName = "Test Co.",
            name = "John Doe",
            title = "Employee",
            address = "123 Test St",
            city = "Test City",
            email = "johndoe@test.com",
            zip = "12345",
            country = "US",
            phone = "1234567890",
            fax = null,
            region = null
        )
        databaseHandler.insertOrUpdateContact(contact)

        val contactsFromDb = databaseHandler.getAllContacts()
        assertEquals(1, contactsFromDb.size)
        assertEquals(contact, contactsFromDb[0])
    }
}
