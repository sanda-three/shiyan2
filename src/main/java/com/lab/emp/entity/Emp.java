package com.lab.emp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 员工实体类（MyBatis-Plus 实体），对应实验一 ssm_emp 库 emp 表
 * 列映射（MP 驼峰转下划线规则，本表列名单词无需转换）：
 * emp_id / emp_name / gender / dept / post / salary / hire_date / status
 * 校验注解供 Controller @Valid 参数校验使用
 */
@TableName("emp")
public class Emp {

    /** 员工编号（自增主键） */
    @TableId(value = "emp_id", type = IdType.AUTO)
    private Integer empId;

    /** 员工姓名（非空，最长 50） */
    @NotBlank(message = "员工姓名不能为空")
    @Size(max = 50, message = "员工姓名长度不能超过 50")
    private String empName;

    /** 性别（男/女） */
    @Pattern(regexp = "^[男女]$", message = "性别只能是男或女")
    private String gender;

    /** 部门 */
    @Size(max = 50, message = "部门长度不能超过 50")
    private String dept;

    /** 岗位 */
    @Size(max = 50, message = "岗位长度不能超过 50")
    private String post;

    /** 薪资 */
    @DecimalMin(value = "0", message = "薪资不能为负数")
    private BigDecimal salary;

    /** 入职日期 */
    private Date hireDate;

    /** 在职状态：1 在职，0 离职（新增未传时由数据库默认值 1 填充） */
    private Integer status;

    /**
     * 所属部门对象（多对一方向的关联字段，一对多拓展）。
     * exist=false：非数据库列，由 EmpServiceImpl.listWithDept 按
     * emp.dept = dept.dept_name 关联填充；JSON 中序列化为 deptInfo 嵌套对象
     */
    @TableField(exist = false)
    private Dept deptInfo;

    public Integer getEmpId() {
        return empId;
    }

    public void setEmpId(Integer empId) {
        this.empId = empId;
    }

    public String getEmpName() {
        return empName;
    }

    public void setEmpName(String empName) {
        this.empName = empName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDept() {
        return dept;
    }

    public void setDept(String dept) {
        this.dept = dept;
    }

    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public Date getHireDate() {
        return hireDate;
    }

    public void setHireDate(Date hireDate) {
        this.hireDate = hireDate;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Dept getDeptInfo() {
        return deptInfo;
    }

    public void setDeptInfo(Dept deptInfo) {
        this.deptInfo = deptInfo;
    }

    @Override
    public String toString() {
        return "Emp{" +
                "empId=" + empId +
                ", empName='" + empName + '\'' +
                ", gender='" + gender + '\'' +
                ", dept='" + dept + '\'' +
                ", post='" + post + '\'' +
                ", salary=" + salary +
                ", hireDate=" + hireDate +
                ", status=" + status +
                '}';
    }
}
