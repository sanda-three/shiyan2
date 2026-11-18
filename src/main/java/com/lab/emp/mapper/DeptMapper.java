package com.lab.emp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lab.emp.entity.Dept;
import org.apache.ibatis.annotations.Mapper;

/**
 * 部门 Mapper 接口（一对多拓展）：通用 CRUD 由 BaseMapper 提供，
 * 由 MapperScannerConfigurer 扫描 com.lab.emp.mapper 包自动注册
 */
@Mapper
public interface DeptMapper extends BaseMapper<Dept> {
}
