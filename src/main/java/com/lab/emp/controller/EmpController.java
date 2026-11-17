package com.lab.emp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.emp.common.Result;
import com.lab.emp.entity.Emp;
import com.lab.emp.service.EmpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 员工 RESTful 控制器（步骤 4.2 Controller 校验点）
 * - 路径规范：资源名复数 /api/emps；主键走路径变量；子资源/视图走名词化路径
 * - 请求方式：GET 查询（幂等）、POST 新增、PUT 更新、DELETE 删除
 * - 返回值：统一 Result 包装，业务失败用 4xx 状态码 + 提示
 */
@RestController
@RequestMapping("/api/emps")
public class EmpController {

    @Autowired
    private EmpService empService;

    /** 列表查询：支持 department 精确、name 模糊过滤，如 GET /api/emps?department=技术部 */
    @GetMapping
    public Result<List<Emp>> list(@RequestParam(required = false) String department,
                                  @RequestParam(required = false) String name) {
        if (department != null && !department.isEmpty()) {
            return Result.ok(empService.listByDepartment(department));
        }
        return Result.ok(empService.searchByEmpName(name));
    }

    /** 分页查询：GET /api/emps/page?current=1&size=5&department=技术部 */
    @GetMapping("/page")
    public Result<Page<Emp>> page(@RequestParam(defaultValue = "1") long current,
                                  @RequestParam(defaultValue = "5") long size,
                                  @RequestParam(required = false) String department) {
        return Result.ok(empService.pageByDepartment(current, size, department));
    }

    /** 主键查询：GET /api/emps/{id} */
    @GetMapping("/{id}")
    public Result<Emp> getById(@PathVariable Integer id) {
        Emp emp = empService.getById(id);
        return emp != null ? Result.ok(emp) : Result.fail(404, "员工不存在: id=" + id);
    }

    /** 新增：POST /api/emps（请求体 JSON，主键由数据库生成） */
    @PostMapping
    public Result<Emp> create(@RequestBody Emp emp) {
        emp.setEmpId(null); // 防止客户端伪造主键
        if (empService.isEmailTaken(emp.getEmail())) {
            return Result.fail(400, "邮箱已被占用: " + emp.getEmail());
        }
        empService.save(emp);
        return Result.ok(emp); // save 后 emp 携带回填的自增主键
    }

    /** 更新：PUT /api/emps/{id}（路径主键为准，忽略请求体中的 id） */
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Integer id, @RequestBody Emp emp) {
        if (empService.getById(id) == null) {
            return Result.fail(404, "员工不存在: id=" + id);
        }
        emp.setEmpId(id);
        return Result.ok(empService.updateById(emp));
    }

    /** 删除：DELETE /api/emps/{id} */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Integer id) {
        if (empService.getById(id) == null) {
            return Result.fail(404, "员工不存在: id=" + id);
        }
        return Result.ok(empService.removeById(id));
    }
}
