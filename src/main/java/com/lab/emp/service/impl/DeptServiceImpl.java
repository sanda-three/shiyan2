package com.lab.emp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lab.emp.common.BizException;
import com.lab.emp.entity.Dept;
import com.lab.emp.entity.Emp;
import com.lab.emp.mapper.DeptMapper;
import com.lab.emp.service.DeptService;
import com.lab.emp.service.EmpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 部门服务实现（一对多拓展）
 * "一"端查询时装配"多"端员工列表；删除前校验名下员工，级联保护在应用层实现
 */
@Service
public class DeptServiceImpl extends ServiceImpl<DeptMapper, Dept> implements DeptService {

    /** 与 EmpService 双向依赖，@Lazy 打破循环（同 EmpServiceImpl） */
    @Lazy
    @Autowired
    private EmpService empService;

    @Override
    @Transactional(readOnly = true)
    public Dept getWithEmps(Integer deptNo) {
        Dept dept = getById(deptNo);
        if (dept != null) {
            // 一对多装配：dept.dept_name = emp.dept（按名称关联）
            List<Emp> empList = empService.listByDept(dept.getDeptName());
            dept.setEmpList(empList);
        }
        return dept;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeDept(Integer deptNo) {
        Dept dept = getById(deptNo);
        if (dept == null) {
            throw new BizException(404, "部门不存在: deptNo=" + deptNo);
        }
        // 级联保护：名下仍有员工时拒绝删除，防止 emp.dept 悬空
        if (!empService.listByDept(dept.getDeptName()).isEmpty()) {
            throw new BizException(400, "部门[" + dept.getDeptName() + "]名下仍有员工，不能删除");
        }
        return removeById(deptNo);
    }
}
