package com.dailytracker.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dailytracker.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 记账明细实体类 - 记录用户的每一笔收入和支出流水
 *
 * <p><b>【数据库表映射】</b></p>
 * <ul>
 *   <li>对应数据库表：{@code t_accounting}</li>
 *   <li>继承自 {@link BaseEntity}，包含 id、createdAt、updatedAt、isDeleted 公共字段</li>
 * </ul>
 *
 * <p><b>【ORM映射关系】</b></p>
 * <ul>
 *   <li>与 User 实体为多对一关系：多条记账记录属于一个用户（通过 userId 关联）</li>
 *   <li>与 AccountingCategory 实体为多对一关系：多条记账记录对应一个分类（通过 categoryId 关联）</li>
 *   <li>与 Budget 实体通过 userId + categoryId + 月份间接关联，用于预算执行情况分析</li>
 * </ul>
 *
 * <p><b>【类注解说明】</b></p>
 * <ul>
 *   <li>{@code @Data} - Lombok 注解，自动生成 getter/setter/toString/equals/hashCode</li>
 *   <li>{@code @EqualsAndHashCode(callSuper = true)} - 包含父类字段参与 equals 和 hashCode 计算</li>
 *   <li>{@code @TableName("t_accounting")} - 指定映射的数据库表名</li>
 * </ul>
 *
 * <p><b>【业务场景】</b></p>
 * <p>记账功能帮助用户记录日常收支，了解资金流向。支持：
 * <ul>
 *   <li>收入和支出两种类型</li>
 *   <li>多种账户（现金、微信、支付宝、银行卡）</li>
 *   <li>分类管理（餐饮、交通、购物等）</li>
 *   <li>凭证图片上传</li>
 *   <li>按日期、分类、账户等多维度统计</li>
 * </ul>
 *
 * <p><b>【技术知识点 - BigDecimal 与金额处理】</b></p>
 * <p>金额字段使用 {@link BigDecimal} 而非 double 或 float，原因：
 * <ul>
 *   <li>浮点数（double/float）在计算机中采用二进制表示，无法精确表示某些十进制小数（如 0.1）</li>
 *   <li>例如：0.1 + 0.2 在 double 中结果为 0.30000000000000004，导致金额计算错误</li>
 *   <li>BigDecimal 使用十进制存储，可以精确表示任意精度的十进制数</li>
 *   <li>数据库中使用 DECIMAL(12,2) 类型对应 Java 的 BigDecimal</li>
 * </ul>
 * 注意事项：
 * <ul>
 *   <li>创建 BigDecimal 时应使用 new BigDecimal("10.5") 而非 new BigDecimal(10.5)</li>
 *   <li>比较大小使用 compareTo() 方法而非 equals()（因为 2.0 和 2.00 的 equals 返回 false）</li>
 * </ul>
 *
 * @see BaseEntity 父类，包含公共字段
 * @see AccountingCategory 记账分类实体
 * @see Budget 预算实体
 * @see com.dailytracker.mapper.AccountingMapper 对应的数据访问层
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_accounting")
public class Accounting extends BaseEntity {

    /**
     * 用户ID - 关联 t_user 表的主键 id
     * <p>
     * 标识此笔记录属于哪个用户。
     * 数据库字段类型建议：BIGINT NOT NULL，应建立索引
     */
    private Long userId;

    /**
     * 类型 - 收支类型
     * <p>
     * <ul>
     *   <li>"INCOME" - 收入：工资、奖金、投资收益、红包等</li>
     *   <li>"EXPENSE" - 支出：餐饮、交通、购物、娱乐等</li>
     * </ul>
     * 数据库字段类型建议：VARCHAR(10) NOT NULL
     */
    private String type;

    /**
     * 金额 - 收支金额
     * <p>
     * 使用 {@link BigDecimal} 类型确保金额计算的精确性。
     * 对于收入，金额为正数；对于支出，金额也为正数（通过 type 字段区分收支方向）。
     * <p>
     * 数据库字段类型建议：DECIMAL(12,2) NOT NULL
     * <ul>
     *   <li>DECIMAL(12,2) 表示最多 12 位数字，其中 2 位小数</li>
     *   <li>最大可存储金额：9999999999.99（99.99 亿），足以应对绝大多数场景</li>
     * </ul>
     */
    private BigDecimal amount;

    /**
     * 分类ID - 关联 t_accounting_category 表的主键 id
     * <p>
     * 每笔收支都应归类到具体的分类（如餐饮、交通、工资等）。
     * 通过此字段关联 AccountingCategory 表获取分类详细信息。
     * <p>
     * 数据库字段类型建议：BIGINT NOT NULL
     */
    private Long categoryId;

    /**
     * 账户类型 - 资金所在的账户/支付方式
     * <p>
     * <ul>
     *   <li>"CASH" - 现金</li>
     *   <li>"WECHAT" - 微信支付</li>
     *   <li>"ALIPAY" - 支付宝</li>
     *   <li>"BANK" - 银行卡</li>
     * </ul>
     * 便于按账户维度统计资金流向。
     * <p>
     * 数据库字段类型建议：VARCHAR(20)
     */
    private String accountType;

    /**
     * 备注 - 用户对这笔收支的补充说明
     * <p>
     * 可选字段，例如记录"和同事聚餐"、"买给妈妈的生日礼物"等。
     * 数据库字段类型建议：VARCHAR(500)
     */
    private String remark;

    /**
     * 凭证图片（JSON）- 收支凭证的图片URL列表
     * <p>
     * 以 JSON 数组格式存储多张图片的访问地址，例如：
     * <pre>
     * ["https://oss.example.com/img/receipt1.jpg", "https://oss.example.com/img/receipt2.jpg"]
     * </pre>
     * 图片通常上传到 OSS（对象存储服务），此处只存储 URL。
     * <p>
     * 数据库字段类型建议：JSON 或 TEXT
     */
    private String images;

    /**
     * 记账日期 - 这笔收支发生的日期
     * <p>
     * 使用 {@link LocalDate} 类型。可能与 createdAt 不同（例如今天补录昨天的账）。
     * 是按日期统计的核心字段。
     * <p>
     * 数据库字段类型建议：DATE NOT NULL，应建立索引（与 userId 组合索引）
     */
    private LocalDate accountingDate;

    /**
     * 记账时间 - 这笔收支发生的时间（精确到时分秒）
     * <p>
     * 使用 {@link LocalTime} 类型，与 accountingDate 配合可以精确定位收支发生的时间点。
     * 例如记账"午餐"时可以记录具体时间 12:30。
     * <p>
     * 数据库字段类型建议：TIME
     */
    private LocalTime accountingTime;
}
