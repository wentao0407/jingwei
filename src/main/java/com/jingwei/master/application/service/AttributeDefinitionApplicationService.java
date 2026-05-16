package com.jingwei.master.application.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.master.application.dto.AttributeDefinitionQueryDTO;
import com.jingwei.master.application.dto.SaveAttributeDefinitionDTO;
import com.jingwei.master.domain.model.AttributeDefinition;
import com.jingwei.master.domain.repository.AttributeDefinitionRepository;
import com.jingwei.master.interfaces.vo.AttributeDefinitionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 属性定义应用服务
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttributeDefinitionApplicationService {

    private final AttributeDefinitionRepository repository;

    /**
     * 创建属性定义
     */
    @Transactional(rollbackFor = Exception.class)
    public AttributeDefinitionVO create(SaveAttributeDefinitionDTO dto) {
        AttributeDefinition entity = new AttributeDefinition();
        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
        entity.setMaterialType(dto.getMaterialType());
        entity.setInputType(dto.getInputType());
        entity.setRequired(dto.getRequired());
        entity.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        entity.setOptions(dto.getOptions());
        entity.setJsonbPath(dto.getJsonbPath());
        entity.setRemark(dto.getRemark());
        repository.insert(entity);
        log.info("创建属性定义: code={}", dto.getCode());
        return toVO(entity);
    }

    /**
     * 更新属性定义
     */
    @Transactional(rollbackFor = Exception.class)
    public AttributeDefinitionVO update(Long id, SaveAttributeDefinitionDTO dto) {
        AttributeDefinition entity = repository.selectById(id);
        if (entity == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "属性定义不存在");
        }
        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
        entity.setMaterialType(dto.getMaterialType());
        entity.setInputType(dto.getInputType());
        entity.setRequired(dto.getRequired());
        entity.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        entity.setOptions(dto.getOptions());
        entity.setJsonbPath(dto.getJsonbPath());
        entity.setRemark(dto.getRemark());
        repository.updateById(entity);
        log.info("更新属性定义: id={}", id);
        return toVO(entity);
    }

    /**
     * 删除属性定义
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        AttributeDefinition entity = repository.selectById(id);
        if (entity == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "属性定义不存在");
        }
        repository.deleteById(id);
        log.info("删除属性定义: id={}", id);
    }

    /**
     * 查询详情
     */
    public AttributeDefinitionVO getDetail(Long id) {
        AttributeDefinition entity = repository.selectById(id);
        if (entity == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "属性定义不存在");
        }
        return toVO(entity);
    }

    /**
     * 分页查询
     */
    public IPage<AttributeDefinitionVO> pageQuery(AttributeDefinitionQueryDTO dto) {
        Page<AttributeDefinition> page = new Page<>(
                Math.max(1, dto.getCurrent()), Math.max(1, dto.getSize()));
        IPage<AttributeDefinition> result = repository.selectPage(page, dto.getMaterialType(), dto.getKeyword());
        return result.convert(this::toVO);
    }

    /**
     * 按物料类型查询列表（不分页，用于前端动态表单渲染）
     */
    public List<AttributeDefinitionVO> listByMaterialType(String materialType) {
        return repository.selectByMaterialType(materialType).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    private AttributeDefinitionVO toVO(AttributeDefinition entity) {
        AttributeDefinitionVO vo = new AttributeDefinitionVO();
        vo.setId(entity.getId());
        vo.setCode(entity.getCode());
        vo.setName(entity.getName());
        vo.setMaterialType(entity.getMaterialType());
        vo.setInputType(entity.getInputType());
        vo.setRequired(entity.getRequired());
        vo.setSortOrder(entity.getSortOrder());
        vo.setOptions(entity.getOptions());
        vo.setJsonbPath(entity.getJsonbPath());
        vo.setRemark(entity.getRemark());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }
}
