package org.mbte.mdds.tests

import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.mbte.mdds.util.DatabaseHandler
import org.mbte.mdds.util.getDirectChildren
import org.mbte.mdds.util.loadXmlFromFile
import org.w3c.dom.Document
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


fun main() = runBlocking {
	val test = ADSBackendTest1()

	// Prepare time-based filename components
	val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
	val time = dateFormatter.format(LocalDateTime.now()).replace(" ", "_").replace(":", ";")

	// Set up JSON file and database paths
	val userHome = File(System.getProperty("user.home"))
	val jsonFile = File(userHome, "${test.javaClass.simpleName}-$time.json").also { it.createNewFile() }
	val dbFile = File("db-$time.sqlite")
	val dbHandler = DatabaseHandler("jdbc:sqlite:${dbFile.path}")

	// Initialize the contacts table
	dbHandler.initContactsTable()

	// Load and process XML contents
	val doc = test.loadXml("src/main/resources/ab.xml")!!
	val addressBook = test.loadAddressBook(doc)
	addressBook.contacts.forEach { dbHandler.insertOrUpdateContact(it) }

	// Convert contacts to JSON and save to file
	val json = test.convertToJson(addressBook)
	test.printOutput(json, jsonFile)

	// Output results
	println(dbHandler.getAllContacts())
	println("Assessment complete.")
	println("JSON output located at ${jsonFile.absolutePath}")
	println("DB output located at ${dbFile.absolutePath}")
}

/**
 * Represents the entire address book with a list of contacts.
 */
data class AddressBook(val contacts: List<Contact>)

/**
 * Represents an individual contact with various attributes.
 */
data class Contact(
	val id: String,
	val companyName: String?,
	val name: String?,
	val title: String?,
	val address: String?,
	val city: String?,
	val email: String?,
	val region: String?,
	val zip: String?,
	val country: String?,
	val phone: String?,
	val fax: String?
)

/**
 * Interface that defines operations for the address book.
 */
interface AddressBookInterface {
	suspend fun loadXml(fileName: String): Document?
	suspend fun loadAddressBook(doc: Document): AddressBook
	suspend fun convertToJson(addressBook: AddressBook): JSONObject
	suspend fun printOutput(json: JSONObject, output: File)
}


/**
 * Main class that implements address book operations
 */
class ADSBackendTest1: AddressBookInterface {
	override suspend fun loadXml(fileName: String): Document? {
		return loadXmlFromFile(File(fileName))
	}

	override suspend fun loadAddressBook(doc: Document): AddressBook {
		return doc.getElementsByTagName("Contact").let { contactsNodeList ->
			AddressBook((0 until contactsNodeList.length).mapNotNull { index ->
				contactsNodeList.item(index).let { node ->
					getDirectChildren(node).let { childElements ->
						Contact(
							id = childElements.find { it.tagName == "CustomerID" }?.textContent ?: "",
							companyName = childElements.find { it.tagName == "CompanyName" }?.textContent ?: "",
							name = childElements.find { it.tagName == "ContactName" }?.textContent ?: "",
							title = childElements.find { it.tagName == "ContactTitle" }?.textContent ?: "",
							address = childElements.find { it.tagName == "Address" }?.textContent ?: "",
							city = childElements.find { it.tagName == "City" }?.textContent ?: "",
							email = childElements.find { it.tagName == "Email" }?.textContent ?: "",
							region = childElements.find { it.tagName == "Region" }?.textContent,
							zip = childElements.find { it.tagName == "PostalCode" }?.textContent,
							country = childElements.find { it.tagName == "Country" }?.textContent ?: "",
							phone = childElements.find { it.tagName == "Phone" }?.textContent ?: "",
							fax = childElements.find { it.tagName == "Fax" }?.textContent
						)
					}
				}
			})
		}
	}

	override suspend fun convertToJson(addressBook: AddressBook): JSONObject {
		return JSONObject().put("contacts", addressBook.contacts.map {
			mapOf(
				"id" to it.id,
				"companyName" to it.companyName,
				"name" to it.name,
				"title" to it.title,
				"address" to it.address,
				"city" to it.city,
				"email" to it.email,
				"region" to it.region,
				"zip" to it.zip,
				"country" to it.country,
				"phone" to it.phone,
				"fax" to it.fax
			)
		})
	}

	override suspend fun printOutput(json: JSONObject, output: File) {
		output.writeText(json.toString(4))
	}

}