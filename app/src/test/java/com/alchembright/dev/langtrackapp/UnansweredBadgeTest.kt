package com.alchembright.dev.langtrackapp
import com.alchembright.dev.langtrackapp.data.model.*
import com.alchembright.dev.langtrackapp.util.UnansweredBadge
import org.junit.Test
import org.junit.Assert.assertEquals
import java.text.SimpleDateFormat
import java.util.TimeZone
class UnansweredBadgeTest {
    @Test fun countsOnlyPublishedUnexpiredUnansweredAndDeduplicates() {
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").apply { timeZone = TimeZone.getTimeZone("UTC") }.parse("2026-10-07T00:00:00")!!.time
        fun item(id: String, published: String = "2026-10-06T00:00:00", expires: String = "2026-10-08T00:00:00", answered: Boolean = false) = Assignment(Survey(), "", "", "qa", if(answered) Dataset("d", "", "", mutableListOf()) else null, published, expires, id)
        val active = item("active")
        assertEquals(1, UnansweredBadge.count(listOf(active,active,item("answered",answered=true),item("future",published="2026-10-08T00:00:00"),item("expired",expires="2026-10-07T00:00:00"),item("bad",expires="bad")), now))
        assertEquals(1, UnansweredBadge.count(listOf(item("boundary",published="2026-10-07T00:00:00")), now))
        assertEquals(0, UnansweredBadge.count(emptyList(), now))
    }
}
