package com.greedy.zupzup.lostitem.presentation;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.greedy.zupzup.category.domain.Category;
import com.greedy.zupzup.common.ControllerTest;
import com.greedy.zupzup.common.fixture.PledgeFixture;
import com.greedy.zupzup.global.exception.ErrorResponse;
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.domain.LostItemStatus;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemListResponse;
import com.greedy.zupzup.member.domain.Member;
import com.greedy.zupzup.pledge.domain.Pledge;
import com.greedy.zupzup.pledge.repository.PledgeRepository;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

class MyPledgedLostItemControllerTest extends ControllerTest {

    private static final String ACCESS_COOKIE = "access_token";

    @Autowired
    private PledgeRepository pledgeRepository;

    private Member member;
    private String accessToken;

    @BeforeEach
    void setUp() {
        member = givenMember("password123!");
        accessToken = givenAccessToken(member);
    }

    private LostItem persistLostItemGraph(Category category) {
        return givenRegisteredLostItem(category);
    }

    private void savePledge(LostItem item) {
        Pledge pledge = PledgeFixture.PLEDGE(member, item);
        pledgeRepository.save(pledge);
        ReflectionTestUtils.setField(item, "status", LostItemStatus.PLEDGED);
        ReflectionTestUtils.setField(item, "pledgedAt", LocalDate.now());
        lostItemRepository.save(item);
    }

    private ExtractableResponse<Response> get(String path) {
        return io.restassured.RestAssured.given().log().all()
                .cookie(ACCESS_COOKIE, accessToken)
                .when().get(path)
                .then().log().all()
                .extract();
    }

    @Nested
    @DisplayName("내 서약 분실물 목록 조회 API")
    class PledgedListApi {

        @Test
        void 여러개의_분실물을_서약하면_모두_조회된다() {
            Category electronics = givenElectronicsCategory();

            LostItem i1 = persistLostItemGraph(electronics);
            LostItem i2 = persistLostItemGraph(electronics);
            LostItem i3 = persistLostItemGraph(electronics);

            savePledge(i1);
            savePledge(i2);
            savePledge(i3);

            ExtractableResponse<Response> extract =
                    get("/api/lost-items/pledged?page=1&limit=10");

            LostItemListResponse response = extract.as(LostItemListResponse.class);

            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(200);
                softly.assertThat(response.items()).hasSize(3);
                List<Long> ids = response.items().stream().map(r -> r.id()).toList();
                softly.assertThat(ids).containsExactlyInAnyOrder(i1.getId(), i2.getId(), i3.getId());
            });
        }


        @Test
        void 내가_서약하지않은_분실물은_조회되지않는다() {
            Category electronics = givenElectronicsCategory();

            LostItem myItem = persistLostItemGraph(electronics);
            savePledge(myItem);

            LostItem unpledgedItem = persistLostItemGraph(electronics);

            // when
            ExtractableResponse<Response> extract =
                    get("/api/lost-items/pledged?page=1&limit=10");

            LostItemListResponse response = extract.as(LostItemListResponse.class);

            // then
            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(200);
                softly.assertThat(response.items()).hasSize(1);
                softly.assertThat(response.items().get(0).id()).isEqualTo(myItem.getId());

                List<Long> ids = response.items().stream().map(i -> i.id()).toList();
                softly.assertThat(ids).doesNotContain(unpledgedItem.getId());
            });
        }


        @Test
        void 로그인없이_요청하면_401_Unauthorized를_응답한다() {
            ExtractableResponse<Response> extract =
                    RestAssured.given().log().all()
                            .when().get("/api/lost-items/pledged?page=1&limit=10")
                            .then().log().all()
                            .extract();

            assertSoftly(softly -> softly.assertThat(extract.statusCode()).isEqualTo(401));
        }

        @Test
        void limit가_50을_초과하면_400_BadRequest를_응답한다() {
            ExtractableResponse<Response> extract =
                    RestAssured.given().log().all()
                            .cookie(ACCESS_COOKIE, accessToken)
                            .queryParam("page", 1)
                            .queryParam("limit", 51)
                            .when().get("/api/lost-items/pledged")
                            .then().log().all()
                            .extract();

            ErrorResponse error = extract.as(ErrorResponse.class);

            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(400);
                softly.assertThat(error.title()).contains("유효하지 않은 입력값");
                softly.assertThat(error.detail()).contains("limit는 50 이하이어야 합니다.");
            });
        }

        @Test
        void page가_0이하이면_400_BadRequest를_응답한다() {
            ExtractableResponse<Response> extract =
                    RestAssured.given().log().all()
                            .cookie(ACCESS_COOKIE, accessToken)
                            .queryParam("page", 0)
                            .queryParam("limit", 10)
                            .when().get("/api/lost-items/pledged")
                            .then().log().all()
                            .extract();

            ErrorResponse error = extract.as(ErrorResponse.class);

            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(400);
                softly.assertThat(error.detail()).contains("page는 1 이상이어야 합니다.");
            });
        }
    }
}
