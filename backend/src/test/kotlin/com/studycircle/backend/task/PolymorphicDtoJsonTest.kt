package com.studycircle.backend.task

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.studycircle.backend.submission.dto.SubmissionEvidenceDto
import com.studycircle.backend.task.dto.TrackingMethodDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

// The client (kotlinx.serialization) and the server (Jackson) must agree on the
// "type" discriminator for every tracking method / evidence variant. These tests
// pin the wire format, including the payload-less ManualCheckIn variants.
class PolymorphicDtoJsonTest {
    private val mapper: ObjectMapper = jacksonObjectMapper()

    @Test
    fun `count based tracking deserializes from client json`() {
        val dto: TrackingMethodDto =
            mapper.readValue("""{"type":"COUNT_BASED","unit":"صفحة","target":20}""")
        assertEquals(TrackingMethodDto.CountBased(unit = "صفحة", target = 20), dto)
    }

    @Test
    fun `manual check-in tracking deserializes from a bare type`() {
        val dto: TrackingMethodDto = mapper.readValue("""{"type":"MANUAL_CHECK_IN"}""")
        assertEquals(TrackingMethodDto.ManualCheckIn(), dto)
    }

    @Test
    fun `count evidence and check-in evidence deserialize`() {
        val count: SubmissionEvidenceDto = mapper.readValue("""{"type":"COUNT_BASED","countAchieved":12}""")
        val checkIn: SubmissionEvidenceDto = mapper.readValue("""{"type":"MANUAL_CHECK_IN"}""")
        assertEquals(SubmissionEvidenceDto.CountBased(countAchieved = 12), count)
        assertEquals(SubmissionEvidenceDto.ManualCheckIn(), checkIn)
    }

    @Test
    fun `serialized output carries the discriminator`() {
        val json = mapper.writeValueAsString(TrackingMethodDto.ManualCheckIn())
        assertEquals("""{"type":"MANUAL_CHECK_IN"}""", json)
    }
}
