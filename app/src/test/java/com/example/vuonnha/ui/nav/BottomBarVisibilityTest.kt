package com.example.vuonnha.ui.nav

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BottomBarVisibilityTest {
    @Test fun bottomBarIsHiddenWhileAddSheetIsOpenOnPlantsRoute() {
        assertFalse(shouldShowBottomBar("plants", addSheetOpen = true))
    }

    @Test fun bottomBarReturnsWhenAddSheetCloses() {
        assertTrue(shouldShowBottomBar("plants", addSheetOpen = false))
    }

    @Test fun scanAndAccountRoutesKeepTheirExistingHiddenBottomBar() {
        assertFalse(shouldShowBottomBar("scan", addSheetOpen = false))
        assertFalse(shouldShowBottomBar("account", addSheetOpen = false))
    }
}
