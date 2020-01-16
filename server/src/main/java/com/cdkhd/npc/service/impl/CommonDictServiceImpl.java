package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.entity.CommonDict;
import com.cdkhd.npc.repository.base.CommonDictRepository;
import com.cdkhd.npc.service.CommonDictService;
import com.cdkhd.npc.vo.CommonVo;
import com.cdkhd.npc.vo.RespBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommonDictServiceImpl implements CommonDictService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonDictServiceImpl.class);

    private CommonDictRepository commonDictRepository;

    @Autowired
    public CommonDictServiceImpl(CommonDictRepository commonDictRepository) {
        this.commonDictRepository = commonDictRepository;
    }

    /**
     * 根据key获取相应的下拉
     * @return 查询结果
     */
    @Override
    public RespBody getListByKey(String key) {
        List<CommonDict> nationDicts = commonDictRepository.findByTypeAndIsDelFalse(key);
        List<CommonVo> nationVos = nationDicts.stream().map(commonDict ->
                CommonVo.convert(commonDict.getUid(), commonDict.getName())).collect(Collectors.toList());
        RespBody<List<CommonVo>> body = new RespBody<>();
        body.setData(nationVos);
        return body;
    }

}
