package com.lab.emp.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lab.emp.entity.Emp;
import com.lab.emp.mapper.EmpMapper;
import com.lab.emp.service.EmpService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 员工服务实现（步骤 4.2：继承 MP ServiceImpl，通用 CRUD 由父类提供，
 * 本类只写业务方法；只读声明下沉到查询方法，避免类级 readOnly 波及继承来的写操作）
 */
@Service
public class EmpServiceImpl extends ServiceImpl<EmpMapper, Emp> implements EmpService {

    @Override
    @Transactional(readOnly = true)
    public List<Emp> listByDepartment(String department) {
        return lambdaQuery()
                .eq(Emp::getDepartment, department)
                .orderByAsc(Emp::getEmpId)
                .list();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Emp> searchByEmpName(String keyword) {
        return lambdaQuery()
                .like(StringUtils.hasText(keyword), Emp::getEmpName, keyword)
                .orderByAsc(Emp::getEmpId)
                .list();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Emp> pageByDepartment(long current, long size, String department) {
        // Page 配合 SpringConfig 注册的 PaginationInnerInterceptor 生效
        return page(new Page<>(current, size),
                Wrappers.<Emp>lambdaQuery()
                        .eq(StringUtils.hasText(department), Emp::getDepartment, department)
                        .orderByAsc(Emp::getEmpId));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailTaken(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }
        return lambdaQuery().eq(Emp::getEmail, email).exists();
    }
}
