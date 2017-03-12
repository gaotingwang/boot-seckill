package com.gtw.seckill.service.impl;

import com.gtw.seckill.cache.RedisDao;
import com.gtw.seckill.dao.SeckillDao;
import com.gtw.seckill.dao.SuccessKilledDao;
import com.gtw.seckill.dto.Exposer;
import com.gtw.seckill.dto.SeckillExecution;
import com.gtw.seckill.entity.Seckill;
import com.gtw.seckill.entity.SuccessKilled;
import com.gtw.seckill.enums.SeckillStatusEnum;
import com.gtw.seckill.exception.RepeatKillException;
import com.gtw.seckill.exception.SeckillCloseException;
import com.gtw.seckill.exception.SeckillException;
import com.gtw.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SeckillServiceImpl implements SeckillService {
    private final SeckillDao seckillDao;
    private final SuccessKilledDao successKilledDao;
    private final RedisDao redisDao;

    private final String md5Salt="!@#$%^&*()_+_)(*&^%$#@!";//md5盐值

    @Autowired
    public SeckillServiceImpl(SeckillDao seckillDao, SuccessKilledDao successKilledDao, RedisDao redisDao) {
        this.seckillDao = seckillDao;
        this.successKilledDao = successKilledDao;
        this.redisDao = redisDao;
    }

    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0,4);
    }

    public Seckill getById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    public Exposer exportSeckillUrl(long seckillId) {
        //优化点：缓存优化,一致性维护建立在超时的基础上
        //1.访问redis
        Seckill seckill = redisDao.getSeckill(seckillId);
        if (seckill == null) {
            //2.查询数据库
            seckill = seckillDao.queryById(seckillId);
            if (seckill == null) {
                return new Exposer(false, seckillId);
            } else {
                //写入redis
                redisDao.putSeckill(seckill);
            }
        }

        //若是秒杀未开启
        Date startTime = seckill.getStartTime();
        Date endTime = seckill.getEndTime();
        Date nowTime = new Date();
        if (startTime.getTime() > nowTime.getTime() || endTime.getTime() < nowTime.getTime()) {
            return new Exposer(false, seckillId, nowTime.getTime(), startTime.getTime(), endTime.getTime());
        }

        //秒杀开启，返回秒杀商品的id、用给接口加密的md5
        String md5 = getMD5(seckillId);
        return new Exposer(true, md5, seckillId);
    }

    private String getMD5(long seckillId) {
        String base = seckillId + "/" + md5Salt;
        return DigestUtils.md5DigestAsHex(base.getBytes());
    }

    /**
     * 使用注解控制事务方法的优点:
     * 1.开发团队达成一致约定，明确标注事务方法的编程风格
     * 2.保证事务方法的执行时间尽可能短，不要穿插其他网络操作RPC/HTTP请求或者剥离到事务方法外部
     * 3.不是所有的方法都需要事务，如只有一条修改操作、只读操作不要事务控制
     */
    @Transactional
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5) throws RepeatKillException, SeckillCloseException, SeckillException {
        if(md5 == null || !md5.equals(getMD5(seckillId))) {
            throw new SeckillException("seckill data rewrite");
        }

        //执行秒杀逻辑:减库存+增加购买明细
        Date nowTime = new Date();
        try{
            /**
             * 对同一商品进行秒杀，是对同一行记录做操作，产生行级锁，造成后续请求锁的等待释放
             * 简单优化思路：
             *    先进行明细插入操作，之后再进行减库存，来分摊压力
             */
            //减库存
            int updateCount = seckillDao.reduceNumber(seckillId, nowTime);
            if (updateCount <= 0) {
                //没有更新库存记录，说明秒杀结束
                throw new SeckillCloseException("seckill is closed");
            }else {
                //否则更新了库存，秒杀成功，增加明细
                int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);
                //看是否该明细被重复插入，即用户是否重复秒杀
                if (insertCount <= 0) {
                    throw new RepeatKillException("seckill repeated");
                }else {
                    //秒杀成功,得到成功插入的明细记录,并返回成功秒杀的信息
                    SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                    return new SeckillExecution(seckillId, SeckillStatusEnum.SUCCESS, successKilled);
                }
            }
        }catch (SeckillCloseException e1) {//防止SeckillCloseException最后被转化为SeckillException
            throw e1;
        }catch (RepeatKillException e2) {
            throw e2;
        }catch (Exception e) {
            log.error(e.getMessage(),e);
            //所有编译期异常转化为运行期异常
            throw new SeckillException("seckill inner error :"+e.getMessage());
        }
    }

    /**
     * 使用存储过程，将事务整体放在数据库中进行，代替之前Java客户端托管的事务行级锁持
     * 因为executeSeckillProcedure已经将重复秒杀，秒杀结束（无库存）合并到返回的SeckillExecution中，
     * 所以不用再捕获SeckillException异常（原本在service层要抛出这两个异常，是为了告诉Spring声明式事务该程序出错要进行事务回滚）
     */
    public SeckillExecution executeSeckillProcedure(long seckillId, long userPhone, String md5) {
        if(md5 == null || !md5.equals(getMD5(seckillId))){
            return new SeckillExecution(seckillId,SeckillStatusEnum.DATA_REWRITE);
        }

        Date time = new Date();
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("seckillId",seckillId);
        map.put("phone",userPhone);
        map.put("killTime",time);
        map.put("result",null);
        try {
            seckillDao.killByProcedure(map);
            int result = MapUtils.getInteger(map,"result",-2);
            if(result == 1){
                SuccessKilled successKill = successKilledDao.queryByIdWithSeckill(seckillId,userPhone);
                return new SeckillExecution(seckillId,SeckillStatusEnum.SUCCESS,successKill);
            }
            else{
                return new SeckillExecution(seckillId,SeckillStatusEnum.stateOf(result));
            }
        }catch(Exception e){
            log.error(e.getMessage(),e);
            return new SeckillExecution(seckillId,SeckillStatusEnum.INNER_ERROR);
        }
    }
}
