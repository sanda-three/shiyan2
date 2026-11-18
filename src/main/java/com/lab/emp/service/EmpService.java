package com.lab.emp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lab.emp.dto.EmpQueryDTO;
import com.lab.emp.entity.Emp;

import java.util.List;

/**
 * 员工服务接口（继承 MP IService，通用 CRUD 由父类提供）
 */
public interface EmpService extends IService<Emp> {

    /**
     * 步骤 5.1：分页 + 多条件模糊查询（签名与实验指导书一致：分页参数在前，条件用 DTO 承载）
     * name 模糊、gender/dept 精确，空参数不参与过滤
     *
     * @param page  页码（从 1 开始）
     * @param size  每页条数
     * @param query 查询条件（name/gender/dept，可为空对象）
     * @return 分页结果（records/total/pages/current）
     */
    Page<Emp> pageQuery(long page, long size, EmpQueryDTO query);

    /**
     * 按部门查询员工列表
     */
    List<Emp> listByDept(String dept);

    /**
     * 步骤 5.1：批量删除（事务保证多语句原子性，全部成功或全部回滚）
     *
     * @param ids 员工 id 集合
     * @return 是否删除成功
     */
    boolean removeBatchByIds(List<Integer> ids);

    /**
     * 一对多拓展：查询所有员工并填充所属部门信息（emp.dept = dept.dept_name 关联）。
     * 部门一次性批量查出后按名称匹配，避免 N+1 查询
     *
     * @return 员工列表（deptInfo 已填充，部门不存在时为 null）
     */
    List<Emp> listWithDept();
}
