package com.example.vuonnha.ui.plants

import com.example.vuonnha.data.Species
import org.junit.Assert.assertEquals
import org.junit.Test

class ReviewedTasksTest {
    private val species = Species("test", "Cây thử", "", listOf("Tưới" to 7, "Bón" to 30))

    @Test fun postponingRemindersSendsNoTasksToRepository() {
        assertEquals(emptyList<Pair<String, Int>>(), speciesWithReviewedTasks(species, emptyList()).tasks)
    }

    @Test fun editedAndRemovedTasksMatchReviewedPlans() {
        assertEquals(listOf("Tưới" to 12), speciesWithReviewedTasks(species, listOf("Tưới" to 12)).tasks)
        assertEquals(listOf("Tưới" to 7, "Bón" to 30), species.tasks)
    }
}
