package com.lab.emp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.emp.common.BizException;
import com.lab.emp.common.Result;
import com.lab.emp.dto.EmpQueryDTO;
import com.lab.emp.entity.Dept;
import com.lab.emp.entity.Emp;
import com.lab.emp.service.DeptService;
import com.lab.emp.service.EmpService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 员工管理 RESTful 控制器（步骤 5.2，接口与需求表一一对应）
 * POST   /emps          新增员工（@Valid 参数校验）
 * DELETE /emps/{id}     删除单条
 * DELETE /emps          批量删除（请求体为 id 数组）
 * PUT    /emps          修改员工（主键在请求体中，必传）
 * GET    /emps/{id}     单条查询
 * GET    /emps          列表 + 多条件模糊 + 分页（name、gender、dept、page、size）
 */
@RestController
@RequestMapping("/emps")
public class EmpController {

    @Autowired
    private EmpService empService;

    @Autowired
    private DeptService deptService;

    /**
     * 分页多条件查询：GET /emps?name=张&gender=男&dept=技术部&page=1&size=5
     * 业务条件由 SpringMVC 自动绑定进 EmpQueryDTO，分页参数单独接收（指导书步骤 5.2 签名）。
     * 不传 page/size 时默认第 1 页、每页 5 条，与需求表一致。
     */
    @GetMapping
    public Result<Page<Emp>> page(EmpQueryDTO query,
                                  @RequestParam(name = "page", defaultValue = "1") long page,
                                  @RequestParam(name = "size", defaultValue = "5") long size) {
        return Result.ok(empService.pageQuery(page, size, query));
    }

    /** 一对多拓展：GET /emps/with-dept（员工列表含嵌套 deptInfo，
     * 声明在 /{id} 之前，具体路径优先于模板匹配） */
    @GetMapping("/with-dept")
    public Result<List<Emp>> listWithDept() {
        return Result.ok(empService.listWithDept());
    }

    /** 单条查询：GET /emps/{id} */
    @GetMapping("/{id}")
    public Result<Emp> getById(@PathVariable("id") Integer id) {
        Emp emp = empService.getById(id);
        return emp != null ? Result.ok(emp) : Result.fail(404, "员工不存在: id=" + id);
    }

    /** 新增：POST /emps（JSON 请求体，主键与状态由数据库生成/默认） */
    @PostMapping
    public Result<Emp> create(@Valid @RequestBody Emp emp) {
        emp.setEmpId(null); // 防止客户端伪造主键
        checkDeptExists(emp.getDept());
        empService.save(emp);
        return Result.ok(emp); // save 后回填自增主键
    }

    /** 修改：PUT /emps（主键在请求体，缺失则拒绝） */
    @PutMapping
    public Result<Boolean> update(@Valid @RequestBody Emp emp) {
        if (emp.getEmpId() == null) {
            throw new BizException(400, "修改操作必须携带 empId");
        }
        if (empService.getById(emp.getEmpId()) == null) {
            return Result.fail(404, "员工不存在: id=" + emp.getEmpId());
        }
        checkDeptExists(emp.getDept());
        return Result.ok(empService.updateById(emp));
    }

    /** 删除单条：DELETE /emps/{id} */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable("id") Integer id) {
        if (empService.getById(id) == null) {
            return Result.fail(404, "员工不存在: id=" + id);
        }
        return Result.ok(empService.removeById(id));
    }

    /** 批量删除：DELETE /emps，请求体为 id 数组，如 [9, 10] */
    @DeleteMapping
    public Result<Boolean> deleteBatch(@RequestBody List<Integer> ids) {
        return Result.ok(empService.removeBatchByIds(ids));
    }

    /** 一对多拓展：员工所填部门必须在 dept 表存在，防止悬空引用（空部门不校验） */
    private void checkDeptExists(String dept) {
        if (dept != null && !dept.isEmpty()
                && deptService.lambdaQuery().eq(Dept::getDeptName, dept).count() == 0) {
            throw new BizException(400, "部门不存在: " + dept + "，请先在 dept 表创建");
        }
    }
}
