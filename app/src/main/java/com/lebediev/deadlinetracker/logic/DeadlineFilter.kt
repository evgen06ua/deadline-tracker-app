package com.lebediev.deadlinetracker.logic

import com.lebediev.deadlinetracker.data.model.Deadline

object DeadlineFilter {
    fun search(list: List<Deadline>, query: String): List<Deadline> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return list
        return list.filter {
            it.title.lowercase().contains(q) || it.courseName.lowercase().contains(q)
        }
    }
}