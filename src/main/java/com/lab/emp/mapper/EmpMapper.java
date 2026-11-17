package com.lab.emp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lab.emp.entity.Emp;
import org.apache.ibatis.annotations.Mapper;

/**
 * 员工 Mapper 接口（MyBatis-Plus）
 * 继承 BaseMapper 即获得 selectById / insert / updateById / selectList(Wrapper) 等通用 CRUD，
 * 由 MapperScannerConfigurer 扫描注册，无需编写映射文件
 */
@Mapper
public interface EmpMapper extends BaseMapper<Emp> {
}
