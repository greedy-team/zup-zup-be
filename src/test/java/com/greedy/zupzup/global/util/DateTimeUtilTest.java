package com.greedy.zupzup.global.util;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

class DateTimeUtilTest {

    @Test
    void LocalDateTime이_KST_OffsetDateTime으로_변환되어야_한다() {
        // Given - 10:30 AM
        LocalDateTime givenTime = LocalDateTime.of(2025, 11, 14, 10, 30, 0);

        // When
        OffsetDateTime result = DateTimeUtil.toKstOffset(givenTime);

        // Then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result).isNotNull();
            softly.assertThat(result.toLocalDateTime()).isEqualTo(givenTime);
            softly.assertThat(result.getOffset()).isEqualTo(ZoneOffset.ofHours(9));
        });
    }

}
