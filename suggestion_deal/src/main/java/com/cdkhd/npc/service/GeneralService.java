package com.cdkhd.npc.service;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.Suggestion;
import com.cdkhd.npc.enums.SuggestionStatusEnum;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;
import java.util.List;

public interface GeneralService {
    RespBody findSugBusiness(UserDetailsImpl userDetails);

    RespBody findSugBusinessByTown(String townUid);

    Specification<Suggestion> basePredicates(UserDetailsImpl userDetails, SuggestionStatusEnum status);

    void scanSuggestions(UserDetailsImpl userDetails);
}
