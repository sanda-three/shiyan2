package com.lab.emp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Date;
import java.util.List;

/**
 * 部门实体类（一对多拓展），对应 ssm_emp.dept 表
 * 一对多关系：一个 Dept 对应多个 Emp，关联键 dept.dept_name = emp.dept（按名称关联）
 */
@TableName("dept")
public class Dept {

    /** 部门编号（自增主键） */
    @TableId(value = "dept_no", type = IdType.AUTO)
    private Integer deptNo;

    /** 部门名称（唯一索引 uk_dept_name，与 emp.dept 关联） */
    @NotBlank(message = "部门名称不能为空")
    @Size(max = 50, message = "部门名称最长 50 字符")
    private String deptName;

    /** 部门描述 */
    @Size(max = 100, message = "部门描述最长 100 字符")
    private String deptDesc;

    /** 创建日期（数据库默认当天） */
    private Date createDate;

    /**
     * 该部门下的员工列表（一对多的"多"端）。
     * exist=false：非数据库列，由 DeptServiceImpl 关联查询填充
     */
    @TableField(exist = false)
    private List<Emp> empList;

    public Integer getDeptNo() {
        return deptNo;
    }

    public void setDeptNo(Integer deptNo) {
        this.deptNo = deptNo;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getDeptDesc() {
        return deptDesc;
    }

    public void setDeptDesc(String deptDesc) {
        this.deptDesc = deptDesc;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public List<Emp> getEmpList() {
        return empList;
    }

    public void setEmpList(List<Emp> empList) {
        this.empList = empList;
    }

    @Override
    public String toString() {
        return "Dept{deptNo=" + deptNo + ", deptName='" + deptName + "', deptDesc='" + deptDesc
                + "', empCount=" + (empList == null ? 0 : empList.size()) + "}";
    }
}
