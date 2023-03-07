# warlock-spring-boot-starter
---

![Warlock](warlock_icon_2.png)

Lock for spring!

warlock-spring-boot-starter is an annotation-driven concurrency tools library

It is easily use in Spring application

Just using annotation on your method , the concurrency problem would be solved


# Quick Start
---

## Requirements

* JDK11 or higher
* Spring ....
* Redisson (Optional, if you want to use distributed lock etc.)

### 1. Lock

How to use ...

1. Add `@Warlock()` Annotation on your method
2. Done

```java
@Component
public class Sample {
    @Warlock(name = "uniquename1", key = "#dto.userId")
    public void doBiz(StudentDto dto) {
        //your business code
    }
}
```

