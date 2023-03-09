package io.github.spitmaster.warlock.core.semaphore;

import org.aspectj.lang.ProceedingJoinPoint;
import org.redisson.api.RPermitExpirableSemaphore;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

/**
 * 分布式信号量的实现
 *
 * @author zhouyijin
 */
public class DistributedWmutex implements Wmutex {

    private final SemaphoreInfo semaphoreInfo;
    private final RedissonClient redissonClient;

    public DistributedWmutex(SemaphoreInfo semaphoreInfo, RedissonClient redissonClient) {
        this.semaphoreInfo = semaphoreInfo;
        this.redissonClient = redissonClient;
    }

    @Override
    public Object doBizWithSemaphore(ProceedingJoinPoint pjp) throws Throwable {
        //1. 获取信号量
        RPermitExpirableSemaphore semaphore = getSemaphore();
        String permitId = null;
        Object result = null;
        try {
            //2. 获取permit
            permitId = semaphore.tryAcquire(
                    semaphoreInfo.getWaitTime().toMillis(),
                    semaphoreInfo.getLeaseTime().toMillis(),
                    TimeUnit.MILLISECONDS);
            if (permitId != null) {
                //3. 如果有permitId返回则说明成功获取到permit, 则执行业务代码
                result = pjp.proceed();
            } else {
                //没获取到permit, 那么说明超时了, 调用回调
                result = semaphoreInfo.getWaitTimeoutHandler().handleWaitTimeout(pjp);
            }
        } finally {
            //4. 归还permit
            if (permitId != null) {
                boolean released = semaphore.tryRelease(permitId);
                if (!released) {
                    //5. 如果没有释放成功, 说明业务执行超时了, 因为这个permitId已经自动过期了, 执行超时处理
                    //替换掉原来的返回值
                    result = semaphoreInfo.getLeaseTimeoutHandler().handleLeaseTimeout(pjp, result);
                }
            }
        }
        return result;
    }

    @Override
    public SemaphoreInfo getSemaphoreInfo() {
        return this.semaphoreInfo;
    }

    private RPermitExpirableSemaphore getSemaphore() {
        //1. redisson实现的信号量对象
        RPermitExpirableSemaphore semaphore = redissonClient.getPermitExpirableSemaphore(semaphoreInfo.getSemaphoreKey());
        //2. 设置信号量的大小, 重复设置没关系;  也就是说如果已经初始化了, 则不会再次设置(哪怕permit已经是0了也不会重复设置)
        semaphore.trySetPermits(semaphoreInfo.getPermits());
        //3. 设置信号量的过期时间, 这个重复设置就会刷新
        semaphore.expire(semaphoreInfo.getLeaseTime().toMillis(), TimeUnit.MILLISECONDS);
        return semaphore;
    }
}
