package com.lebediev.deadlinetracker.logic

import com.lebediev.deadlinetracker.data.model.Deadline

object StatusFilter {
    fun visible(list: List<Deadline>, hideDone: Boolean): List<Deadline> =
        if (hideDone) list.filter { !it.done } else list
}