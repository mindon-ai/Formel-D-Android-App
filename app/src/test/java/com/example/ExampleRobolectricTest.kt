package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.core.db.AppDatabase
import com.example.core.db.CachedMoEntity
import com.example.core.model.InspectionDraft
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Formel D QC", appName)
  }

  @Test
  fun `verify quality control rule that defects cannot exceed quantity`() {
    val draftValid = InspectionDraft(
      partSerial = "RN-82001-A904",
      boxId = "BOX-4029",
      quantity = 10,
      defectCounts = mapOf("p1" to 3, "p2" to 2)
    )
    assertEquals(5, draftValid.totalDefects)
    assertEquals(5, draftValid.goodQuantity)
    assertFalse(draftValid.isExceedingQuantity)
    assertTrue(draftValid.isValidForSend)

    val draftExceeded = InspectionDraft(
      partSerial = "RN-82001-A904",
      boxId = "BOX-4029",
      quantity = 4,
      defectCounts = mapOf("p1" to 3, "p2" to 2)
    )
    assertEquals(5, draftExceeded.totalDefects)
    assertTrue(draftExceeded.isExceedingQuantity)
    assertFalse(draftExceeded.isValidForSend)
  }

  @Test
  fun `verify active MOs are persisted in Room database for offline use`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val testDb = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()

    val testEntity = CachedMoEntity(
      id = "mo_0891",
      moNumber = "MO-2026-0891",
      title = "Brake Caliper Front Right - Megane IV",
      partReference = "RN-7701208945",
      status = "ACTIVE",
      problemsVersion = 1,
      problemCount = 2,
      problemsJson = "[{\"id\":\"p1\",\"code\":\"DEF-01\",\"label\":\"Rayures\",\"sortOrder\":1}]",
      lastUpdated = System.currentTimeMillis()
    )

    testDb.cachedMoDao().insertMos(listOf(testEntity))

    val cachedList = testDb.cachedMoDao().getAllActiveMos()
    assertEquals(1, cachedList.size)
    assertEquals("MO-2026-0891", cachedList[0].moNumber)

    val singleMo = testDb.cachedMoDao().getMoById("mo_0891")
    assertNotNull(singleMo)
    assertEquals("Brake Caliper Front Right - Megane IV", singleMo?.title)

    testDb.close()
  }
}
