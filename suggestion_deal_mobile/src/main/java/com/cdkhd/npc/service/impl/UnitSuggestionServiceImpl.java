package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.MobileUserDetailsImpl;
import com.cdkhd.npc.dto.PageDto;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.dto.HandleProcessAddDto;
import com.cdkhd.npc.entity.dto.ResultAddDto;
import com.cdkhd.npc.entity.vo.HandleProcessVo;
import com.cdkhd.npc.entity.vo.SugListItemVo;
import com.cdkhd.npc.entity.vo.ToDealDetailVo;
import com.cdkhd.npc.entity.vo.UnitSugDetailVo;
import com.cdkhd.npc.enums.*;
import com.cdkhd.npc.repository.base.AccountRepository;
import com.cdkhd.npc.repository.base.ConveyProcessRepository;
import com.cdkhd.npc.repository.base.DelaySuggestionRepository;
import com.cdkhd.npc.repository.member_house.SuggestionRepository;
import com.cdkhd.npc.repository.suggestion_deal.*;
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

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Date;
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
    private SuggestionSettingRepository suggestionSettingRepository;
    private UnitImageRepository unitImageRepository;
    private DelaySuggestionRepository delaySuggestionRepository;
    private HandleProcessRepository handleProcessRepository;
    private ResultRepository resultRepository;
    private SuggestionRepository suggestionRepository;

    @Autowired
    public UnitSuggestionServiceImpl(AccountRepository accountRepository, ConveyProcessRepository conveyProcessRepository, UnitSuggestionRepository unitSuggestionRepository, SuggestionSettingRepository suggestionSettingRepository, UnitImageRepository unitImageRepository, DelaySuggestionRepository delaySuggestionRepository, HandleProcessRepository handleProcessRepository, ResultRepository resultRepository, SuggestionRepository suggestionRepository) {
        this.accountRepository = accountRepository;
        this.conveyProcessRepository = conveyProcessRepository;
        this.unitSuggestionRepository = unitSuggestionRepository;
        this.suggestionSettingRepository = suggestionSettingRepository;
        this.unitImageRepository = unitImageRepository;
        this.delaySuggestionRepository = delaySuggestionRepository;
        this.handleProcessRepository = handleProcessRepository;
        this.resultRepository = resultRepository;
        this.suggestionRepository = suggestionRepository;
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
        conveyProcess.setUnitDealTime(new Date());
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
     * 分页查询办理中建议
     * @param userDetails
     * @param pageDto 分页dto
     * @return
     */
    @Override
    public RespBody findPageOfInDealing(MobileUserDetailsImpl userDetails, PageDto pageDto) {
        RespBody<PageVo<SugListItemVo>> body = new RespBody<>();

        Account account = accountRepository.findByUid(userDetails.getUid());
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
        orders.add(new Sort.Order(Sort.Direction.ASC, "unitView"));
        //按接受时间降序排序
        orders.add(new Sort.Order(Sort.Direction.DESC, "acceptTime"));
        Pageable pageable = PageRequest.of(pageDto.getPage()-1, pageDto.getSize(), Sort.by(orders));

        //办理中建议查询条件
        Specification<UnitSuggestion> spec = (root, query, cb) -> {
            List<Predicate> predicateList = new ArrayList<>();
            //建议未删除
            predicateList.add(cb.isFalse(root.get("suggestion").get("isDel").as(Boolean.class)));
            //建议状态为办理中（如果有另外的单位还未接受转办的话，suggestion的状态就不是办理中，故注释掉这一行）
            /*predicateList.add(cb.equal(root.get("suggestion").get("status").as(Byte.class),
                    SuggestionStatusEnum.HANDLING.getValue()));*/
            //unitSug的finish为false未办完
            predicateList.add(cb.isFalse(root.get("finish").as(Boolean.class)));
            //当前单位的建议
            predicateList.add(cb.equal(root.get("unit").get("uid").as(String.class),
                    unitUser.getUnit().getUid()));
            return cb.and(predicateList.toArray(new Predicate[0]));
        };

        Page<UnitSuggestion> page = unitSuggestionRepository.findAll(spec, pageable);
        List<SugListItemVo> sugListItemVoList = page.stream()
                .map(SugListItemVo::convert)
                .collect(Collectors.toList());

        PageVo<SugListItemVo> pageVo = new PageVo<>(page, pageDto);
        pageVo.setContent(sugListItemVoList);

        body.setData(pageVo);
        return body;
    }

    /**
     * 查看办理中建议详情
     * @param userDetails
     * @param unitSuggestionUid
     * @return
     */
    @Override
    public RespBody checkDealingDetail(MobileUserDetailsImpl userDetails, String unitSuggestionUid) {
        RespBody<UnitSugDetailVo> body = new RespBody<>();

        Account account = accountRepository.findByUid(userDetails.getUid());
        if (!checkIdentity(account)) {
            LOGGER.error("用户无权限查询建议详情，Account username: {}", account.getUsername());
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("当前用户不是办理单位，无法查询建议详情");
            return body;
        }

        UnitSuggestion unitSuggestion = unitSuggestionRepository.findByUid(unitSuggestionUid);
        if (unitSuggestion.getFinish()) {
            LOGGER.error("该建议状态不在办理中，UnitSuggestion uid: {}", unitSuggestion.getUid());
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("该建议状态不在办理中，无法查看");
            return body;
        }

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

        UnitSugDetailVo vo = UnitSugDetailVo.convert(unitSuggestion);
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
    public RespBody applyDelay(MobileUserDetailsImpl userDetails, String unitSuggestionUid, Date delayUntil, String reason) {
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
    public RespBody addHandleProcess(MobileUserDetailsImpl userDetails, HandleProcessAddDto toAdd) {
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
    public RespBody finishDeal(MobileUserDetailsImpl userDetails, ResultAddDto toAdd) {
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
        result.setSuggestion(unitSuggestion.getSuggestion());
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
    public RespBody uploadOneImage(MobileUserDetailsImpl userDetails, MultipartFile image, Byte type) {
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
     * 查看已办完建议
     * @param userDetails
     * @param pageDto
     * @return
     */
    @Override
    public RespBody findPageOfDone(MobileUserDetailsImpl userDetails, PageDto pageDto) {
        return null;
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
