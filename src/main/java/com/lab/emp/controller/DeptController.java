package com.lab.emp.controller;

import com.lab.emp.common.Result;
import com.lab.emp.entity.Dept;
import com.lab.emp.service.DeptService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门管理 RESTful 控制器（一对多拓展）
 * GET    /depts              部门列表
 * GET    /depts/{deptNo}     部门详情（不含员工，避免歧义）
 * GET    /depts/{deptNo}/emps 一对多核心接口：部门 + 名下员工列表
 * POST   /depts              新增部门（@Valid 校验，名称唯一）
 * DELETE /depts/{deptNo}     删除部门（名下有员工时 400 拒绝）
 */
@RestController
@RequestMapping("/depts")
public class DeptController {

    @Autowired
    private DeptService deptService;

    /** 部门列表：GET /depts */
    @GetMapping
    public Result<List<Dept>> list() {
        return Result.ok(deptService.lambdaQuery().orderByAsc(Dept::getDeptNo).list());
    }

    /** 单条查询：GET /depts/{deptNo} */
    @GetMapping("/{deptNo}")
    public Result<Dept> getById(@PathVariable("deptNo") Integer deptNo) {
        Dept dept = deptService.getById(deptNo);
        return dept != null ? Result.ok(dept) : Result.fail(404, "部门不存在: deptNo=" + deptNo);
    }

    /** 一对多查询：GET /depts/{deptNo}/emps（部门及其名下员工） */
    @GetMapping("/{deptNo}/emps")
    public Result<Dept> getWithEmps(@PathVariable("deptNo") Integer deptNo) {
        Dept dept = deptService.getWithEmps(deptNo);
        return dept != null ? Result.ok(dept) : Result.fail(404, "部门不存在: deptNo=" + deptNo);
    }

    /** 新增：POST /depts（JSON：{"deptName":"财务部","deptDesc":"管钱的"}） */
    @PostMapping
    public Result<Dept> create(@Valid @RequestBody Dept dept) {
        dept.setDeptNo(null); // 防止客户端伪造主键
        deptService.save(dept);
        return Result.ok(dept); // save 后回填自增主键
    }

    /** 删除：DELETE /depts/{deptNo}（级联保护：名下有员工返回 400） */
    @DeleteMapping("/{deptNo}")
    public Result<Boolean> delete(@PathVariable("deptNo") Integer deptNo) {
        return Result.ok(deptService.removeDept(deptNo));
    }
}
