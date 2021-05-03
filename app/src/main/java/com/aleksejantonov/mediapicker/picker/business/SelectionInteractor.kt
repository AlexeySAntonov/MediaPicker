package com.aleksejantonov.mediapicker.picker.business

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch
import java.util.*

class SelectionInteractor<T : Any> constructor(initialSelection: List<T> = emptyList()) {

  private val selected = LinkedList(initialSelection.map { SelectionModel(it) })
  private val selectionChannel = ConflatedBroadcastChannel(initialSelection)
  private val scope = CoroutineScope(Dispatchers.IO)

  fun observeSelection(): Flow<List<T>> = selectionChannel.asFlow()
  fun selected(): List<T> = selectionChannel.valueOrNull ?: emptyList()

  @Synchronized
  fun select(model: T, index: Int? = null) {
    if (isSelected(model).not()) {
      if (index == null || index >= selected.size) selected.add(SelectionModel(model))
      else selected.add(index, SelectionModel(model))
      val data = selected.map { it.data }
      scope.launch { selectionChannel.send(data) }
    }
  }

  @Synchronized
  fun select(model: T, after: (T) -> Boolean) {
    if (isSelected(model).not()) {
      selected.add(selected.indexOfFirst { after.invoke(it.data) } + 1, SelectionModel(model))
      val data = selected.map { it.data }
      scope.launch { selectionChannel.send(data) }
    }
  }

  @Synchronized
  fun select(models: Iterable<T>) {
    val filtered = models.filter { !isSelected(it) }
    if (filtered.isNotEmpty()) {
      selected.addAll(filtered.map { SelectionModel(it) })
      val data = selected.map { it.data }
      scope.launch { selectionChannel.send(data) }
    }
  }

  @Synchronized
  fun replace(models: Iterable<T>) {
    selected.clear()
    selected.addAll(models.map { SelectionModel(it) })
    val data = selected.map { it.data }
    scope.launch { selectionChannel.send(data) }
  }

  @Synchronized
  fun deselect(model: T) {
    selected.firstOrNull { it.data == model }?.let { modelToRemove ->
      selected.remove(modelToRemove)
      val data = selected.map { it.data }
      scope.launch { selectionChannel.send(data) }
    }
  }

  private fun isSelected(model: T): Boolean = selected.any { it.data == model }

  fun selectedCount(): Int = selected.size

  @Synchronized
  fun clear() {
    selected.clear()
    scope.launch { selectionChannel.send(emptyList()) }
  }

  fun reset() {
    scope.cancel()
  }

  private inner class SelectionModel<E : Any>(val data: E) {
    override fun equals(other: Any?): Boolean = (other is SelectionInteractor<*>.SelectionModel<*>) && other.data == data
    override fun hashCode(): Int = data.hashCode()
  }
}