package com.lab.emp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.emp.entity.Emp;

import java.util.List;

/**
 * 员工服务接口（步骤 4.2：继承 MP IService，获得 save/updateById/removeById/
 * list/page/getById 等通用方法；自定义方法仅补充业务语义部分）
 */
public interface EmpService extends IService<Emp> {

    /**
     * 按部门查询员工（业务语义：listBy + 维度）
     */
    List<Emp> listByDepartment(String department);

    /**
     * 按姓名模糊搜索
     */
    List<Emp> searchByEmpName(String keyword);

    /**
     * 按部门分页查询（验证分页插件，department 可为空表示全部）
     */
    Page<Emp> pageByDepartment(long current, long size, String department);

    /**
     * 邮箱是否已被占用（对应 uk_email 唯一约束的前置校验）
     */
    boolean isEmailTaken(String email);
}
