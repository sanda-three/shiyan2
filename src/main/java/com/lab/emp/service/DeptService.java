package com.lab.emp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lab.emp.entity.Dept;

import java.util.List;

/**
 * 部门服务接口（一对多拓展，继承 MP IService）
 */
public interface DeptService extends IService<Dept> {

    /**
     * 查询部门及其下属员工（一对多的核心演示：Dept.empList）
     *
     * @param deptNo 部门编号
     * @return 部门（含 empList），部门不存在返回 null
     */
    Dept getWithEmps(Integer deptNo);

    /**
     * 删除部门（级联保护：名下仍有员工时拒绝，保证数据一致性）
     *
     * @param deptNo 部门编号
     * @return 是否删除成功
     */
    boolean removeDept(Integer deptNo);
}
