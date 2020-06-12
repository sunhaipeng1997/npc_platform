package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.dto.ToDealPageDto;
import com.cdkhd.npc.entity.vo.ToDealListItemVo;
import com.cdkhd.npc.enums.*;
import com.cdkhd.npc.repository.base.AccountRepository;
import com.cdkhd.npc.repository.base.ConveyProcessRepository;
import com.cdkhd.npc.repository.member_house.SuggestionRepository;
import com.cdkhd.npc.repository.suggestion_deal.SuggestionSettingRepository;
import com.cdkhd.npc.repository.suggestion_deal.UnitSuggestionRepository;
import com.cdkhd.npc.service.GeneralService;
import com.cdkhd.npc.service.UnitSuggestionService;
import com.cdkhd.npc.vo.PageVo;
import com.cdkhd.npc.vo.RespBody;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class UnitSuggestionServiceImpl implements UnitSuggestionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnitSuggestionServiceImpl.class);

    private SuggestionRepository suggestionRepository;
    private GeneralService generalService;
    private ConveyProcessRepository conveyProcessRepository;
    private AccountRepository accountRepository;
    private UnitSuggestionRepository unitSuggestionRepository;
    private SuggestionSettingRepository suggestionSettingRepository;

    @Autowired
    public UnitSuggestionServiceImpl(SuggestionRepository suggestionRepository, GeneralService generalService, ConveyProcessRepository conveyProcessRepository, AccountRepository accountRepository, UnitSuggestionRepository unitSuggestionRepository, SuggestionSettingRepository suggestionSettingRepository) {
        this.suggestionRepository = suggestionRepository;
        this.generalService = generalService;
        this.conveyProcessRepository = conveyProcessRepository;
        this.accountRepository = accountRepository;
        this.unitSuggestionRepository = unitSuggestionRepository;
        this.suggestionSettingRepository = suggestionSettingRepository;
    }

    /**
     * 分页查询待办建议
     * @param userDetails
     * @param pageDto
     * @return
     */
    @Override
    public RespBody findToDeal(UserDetailsImpl userDetails, ToDealPageDto pageDto) {
        RespBody<PageVo<ToDealListItemVo>> body = new RespBody<>();

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
//        orders.add(new Sort.Order(Sort.Direction.ASC, "unitView"));
        //按转办时间降序排序
        orders.add(new Sort.Order(Sort.Direction.DESC, "conveyTime"));
        Pageable pageable = PageRequest
                .of(pageDto.getPage() - 1, pageDto.getSize(), Sort.by(orders));

        //待办建议查询条件
        Specification<ConveyProcess> toDealSpec = (root, query, cb) -> {
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

        //前端筛选条件
        Specification<ConveyProcess> filterSpec = (root, query, cb) -> {
            List<Predicate> predicateList = new ArrayList<>();

            //标题
            if (StringUtils.isNotBlank(pageDto.getTitle())) {
                predicateList.add(cb.like(root.get("suggestion").get("title").as(String.class), "%" + pageDto.getTitle() + "%"));
            }
            //类型
            if (StringUtils.isNotBlank(pageDto.getBusinessUid())) {
                predicateList.add(cb.equal(root.get("suggestion").get("suggestionBusiness").get("uid").as(String.class), pageDto.getBusinessUid()));
            }

            //提出代表
            if (StringUtils.isNotBlank(pageDto.getMemberName())) {
                predicateList.add(cb.like(root.get("suggestion").get("raiser").get("name").as(String.class), "%" + pageDto.getMemberName() + "%"));
            }
            if (StringUtils.isNotBlank(pageDto.getMemberMobile())) {
                predicateList.add(cb.equal(root.get("suggestion").get("raiser").get("mobile").as(String.class), pageDto.getMemberMobile()));
            }

            //转办时间 开始
            if (pageDto.getDateStart() != null) {
                predicateList.add(cb.greaterThanOrEqualTo(root.get("conveyTime").as(Date.class), pageDto.getDateStart()));
            }
            //转办时间 结束
            if (pageDto.getDateEnd() != null) {
                predicateList.add(cb.lessThanOrEqualTo(root.get("conveyTime").as(Date.class), pageDto.getDateEnd()));
            }

            if (Objects.nonNull(pageDto.getUnitType()) && !pageDto.getUnitType().equals((byte)0)) {
                predicateList.add(cb.equal(root.get("type").as(Byte.class), pageDto.getUnitType()));
            }

            return cb.and(predicateList.toArray(new Predicate[0]));
        };


        Page<ConveyProcess> page = conveyProcessRepository.findAll(toDealSpec.and(filterSpec), pageable);

        List<ToDealListItemVo> toDealListItemVoList = page.stream()
                .map(ToDealListItemVo::convert)
                .collect(Collectors.toList());

        PageVo<ToDealListItemVo> pageVo = new PageVo<>(page, pageDto);
        pageVo.setContent(toDealListItemVoList);

        body.setData(pageVo);
        return body;
    }

    /**
     * 申请调整办理单位
     * @param userDetails
     * @param conveyProcessUid 申请调整的转办记录uid
     * @return
     */
    @Override
    public RespBody applyAdjust(UserDetailsImpl userDetails, String conveyProcessUid, String adjustReason) {
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
        conveyProcess.setUnitDealTime(new Date());
        Suggestion suggestion = conveyProcess.getSuggestion();
        suggestion.setStatus(SuggestionStatusEnum.SUBMITTED_GOVERNMENT.getValue());
        conveyProcessRepository.saveAndFlush(conveyProcess);

        return body;
    }

    @Override
    public RespBody startDealing(UserDetailsImpl userDetails, String conveyProcessUid) {
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
            body.setMessage("该建议状态不是待办理，无法开始办理");
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

        //设置ConveyProcess的状态
        conveyProcess.setStatus(ConveyStatusEnum.CONVEY_SUCCESS.getValue());
        conveyProcess.setGovView(false);
        conveyProcess.setDealDone(true);
        conveyProcessRepository.saveAndFlush(conveyProcess);

        //计算办理截止时间
        SuggestionSetting setting = findSetting(unitUser.getUnit());
        int defaultDealTimeLimit = setting.getExpectDate();
        Date now  = new Date();
        Date expectDate = DateUtils.addDays(now, defaultDealTimeLimit);

        //创建UnitSuggestion记录
        UnitSuggestion unitSuggestion = new UnitSuggestion();
        unitSuggestion.setType(conveyProcess.getType());
        unitSuggestion.setAcceptTime(now);
        unitSuggestion.setExpectDate(expectDate);
        unitSuggestion.setUnitUser(unitUser);
        unitSuggestion.setUnit(unitUser.getUnit());
        unitSuggestion.setGovernmentUser(conveyProcess.getGovernmentUser());
        unitSuggestion.setSuggestion(conveyProcess.getSuggestion());
        unitSuggestionRepository.saveAndFlush(unitSuggestion);

        //如果当前接受转办的是主办单位，设置 Suggestion 的 unit （主办单位）字段
        Suggestion suggestion = conveyProcess.getSuggestion();
        if (conveyProcess.getType().equals(UnitTypeEnum.MAIN_UNIT.getValue())) {
            suggestion.setUnit(unitUser.getUnit());
            suggestionRepository.saveAndFlush(suggestion);
        }

        //只有该建议的所有转办流程都结束之后，才能修改 Suggestion 的状态为办理中
        List<ConveyProcess> conveyProcessList = conveyProcessRepository.findBySuggestionId(suggestion.getId());
        boolean allDone = true; //结束标志
        for (ConveyProcess process : conveyProcessList) {
            if (!process.getDealDone()) {
                allDone = false;
                break;
            }
        }
        if (allDone) {
            suggestion.setStatus(SuggestionStatusEnum.HANDLING.getValue());
            suggestion.setAcceptTime(now);
            suggestion.setExpectDate(expectDate);
            suggestionRepository.saveAndFlush(suggestion);
        }

        return body;
    }

    /**
     * 分页查询建议
     * @param userDetails 当前用户
     * @param dto 查询参数
     * @param status 建议状态
     * @return 分页查询结果
     */
    /*private Page<Suggestion> findPage(UserDetailsImpl userDetails, GovSuggestionPageDto dto, SuggestionStatusEnum status) {
        //分页查询条件
        Pageable pageable = PageRequest.of(dto.getPage()-1, dto.getSize(),
                Sort.Direction.fromString(dto.getDirection()), dto.getProperty());

        //其他查询条件
        Specification<Suggestion> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            //标题
            if (StringUtils.isNotBlank(dto.getTitle())) {
                predicates.add(cb.like(root.get("title").as(String.class), "%" + dto.getTitle() + "%"));
            }
            //类型
            if (StringUtils.isNotBlank(dto.getBusiness())) {
                predicates.add(cb.equal(root.get("suggestionBusiness").get("uid").as(String.class), dto.getBusiness()));
            }
            //提出代表
            if (StringUtils.isNotBlank(dto.getMember())) {
                predicates.add(cb.like(root.get("raiser").get("name").as(String.class), "%" + dto.getMember() + "%"));
            }
            if (StringUtils.isNotBlank(dto.getMobile())) {
                predicates.add(cb.equal(root.get("raiser").get("mobile").as(String.class), dto.getMobile()));
            }
            //审核时间 开始
            if (dto.getDateStart() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("workAt").as(Date.class), dto.getDateStart()));
            }
            if (dto.getDateEnd() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("workAt").as(Date.class), dto.getDateEnd()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        //基础查询条件
//        Specification<Suggestion> baseSpec = generalService.basePredicates(userDetails, status);

        return suggestionRepository.findAll(spec, pageable);
    }*/

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

    /**
     * 为当前单位查找系统设置
     * @param unit
     * @return 建议办理系统系统设置
     */
    private SuggestionSetting findSetting(Unit unit) {
        if (unit.getLevel().equals(LevelEnum.TOWN.getValue())) {
            return suggestionSettingRepository.findByLevelAndTownUid(unit.getLevel(), unit.getTown().getUid());
        } else if (unit.getLevel().equals(LevelEnum.AREA.getValue())) {
            return suggestionSettingRepository.findByLevelAndAreaUid(unit.getLevel(), unit.getArea().getUid());
        }
        throw new RuntimeException("当前单位的Level值不合法");
    }
}
