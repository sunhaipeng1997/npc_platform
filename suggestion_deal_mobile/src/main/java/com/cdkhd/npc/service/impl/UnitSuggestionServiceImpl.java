package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.MobileUserDetailsImpl;
import com.cdkhd.npc.dto.PageDto;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.vo.SugListItemVo;
import com.cdkhd.npc.entity.vo.ToDealDetailVo;
import com.cdkhd.npc.enums.AccountRoleEnum;
import com.cdkhd.npc.enums.ConveyStatusEnum;
import com.cdkhd.npc.enums.GovDealStatusEnum;
import com.cdkhd.npc.enums.SuggestionStatusEnum;
import com.cdkhd.npc.repository.base.AccountRepository;
import com.cdkhd.npc.repository.base.ConveyProcessRepository;
import com.cdkhd.npc.repository.suggestion_deal.UnitSuggestionRepository;
import com.cdkhd.npc.service.UnitSuggestionService;
import com.cdkhd.npc.vo.PageVo;
import com.cdkhd.npc.vo.RespBody;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class UnitSuggestionServiceImpl implements UnitSuggestionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnitSuggestionServiceImpl.class);

    private AccountRepository accountRepository;
    private ConveyProcessRepository conveyProcessRepository;
    private UnitSuggestionRepository unitSuggestionRepository;

    @Autowired
    public UnitSuggestionServiceImpl(AccountRepository accountRepository, ConveyProcessRepository conveyProcessRepository, UnitSuggestionRepository unitSuggestionRepository) {
        this.accountRepository = accountRepository;
        this.conveyProcessRepository = conveyProcessRepository;
        this.unitSuggestionRepository = unitSuggestionRepository;
    }

    /**
     * 分页查询待办建议
     * @param userDetails 当前用户
     * @param pageDto 分页查询dto
     * @return 查询结果
     */
    @Override
    public RespBody findPageOfToDeal(MobileUserDetailsImpl userDetails, PageDto pageDto) {
        RespBody<PageVo<SugListItemVo>> body = new RespBody<>();

        Account account = accountRepository.findByUid(userDetails.getUid());
        if (!checkIdentity(account)) {
            LOGGER.error("用户无权限查询待办建议，Account username: {}", account.getUsername());
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("当前用户不是办理单位，无法查询待办建议");
            return body;
        }

        UnitUser unitUser = account.getUnitUser();

        //构造分页条件
        List<Sort.Order> orders = new ArrayList<>();
        //未读消息在前
        orders.add(new Sort.Order(Sort.Direction.ASC, "unitView"));
        //按转办时间降序排序
        orders.add(new Sort.Order(Sort.Direction.DESC, "conveyTime"));
        Pageable pageable = PageRequest.of(pageDto.getPage()-1, pageDto.getSize(), Sort.by(orders));

        //待办建议查询条件
        Specification<ConveyProcess> spec = (root, query, cb) -> {
            List<Predicate> predicateList = new ArrayList<>();
            //建议未删除
            predicateList.add(cb.isFalse(root.get("suggestion").get("isDel").as(Boolean.class)));
            //建议状态为已转交办理单位
            predicateList.add(cb.equal(root.get("suggestion").get("status").as(Byte.class),
                    SuggestionStatusEnum.TRANSFERRED_UNIT.getValue()));
            //转办过程的状态为转办中
            predicateList.add(cb.equal(root.get("status").as(Byte.class), ConveyStatusEnum.CONVEYING.getValue()));
            //建议转交给当前单位
            predicateList.add(cb.equal(root.get("unit").get("uid").as(String.class),
                    unitUser.getUnit().getUid()));
            return cb.and(predicateList.toArray(new Predicate[0]));
        };

        Page<ConveyProcess> page = conveyProcessRepository.findAll(spec, pageable);
        List<SugListItemVo> sugListItemVoList = page.stream()
                .map(SugListItemVo::convert)
                .collect(Collectors.toList());

        PageVo<SugListItemVo> pageVo = new PageVo<>(page, pageDto);
        pageVo.setContent(sugListItemVoList);

        body.setData(pageVo);
        return body;
    }

    /**
     * 办理单位查看待办建议详情
     * @param userDetails
     * @param conveyProcessUid 转办记录uid
     * @return
     */
    @Override
    public RespBody checkToDealDetail(MobileUserDetailsImpl userDetails, String conveyProcessUid) {
        RespBody<ToDealDetailVo> body = new RespBody<>();

        Account account = accountRepository.findByUid(userDetails.getUid());
        if (!checkIdentity(account)) {
            LOGGER.error("用户无权限查询待办建议详情，Account username: {}", account.getUsername());
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("当前用户不是办理单位，无法查询待办建议详情");
            return body;
        }

        ConveyProcess conveyProcess = conveyProcessRepository.findByUid(conveyProcessUid);
        if (!conveyProcess.getStatus().equals(ConveyStatusEnum.CONVEYING.getValue())) {
            LOGGER.error("该建议状态不是待办理，ConveyProcess uid: {}", conveyProcess.getUid());
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("该建议状态不是待办理，办理单位无法查看");
            return body;
        }

        UnitUser unitUser = account.getUnitUser();
        if (!conveyProcess.getUnit().getUid().equals(unitUser.getUnit().getUid())) {
            LOGGER.error("该建议未转办给当前单位 \n " +
                    "ConveyProcess uid: {} \n " +
                    "Current User's Unit uid: {}",
                    conveyProcess.getUid(), unitUser.getUnit().getUid());
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("该建议未转办给你，你无法查看建议详情");
            return body;
        }

        conveyProcess.setUnitView(true);
        conveyProcessRepository.saveAndFlush(conveyProcess);

        ToDealDetailVo vo = ToDealDetailVo.convert(conveyProcess);
        body.setData(vo);
        return body;
    }

    /**
     * 申请调整办理单位
     * @param userDetails
     * @param conveyProcessUid 申请调整的转办记录uid
     * @return
     */
    @Override
    public RespBody applyAdjust(MobileUserDetailsImpl userDetails, String conveyProcessUid, String adjustReason) {
        RespBody body = new RespBody();

        Account account = accountRepository.findByUid(userDetails.getUid());
        if (!checkIdentity(account)) {
            LOGGER.error("用户无权限操作待转办建议，Account username: {}", account.getUsername());
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("当前用户不是办理单位，申请失败");
            return body;
        }

        ConveyProcess conveyProcess = conveyProcessRepository.findByUid(conveyProcessUid);
        if (!conveyProcess.getStatus().equals(ConveyStatusEnum.CONVEYING.getValue())) {
            LOGGER.error("该建议状态不是待办理，ConveyProcess uid: {}", conveyProcess.getUid());
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("该建议状态不是待办理，无法申请");
            return body;
        }

        UnitUser unitUser = account.getUnitUser();
        if (!conveyProcess.getUnit().getUid().equals(unitUser.getUnit().getUid())) {
            LOGGER.error("该建议未转办给当前单位 \n " +
                            "ConveyProcess uid: {} \n " +
                            "Current User's Unit uid: {}",
                    conveyProcess.getUid(), unitUser.getUnit().getUid());
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("该建议未转办给你，申请调整失败");
            return body;
        }

        if (StringUtils.isBlank(adjustReason)) {
            LOGGER.error("申请调整理由不能为空");
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("申请调整理由不能为空");
            return body;
        }

        conveyProcess.setStatus(ConveyStatusEnum.CONVEY_FAILED.getValue());
        conveyProcess.setRemark(adjustReason);
        //设置政府未读
        conveyProcess.setGovView(false);
        conveyProcess.setDealStatus(GovDealStatusEnum.NOT_DEAL.getValue());
        Suggestion suggestion = conveyProcess.getSuggestion();
        suggestion.setStatus(SuggestionStatusEnum.SUBMITTED_GOVERNMENT.getValue());
        conveyProcessRepository.saveAndFlush(conveyProcess);

        return body;
    }

    /**
     * 接受转办，开始办理
     * @param userDetails
     * @param conveyProcessUid 转办记录uid
     * @return
     */
    @Override
    public RespBody startDealing(MobileUserDetailsImpl userDetails, String conveyProcessUid) {
        RespBody body = new RespBody();

        Account account = accountRepository.findByUid(userDetails.getUid());
        if (!checkIdentity(account)) {
            LOGGER.error("用户无权限操作待转办建议，Account username: {}", account.getUsername());
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("当前用户不是办理单位，无法办理");
            return body;
        }

        ConveyProcess conveyProcess = conveyProcessRepository.findByUid(conveyProcessUid);
        if (!conveyProcess.getStatus().equals(ConveyStatusEnum.CONVEYING.getValue())) {
            LOGGER.error("该建议状态不是待办理，ConveyProcess uid: {}", conveyProcess.getUid());
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("该建议状态不是待办理，无法办理");
            return body;
        }

        UnitUser unitUser = account.getUnitUser();
        if (!conveyProcess.getUnit().getUid().equals(unitUser.getUnit().getUid())) {
            LOGGER.error("该建议未转办给当前单位 \n " +
                            "ConveyProcess uid: {} \n " +
                            "Current User's Unit uid: {}",
                    conveyProcess.getUid(), unitUser.getUnit().getUid());
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("该建议未转办给你，无法办理");
            return body;
        }

        //设置ConveyProcess和Suggestion状态
        conveyProcess.setStatus(ConveyStatusEnum.CONVEY_SUCCESS.getValue());
        conveyProcess.setGovView(false);
        conveyProcess.setDealDone(true);
        Suggestion suggestion = conveyProcess.getSuggestion();
        suggestion.setStatus(SuggestionStatusEnum.HANDLING.getValue());
        conveyProcessRepository.saveAndFlush(conveyProcess);

        //创建UnitSuggestion记录
        UnitSuggestion unitSuggestion = new UnitSuggestion();
        unitSuggestion.setType(conveyProcess.getType());
        unitSuggestion.setUnitUser(unitUser);
        unitSuggestion.setUnit(unitUser.getUnit());
        unitSuggestion.setGovernmentUser(conveyProcess.getGovernmentUser());
        unitSuggestion.setSuggestion(suggestion);
        unitSuggestionRepository.saveAndFlush(unitSuggestion);

        return body;
    }

    /**
     * 分页查询办理中建议
     * @param userDetails
     * @param pageDto 分页dto
     * @return
     */
    @Override
    public RespBody findPageOfInDealing(MobileUserDetailsImpl userDetails, PageDto pageDto) {
        RespBody<PageVo<SugListItemVo>> body = new RespBody<>();

        /*Account account = accountRepository.findByUid(userDetails.getUid());
        if (!checkIdentity(account)) {
            LOGGER.error("用户无权限查询办理中建议，Account username: {}", account.getUsername());
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("当前用户不是办理单位，无法查询办理中建议");
            return body;
        }

        UnitUser unitUser = account.getUnitUser();

        //构造分页条件
        List<Sort.Order> orders = new ArrayList<>();
        //未读消息在前
        orders.add(new Sort.Order(Sort.Direction.ASC, "view"));
        //按接受时间降序排序
        orders.add(new Sort.Order(Sort.Direction.DESC, "acceptTime"));
        Pageable pageable = PageRequest.of(pageDto.getPage()-1, pageDto.getSize(), Sort.by(orders));

        //待办建议查询条件
        Specification<Unit> spec = (root, query, cb) -> {
            List<Predicate> predicateList = new ArrayList<>();
            //建议未删除
            predicateList.add(cb.isFalse(root.get("suggestion").get("isDel").as(Boolean.class)));
            //建议状态为已转交办理单位
            predicateList.add(cb.equal(root.get("suggestion").get("status").as(Byte.class),
                    SuggestionStatusEnum.TRANSFERRED_UNIT.getValue()));
            //转办过程的状态为转办中
            predicateList.add(cb.equal(root.get("status").as(Byte.class), (byte)0));
            //建议转交给当前单位
            predicateList.add(cb.equal(root.get("unit").get("uid").as(String.class),
                    unitUser.getUnit().getUid()));
            return cb.and(predicateList.toArray(new Predicate[0]));
        };

        Page<ConveyProcess> page = conveyProcessRepository.findAll(spec, pageable);
        List<SugListItemVo> sugListItemVoList = page.stream()
                .map(SugListItemVo::convert)
                .collect(Collectors.toList());

        PageVo<SugListItemVo> pageVo = new PageVo<>(page, pageDto);
        pageVo.setContent(sugListItemVoList);

        body.setData(pageVo);*/
        return body;
    }

    /**
     * 检查一个账号是否有办理单位角色
     * @param account 待检查的账号
     * @return 有/无 true/false
     */
    private boolean checkIdentity(Account account) {
        Set<AccountRole> roles = account.getAccountRoles();
        for (AccountRole role : roles) {
            if (role.getKeyword().equals(AccountRoleEnum.UNIT.name())) {
                return true;
            }
        }
        return false;
    }
}
