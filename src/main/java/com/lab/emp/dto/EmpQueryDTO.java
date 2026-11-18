package com.lab.emp.dto;

/**
 * 员工列表查询条件对象（步骤 2.1 目录结构中的 dto 包，步骤 5.2 接口签名要求）。
 *
 * <p>为什么不直接把 name/gender/dept 摊在 Controller 方法参数上？
 * <ul>
 *   <li>查询条件会随需求增长（后续加薪资区间、入职日期段），逐个加参数
 *       会让方法签名不断膨胀，Service 接口也要跟着改；</li>
 *   <li>封装成 DTO 后 SpringMVC 会自动按同名请求参数完成绑定
 *       （GET /emps?name=张&gender=男 直接填充本对象），新增条件只改这一个类。</li>
 * </ul>
 *
 * <p>page/size 属于分页参数而非业务条件，仍由 Controller 单独接收，
 * 与实验指导书步骤 5.2 的接口签名保持一致。
 */
public class EmpQueryDTO {

    /** 员工姓名，模糊匹配（LIKE %关键字%），空值不参与过滤 */
    private String name;

    /** 性别，精确匹配（男/女），空值不参与过滤 */
    private String gender;

    /** 部门，精确匹配，空值不参与过滤 */
    private String dept;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    @Override
    public String toString() {
        return "EmpQueryDTO{name='" + name + '\'' + ", gender='" + gender + '\''
                + ", dept='" + dept + '\'' + '}';
    }
}
