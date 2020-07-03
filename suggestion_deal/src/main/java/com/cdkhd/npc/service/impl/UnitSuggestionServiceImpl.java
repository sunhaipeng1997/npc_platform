package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.dto.*;
import com.cdkhd.npc.entity.vo.*;
import com.cdkhd.npc.enums.*;
import com.cdkhd.npc.repository.base.AccountRepository;
import com.cdkhd.npc.repository.base.ConveyProcessRepository;
import com.cdkhd.npc.repository.base.DelaySuggestionRepository;
import com.cdkhd.npc.repository.member_house.SuggestionRepository;
import com.cdkhd.npc.repository.suggestion_deal.*;
import com.cdkhd.npc.service.GeneralService;
import com.cdkhd.npc.service.UnitSuggestionService;
import com.cdkhd.npc.util.ImageUploadUtil;
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
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.Order;
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
    private DelaySuggestionRepository delaySuggestionRepository;
    private HandleProcessRepository handleProcessRepository;
    private UnitImageRepository unitImageRepository;
    private ResultRepository resultRepository;

    @Autowired
    public UnitSuggestionServiceImpl(SuggestionRepository suggestionRepository, GeneralService generalService, ConveyProcessRepository conveyProcessRepository, AccountRepository accountRepository, UnitSuggestionRepository unitSuggestionRepository, SuggestionSettingRepository suggestionSettingRepository, DelaySuggestionRepository delaySuggestionRepository, HandleProcessRepository handleProcessRepository, UnitImageRepository unitImageRepository, ResultRepository resultRepository) {
        this.suggestionRepository = suggestionRepository;
        this.generalService = generalService;
        this.conveyProcessRepository = conveyProcessRepository;
        this.accountRepository = accountRepository;
        this.unitSuggestionRepository = unitSuggestionRepository;
        this.suggestionSettingRepository = suggestionSettingRepository;
        this.delaySuggestionRepository = delaySuggestionRepository;
        this.handleProcessRepository = handleProcessRepository;
        this.unitImageRepository = unitImageRepository;
        this.resultRepository = resultRepository;
    }

    /**
     * 分页查询待办建议
     * @param userDetails
     * @param pageDto
     * @return
     */
    @Override
    public RespBody findPageOfToDeal(UserDetailsImpl userDetails, ToDealPageDto pageDto) {
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
     * 查看待办建议详情
     * @param userDetails
     * @param cpUid
     * @return
     */
    @Override
    public RespBody checkToDealDetail(UserDetailsImpl userDetails, String cpUid) {
        RespBody<ToDealVo> body = new RespBody<>();

        Account account = accountRepository.findByUid(userDetails.getUid());
        if (!checkIdentity(account)) {
            LOGGER.error("用户无权限查询待办建议详情，Account username: {}", account.getUsername());
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("当前用户不是办理单位，无法查询待办建议详情");
            return body;
        }

        ConveyProcess conveyProcess = conveyProcessRepository.findByUid(cpUid);
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

        ToDealVo vo = ToDealVo.convert(conveyProcess);
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
            suggestionRepository.saveAndFlush(suggestion);
        }

        return body;
    }

    /**
     * 分页查询办理中建议
     * @param userDetails
     * @param pageDto
     * @return
     */
    @Override
    public RespBody findPageOfInDealing(UserDetailsImpl userDetails, InDealingPageDto pageDto) {
        RespBody<PageVo<InDealingListItemVo>> body = new RespBody<>();

        Account account = accountRepository.findByUid(userDetails.getUid());
        if (!checkIdentity(account)) {
            LOGGER.error("用户无权限查询办理中建议，Account username: {}", account.getUsername());
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("当前用户不是办理单位，无法查询办理中建议");
            return body;
        }

        UnitUser unitUser = account.getUnitUser();

        //扫描建议，更新临期和逾期标志
        generalService.scanSuggestions(userDetails);

        //分页条件
        Pageable pageable = PageRequest
                .of(pageDto.getPage() - 1, pageDto.getSize());

        //办理中建议查询条件
        Specification<UnitSuggestion> inDealingSpec = (root, query, cb) -> {
            List<Predicate> predicateList = new ArrayList<>();
            //建议未删除
            predicateList.add(cb.isFalse(root.get("suggestion").get("isDel").as(Boolean.class)));
            //建议状态为办理中
            /*predicateList.add(cb.equal(root.get("suggestion").get("status").as(Byte.class),
                    SuggestionStatusEnum.HANDLING.getValue()));*/
            //unitSuggestion的状态为办理中
            predicateList.add(cb.isFalse(root.get("finish").as(Boolean.class)));
            //当前单位的建议
            predicateList.add(cb.equal(root.get("unit").get("uid").as(String.class),
                    unitUser.getUnit().getUid()));

            //排序条件
            List<Order> orderList = new ArrayList<>();
            //催办的建议排在最前面
            orderList.add(cb.desc(
                    root.get("suggestion").get("urgeLevel").as(Integer.class)
            ));
            //逾期的建议
            orderList.add(cb.desc(
                    root.get("suggestion").get("exceedLimit").as(Boolean.class)
            ));
            //临期的建议
            orderList.add(cb.desc(
                    root.get("suggestion").get("closeDeadLine").as(Boolean.class)
            ));
            //未读的建议
            orderList.add(cb.asc(
                    root.get("unitView").as(Boolean.class)
            ));
            //最后按开始办理时间排序
            orderList.add(cb.desc(
                    root.get("acceptTime").as(Date.class)
            ));

            query.orderBy(orderList);
            query.where(predicateList.toArray(new Predicate[0]));

            return query.getRestriction();
        };

        //前端筛选条件
        Specification<UnitSuggestion> filterSpec = (root, query, cb) -> {
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

            //受理时间 开始
            if (pageDto.getDateStart() != null) {
                predicateList.add(cb.greaterThanOrEqualTo(root.get("acceptTime").as(Date.class), pageDto.getDateStart()));
            }
            //受理时间 结束
            if (pageDto.getDateEnd() != null) {
                predicateList.add(cb.lessThanOrEqualTo(root.get("acceptTime").as(Date.class), pageDto.getDateEnd()));
            }

            if (Objects.nonNull(pageDto.getUnitType()) && !pageDto.getUnitType().equals((byte)0)) {
                predicateList.add(cb.equal(root.get("type").as(Byte.class), pageDto.getUnitType()));
            }

            return cb.and(predicateList.toArray(new Predicate[0]));
        };


        Page<UnitSuggestion> page = unitSuggestionRepository.findAll(inDealingSpec.and(filterSpec), pageable);

        List<InDealingListItemVo> inDealingListItemVoList = page.stream()
                .map(InDealingListItemVo::convert)
                .collect(Collectors.toList());

        PageVo<InDealingListItemVo> pageVo = new PageVo<>(page, pageDto);
        pageVo.setContent(inDealingListItemVoList);

        body.setData(pageVo);
        return body;
    }

    /**
     * 查看建议详情（办理中，已办完，已办结）
     * @param userDetails
     * @param usUid
     * @return
     */
    @Override
    public RespBody checkDetail(UserDetailsImpl userDetails, String usUid) {
        RespBody<UnitSugVo> body = new RespBody<>();

        Account account = accountRepository.findByUid(userDetails.getUid());
        if (!checkIdentity(account)) {
            LOGGER.error("用户无权限查询建议详情，Account username: {}", account.getUsername());
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("当前用户不是办理单位，无法查询建议详情");
            return body;
        }

        UnitSuggestion unitSuggestion = unitSuggestionRepository.findByUid(usUid);
        UnitUser unitUser = account.getUnitUser();
        if (!unitSuggestion.getUnit().getUid().equals(unitUser.getUnit().getUid()) || !unitSuggestion.getUnitUser().getUid().equals(unitUser.getUid())) {
            LOGGER.error("该建议未转办给当前单位/办理人员 \n " +
                            "UnitSuggestion uid: {} \n " +
                            "Current User's uid: {}",
                    unitSuggestion.getUid(), unitUser.getUid());
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("该建议未转办给你，你无法查看建议详情");
            return body;
        }

        unitSuggestion.setUnitView(true);
        unitSuggestionRepository.saveAndFlush(unitSuggestion);

        UnitSugVo vo = UnitSugVo.convert(unitSuggestion);
        //返回办理流程图片
        for (HandleProcessVo processVo : vo.getProcesses()) {
            HandleProcess process = handleProcessRepository.findByUid(processVo.getUid());
            List<UnitImage> unitImages = unitImageRepository.findByTypeAndBelongToId(ImageTypeEnum.HANDLE_PROCESS.getValue(), process.getId());
            List<String> images = unitImages.stream()
                    .map(UnitImage::getUrl)
                    .collect(Collectors.toList());
            processVo.setImages(images);
        }

        body.setData(vo);
        return body;
    }

    /**
     * 申请延期建议
     * @param userDetails
     * @param unitSuggestionUid
     * @param delayUntil 申请延期至的日期
     * @param reason 原因
     * @return
     */
    @Override
    public RespBody applyDelay(UserDetailsImpl userDetails, String unitSuggestionUid, Date delayUntil, String reason) {
        RespBody body = new RespBody();

        Account account = accountRepository.findByUid(userDetails.getUid());
        if (!checkIdentity(account)) {
            LOGGER.error("用户无权限操作办理中建议，Account username: {}", account.getUsername());
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("当前用户不是办理单位，申请失败");
            return body;
        }

        UnitSuggestion unitSuggestion = unitSuggestionRepository.findByUid(unitSuggestionUid);
        if (unitSuggestion.getFinish()) {
            LOGGER.error("该建议状态不在办理中，UnitSuggestion uid: {}", unitSuggestion.getUid());
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("该建议状态不在办理中，申请失败");
            return body;
        }

        UnitUser unitUser = account.getUnitUser();
        if (!unitSuggestion.getUnit().getUid().equals(unitUser.getUnit().getUid()) || !unitSuggestion.getUnitUser().getUid().equals(unitUser.getUid())) {
            LOGGER.error("该建议未转办给当前单位/办理人员 \n " +
                            "UnitSuggestion uid: {} \n " +
                            "Current User's uid: {}",
                    unitSuggestion.getUid(), unitUser.getUid());
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("该建议未转办给你，无法申请延期");
            return body;
        }

        if (StringUtils.isBlank(reason)) {
            LOGGER.error("申请延期理由不能为空");
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("申请延期理由不能为空");
            return body;
        }

        if (delayUntil == null || delayUntil.before(unitSuggestion.getExpectDate())) {
            LOGGER.error("申请延期日期参数不合法，delayUntil: {}", delayUntil);
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("申请延期日期不合法");
            return body;
        }

        //检查是否有正在申请延期的建议
        Set<DelaySuggestion> delaySuggestions = unitSuggestion.getDelaySuggestions();
        for (DelaySuggestion delaySug : delaySuggestions) {
            if (delaySug.getAccept() == null) {
                LOGGER.error("还有未审核的延期申请，暂时无法申请延期");
                body.setStatus(HttpStatus.BAD_REQUEST);
                body.setMessage("还有未审核的延期申请，暂时无法申请延期");
                return body;
            }
        }

        //添加延期申请记录
        DelaySuggestion delay = new DelaySuggestion();
        delay.setApplyTime(delayUntil);
        delay.setReason(reason);
        delay.setUnitSuggestion(unitSuggestion);
        delay.setSuggestion(unitSuggestion.getSuggestion());
        delaySuggestionRepository.saveAndFlush(delay);

        return body;
    }

    /**
     * 添加办理过程
     * @param userDetails
     * @param toAdd
     * @return
     */
    @Override
    public RespBody addHandleProcess(UserDetailsImpl userDetails, HandleProcessAddDto toAdd) {
        RespBody<String> body = new RespBody<>();

        Account account = accountRepository.findByUid(userDetails.getUid());
        if (!checkIdentity(account)) {
            LOGGER.error("用户无权限操作办理中建议，Account username: {}", account.getUsername());
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("当前用户不是办理单位，添加失败");
            return body;
        }

        if (StringUtils.isBlank(toAdd.getUnitSugUid())) {
            LOGGER.error("参数不合法，unitSugUid不能为空");
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("unitSugUid为空，添加失败");
            return body;
        }

        UnitSuggestion unitSuggestion = unitSuggestionRepository.findByUid(toAdd.getUnitSugUid());
        if (unitSuggestion.getFinish()) {
            LOGGER.error("该建议状态不在办理中，UnitSuggestion uid: {}", unitSuggestion.getUid());
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("该建议状态不在办理中，添加失败");
            return body;
        }

        UnitUser unitUser = account.getUnitUser();
        if (!unitSuggestion.getUnit().getUid().equals(unitUser.getUnit().getUid()) || !unitSuggestion.getUnitUser().getUid().equals(unitUser.getUid())) {
            LOGGER.error("该建议未转办给当前单位/办理人员 \n " +
                            "UnitSuggestion uid: {} \n " +
                            "Current User's uid: {}",
                    unitSuggestion.getUid(), unitUser.getUid());
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("该建议未转办给你，无法添加流程");
            return body;
        }

        //参数校验
        if (toAdd.getHandleTime() == null || StringUtils.isBlank(toAdd.getDescription())) {
            LOGGER.error("参数不合法，handleTime和description不能为空");
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("handleTime或description为空，添加失败");
            return body;
        }

        //新增流程
        HandleProcess process = new HandleProcess();
        process.setHandleTime(toAdd.getHandleTime());
        process.setDescription(toAdd.getDescription());
        process.setUnitSuggestion(unitSuggestion);
        handleProcessRepository.saveAndFlush(process);

        //保存图片
        List<UnitImage> unitImageList = new ArrayList<>();
        for (String url : toAdd.getImageUrls()) {
            UnitImage image = new UnitImage();
            image.setType(ImageTypeEnum.HANDLE_PROCESS.getValue());
            image.setBelongToId(process.getId());
            image.setUrl(url);
            unitImageList.add(image);
        }
        unitImageRepository.saveAll(unitImageList);

        return body;
    }

    /**
     * 完成办理，添加办理结果。
     * 主办单位完成办理时，所有的协办单位必须已经完成办理
     * @param userDetails
     * @param toAdd
     * @return
     */
    @Override
    public RespBody finishDeal(UserDetailsImpl userDetails, ResultAddDto toAdd) {
        RespBody body = new RespBody();

        Account account = accountRepository.findByUid(userDetails.getUid());
        if (!checkIdentity(account)) {
            LOGGER.error("用户无权限操作办理中建议，Account username: {}", account.getUsername());
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("当前用户不是办理单位，无法完成办理");
            return body;
        }

        //参数校验
        if (StringUtils.isBlank(toAdd.getUnitSugUid()) || StringUtils.isBlank(toAdd.getResult())) {
            LOGGER.error("参数不合法，unitSugUid / result 不能为空");
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("参数不完整，操作失败");
            return body;
        }

        UnitSuggestion unitSuggestion = unitSuggestionRepository.findByUid(toAdd.getUnitSugUid());
        UnitUser unitUser = account.getUnitUser();
        if (!unitSuggestion.getUnit().getUid().equals(unitUser.getUnit().getUid()) || !unitSuggestion.getUnitUser().getUid().equals(unitUser.getUid())) {
            LOGGER.error("该建议未转办给当前单位/办理人员 \n " +
                            "UnitSuggestion uid: {} \n " +
                            "Current User's uid: {}",
                    unitSuggestion.getUid(), unitUser.getUid());
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("该建议未转办给你，无法完成办理");
            return body;
        }

        //检查建议状态
        if (unitSuggestion.getFinish()) {
            LOGGER.error("该建议状态不在办理中，UnitSuggestion uid: {}", unitSuggestion.getUid());
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("该建议状态不在办理中，操作失败");
            return body;
        }

        //是否可以完成办理
        if (!canFinish(unitSuggestion)) {
            LOGGER.error("有协办单位尚未完成办理");
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("有协办单位尚未完成办理，无法完成办理");
            return body;
        }

        //添加办理结果
        Result result = new Result();
        result.setResult(toAdd.getResult());
        result.setUnitSuggestion(unitSuggestion);
        /*
         * 协办单位完成办理时，不能设置 Suggestion 字段。
         * 设置以后会导致一个 Suggestion 关联多个 Result，查询报错
         */
        if (unitSuggestion.getType().equals(UnitTypeEnum.MAIN_UNIT.getValue())) {
            result.setSuggestion(unitSuggestion.getSuggestion());
        }
        resultRepository.saveAndFlush(result);

        //保存结果图片
        List<UnitImage> unitImageList = new ArrayList<>();
        for (String url : toAdd.getImageUrls()) {
            UnitImage image = new UnitImage();
            image.setType(ImageTypeEnum.HANDLE_RESULT.getValue());
            image.setBelongToId(result.getId());
            image.setUrl(url);
            unitImageList.add(image);
        }
        unitImageRepository.saveAll(unitImageList);

        //修改 UnitSuggestion 的相关字段
        Date now = new Date();
        unitSuggestion.setFinishTime(now);
        unitSuggestion.setFinish(true);
        unitSuggestionRepository.saveAndFlush(unitSuggestion);

        //只有主办单位完成办理的时候，才能修改 Suggestion 的状态
        if (unitSuggestion.getType().equals(UnitTypeEnum.MAIN_UNIT.getValue())) {
            Suggestion suggestion = unitSuggestion.getSuggestion();
            suggestion.setStatus(SuggestionStatusEnum.HANDLED.getValue());
            suggestion.setFinishTime(now);
            //设置代表未查看
            suggestion.setDoneView(false);
            //设置政府未查看
            suggestion.setGovView(false);
            suggestionRepository.saveAndFlush(suggestion);
        }

        return body;
    }

    /**
     * 上传一张图片（办理流程，办理结果），写入磁盘，返回图片url
     * @param userDetails
     * @param image 图片二进制数据
     * @param type 图片类型
     * @return 图片访问url
     */
    @Override
    public RespBody uploadOneImage(UserDetailsImpl userDetails, MultipartFile image, Byte type) {
        RespBody<String> body = new RespBody<>();

        Account account = accountRepository.findByUid(userDetails.getUid());
        if (!checkIdentity(account)) {
            LOGGER.error("用户无权限添加图片，Account username: {}", account.getUsername());
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("当前用户不是办理单位，添加图片失败");
            return body;
        }

        //参数校验
        if (image == null || type == null) {
            LOGGER.error("参数不合法，image 和 type 不能为空");
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("参数不完整，添加图片失败");
            return body;
        }

        //校验type值的范围
        String kind;
        if (type.equals(ImageTypeEnum.HANDLE_PROCESS.getValue())) {
            kind = "handleProcessImage";
        } else if (type.equals(ImageTypeEnum.HANDLE_RESULT.getValue())) {
            kind = "resultImage";
        } else {
            LOGGER.error("参数 type 不合法");
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("参数 type 不合法，添加图片失败");
            return body;
        }

        //存储图片
        String url = ImageUploadUtil.saveImage(kind, image);
        if (url.equals("error")) {
            LOGGER.error("图片写入磁盘失败");
            body.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            body.setMessage("图片上传失败");
            return body;
        }

        body.setData(url);
        return body;
    }

    /**
     * 分页查询已办完建议
     *
     * @param userDetails
     * @param pageDto
     * @return
     */
    @Override
    public RespBody findPageOfDone(UserDetailsImpl userDetails, DonePageDto pageDto) {
        RespBody<PageVo<CompleteListItemVo>> body = new RespBody<>();

        Account account = accountRepository.findByUid(userDetails.getUid());
        if (!checkIdentity(account)) {
            LOGGER.error("用户无权限查询已办完建议，Account username: {}", account.getUsername());
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("当前用户不是办理单位，无法查询已办完建议");
            return body;
        }

        UnitUser unitUser = account.getUnitUser();

        //构造分页条件
        List<Sort.Order> orders = new ArrayList<>();
        //未读消息在前
//        orders.add(new Sort.Order(Sort.Direction.ASC, "unitView"));
        //按办完时间降序排序
        orders.add(new Sort.Order(Sort.Direction.DESC, "finishTime"));
        Pageable pageable = PageRequest
                .of(pageDto.getPage() - 1, pageDto.getSize(), Sort.by(orders));

        //已办完建议查询条件
        Specification<UnitSuggestion> doneSpec = (root, query, cb) -> {
            List<Predicate> predicateList = new ArrayList<>();
            //建议未删除
            predicateList.add(cb.isFalse(root.get("suggestion").get("isDel").as(Boolean.class)));
            //建议状态为已办完
            predicateList.add(cb.equal(root.get("suggestion").get("status").as(Byte.class),
                    SuggestionStatusEnum.HANDLED.getValue()));
            //unitSuggestion的状态为已办完
            predicateList.add(cb.isTrue(root.get("finish").as(Boolean.class)));
            //当前单位的建议
            predicateList.add(cb.equal(root.get("unit").get("uid").as(String.class),
                    unitUser.getUnit().getUid()));
            return cb.and(predicateList.toArray(new Predicate[0]));
        };

        //前端筛选条件
        Specification<UnitSuggestion> filterSpec = (root, query, cb) -> {
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

            //办完时间 开始
            if (pageDto.getDateStart() != null) {
                predicateList.add(cb.greaterThanOrEqualTo(root.get("finishTime").as(Date.class), pageDto.getDateStart()));
            }
            //办完时间 结束
            if (pageDto.getDateEnd() != null) {
                predicateList.add(cb.lessThanOrEqualTo(root.get("finishTime").as(Date.class), pageDto.getDateEnd()));
            }

            if (Objects.nonNull(pageDto.getUnitType()) && !pageDto.getUnitType().equals((byte)0)) {
                predicateList.add(cb.equal(root.get("type").as(Byte.class), pageDto.getUnitType()));
            }

            return cb.and(predicateList.toArray(new Predicate[0]));
        };


        Page<UnitSuggestion> page = unitSuggestionRepository.findAll(doneSpec.and(filterSpec), pageable);

        List<CompleteListItemVo> doneListItemVoList = page.stream()
                .map(CompleteListItemVo::convert)
                .collect(Collectors.toList());

        PageVo<CompleteListItemVo> pageVo = new PageVo<>(page, pageDto);
        pageVo.setContent(doneListItemVoList);

        body.setData(pageVo);
        return body;
    }

    /**
     * 分页查询已办结建议
     *
     * @param userDetails
     * @param pageDto
     * @return
     */
    @Override
    public RespBody findPageOfComplete(UserDetailsImpl userDetails, CompletePageDto pageDto) {
        RespBody<PageVo<CompleteListItemVo>> body = new RespBody<>();

        Account account = accountRepository.findByUid(userDetails.getUid());
        if (!checkIdentity(account)) {
            LOGGER.error("用户无权限查询已办结建议，Account username: {}", account.getUsername());
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("当前用户不是办理单位，无法查询已办结建议");
            return body;
        }

        UnitUser unitUser = account.getUnitUser();

        //构造分页条件
        List<Sort.Order> orders = new ArrayList<>();
        //未读消息在前
//        orders.add(new Sort.Order(Sort.Direction.ASC, "unitView"));
        //按办完时间降序排序
        orders.add(new Sort.Order(Sort.Direction.DESC, "finishTime"));
        Pageable pageable = PageRequest
                .of(pageDto.getPage() - 1, pageDto.getSize(), Sort.by(orders));

        //已办结建议查询条件
        Specification<UnitSuggestion> completeSpec = (root, query, cb) -> {
            List<Predicate> predicateList = new ArrayList<>();
            //建议未删除
            predicateList.add(cb.isFalse(root.get("suggestion").get("isDel").as(Boolean.class)));
            //建议状态为已办结
            predicateList.add(cb.equal(root.get("suggestion").get("status").as(Byte.class),
                    SuggestionStatusEnum.ACCOMPLISHED.getValue()));
            //unitSuggestion的状态为已办完
            predicateList.add(cb.isTrue(root.get("finish").as(Boolean.class)));
            //当前单位的建议
            predicateList.add(cb.equal(root.get("unit").get("uid").as(String.class),
                    unitUser.getUnit().getUid()));
            return cb.and(predicateList.toArray(new Predicate[0]));
        };

        //前端筛选条件
        Specification<UnitSuggestion> filterSpec = (root, query, cb) -> {
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

            //办完时间 开始
            if (pageDto.getDateStart() != null) {
                predicateList.add(cb.greaterThanOrEqualTo(root.get("finishTime").as(Date.class), pageDto.getDateStart()));
            }
            //办完时间 结束
            if (pageDto.getDateEnd() != null) {
                predicateList.add(cb.lessThanOrEqualTo(root.get("finishTime").as(Date.class), pageDto.getDateEnd()));
            }

            if (Objects.nonNull(pageDto.getUnitType()) && !pageDto.getUnitType().equals((byte)0)) {
                predicateList.add(cb.equal(root.get("type").as(Byte.class), pageDto.getUnitType()));
            }

            return cb.and(predicateList.toArray(new Predicate[0]));
        };


        Page<UnitSuggestion> page = unitSuggestionRepository.findAll(completeSpec.and(filterSpec), pageable);

        List<CompleteListItemVo> completeListItemVoList = page.stream()
                .map(CompleteListItemVo::convert)
                .collect(Collectors.toList());

        PageVo<CompleteListItemVo> pageVo = new PageVo<>(page, pageDto);
        pageVo.setContent(completeListItemVoList);

        body.setData(pageVo);
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

    /**
     * 检查一个办理中建议能否完成办理
     * @param unitSuggestion
     * @return
     */
    private boolean canFinish(UnitSuggestion unitSuggestion) {
        //主办单位只有在所有协办单位完成办理之后，才能完成办理
        if (unitSuggestion.getType().equals(UnitTypeEnum.MAIN_UNIT.getValue())) {
            List<UnitSuggestion> assistUnitSugs = unitSuggestionRepository
                    .findBySuggestionUidAndType(unitSuggestion.getSuggestion().getUid(), UnitTypeEnum.CO_UNIT.getValue());
            for (UnitSuggestion unitSug : assistUnitSugs) {
                if (!unitSug.getFinish()) {
                    LOGGER.info("协办单位尚未完成办理，UnitSuggestion Uid: {}", unitSug.getUid());
                    return false;
                }
            }
        }
        return true;
    }
}
