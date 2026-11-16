package tororo1066.displaymonitor.documentation.generation

import com.networknt.schema.InputFormat
import com.networknt.schema.Schema
import com.networknt.schema.SchemaRegistry
import com.networknt.schema.SpecificationVersion
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import tororo1066.displaymonitor.actions.builtin.GiveItemAction
import tororo1066.displaymonitor.actions.builtin.RestoreDataAction
import tororo1066.displaymonitor.actions.builtin.StoreDataAction
import java.nio.file.Files

class JsonSchemaWriterTest {
    @Test
    fun acceptsEquipmentSlotName() {
        assertValid("/schema-fixtures/give-item-slot-name.json")
    }

    @Test
    fun acceptsNumericInventorySlot() {
        assertValid("/schema-fixtures/give-item-slot-index.json")
    }

    @Test
    fun acceptsNullSlot() {
        assertValid("/schema-fixtures/give-item-slot-null.json")
    }

    @Test
    fun rejectsBooleanSlot() {
        val errors = validate("/schema-fixtures/give-item-slot-invalid-boolean.json")
        assertFalse("Expected boolean slot to fail Schema validation", errors.isEmpty())
    }

    @Test
    fun acceptsStoreDataListEntries() {
        assertValid("/schema-fixtures/store-data-list.json")
    }

    @Test
    fun rejectsStoreDataObjectInsteadOfList() {
        val errors = validate("/schema-fixtures/store-data-object.json")
        assertFalse("Expected object-valued data to fail Schema validation", errors.isEmpty())
    }

    @Test
    fun rejectsStoreDataEntryWithoutValueOrRemove() {
        val errors = validate("/schema-fixtures/store-data-entry-incomplete.json")
        assertFalse("Expected incomplete data entry to fail Schema validation", errors.isEmpty())
    }

    @Test
    fun acceptsRestoreDataUuid() {
        assertValid("/schema-fixtures/restore-data-uuid.json")
    }

    @Test
    fun rejectsNumericRestoreDataUuid() {
        val errors = validate("/schema-fixtures/restore-data-invalid-uuid.json")
        assertFalse("Expected numeric UUID to fail Schema validation", errors.isEmpty())
    }

    @Test
    fun rejectsUnsupportedDatabaseStoreType() {
        val errors = validate("/schema-fixtures/store-data-database.json")
        assertFalse("Expected unsupported DATABASE type to fail Schema validation", errors.isEmpty())
    }

    @Test
    fun acceptsCaseInsensitiveStoreType() {
        assertValid("/schema-fixtures/store-data-lowercase-type.json")
    }

    private fun assertValid(resource: String) {
        val errors = validate(resource)
        assertTrue("Expected $resource to pass Schema validation: $errors", errors.isEmpty())
    }

    private fun validate(resource: String) = schema().validate(
        javaClass.getResource(resource)?.readText()
            ?: error("Missing test fixture: $resource"),
        InputFormat.JSON,
    )

    private fun schema(): Schema {
        val catalog = DocumentationCollector().collect(
            actionClasses = listOf(
                GiveItemAction::class.java,
                RestoreDataAction::class.java,
                StoreDataAction::class.java,
            ),
            elementClasses = emptyList(),
            functionClasses = emptyList(),
        )
        val temporarySchema = Files.createTempFile("give-item-schema-", ".json")
        return try {
            JsonSchemaWriter().write(catalog, temporarySchema)
            val registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12)
            registry.getSchema(Files.readString(temporarySchema), InputFormat.JSON)
        } finally {
            Files.deleteIfExists(temporarySchema)
        }
    }
}
