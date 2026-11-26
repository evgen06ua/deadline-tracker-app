package com.lebediev.deadlinetracker.logic

import com.lebediev.deadlinetracker.data.model.Deadline
import java.util.Date

data class DeadlineStats(
    val upcoming: Int = 0,
    val overdue: Int = 0,
    val done: Int = 0
)

object DeadlineStatsCalculator {
    fun compute(list: List<Deadline>, now: Date): DeadlineStats {
        val done = list.count { it.done }
        val overdue = list.count { !it.done && it.dueDate != null && it.dueDate.before(now) }
        val upcoming = list.count { !it.done } - overdue
        return DeadlineStats(upcoming = upcoming, overdue = overdue, done = done)
    }
}