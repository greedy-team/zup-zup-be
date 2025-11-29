package com.greedy.zupzup.lostitem.presentation;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.greedy.zupzup.category.domain.Category;
import com.greedy.zupzup.common.ControllerTest;
import com.greedy.zupzup.common.fixture.PledgeFixture;
import com.greedy.zupzup.global.exception.ErrorResponse;
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.domain.LostItemStatus;
import com.greedy.zupzup.member.domain.Member;
import com.greedy.zupzup.member.domain.Role;
import com.greedy.zupzup.pledge.domain.Pledge;
import com.greedy.zupzup.pledge.repository.PledgeRepository;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
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

            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(200);

                List<Long> ids = extract.jsonPath().getList("items.id", Long.class);
                softly.assertThat(ids).hasSize(3);
                softly.assertThat(ids).containsExactlyInAnyOrder(i1.getId(), i2.getId(), i3.getId());

                softly.assertThat(extract.jsonPath().getLong("count")).isEqualTo(3);
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

            // then
            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(200);

                List<Long> ids = extract.jsonPath().getList("items.id", Long.class);

                softly.assertThat(ids).hasSize(1);
                softly.assertThat(ids.get(0)).isEqualTo(myItem.getId());
                softly.assertThat(ids).doesNotContain(unpledgedItem.getId());

                softly.assertThat(extract.jsonPath().getLong("count")).isEqualTo(1);
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

        @Test
        void 서약한_분실물이_없으면_빈_목록을_응답한다() {
            // given

            // when
            ExtractableResponse<Response> extract =
                    get("/api/lost-items/pledged?page=1&limit=10");

            // then
            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(200);
                softly.assertThat(extract.jsonPath().getLong("count")).isEqualTo(0);
                softly.assertThat(extract.jsonPath().getList("items")).isEmpty();
            });
        }

        @Test
        void 페이징_파라미터가_없으면_기본값으로_조회된다() {
            Category electronics = givenElectronicsCategory();

            // given
            LostItem i1 = persistLostItemGraph(electronics);
            LostItem i2 = persistLostItemGraph(electronics);
            LostItem i3 = persistLostItemGraph(electronics);
            savePledge(i1);
            savePledge(i2);
            savePledge(i3);

            // when
            ExtractableResponse<Response> extract =
                    get("/api/lost-items/pledged");

            // then
            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(200);
                softly.assertThat(extract.jsonPath().getLong("count")).isEqualTo(3);
                softly.assertThat(extract.jsonPath().getList("items")).hasSize(3);
                softly.assertThat(extract.jsonPath().getInt("pageInfo.size")).isEqualTo(20);
            });
        }

        @Test
        void 페이징_범위를_벗어나면_빈_목록을_응답한다() {
            Category electronics = givenElectronicsCategory();

            // given
            LostItem myItem = persistLostItemGraph(electronics);
            savePledge(myItem);

            // when
            ExtractableResponse<Response> extract =
                    get("/api/lost-items/pledged?page=2&limit=10");

            // then
            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(200);
                softly.assertThat(extract.jsonPath().getLong("count")).isEqualTo(0);
                softly.assertThat(extract.jsonPath().getList("items")).isEmpty();
                softly.assertThat(extract.jsonPath().getInt("pageInfo.page")).isEqualTo(2);
                softly.assertThat(extract.jsonPath().getLong("pageInfo.totalElements")).isEqualTo(1);
            });
        }

        @Test
        void 서약하지_않은_다른_사용자의_분실물은_조회되지_않는다() {
            Category electronics = givenElectronicsCategory();

            // given
            Member otherMember = Member.builder()
                    .studentId(999999)
                    .password("other_pass")
                    .role(Role.USER)
                    .build();
            memberRepository.save(otherMember);
            LostItem otherPledgedItem = persistLostItemGraph(electronics);

            Pledge otherPledge = PledgeFixture.PLEDGE(otherMember, otherPledgedItem);
            pledgeRepository.save(otherPledge);
            ReflectionTestUtils.setField(otherPledgedItem, "status", LostItemStatus.PLEDGED);
            lostItemRepository.save(otherPledgedItem);

            LostItem myPledgedItem = persistLostItemGraph(electronics);
            savePledge(myPledgedItem);

            // when
            ExtractableResponse<Response> extract =
                    get("/api/lost-items/pledged?page=1&limit=10");

            // then
            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(200);
                softly.assertThat(extract.jsonPath().getLong("count")).isEqualTo(1);
                List<Long> ids = extract.jsonPath().getList("items.id", Long.class);
                softly.assertThat(ids).containsExactly(myPledgedItem.getId());
                softly.assertThat(ids).doesNotContain(otherPledgedItem.getId());
            });
        }

    }
}
