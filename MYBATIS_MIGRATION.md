# MyBatis 迁移实验

## 概述

本项目正在进行从 JPA/Hibernate 到 MyBatis 的迁移实验，目的是：
1. 学习 MyBatis 的使用
2. 对比两种 ORM 框架的性能和开发体验
3. 为项目提供更多的技术选择

## 当前状态

### ✅ 已完成
- [x] 添加 MyBatis 相关依赖
- [x] 配置 MyBatis 和 PageHelper
- [x] 创建 PostMapper 接口
- [x] 创建 PostMapper.xml 映射文件
- [x] 创建 MyBatisPostService
- [x] 创建 MyBatisPostController（用于对比测试）

### 🔄 进行中
- [ ] 修复编译错误
- [ ] 完善其他实体的 MyBatis 映射
- [ ] 性能对比测试

### 📋 待完成
- [ ] User 实体的 MyBatis 映射
- [ ] Comment 实体的 MyBatis 映射
- [ ] Tag 实体的 MyBatis 映射
- [ ] 完整的性能对比测试
- [ ] 前端适配（如果需要）

## 技术栈对比

### JPA/Hibernate（当前使用）
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

**优点：**
- 自动生成 SQL
- 对象关系映射
- 类型安全
- Spring Data 集成良好

**缺点：**
- 复杂查询性能可能不佳
- SQL 不可控
- 学习曲线陡峭
- 调试困难

### MyBatis（实验性）
```xml
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
    <version>3.0.3</version>
</dependency>
```

**优点：**
- SQL 完全可控
- 性能优秀
- 学习曲线平缓
- 调试方便
- 支持复杂查询

**缺点：**
- 需要手写 SQL
- 配置相对复杂
- 需要管理 XML 文件

## 文件结构

```
src/main/java/com/broadblog/
├── mapper/
│   └── mybatis/
│       └── PostMapper.java          # MyBatis Mapper 接口
├── service/
│   └── MyBatisPostService.java      # MyBatis 服务层
└── controller/
    └── MyBatisPostController.java   # MyBatis 控制器（对比测试）

src/main/resources/
├── mapper/
│   └── PostMapper.xml               # MyBatis XML 映射文件
└── application.properties           # MyBatis 配置
```

## API 对比

### JPA 版本
```
GET /api/posts/page          # 分页查询
GET /api/posts/my           # 我的文章
GET /api/posts/search       # 搜索文章
```

### MyBatis 版本
```
GET /api/mybatis/posts/page          # 分页查询
GET /api/mybatis/posts/my           # 我的文章
GET /api/mybatis/posts/search       # 搜索文章
GET /api/mybatis/posts/performance-test  # 性能测试
```

## 配置说明

### MyBatis 配置
```properties
# MyBatis Configuration
mybatis.mapper-locations=classpath:mapper/*.xml
mybatis.type-aliases-package=com.broadblog.entity
mybatis.configuration.map-underscore-to-camel-case=true
mybatis.configuration.log-impl=org.apache.ibatis.logging.stdout.StdOutImpl

# PageHelper Configuration
pagehelper.helper-dialect=mysql
pagehelper.reasonable=true
pagehelper.support-methods-arguments=true
pagehelper.params=count=countSql
```

## 使用示例

### 1. 基础查询
```java
// MyBatis 方式
@Autowired
private PostMapper postMapper;

public Post getPostById(Long id) {
    return postMapper.selectById(id);
}
```

### 2. 分页查询
```java
// MyBatis + PageHelper 方式
public PageInfo<Post> getPostsByPage(int page, int size) {
    PageHelper.startPage(page, size);
    List<Post> posts = postMapper.selectAll();
    return new PageInfo<>(posts);
}
```

### 3. 复杂查询
```xml
<!-- XML 映射文件 -->
<select id="searchPosts" resultMap="PostResultMap">
    SELECT DISTINCT p.id, p.title, p.content, p.created_at, p.updated_at,
           u.id as author_id, u.username as author_username, u.email as author_email,
           t.id as tag_id, t.name as tag_name
    FROM posts p
    LEFT JOIN users u ON p.author_id = u.id
    LEFT JOIN post_tags pt ON p.id = pt.post_id
    LEFT JOIN tags t ON pt.tag_id = t.id
    WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', #{keyword}, '%'))
       OR LOWER(p.content) LIKE LOWER(CONCAT('%', #{keyword}, '%'))
       OR LOWER(t.name) LIKE LOWER(CONCAT('%', #{keyword}, '%'))
    ORDER BY p.created_at DESC
    LIMIT #{offset}, #{limit}
</select>
```

## 性能对比测试

### 测试接口
```
GET /api/posts/page?page=1&size=100          # JPA 版本
GET /api/mybatis/posts/page?page=1&size=100  # MyBatis 版本
GET /api/mybatis/posts/performance-test      # MyBatis 性能测试
```

### 测试指标
- 查询响应时间
- SQL 执行效率
- 内存使用情况
- 开发调试便利性

## 迁移建议

### 1. 渐进式迁移
- 保留现有的 JPA 实现
- 逐步添加 MyBatis 实现
- 通过对比测试验证效果

### 2. 选择标准
- **简单 CRUD**：JPA 更便捷
- **复杂查询**：MyBatis 更灵活
- **性能要求高**：MyBatis 更可控
- **团队熟悉度**：根据团队情况选择

### 3. 混合使用
- 可以同时使用两种框架
- 根据业务场景选择合适的技术
- 通过配置控制使用哪种实现

## 下一步计划

1. **修复编译错误**：解决 PageHelper 依赖问题
2. **完善映射文件**：添加其他实体的 MyBatis 映射
3. **性能测试**：对比两种框架的性能表现
4. **文档完善**：编写详细的使用文档
5. **团队培训**：分享 MyBatis 使用经验

## 注意事项

1. **分支管理**：当前在 `mybatis` 分支进行实验
2. **数据一致性**：确保两种实现的数据一致性
3. **测试覆盖**：充分测试 MyBatis 实现的功能
4. **回滚准备**：保留 JPA 实现作为备选方案

## 参考资料

- [MyBatis 官方文档](https://mybatis.org/mybatis-3/)
- [MyBatis Spring Boot Starter](https://mybatis.org/spring-boot-starter/mybatis-spring-boot-starter/)
- [PageHelper 分页插件](https://pagehelper.github.io/)
- [Spring Boot + MyBatis 集成](https://spring.io/guides/gs/accessing-data-mysql/)
