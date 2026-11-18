package com.lab.emp.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lab.emp.common.BizException;
import com.lab.emp.dto.EmpQueryDTO;
import com.lab.emp.entity.Dept;
import com.lab.emp.entity.Emp;
import com.lab.emp.mapper.EmpMapper;
import com.lab.emp.service.DeptService;
import com.lab.emp.service.EmpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 员工服务实现（步骤 5.1）
 * 事务声明精确到方法：查询 readOnly=true，批量删除 REQUIRED——
 * 不可打在类级别（会波及继承来的写方法）
 */
@Service
public class EmpServiceImpl extends ServiceImpl<EmpMapper, Emp> implements EmpService {

    /** 与 DeptService 存在双向依赖（部门校验员工/员工填充部门），
     * 用 @Lazy 注入打破循环：实际调用时才解析目标 Bean */
    @Lazy
    @Autowired
    private DeptService deptService;

    @Override
    @Transactional(readOnly = true)
    public Page<Emp> pageQuery(long page, long size, EmpQueryDTO query) {
        // 调用方未传条件时按"无过滤"处理，避免下面逐条判空出现空指针
        EmpQueryDTO cond = query != null ? query : new EmpQueryDTO();
        // 分页由 SpringConfig 注册的 PaginationInnerInterceptor 拦截生成 limit/count sql
        return lambdaQuery()
                .like(StringUtils.hasText(cond.getName()), Emp::getEmpName, cond.getName())
                .eq(StringUtils.hasText(cond.getGender()), Emp::getGender, cond.getGender())
                .eq(StringUtils.hasText(cond.getDept()), Emp::getDept, cond.getDept())
                .orderByAsc(Emp::getEmpId)
                .page(new Page<>(page, size));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Emp> listByDept(String dept) {
        return lambdaQuery()
                .eq(Emp::getDept, dept)
                .orderByAsc(Emp::getEmpId)
                .list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeBatchByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BizException(400, "批量删除的 id 列表不能为空");
        }
        return removeByIds(ids);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Emp> listWithDept() {
        List<Emp> emps = lambdaQuery().orderByAsc(Emp::getEmpId).list();
        // 部门一次性查出按名称建索引，避免逐条查询的 N+1 问题
        Map<String, Dept> deptMap = deptService.list().stream()
                .collect(Collectors.toMap(Dept::getDeptName, d -> d));
        emps.forEach(e -> e.setDeptInfo(deptMap.get(e.getDept())));
        return emps;
    }
}
