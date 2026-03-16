package com.greedy.zupzup.member.presentation;

import com.greedy.zupzup.auth.presentation.annotation.MemberAuth;
import com.greedy.zupzup.auth.presentation.argumentresolver.LoginMember;
import com.greedy.zupzup.member.application.MemberService;
import com.greedy.zupzup.member.application.dto.MemberEmailInfoResult;
import com.greedy.zupzup.member.application.dto.MemberEmailUpdateCommand;
import com.greedy.zupzup.member.presentation.dto.MemberEmailInfoResponse;
import com.greedy.zupzup.member.presentation.dto.MemberEmailUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController implements MemberControllerDocs {

    private final MemberService memberService;

    @Override
    @GetMapping("/me/email")
    public ResponseEntity<MemberEmailInfoResponse> getEmailInfo(
            @MemberAuth LoginMember loginMember
    ) {
        MemberEmailInfoResult result = memberService.getEmailInfo(loginMember.memberId());
        return ResponseEntity.ok(MemberEmailInfoResponse.from(result));
    }

    @Override
    @PutMapping("/me/email")
    public ResponseEntity<Void> updateEmail(
            @MemberAuth LoginMember loginMember,
            @RequestBody @Valid MemberEmailUpdateRequest request
    ) {
        MemberEmailUpdateCommand command = MemberEmailUpdateCommand.of(
                loginMember.memberId(),
                request.email(),
                request.emailAlertEnabled()
        );
        memberService.updateEmail(command);
        return ResponseEntity.ok().build();
    }
}
