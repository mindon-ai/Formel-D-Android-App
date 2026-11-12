package com.example.feature.inspection

import androidx.lifecycle.ViewModel
import com.example.core.model.InspectionDraft
import com.example.core.model.MissionOrder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InspectionDraftViewModel : ViewModel() {
  private val _draft = MutableStateFlow(InspectionDraft())
  val draft: StateFlow<InspectionDraft> = _draft.asStateFlow()

  private val _selectedMo = MutableStateFlow<MissionOrder?>(null)
  val selectedMo: StateFlow<MissionOrder?> = _selectedMo.asStateFlow()

  fun initForMo(mo: MissionOrder) {
    _selectedMo.value = mo
    _draft.value = InspectionDraft(
      moId = mo.id,
      moNumber = mo.moNumber,
      moTitle = mo.title,
      partReference = mo.partReference,
      quantity = 1,
      defectCounts = emptyMap()
    )
  }

  fun updatePartSerial(serial: String) {
    _draft.update { it.copy(partSerial = serial) }
  }

  fun updateBoxId(boxId: String) {
    _draft.update { it.copy(boxId = boxId) }
  }

  fun updateQuantity(quantity: Int) {
    _draft.update { it.copy(quantity = quantity.coerceAtLeast(1)) }
  }

  fun updateProblemCount(problemId: String, count: Int) {
    _draft.update { current ->
      val counts = current.defectCounts.toMutableMap()
      if (count <= 0) {
        counts.remove(problemId)
      } else {
        counts[problemId] = count
      }
      current.copy(defectCounts = counts)
    }
  }

  fun resetForSameMo() {
    val current = _draft.value
    _draft.value = InspectionDraft(
      moId = current.moId,
      moNumber = current.moNumber,
      moTitle = current.moTitle,
      partReference = current.partReference,
      partSerial = "",
      boxId = "",
      quantity = 1,
      defectCounts = emptyMap()
    )
  }

  fun clear() {
    _draft.value = InspectionDraft()
    _selectedMo.value = null
  }
}
